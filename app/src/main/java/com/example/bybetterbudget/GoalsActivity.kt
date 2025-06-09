package com.example.bybetterbudget

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bybetterbudget.utils.FirestoreHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data model for goal
data class Goal(
    var category: String,
    var min: Double,
    var max: Double
)

class GoalsActivity : AppCompatActivity() {

    private lateinit var rvGoals: RecyclerView
    private lateinit var btnAddGoal: Button
    private lateinit var adapter: GoalsAdapter

    private val categories = listOf("Food", "Transport", "Entertainment", "Other")
    private val goalsList = mutableListOf<Goal>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        // Back  Home
        findViewById<Button>(R.id.btnBackHome).setOnClickListener {
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(this)
            }
            finish()
        }

        btnAddGoal = findViewById(R.id.btnAddGoal)
        rvGoals = findViewById(R.id.rvGoals)

        adapter = GoalsAdapter(goalsList) { position -> showGoalDialog(position) }
        rvGoals.layoutManager = LinearLayoutManager(this)
        rvGoals.adapter = adapter


        lifecycleScope.launch {
            try {
                val uid = FirestoreHelper.getUid()
                val snapshot = db.collection("users")
                    .document(uid)
                    .collection("goals")
                    .get()
                    .await()
                goalsList.clear()
                for (doc in snapshot.documents) {
                    val cat = doc.id
                    val minVal = doc.getDouble("min") ?: 0.0
                    val maxVal = doc.getDouble("max") ?: 0.0
                    goalsList.add(Goal(cat, minVal, maxVal))
                }
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@GoalsActivity, "Failed to load goals", Toast.LENGTH_SHORT).show()
            }
        }

        btnAddGoal.setOnClickListener { showGoalDialog(-1) }
    }

    private fun showGoalDialog(editPosition: Int) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_edit_goal, null)
        val spinner: Spinner = dialogView.findViewById(R.id.spinnerCategory)
        val etMin: EditText = dialogView.findViewById(R.id.etMin)
        val etMax: EditText = dialogView.findViewById(R.id.etMax)

        spinner.visibility = View.VISIBLE
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        if (editPosition >= 0) {
            val goal = goalsList[editPosition]
            spinner.setSelection(categories.indexOf(goal.category))
            etMin.setText(goal.min.toString())
            etMax.setText(goal.max.toString())
        }

        AlertDialog.Builder(this)
            .setTitle(if (editPosition >= 0) "Edit Goal" else "New Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val selectedCat = spinner.selectedItem as String
                val minValue = etMin.text.toString().toDoubleOrNull() ?: 0.0
                val maxValue = etMax.text.toString().toDoubleOrNull() ?: 0.0

                lifecycleScope.launch {
                    try {
                        FirestoreHelper.saveDocument(
                            collectionName = "goals",
                            data = mapOf("min" to minValue, "max" to maxValue),
                            docId = selectedCat
                        )

                        if (editPosition >= 0) {
                            goalsList[editPosition].apply {
                                category = selectedCat
                                min = minValue
                                max = maxValue
                            }
                            adapter.notifyItemChanged(editPosition)
                        } else {
                            goalsList.add(Goal(selectedCat, minValue, maxValue))
                            adapter.notifyItemInserted(goalsList.lastIndex)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@GoalsActivity,
                            "Error saving goal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
