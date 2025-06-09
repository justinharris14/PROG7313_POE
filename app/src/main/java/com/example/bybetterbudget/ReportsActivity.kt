package com.example.bybetterbudget

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bybetterbudget.utils.FirestoreHelper
import com.example.bybetterbudget.R
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ReportsActivity : AppCompatActivity() {

    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText
    private lateinit var btnGenerate: Button
    private lateinit var combinedChart: CombinedChart

    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val categories = listOf("Food", "Transport", "Entertainment", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        findViewById<Button>(R.id.btnBackHome).setOnClickListener {
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(this)
            }
            finish()
        }

        etFromDate    = findViewById(R.id.etFromDate)
        etToDate      = findViewById(R.id.etToDate)
        btnGenerate   = findViewById(R.id.btnGenerate)
        combinedChart = findViewById(R.id.combinedChart)

        setupDatePickers()
        setupChartAppearance()

        btnGenerate.setOnClickListener { generateReport() }
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val fromListener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
            Calendar.getInstance().apply {
                set(y, m, d)
                etFromDate.setText(dateFormat.format(time))
            }
        }
        val toListener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
            Calendar.getInstance().apply {
                set(y, m, d)
                etToDate.setText(dateFormat.format(time))
            }
        }
        etFromDate.setOnClickListener {
            DatePickerDialog(
                this, fromListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        etToDate.setOnClickListener {
            DatePickerDialog(
                this, toListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val now = Date()
        val first = Calendar.getInstance().apply { time = now; set(Calendar.DAY_OF_MONTH, 1) }
        etFromDate.setText(dateFormat.format(first.time))
        etToDate.setText(dateFormat.format(now))
    }

    private fun setupChartAppearance() {
        combinedChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.verticalAlignment   = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
        }
    }

    private fun generateReport() {
        val from = etFromDate.text.toString()
        val to   = etToDate.text.toString()

        if (from.isBlank() || to.isBlank()) {
            Toast.makeText(this, "Enter both dates", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = FirestoreHelper.getUid()


                val budgetsSnap = db.collection("users").document(uid)
                    .collection("budgets")
                    .whereGreaterThanOrEqualTo("date", from)
                    .whereLessThanOrEqualTo("date", to)
                    .get().await()

                val totals = categories.associateWith { 0f }.toMutableMap()
                budgetsSnap.forEach { doc ->
                    val cat = doc.getString("category") ?: return@forEach
                    val amt = doc.getDouble("amount")?.toFloat() ?: 0f
                    totals[cat] = totals.getOrDefault(cat, 0f) + amt
                }


                val goalsSnap = db.collection("users").document(uid)
                    .collection("goals").get().await()
                val minGoals = categories.map { cat ->
                    goalsSnap.documents.firstOrNull { it.id == cat }
                        ?.getDouble("min")?.toFloat() ?: 0f
                }
                val maxGoals = categories.map { cat ->
                    goalsSnap.documents.firstOrNull { it.id == cat }
                        ?.getDouble("max")?.toFloat() ?: 0f
                }

                withContext(Dispatchers.Main) {
                    displayCombinedChart(totals, minGoals, maxGoals)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportsActivity, "Error loading report", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayCombinedChart(
        totals: Map<String, Float>,
        minGoals: List<Float>,
        maxGoals: List<Float>
    ) {
        val barEntries = totals.values.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val barDataSet = BarDataSet(barEntries, "Spent").apply {
            axisDependency = YAxis.AxisDependency.LEFT
        }
        val barData = BarData(barDataSet).apply { barWidth = 0.45f }

        val minLine = LineDataSet(
            minGoals.mapIndexed { i, v -> Entry(i.toFloat(), v) }, "Min Goal"
        ).apply { setDrawCircles(false); lineWidth = 1.5f; axisDependency = YAxis.AxisDependency.LEFT }
        val maxLine = LineDataSet(
            maxGoals.mapIndexed { i, v -> Entry(i.toFloat(), v) }, "Max Goal"
        ).apply { setDrawCircles(false); lineWidth = 1.5f; axisDependency = YAxis.AxisDependency.LEFT }
        val lineData = LineData(minLine, maxLine)

        combinedChart.data = CombinedData().apply { setData(barData); setData(lineData) }
        combinedChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(categories)
            labelCount = categories.size
        }
        combinedChart.invalidate()
    }
}
