package com.revoluttest.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.revoluttest.R
import com.revoluttest.businesslogic.models.Currency
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel
    private lateinit var adapter: CurrenciesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupUI()
        initViewModel()
    }

    private fun setupUI() {
        toolbarView.setNavigationOnClickListener {
            finish()
        }

        adapter = CurrenciesAdapter(object : CurrenciesAdapter.Callback {
            override fun onFirstResponderChanged(currency: Currency) {
                viewModel.firstResponder = currency
                viewModel.getCurrencyRate()
            }

            override fun onValueChanged(value: String) {
                viewModel.onValueChanged(value)
            }

            override fun scrollToTop() {
                currenciesRecyclerView.scrollToPosition(0)
            }
        })
        currenciesRecyclerView.adapter = this.adapter
        currenciesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.viewState.observe(this, { viewState ->
            when (viewState) {
                is MainViewModel.ViewState.Result -> dataState(viewState.currencies)
                MainViewModel.ViewState.Error -> errorState()
            }
        })
    }

    private fun dataState(currencies: MutableList<Currency>) {
        errorMessageView.visibility = View.GONE
        currenciesRecyclerView.visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.Main) {
            adapter.updatedItems(currencies)
        }
    }

    private fun errorState() {
        currenciesRecyclerView.visibility = View.GONE
        errorMessageView.visibility = View.VISIBLE
    }

}