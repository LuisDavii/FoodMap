package com.example.foodmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val historyList: List<ScanResult>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView = itemView.findViewById(R.id.textViewHistoryFoodName)
        val details: TextView = itemView.findViewById(R.id.textViewHistoryDetails)
        val timestamp: TextView = itemView.findViewById(R.id.textViewHistoryTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_scan, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = historyList[position]

        holder.foodName.text = currentItem.foodName
        holder.timestamp.text = currentItem.timestamp
        holder.details.text =
            "${currentItem.calories} kcal | Proteína: ${currentItem.protein}g | Fibra: ${currentItem.fiber}g"
    }
}