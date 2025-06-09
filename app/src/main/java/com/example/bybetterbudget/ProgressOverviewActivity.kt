package com.example.bybetterbudget

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bybetterbudget.utils.FirestoreHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class ProgressOverviewActivity : AppCompatActivity() {

    private lateinit var rvStatus: RecyclerView
    private lateinit var tvSummary: TextView

    private val db = FirebaseFirestore.getInstance()
    private val categories = listOf("Food", "Transport", "Entertainment", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_overview)

        // Single Home button at bottom
        findViewById<Button>(R.id.btnBackHome).setOnClickListener {
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(this)
            }
            finish()
        }

        rvStatus  = findViewById(R.id.rvStatus)
        tvSummary = findViewById(R.id.tvSummary)

        rvStatus.layoutManager = LinearLayoutManager(this)
        loadProgressData()
    }

    private fun loadProgressData() {
        // 30 days ago → today
        val now = Date()
        val cal = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_YEAR, -30)
        }
        val fromDate = String.format("%tF", cal)
        val toDate   = String.format("%tF", now)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = FirestoreHelper.getUid()

                // 1) Fetch budgets in last 30 days
                val budgetsSnap = db.collection("users").document(uid)
                    .collection("budgets")
                    .whereGreaterThanOrEqualTo("date", fromDate)
                    .whereLessThanOrEqualTo("date", toDate)
                    .get()
                    .await()

                val totals = categories.associateWith { 0f }.toMutableMap()
                budgetsSnap.forEach { doc ->
                    val cat = doc.getString("category") ?: return@forEach
                    val amt = doc.getDouble("amount")?.toFloat() ?: 0f
                    totals[cat] = totals.getOrDefault(cat, 0f) + amt
                }

                // 2) Fetch goals
                val goalsSnap = db.collection("users").document(uid)
                    .collection("goals")
                    .get()
                    .await()

                val statusList = categories.map { cat ->
                    val spent  = totals[cat] ?: 0f
                    val minG   = goalsSnap.documents.firstOrNull { it.id == cat }
                        ?.getDouble("min")?.toFloat() ?: 0f
                    val maxG   = goalsSnap.documents.firstOrNull { it.id == cat }
                        ?.getDouble("max")?.toFloat() ?: 0f
                    val within = spent in minG..maxG
                    CategoryStatus(cat, spent, minG, maxG, within)
                }

                // 3) Update UI
                withContext(Dispatchers.Main) {
                    val okCount = statusList.count { it.withinGoals }
                    tvSummary.text = "$okCount of ${statusList.size} categories OK this month"
                    rvStatus.adapter = CategoryStatusAdapter(statusList)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProgressOverviewActivity,
                        "Error loading progress",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}