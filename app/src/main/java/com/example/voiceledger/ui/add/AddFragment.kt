package com.example.voiceledger.ui.add

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.voiceledger.R
import com.example.voiceledger.data.Bill
import com.example.voiceledger.databinding.FragmentAddBinding
import com.example.voiceledger.util.VoiceParser
import com.example.voiceledger.util.VoiceRecognizer
import com.example.voiceledger.viewmodel.BillViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddFragment : Fragment() {
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BillViewModel by activityViewModels()
    private var voiceRecognizer: VoiceRecognizer? = null
    private var selectedDate: Long = System.currentTimeMillis()
    private var isExpense = true

    private val categories = listOf("餐饮", "交通", "购物", "娱乐", "住房", "医疗", "教育", "其他")

    companion object {
        private const val REQUEST_RECORD_AUDIO = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupVoiceRecognizer()
    }

    private fun setupUI() {
        // 分类下拉
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(categoryAdapter)
        binding.actvCategory.setText(categories[0], false)

        // 日期选择
        updateDateDisplay()
        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        // 收入/支出切换
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            isExpense = checkedIds.firstOrNull() != R.id.chipIncome
        }

        // 保存按钮
        binding.btnSave.setOnClickListener {
            saveBill()
        }

        // 语音按钮
        binding.btnVoice.setOnClickListener {
            if (checkAudioPermission()) {
                startVoiceInput()
            }
        }
    }

    private fun setupVoiceRecognizer() {
        voiceRecognizer = VoiceRecognizer(
            context = requireContext(),
            onResult = { text ->
                binding.btnVoice.setIconResource(R.drawable.ic_mic)
                handleVoiceResult(text)
            },
            onError = { error ->
                binding.btnVoice.setIconResource(R.drawable.ic_mic)
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            },
            onReady = {
                binding.btnVoice.setIconResource(R.drawable.ic_mic_active)
            },
            onListening = {
                binding.btnVoice.setIconResource(R.drawable.ic_mic_active)
            }
        )
    }

    private fun checkAudioPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO
            )
            return false
        }
        return true
    }

    private fun startVoiceInput() {
        voiceRecognizer?.startListening()
        Toast.makeText(requireContext(), "请说话...", Toast.LENGTH_SHORT).show()
    }

    private fun handleVoiceResult(text: String) {
        val result = VoiceParser.parse(text)
        showConfirmDialog(result)
    }

    private fun showConfirmDialog(result: VoiceParser.ParseResult) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_bill_confirm, null)

        // 预填充数据
        val etAmount = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDialogAmount)
        val etCategory = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.actvDialogCategory)
        val etNote = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDialogNote)
        val chipGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupDialog)

        result.amount?.let { etAmount.setText(it.toString()) }
        etCategory.setText(result.category, false)
        etNote.setText(result.rawText)

        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(categoryAdapter)

        if (!result.isExpense) {
            chipGroup.check(R.id.chipDialogIncome)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("确认账单信息")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val amount = etAmount.text.toString().toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(requireContext(), "请输入有效金额", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val category = etCategory.text.toString().ifEmpty { "其他" }
                val note = etNote.text.toString()
                val isExp = chipGroup.checkedChipId != R.id.chipDialogIncome

                val bill = Bill(
                    amount = amount,
                    category = category,
                    note = note,
                    date = selectedDate,
                    isExpense = isExp
                )
                viewModel.insert(bill)
                Toast.makeText(requireContext(), "已保存", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun saveBill() {
        val amountStr = binding.etAmount.text.toString()
        val amount = amountStr.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "请输入有效金额", Toast.LENGTH_SHORT).show()
            return
        }

        val category = binding.actvCategory.text.toString().ifEmpty { "其他" }
        val note = binding.etNote.text.toString()

        val bill = Bill(
            amount = amount,
            category = category,
            note = note,
            date = selectedDate,
            isExpense = isExpense
        )

        viewModel.insert(bill)
        Toast.makeText(requireContext(), "已保存", Toast.LENGTH_SHORT).show()
        clearForm()
    }

    private fun clearForm() {
        binding.etAmount.text?.clear()
        binding.etNote.text?.clear()
        binding.actvCategory.setText(categories[0], false)
        selectedDate = System.currentTimeMillis()
        updateDateDisplay()
        binding.chipExpense.isChecked = true
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                cal.set(year, month, day)
                selectedDate = cal.timeInMillis
                updateDateDisplay()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.etDate.setText(sdf.format(Date(selectedDate)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        voiceRecognizer?.destroy()
        _binding = null
    }
}
