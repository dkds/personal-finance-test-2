package com.dkds.util

import com.google.gson.Gson
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.random.Random

class DataGenerator {
    private val yearMonthFormat = DateTimeFormatter.ofPattern("yyyy-MM")

    private val transactionCategories = listOf(
        "food", "transport", "entertainment", "shopping", "bills", "others"
    )
    private val paymentMethods = listOf(
        "cash", "credit_card", "debit_card", "e-wallet", "online_banking"
    )

    private fun roundToMultiple(number: Double, multiple: Double): Double {
        return multiple * (number / multiple)
    }

    private fun generateDailyTransactionCounts(dailyAverageCount: Double, numDays: Int): List<Int> {
        val dailyCounts = mutableListOf<Int>()
        var totalCount = 0

        for (day in 1..numDays) {
            val dayWeight = 1 + ((day - 1).toDouble() / numDays) * 0.5
            val randomMultiplier = 0.8 + (Math.random() * 0.4) // Randomness between 80% and 120%
            val dailyCount = (dailyAverageCount * dayWeight * randomMultiplier).toInt()
            dailyCounts.add(dailyCount)
            totalCount += dailyCount
        }

        val actualDailyAverage = totalCount.toDouble() / numDays
        val adjustmentFactor = dailyAverageCount / actualDailyAverage

        for (i in dailyCounts.indices) {
            dailyCounts[i] = (dailyCounts[i] * adjustmentFactor).toInt()
        }

        return dailyCounts
    }

    fun generateTransactionsData(
        year: Int, personalData: PersonalData
    ): List<TransactionData> {
        val (inflationRate, dependentFamilySize, age, monthsWithHigherSpending, numberExpensesAMonth, mostFrequentExpenseCategories, estimatedMonthlyExpenses) = personalData

        val generatedCategories =
            transactionCategories + mostFrequentExpenseCategories + mostFrequentExpenseCategories

        val records = mutableListOf<TransactionData>()

        for (month in 1..12) {
            val isHigherSpendingMonth = monthsWithHigherSpending.contains(month)
            var monthlyTransactionsCount =
                (numberExpensesAMonth - 10..numberExpensesAMonth + 10).random()
            val monthlyAverageAmount =
                Random.nextDouble(estimatedMonthlyExpenses * 0.8, estimatedMonthlyExpenses * 1.2)

            if (month in monthsWithHigherSpending) {
                if (isHigherSpendingMonth) {
                    monthlyTransactionsCount += 10
                    monthlyTransactionsCount *= Random.nextDouble(1.1, 1.2).toInt()
                } else {
                    monthlyTransactionsCount -= 10
                    monthlyTransactionsCount *= Random.nextDouble(0.8, 0.9).toInt()
                }
            }

            val numDays = if (month in listOf(1, 3, 5, 7, 8, 10, 12)) 31 else 28
            val dailyAverageCount = monthlyTransactionsCount.toDouble() / numDays
            val dailyTransactionCounts = generateDailyTransactionCounts(
                dailyAverageCount, numDays
            )

            println(dailyTransactionCounts)

            val dailyAverageAmount = monthlyAverageAmount / numDays
            val monthlyRecords = mutableListOf<TransactionData>()

            for (day in 1..numDays) {
                val dailyCount = dailyTransactionCounts[day - 1]
                val averageTransactionAmount = dailyAverageAmount / dailyCount

                for (i in 0 until dailyCount) {
                    val date = LocalDate.of(year, month, day)
                    val amount =
                        Random.nextDouble(averageTransactionAmount * 0.5, averageTransactionAmount)
                    val category = generatedCategories.random()
                    val isMostFrequentCategory = mostFrequentExpenseCategories.contains(category)

                    var finalAmount = if (isMostFrequentCategory) {
                        Random.nextDouble(amount * 0.7, amount)
                    } else {
                        Random.nextDouble(amount * 0.9, amount * 1.1)
                    }

                    if (isHigherSpendingMonth) {
                        finalAmount *= Random.nextDouble(1.0, 1.06)
                    }

                    finalAmount *= (1 + (inflationRate * 0.01))
                    finalAmount *= (1 + (dependentFamilySize * 0.02))
                    finalAmount *= (1 + (age * 0.02))

                    if (finalAmount < 100) {
                        finalAmount = roundToMultiple(finalAmount, 5.0)
                    } else {
                        finalAmount = roundToMultiple(finalAmount / 100, 10.0) * 100
                    }

                    val paymentMethod = paymentMethods.random()
                    val creditOrDebit = "debit" // Since there is no "income" category

                    val record = TransactionData(
                        date = date,
                        transactionCategory = category,
                        amount = finalAmount,
                        creditOrDebit = creditOrDebit,
                        paymentMethod = paymentMethod,
                        inflationRate = inflationRate,
                        dependentFamilySize = dependentFamilySize,
                        age = age,
                        monthsWithHigherSpending = monthsWithHigherSpending,
                        numberExpensesAMonth = numberExpensesAMonth,
                        mostFrequentExpenseCategories = mostFrequentExpenseCategories,
                        estimatedMonthlyExpenses = estimatedMonthlyExpenses,
                        yearMonth = date.format(yearMonthFormat),
                    )

                    monthlyRecords.add(record)
                }
            }

            records.addAll(monthlyRecords)
        }

        // get monthly totals
        val monthlyBudgets = records.groupingBy { it.yearMonth }.eachSumBy { it.amount }
        Timber.v(Gson().toJson(monthlyBudgets))

        val averageMonthlyBudget = monthlyBudgets.values.average()

        records.map { transactionData ->
            transactionData.budget = monthlyBudgets[transactionData.yearMonth] ?: 0.0
            transactionData.averageMonthlyBudget = averageMonthlyBudget
            transactionData.cumulativeMonthlySpending =
                records.filter { it.yearMonth == transactionData.yearMonth && it.date < transactionData.date }
                    .sumOf { it.amount }
            transactionData.lastMonthBudget =
                monthlyBudgets[transactionData.date.minusMonths(1).format(yearMonthFormat)]
                    ?: monthlyBudgets[transactionData.yearMonth]
                            ?: 0.0
        }

        records.sortBy { it.date }

        return records
    }

    private fun <T, K> Grouping<T, K>.eachSumBy(
        selector: (T) -> Double
    ): Map<K, Double> =
        fold(0.0) { acc, elem -> acc + selector(elem) }

    fun main() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val personalData = PersonalData(
            inflationRate = 5.0,
            dependentFamilySize = 2,
            age = 30,
            monthsWithHigherSpending = listOf(3, 7),
            numberExpensesAMonth = 20,
            mostFrequentExpenseCategories = listOf("food", "shopping"),
            estimatedMonthlyExpenses = 3000.0
        )

        val transactionsData = generateTransactionsData(year, personalData)
        print(transactionsData)
    }

}

data class PersonalData(
    val inflationRate: Double,
    val dependentFamilySize: Int,
    val age: Int,
    val monthsWithHigherSpending: List<Int>,
    val numberExpensesAMonth: Int,
    val mostFrequentExpenseCategories: List<String>,
    val estimatedMonthlyExpenses: Double
)

data class TransactionData(
    val date: LocalDate,
    val transactionCategory: String,
    val amount: Double,
    val creditOrDebit: String,
    val paymentMethod: String,
    val inflationRate: Double,
    val dependentFamilySize: Int,
    val age: Int,
    val monthsWithHigherSpending: List<Int>,
    val numberExpensesAMonth: Int,
    val mostFrequentExpenseCategories: List<String>,
    val estimatedMonthlyExpenses: Double,
    val day: Int = date.dayOfMonth,
    val month: Int = date.month.value,
    val year: Int = date.year,
    val yearMonth: String,
    var cumulativeMonthlySpending: Double = 0.0,
    var lastMonthBudget: Double = 0.0,
    var averageMonthlyBudget: Double = 0.0,
    var budget: Double = 0.0
)
