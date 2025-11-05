package com.example.foodmap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // MUDANÇA: Carrega os dados reais do HistoryManager
        val historyData = HistoryManager.getHistory(this)

        // (Opcional: Mostrar uma mensagem se o histórico estiver vazio)
        // val emptyMessage = findViewById<TextView>(R.id.textViewEmptyHistory) // Você precisaria adicionar este TextView ao seu layout
        // if (historyData.isEmpty()) {
        //     emptyMessage.visibility = View.VISIBLE
        //     recyclerView.visibility = View.GONE
        // } else {
        //     emptyMessage.visibility = View.GONE
        //     recyclerView.visibility = View.VISIBLE
        // }

        // Configura o adapter com os dados reais
        val adapter = HistoryAdapter(historyData)
        recyclerView.adapter = adapter
    }

    // MUDANÇA: A data class ScanResult foi movida para o seu próprio ficheiro (ScanResult.kt)
    // MUDANÇA: A função createMockData() foi removida pois não é mais necessária.
}