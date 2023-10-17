package com.dkds.view_model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dkds.R
import com.dkds.data.entity.Transaction
import java.time.format.DateTimeFormatter

class TransactionsListAdapter :
    ListAdapter<Transaction, TransactionsListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val transactionTypeTextView: TextView = view.findViewById(R.id.transaction_type)
        private val transactionTimeTextView: TextView = view.findViewById(R.id.transaction_time)
        private val transactionDescTextView: TextView =
            view.findViewById(R.id.transaction_description)
        private val transactionAmountTextView: TextView = view.findViewById(R.id.transaction_amount)

        // ViewHolder implementation and binding
        fun bind(transaction: Transaction) {
            transactionTypeTextView.text = transaction.type.toString()
            transactionTimeTextView.text =
                transaction.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss a"))
            if (transaction.description == null) {
                transactionDescTextView.visibility = View.GONE
            } else {
                transactionDescTextView.visibility = View.VISIBLE
                transactionDescTextView.text = transaction.description
            }
            transactionAmountTextView.text = String.format("%.2f", transaction.amount)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
