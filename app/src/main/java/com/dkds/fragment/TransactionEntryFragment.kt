package com.dkds.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dkds.AppExecutors
import com.dkds.R
import com.dkds.data.entity.Transaction
import com.dkds.data.entity.TransactionType
import com.dkds.databinding.FragmentTransactionEntryBinding
import com.dkds.view_model.TransactionViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class TransactionEntryFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentTransactionEntryBinding? = null
    private var transaction: Transaction? = null
    private val transactionViewModel: TransactionViewModel by viewModels()

    @Inject
    lateinit var appExecutors: AppExecutors

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.transactionTypeRadioGroup.setOnCheckedChangeListener { _, checked ->
            when (checked) {
                R.id.radio_income -> {
                    transaction?.type = TransactionType.INCOME
                }

                R.id.radio_expense -> {
                    transaction?.type = TransactionType.EXPENSE
                }
            }
        }
        transaction = Transaction()

        binding.transactionAmount.addTextChangedListener(afterTextChanged = {
            transaction?.amount = BigDecimal.valueOf(it.toString().toDouble())
        })

        binding.transactionDescription.addTextChangedListener(afterTextChanged = {
            transaction?.description = it.toString()
        })

        binding.transactionSaveButton.setOnClickListener {
            transaction!!.time = LocalDateTime.now()
            Timber.v(Gson().toJson(transaction))

            appExecutors.diskIO().execute {
                transactionViewModel.addNew(transaction!!)
            }
            appExecutors.mainThread().execute {
                findNavController().navigate(R.id.action_TransactionEntryFragment_to_TransactionsFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
