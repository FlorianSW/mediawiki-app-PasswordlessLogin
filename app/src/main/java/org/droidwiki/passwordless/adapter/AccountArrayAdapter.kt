package org.droidwiki.passwordless.adapter

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import org.droidwiki.passwordless.R
import org.droidwiki.passwordless.model.Account

class AccountArrayAdapter(
    private val onDeleteListener: OnDeleteListener,
    private val accounts: MutableList<Account> = mutableListOf()
) :
    RecyclerView.Adapter<AccountArrayAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val listItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item, parent, false) as CardView

        return ViewHolder(listItem)
    }

    override fun getItemCount(): Int = accounts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = accounts[position]
        holder.listItem.findViewById<TextView>(R.id.site_name).text = account.name
        holder.listItem.findViewById<TextView>(R.id.api_url).text = account.apiUrl
        holder.listItem.findViewById<ImageButton>(R.id.secondary_action).setOnClickListener {
            onDeleteListener.onDelete(account)
        }
    }

    fun clear() {
        val numberOfAccounts = accounts.size
        accounts.clear()
        notifyItemRangeRemoved(0, numberOfAccounts)
    }

    fun add(account: Account) {
        accounts.add(account)
        notifyItemInserted(accounts.indexOf(account))
    }

    fun isEmpty() = accounts.isEmpty()

    class ViewHolder(val listItem: CardView) : RecyclerView.ViewHolder(listItem)

    interface OnDeleteListener {
        fun onDelete(account: Account)
    }
}