package org.droidwiki.passwordless.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import org.droidwiki.passwordless.R
import org.droidwiki.passwordless.model.Account

class AccountArrayAdapter(context: Context, resource: Int, private val accounts: List<Account> = mutableListOf()) :
    ArrayAdapter<Account>(context, resource, 0, accounts) {

    private var onDeleteListener: OnDeleteListener? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView

        if (v == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = inflater.inflate(R.layout.list_item, null)
        }

        val account = accounts[position]
        v!!.findViewById<TextView>(R.id.site_name).text = account.name
        v.findViewById<TextView>(R.id.api_url).text = account.apiUrl
        v.findViewById<ImageButton>(R.id.secondary_action).setOnClickListener {
            onDeleteListener?.onDelete(account)
        }

        return v
    }

    fun setOnDeleteListener(listener: OnDeleteListener?) {
        this.onDeleteListener = listener
    }

    interface OnDeleteListener {
        fun onDelete(account: Account)
    }
}