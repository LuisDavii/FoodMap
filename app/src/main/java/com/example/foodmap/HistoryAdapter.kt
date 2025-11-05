
package com.example.foodmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter para o RecyclerView da HistoryActivity.
 * Recebe uma lista de ScanResult e a exibe, usando o layout 'item_history_scan.xml'.
 */
class HistoryAdapter(private val historyList: List<ScanResult>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    /**
     * ViewHolder (Contentor) que armazena as referências para os TextViews
     * do layout item_history_scan.xml
     *
     * <-- 💎 CORREÇÃO: Os IDs foram atualizados para corresponder
     * exatamente aos IDs do seu ficheiro XML.
     */
    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView = itemView.findViewById(R.id.textViewFoodNameHistory)
        val calories: TextView = itemView.findViewById(R.id.textViewCaloriesHistory)
        val nutrients: TextView = itemView.findViewById(R.id.textViewNutrientsHistory)
        val timestamp: TextView = itemView.findViewById(R.id.textViewTimestampHistory)
    }

    /**
     * Chamado quando o RecyclerView precisa criar uma nova "linha" (ViewHolder).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        // Infla (lê) o seu layout XML
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_scan, parent, false) // <-- Apontando para o seu layout

        return HistoryViewHolder(view)
    }

    /**
     * Chamado quando o RecyclerView precisa exibir os dados de um item
     * específico (na 'position') numa linha (ViewHolder).
     */
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        // Pega o item da lista (do JSON)
        val item = historyList[position]

        // <-- 💎 CORREÇÃO: Preenche os TextViews corretos

        // 1. Define o nome do alimento
        holder.foodName.text = item.foodName

        // 2. Define as calorias (no formato "34 kcal" como no seu layout)
        holder.calories.text = "${item.calories} kcal"

        // 3. Define os nutrientes (Proteína e Fibra)
        holder.nutrients.text = "Prot: ${item.protein}g | Fibra: ${item.fiber}g"

        // 4. Define a data e hora
        holder.timestamp.text = item.timestamp
    }

    /**
     * Informa ao RecyclerView quantos itens no total existem na lista.
     */
    override fun getItemCount(): Int {
        return historyList.size
    }
}