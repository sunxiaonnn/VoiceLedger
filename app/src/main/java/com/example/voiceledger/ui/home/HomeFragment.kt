package com.example.voiceledger.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.voiceledger.adapter.BillAdapter
import com.example.voiceledger.data.Bill
import com.example.voiceledger.databinding.FragmentHomeBinding
import com.example.voiceledger.viewmodel.BillViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BillViewModel by activityViewModels()
    private lateinit var adapter: BillAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = BillAdapter { bill ->
            showDeleteConfirmDialog(bill)
        }

        binding.rvBills.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }
    }

    private fun showDeleteConfirmDialog(bill: Bill) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除账单")
            .setMessage("确定要删除这条记录吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.delete(bill)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun observeData() {
        viewModel.allBills.observe(viewLifecycleOwner) { bills ->
            adapter.submitList(bills)
            binding.tvEmpty.visibility = if (bills.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
