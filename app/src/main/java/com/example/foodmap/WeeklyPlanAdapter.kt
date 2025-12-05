package com.example.foodmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// A classe Meal foi APAGADA daqui porque já existe no arquivo Meal.kt

class WeeklyPlanAdapter(
    private val mealList: List<Meal>,
    private val onCheckChanged: (Meal, Boolean) -> Unit
) : RecyclerView.Adapter<WeeklyPlanAdapter.MealViewHolder>() {

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvMealTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvMealDesc)
        val tvCal: TextView = itemView.findViewById(R.id.tvMealCalories)
        val cbDone: CheckBox = itemView.findViewById(R.id.cbDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_plan, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = mealList[position]

        holder.tvTitle.text = "${meal.dayOfWeek} - ${meal.type}"
        holder.tvDesc.text = meal.description
        holder.tvCal.text = "${meal.calories} kcal"

        holder.cbDone.setOnCheckedChangeListener(null)
        holder.cbDone.isChecked = meal.isDone

        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            meal.isDone = isChecked
            onCheckChanged(meal, isChecked)
        }
    }

    override fun getItemCount(): Int = mealList.size
}