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

        val historyData = createMockData()

        val adapter = HistoryAdapter(historyData)
        recyclerView.adapter = adapter
    }

    private fun createMockData(): List<ScanResult> {
        return listOf(
            ScanResult("Brócolis Cozido", 34, 2.8, 2.6, "07/10/2025 14:05"),
            ScanResult("Maçã Fuji", 52, 0.3, 2.4, "07/10/2025 09:22"),
            ScanResult("Peito de Frango Grelhado", 165, 31.0, 0.0, "06/10/2025 19:45")
        )
    }
}