package com.example.bybetterbudget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bybetterbudget.R

class GoalsAdapter(
    private val items: List<Goal>,
    private val onEditClick: (Int) -> Unit
) : RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {

    inner class GoalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvMin: TextView = view.findViewById(R.id.tvMin)
        val tvMax: TextView = view.findViewById(R.id.tvMax)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = items[position]
        holder.tvCategory.text = goal.category
        holder.tvMin.text = "Min: R%.2f".format(goal.min)
        holder.tvMax.text = "Max: R%.2f".format(goal.max)
        holder.btnEdit.setOnClickListener { onEditClick(position) }
    }

    override fun getItemCount(): Int = items.size
}