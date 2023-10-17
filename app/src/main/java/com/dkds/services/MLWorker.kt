package com.dkds.services

import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dkds.R
import com.dkds.data.entity.Settings
import com.dkds.util.DataGenerator
import com.dkds.util.PersonalData
import com.dkds.util.TransactionData
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.Calendar
import java.util.LinkedList

class MLWorker constructor(
    val context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters) {


    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        val progress = "Starting Download"
        setForeground(createForegroundInfo(progress))
        generateData()
        return Result.success()
    }

    private fun generateData() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
//        val settingsList = settingsDao.getAll().value
        var settings = Settings()
//        if (settingsList != null) {
//            if (settingsList.isNotEmpty()) {
//                settings = settingsList[0]
//            }
//        }

        val personalData = PersonalData(
            inflationRate = settings.inflationRate,
            dependentFamilySize = settings.dependentFamilySize,
            age = settings.age,
            monthsWithHigherSpending = settings.monthsWithHigherSpending.split(",")
                .map { it.toInt() },
            numberExpensesAMonth = settings.noOfExpensesAMonth,
            mostFrequentExpenseCategories = settings.mostFrequentExpenseCategories.split(","),
            estimatedMonthlyExpenses = settings.estimatedMonthlyExpenses.toDouble()
        )

        val dataGenerator = DataGenerator()
        val records = LinkedList<TransactionData>()
        (year - 5..year).forEach {
            records.addAll(dataGenerator.generateTransactionsData(it, personalData))
        }
        writeCsv(records)
    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = applicationContext.getString(R.string.notification_channel_id)
        val title = applicationContext.getString(R.string.notification_title)
        val cancel = applicationContext.getString(R.string.cancel_download)
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        createChannel()

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_network_node)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent).build()


        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(0, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            ForegroundInfo(0, notification)
        }
    }

    private fun writeCsv(transactionDataList: List<TransactionData>) {
        val parent = context.getDir("generated", Context.MODE_PRIVATE)
        val file = File(parent, "data.csv")
        file.createNewFile()
        val writer = BufferedWriter(FileWriter(file))
        val csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader(
                "Date",
                "Transaction Category",
                "Amount",
                "Credit/Debit",
                "Payment Method",
                "Inflation Rate",
                "Dependent Family Size",
                "Age",
                "Months with Higher Spending",
                "Number of Expenses a Month",
                "Most Frequent Expense Categories",
                "Estimated Monthly Expenses",
                "Day",
                "Month",
                "Year",
                "Year-Month",
                "Cumulative Monthly Spending",
                "Last Month Budget",
                "Average Monthly Budget",
                "Budget",
            )
            .build()

        val csvPrinter = CSVPrinter(writer, csvFormat)
        Timber.v("printing csv: ${transactionDataList.size} entries")
        transactionDataList.forEach { transaction ->
            csvPrinter.printRecord(
                transaction.date.toString(),
                transaction.transactionCategory,
                String.format("%.2f", transaction.amount),
                transaction.creditOrDebit,
                transaction.paymentMethod,
                transaction.inflationRate,
                transaction.dependentFamilySize,
                transaction.age,
                transaction.monthsWithHigherSpending,
                transaction.numberExpensesAMonth,
                transaction.mostFrequentExpenseCategories,
                transaction.estimatedMonthlyExpenses,
                transaction.day,
                transaction.month,
                transaction.year,
                transaction.yearMonth,
                String.format("%.2f", transaction.cumulativeMonthlySpending),
                String.format("%.2f", transaction.lastMonthBudget),
                String.format("%.2f", transaction.averageMonthlyBudget),
                String.format("%.2f", transaction.budget),
            )
        }
        csvPrinter.flush();
        csvPrinter.close();
    }

    private fun createChannel() {
        // Create a Notification channel
    }

    companion object {
        const val KEY_OUTPUT_FILE_NAME = "KEY_OUTPUT_FILE_NAME"
    }
}
