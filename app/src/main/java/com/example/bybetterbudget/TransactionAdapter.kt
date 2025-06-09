package com.example.bybetterbudget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bybetterbudget.R

data class Transaction(
    val category: String,
    val amount: Double,
    val date: String,
    val description: String? = null
)

class TransactionAdapter(
    private val items: MutableList<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val tx = items[position]
        holder.tvCategory.text = tx.category
        holder.tvAmount.text = String.format("R%.2f", tx.amount)
        holder.tvDate.text = tx.date
    }

    override fun getItemCount(): Int = items.size

    fun addTransaction(tx: Transaction) {
        items.add(0, tx)
        notifyItemInserted(0)
    }
}