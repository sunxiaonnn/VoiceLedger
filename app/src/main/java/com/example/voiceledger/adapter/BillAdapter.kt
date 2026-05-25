package com.example.voiceledger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceledger.R
import com.example.voiceledger.data.Bill
import com.example.voiceledger.databinding.ItemBillBinding
import java.text.SimpleDateFormat
import java.util.*

class BillAdapter(
    private val onDeleteClick: (Bill) -> Unit
) : ListAdapter<Bill, BillAdapter.BillViewHolder>(BillDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = ItemBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BillViewHolder(private val binding: ItemBillBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bill: Bill) {
            val context = binding.root.context
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            binding.apply {
                tvCategory.text = bill.category
                tvNote.text = bill.note.ifEmpty { bill.category }
                tvDate.text = sdf.format(Date(bill.date))

                if (bill.isExpense) {
                    tvAmount.text = String.format("-¥%.2f", bill.amount)
                    tvAmount.setTextColor(context.getColor(R.color.expense_red))
                } else {
                    tvAmount.text = String.format("+¥%.2f", bill.amount)
                    tvAmount.setTextColor(context.getColor(R.color.income_green))
                }

                // 分类图标
                ivCategory.setImageResource(getCategoryIcon(bill.category))

                // 删除按钮
                btnDelete.setOnClickListener {
                    onDeleteClick(bill)
                }
            }
        }

        private fun getCategoryIcon(category: String): Int {
            return when (category) {
                "餐饮" -> R.drawable.ic_food
                "交通" -> R.drawable.ic_transport
                "购物" -> R.drawable.ic_shopping
                "娱乐" -> R.drawable.ic_entertainment
                "住房" -> R.drawable.ic_housing
                "医疗" -> R.drawable.ic_medical
                "教育" -> R.drawable.ic_education
                "收入" -> R.drawable.ic_income
                else -> R.drawable.ic_other
            }
        }
    }

    class BillDiffCallback : DiffUtil.ItemCallback<Bill>() {
        override fun areItemsTheSame(oldItem: Bill, newItem: Bill): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bill, newItem: Bill): Boolean {
            return oldItem == newItem
        }
    }
}
