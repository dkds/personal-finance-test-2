package com.dkds.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.dkds.AppExecutors
import com.dkds.R
import com.dkds.data.entity.Settings
import com.dkds.databinding.FragmentSettingsEntryBinding
import com.dkds.services.MLWorker
import com.dkds.view_model.SettingsViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class SettingsEntryFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentSettingsEntryBinding? = null
    private var settings: Settings? = null
    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var appExecutors: AppExecutors

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel.settings.observe(viewLifecycleOwner) { settings ->
            Timber.v(Gson().toJson(settings))
            if (settings.isNotEmpty()) {
                this.settings = settings[0]
            } else {
                this.settings = Settings()
            }
            binding.age.setText(this.settings!!.age.toString())
            binding.dependentFamilySize.setText(this.settings!!.dependentFamilySize.toString())
            binding.noOfExpensesAMonth.setText(this.settings!!.noOfExpensesAMonth.toString())
            binding.estimatedMonthlyExpenses.setText(this.settings!!.estimatedMonthlyExpenses.toString())
            binding.inflationRate.setText(this.settings!!.inflationRate.toString())
            binding.monthsWithHigherSpending.setText(this.settings!!.monthsWithHigherSpending)
            binding.mostFrequentExpenseCategories.setText(this.settings!!.mostFrequentExpenseCategories)
        }

        binding.age.addTextChangedListener(afterTextChanged = {
            try {
                settings?.age = it.toString().toInt()
            } catch (e: NumberFormatException) {
                settings?.age = 25
            }
        })
        binding.dependentFamilySize.addTextChangedListener(afterTextChanged = {
            try {
                settings?.dependentFamilySize = it.toString().toInt()
            } catch (e: NumberFormatException) {
                settings?.dependentFamilySize = 0
            }
        })
        binding.noOfExpensesAMonth.addTextChangedListener(afterTextChanged = {
            try {
                settings?.noOfExpensesAMonth = it.toString().toInt()
            } catch (e: NumberFormatException) {
                settings?.noOfExpensesAMonth = 60
            }
        })
        binding.estimatedMonthlyExpenses.addTextChangedListener(afterTextChanged = {
            try {
                settings?.estimatedMonthlyExpenses = BigDecimal.valueOf(it.toString().toDouble())
            } catch (e: NumberFormatException) {
                settings?.estimatedMonthlyExpenses = BigDecimal.valueOf(100000.0)
            }
        })
        binding.inflationRate.addTextChangedListener(afterTextChanged = {
            try {
                settings?.inflationRate = it.toString().toDouble()
            } catch (e: NumberFormatException) {
                settings?.inflationRate = 100000.0
            }
        })
        binding.monthsWithHigherSpending.addTextChangedListener(afterTextChanged = {
            try {
                settings?.monthsWithHigherSpending = it.toString()
            } catch (e: NumberFormatException) {
                settings?.monthsWithHigherSpending = ""
            }
        })
        binding.mostFrequentExpenseCategories.addTextChangedListener(afterTextChanged = {
            try {
                settings?.mostFrequentExpenseCategories = it.toString()
            } catch (e: NumberFormatException) {
                settings?.mostFrequentExpenseCategories = ""
            }
        })

        binding.settingsSaveButton.setOnClickListener {
            Timber.v(Gson().toJson(settings))

            appExecutors.diskIO().execute {
                settingsViewModel.save(settings!!)
            }
            appExecutors.mainThread().execute {
                findNavController().navigate(R.id.action_SettingsEntryFragment_to_TransactionsFragment)
            }
        }
        binding.startMlProcess.setOnClickListener {
            Timber.v(Gson().toJson(settings))

            appExecutors.diskIO().execute {
                settingsViewModel.save(settings!!)
                val mlWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<MLWorker>().build()
                WorkManager.getInstance(requireContext()).enqueue(mlWorkRequest)
            }
            appExecutors.mainThread().execute {
                findNavController().navigate(R.id.action_SettingsEntryFragment_to_TransactionsFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
