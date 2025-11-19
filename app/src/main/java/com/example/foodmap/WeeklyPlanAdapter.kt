package com.example.foodmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeeklyPlanAdapter(
    private val meals: List<Meal>,
    private val onCheckChanged: (Meal, Boolean) -> Unit // Função de callback
) : RecyclerView.Adapter<WeeklyPlanAdapter.MealViewHolder>() {

    class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDayAndMeal: TextView = itemView.findViewById(R.id.txtDayAndMeal)
        val txtDescription: TextView = itemView.findViewById(R.id.txtDescription)
        val txtCalories: TextView = itemView.findViewById(R.id.txtCalories)
        val checkboxDone: CheckBox = itemView.findViewById(R.id.checkboxDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_plan, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]

        // Junta Dia e Tipo (ex: "Segunda - Almoço")
        holder.txtDayAndMeal.text = "${meal.dayOfWeek} - ${meal.type}"
        holder.txtDescription.text = meal.description
        holder.txtCalories.text = "${meal.calories} kcal"

        // Define o estado do checkbox sem acionar o listener
        holder.checkboxDone.setOnCheckedChangeListener(null)
        holder.checkboxDone.isChecked = meal.isDone

        // Ao clicar no checkbox, avisa a Activity para salvar
        holder.checkboxDone.setOnCheckedChangeListener { _, isChecked ->
            onCheckChanged(meal, isChecked)
        }
    }

    override fun getItemCount() = meals.size
}