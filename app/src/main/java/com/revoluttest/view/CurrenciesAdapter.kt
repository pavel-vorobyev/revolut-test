package com.revoluttest.view

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding.widget.RxTextView
import com.revoluttest.R
import com.revoluttest.businesslogic.models.Currency
import kotlinx.android.synthetic.main.view_item_currency.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class CurrenciesAdapter(
    private val callback: Callback) : RecyclerView.Adapter<CurrenciesAdapter.ViewHolder>() {

    private var currencies: MutableList<Currency> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.view_item_currency, parent, false)
        )

        holder.itemView.setOnClickListener {
            val target = currencies[holder.adapterPosition]
            callback.onFirstResponderChanged(target)
        }

        holder.itemView.valueInputView.setOnClickListener {
            val target = currencies[holder.adapterPosition]
            callback.onFirstResponderChanged(target)
        }

        RxTextView.textChanges(holder.itemView.valueInputView)
            .filter {
                holder.itemView.valueInputView.hasFocus() && holder.adapterPosition == 0
            }
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                callback.onValueChanged(it.toString())
            }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currency = currencies[position]
        holder.itemView.flagView.text = currency.flag
        holder.itemView.currencyNameView.text = currency.name
        holder.itemView.currencyIso3View.text = currency.iso3
        holder.itemView.valueInputView.text =
            Editable.Factory.getInstance().newEditable(currency.value.toString())
    }

    override fun getItemCount(): Int {
        return currencies.size
    }

    suspend fun updatedItems(newCurrencies: MutableList<Currency>) {
        withContext(Dispatchers.Default) {
            val diffUtilCallback = CurrenciesDiffCallback(currencies, newCurrencies)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)

            val newFirstResponder = when (currencies.isNotEmpty()) {
                true -> currencies[0].iso3 != newCurrencies[0].iso3
                else -> false
            }

            currencies = newCurrencies

            withContext(Dispatchers.Main) {
                diffResult.dispatchUpdatesTo(this@CurrenciesAdapter)
                if (newFirstResponder)
                    callback.scrollToTop()
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class CurrenciesDiffCallback(
        private val oldV: List<Currency>,
        private val newV: List<Currency>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldV.size
        }

        override fun getNewListSize(): Int {
            return newV.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldV[oldItemPosition].iso3 == newV[newItemPosition].iso3
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldV[oldItemPosition].value == newV[newItemPosition].value
        }

    }

    interface Callback {
        fun onFirstResponderChanged(currency: Currency)
        fun onValueChanged(value: String)
        fun scrollToTop()
    }

}