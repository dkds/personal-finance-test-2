package com.dkds.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dkds.data.dao.TransactionDao
import com.dkds.data.entity.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionDao: TransactionDao
) : ViewModel() {
    val transactions: LiveData<List<Transaction>> = transactionDao.getAll()

    fun addNew(transaction: Transaction) {
        transactionDao.insert(transaction)
    }
}
