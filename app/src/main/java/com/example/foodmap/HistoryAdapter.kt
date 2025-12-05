package com.example.foodmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val historyList: List<ScanResult>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView = itemView.findViewById(R.id.textViewFoodNameHistory)
        val calories: TextView = itemView.findViewById(R.id.textViewCaloriesHistory)
        val nutrients: TextView = itemView.findViewById(R.id.textViewNutrientsHistory)

        // CORREÇÃO: Removemos o "S" extra que estava aqui
        val timestamp: TextView = itemView.findViewById(R.id.textViewTimestampHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_scan, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]

        holder.foodName.text = item.foodName
        holder.calories.text = "${item.calories} kcal"
        holder.nutrients.text = "Prot: ${item.protein}g | Fibra: ${item.fiber}g"
        holder.timestamp.text = item.timestamp
    }

    override fun getItemCount(): Int {
        return historyList.size
    }
}