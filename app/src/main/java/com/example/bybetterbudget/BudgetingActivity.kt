// File: app/src/main/java/com/example/bybetterbudget/BudgetingActivity.kt

package com.example.bybetterbudget

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.bybetterbudget.utils.FirestoreHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class BudgetingActivity : AppCompatActivity() {

    private lateinit var spinnerType: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnAddCategory: Button
    private lateinit var btnSelectImage: Button
    private lateinit var ivReceipt: ImageView
    private lateinit var btnSaveExpense: Button
    private lateinit var rvTransactions: RecyclerView

    private lateinit var adapter: TransactionAdapter
    private var selectedImageUri: Uri? = null

    private val categories = mutableListOf("Food", "Transport", "Entertainment", "Other")
    private val types = listOf("Expense", "Income")

    private val firestore = FirebaseFirestore.getInstance()
    private val storage: StorageReference = FirebaseStorage.getInstance().reference

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivReceipt.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budgeting)

        // Back Home
        findViewById<Button>(R.id.btnBackHome).setOnClickListener {
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(this)
            }
            finish()
        }

        // View binding
        spinnerType     = findViewById(R.id.spinnerType)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etAmount        = findViewById(R.id.etAmount)
        etDate          = findViewById(R.id.etDate)
        etDescription   = findViewById(R.id.etDescription)
        btnAddCategory  = findViewById(R.id.btnAddCategory)
        btnSelectImage  = findViewById(R.id.btnSelectImage)
        ivReceipt       = findViewById(R.id.ivReceipt)
        btnSaveExpense  = findViewById(R.id.btnSaveExpense)
        rvTransactions  = findViewById(R.id.rvTransactions)

        // Spinners
        spinnerType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )
        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        spinnerCategory.adapter = categoryAdapter

        // Add new category
        btnAddCategory.setOnClickListener {
            val input = EditText(this).apply { hint = "New category" }
            AlertDialog.Builder(this)
                .setTitle("Add Category")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val newCat = input.text.toString().trim()
                    if (newCat.isNotBlank() && !categories.contains(newCat)) {
                        categories.add(newCat)
                        categoryAdapter.notifyDataSetChanged()
                        spinnerCategory.setSelection(categories.indexOf(newCat))
                    } else {
                        Toast.makeText(this, "Invalid or duplicate", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Image picker
        btnSelectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Date picker
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    etDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // RecyclerView
        adapter = TransactionAdapter(mutableListOf())
        rvTransactions.adapter = adapter

        // Save expense
        btnSaveExpense.setOnClickListener {
            val type     = spinnerType.selectedItem as String
            val category = spinnerCategory.selectedItem as String
            val amount   = etAmount.text.toString().toDoubleOrNull()
            val dateStr  = etDate.text.toString().trim()
            if (amount == null || dateStr.isBlank()) {
                Toast.makeText(this, "Enter amount & date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val desc = etDescription.text.toString().takeIf { it.isNotBlank() } ?: ""

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val uid    = FirestoreHelper.getUid()
                    val coll   = firestore.collection("users")
                        .document(uid)
                        .collection("budgets")
                    val docRef = coll.document()

                    val data = mutableMapOf<String, Any>(
                        "type"        to type,
                        "amount"      to amount,
                        "category"    to category,
                        "date"        to dateStr,
                        "description" to desc
                    )

                    // Upload image if selected
                    selectedImageUri?.let { uri ->
                        val imgRef = storage.child("users/$uid/budgets/${docRef.id}/receipt.jpg")
                        imgRef.putFile(uri).await()
                        val url = imgRef.downloadUrl.await().toString()
                        data["imageUrl"] = url
                    }

                    // Write to Firestore
                    docRef.set(data, SetOptions.merge()).await()

                    withContext(Dispatchers.Main) {
                        adapter.addTransaction(Transaction(category, amount, dateStr, desc))
                        etAmount.text.clear()
                        etDate.text.clear()
                        etDescription.text.clear()
                        ivReceipt.setImageResource(android.R.drawable.ic_menu_report_image)
                        selectedImageUri = null
                        Toast.makeText(this@BudgetingActivity, "Saved!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@BudgetingActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}