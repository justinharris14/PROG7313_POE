package com.example.bybetterbudget

data class CategoryStatus(
    val category: String,
    val spent: Float,
    val min: Float,
    val max: Float,
    val withinGoals: Boolean
)