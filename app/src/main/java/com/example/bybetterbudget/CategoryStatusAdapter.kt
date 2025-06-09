package com.example.bybetterbudget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bybetterbudget.R

class CategoryStatusAdapter(
    private val items: List<CategoryStatus>
) : RecyclerView.Adapter<CategoryStatusAdapter.StatusViewHolder>() {

    inner class StatusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView  = view.findViewById(R.id.tvCat)
        val tvSpent: TextView     = view.findViewById(R.id.tvSpent)
        val tvGoalRange: TextView = view.findViewById(R.id.tvGoalRange)
        val pbProgress: ProgressBar = view.findViewById(R.id.pbProgress)
        val tvPercent: TextView   = view.findViewById(R.id.tvPercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_status, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val s = items[position]
        holder.tvCategory.text   = s.category
        holder.tvSpent.text      = "Spent: R%.2f".format(s.spent)
        holder.tvGoalRange.text  = "Range: R%.2f - R%.2f".format(s.min, s.max)

        // Calculate percent within range
        val range = s.max - s.min
        val percent = if (range > 0f) {
            ((s.spent - s.min) / range * 100f).coerceIn(0f, 100f)
        } else 0f

        holder.pbProgress.progress = percent.toInt()
        holder.tvPercent.text     = "${percent.toInt()}%"
    }

    override fun getItemCount(): Int = items.size
}