package com.dkds.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dkds.AppExecutors
import com.dkds.data.dao.TransactionDao
import com.dkds.databinding.FragmentTransactionsBinding
import com.dkds.view_model.TransactionViewModel
import com.dkds.view_model.TransactionsListAdapter
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val transactionViewModel: TransactionViewModel by viewModels()

    @Inject
    lateinit var transactionDao: TransactionDao

    @Inject
    lateinit var appExecutors: AppExecutors

    // This property is only valid between onCreateView and
    // onDestroyView.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        val adapter = TransactionsListAdapter()
        binding.transactionList.adapter = adapter

        transactionViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            Timber.v(Gson().toJson(transactions))
            adapter.submitList(transactions)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
