package com.example.voiceledger.ui.stats

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.voiceledger.data.CategoryTotal
import com.example.voiceledger.databinding.FragmentStatsBinding
import com.example.voiceledger.viewmodel.BillViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.lifecycleScope

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BillViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        observeData()

        binding.btnExport.setOnClickListener {
            exportToCsv()
        }
    }

    private fun setupChart() {
        binding.pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 45f
            transparentCircleRadius = 50f
            setDrawCenterText(true)
            centerText = "支出分类"
            setCenterTextSize(14f)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = 12f
            }
        }
    }

    private fun observeData() {
        val (start, end) = viewModel.getMonthRange()

        // 总支出
        viewModel.getTotalExpense(start, end).observe(viewLifecycleOwner) { total ->
            binding.tvTotalExpense.text = String.format("¥%.2f", total ?: 0.0)
        }

        // 总收入
        viewModel.getTotalIncome(start, end).observe(viewLifecycleOwner) { total ->
            binding.tvTotalIncome.text = String.format("¥%.2f", total ?: 0.0)
        }

        // 分类饼图
        viewModel.getCategoryExpenses(start, end).observe(viewLifecycleOwner) { categories ->
            updateChart(categories)
            updateCategoryList(categories)
        }
    }

    private fun updateChart(categories: List<CategoryTotal>) {
        if (categories.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.centerText = "暂无数据"
            binding.pieChart.invalidate()
            return
        }

        val entries = categories.map { PieEntry(it.total.toFloat(), it.category) }

        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList() +
                    ColorTemplate.COLORFUL_COLORS.toList()
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 12f
            valueFormatter = PercentFormatter(binding.pieChart)
        }

        val data = PieData(dataSet)
        binding.pieChart.data = data
        binding.pieChart.invalidate()
    }

    private fun updateCategoryList(categories: List<CategoryTotal>) {
        val total = categories.sumOf { it.total }
        val sb = StringBuilder()

        for (cat in categories.sortedByDescending { it.total }) {
            val percent = if (total > 0) (cat.total / total * 100) else 0.0
            sb.appendLine("${cat.category}: ¥${String.format("%.2f", cat.total)} (${String.format("%.1f", percent)}%)")
        }

        binding.tvCategoryDetails.text = sb.toString().trimEnd()
    }

    private fun exportToCsv() {
        val (start, end) = viewModel.getMonthRange()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val fileNameSdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        viewLifecycleOwner.lifecycleScope.launch {
            val bills = withContext(Dispatchers.IO) {
                viewModel.getBillsByDateRangeList(start, end)
            }

            if (bills.isEmpty()) {
                Toast.makeText(requireContext(), "本月暂无数据可导出", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val fileName = "VoiceLedger_${fileNameSdf.format(Date())}.csv"
                val file = File(requireContext().cacheDir, fileName)

                withContext(Dispatchers.IO) {
                    FileWriter(file).use { writer ->
                        // BOM for Excel
                        writer.write("\uFEFF")
                        writer.write("日期,类型,分类,金额,备注\n")
                        for (bill in bills) {
                            val type = if (bill.isExpense) "支出" else "收入"
                            val date = sdf.format(Date(bill.date))
                            writer.write("$date,$type,${bill.category},${bill.amount},${bill.note}\n")
                        }
                    }
                }

                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "账单导出")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(shareIntent, "分享账单"))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
