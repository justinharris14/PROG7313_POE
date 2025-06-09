
package com.example.bybetterbudget

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bybetterbudget.utils.FirestoreHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class Feature2Activity : AppCompatActivity() {

    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var btnShowExpenses: Button
    private lateinit var rvExpenses: RecyclerView

    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private lateinit var adapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature2)

        // Back to Home
        findViewById<Button>(R.id.btnBackHome2).setOnClickListener {
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(this)
            }
            finish()
        }

        etStartDate      = findViewById(R.id.etStartDate)
        etEndDate        = findViewById(R.id.etEndDate)
        btnShowExpenses  = findViewById(R.id.btnShowExpenses)
        rvExpenses       = findViewById(R.id.rvExpenses)

        // RecyclerView setup
        adapter = TransactionAdapter(transactions)
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = adapter

        // Date pickers
        val now = Calendar.getInstance()
        val listener = DatePickerDialog.OnDateSetListener { view, y, m, d ->
            (view.tag as? EditText)?.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
        }
        fun setupPicker(et: EditText) {
            et.setOnClickListener {
                DatePickerDialog(
                    this, listener,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
                ).apply {

                    datePicker.tag = et
                    show()
                }
            }
        }
        setupPicker(etStartDate)
        setupPicker(etEndDate)


        val toDate   = now.time
        val fromDate = Calendar.getInstance().apply {
            time = toDate
            add(Calendar.DAY_OF_YEAR, -30)
        }.time
        etStartDate.setText(dateFormat.format(fromDate))
        etEndDate.setText(dateFormat.format(toDate))

        // Show Expenses button
        btnShowExpenses.setOnClickListener {
            val from = etStartDate.text.toString()
            val to   = etEndDate.text.toString()
            if (from.isBlank() || to.isBlank()) {
                Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadExpenses(from, to)
        }
    }

    private fun loadExpenses(from: String, to: String) {
        lifecycleScope.launch {
            try {
                val uid = FirestoreHelper.getUid()
                val snap = db.collection("users")
                    .document(uid)
                    .collection("budgets")
                    .whereGreaterThanOrEqualTo("date", from)
                    .whereLessThanOrEqualTo("date", to)
                    .get()
                    .await()

                transactions.clear()
                for (doc in snap.documents) {
                    val cat = doc.getString("category") ?: continue
                    val amt = doc.getDouble("amount") ?: continue
                    val dateStr = doc.getString("date") ?: continue
                    val desc = doc.getString("description")
                    // Only include expenses (if you later store type)
                    transactions.add(Transaction(cat, amt, dateStr, desc))
                }
                adapter.notifyDataSetChanged()
                if (transactions.isEmpty()) {
                    Toast.makeText(this@Feature2Activity, "No entries found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@Feature2Activity, "Error loading entries", Toast.LENGTH_LONG).show()
            }
        }
    }
}