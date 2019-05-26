package org.droidwiki.passwordless

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import org.droidwiki.passwordless.adapter.AccountArrayAdapter
import org.droidwiki.passwordless.model.Account


class AccountListFragment : Fragment() {
    private var listener: AccountListListener? = null
    private var noAccountsText: TextView? = null
    private lateinit var accountListContent: AccountArrayAdapter

    fun reloadList() {
        accountListContent.clear()
        noAccountsText?.visibility = View.GONE

        listener?.accountsProvider()?.list()?.forEach {
            accountListContent.add(it)
        }

        if (accountListContent.isEmpty) {
            noAccountsText?.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_list, container, false)
        noAccountsText = view.findViewById(R.id.no_accounts_text)
        val addAccountButton = view.findViewById<FloatingActionButton>(R.id.action_add)
        addAccountButton.setOnClickListener {
            listener?.openAddAccountDialog()
        }

        val accountList = view.findViewById<ListView>(R.id.account_list)
        accountListContent = AccountArrayAdapter(context!!, R.layout.list_item)
        accountListContent.setOnDeleteListener(object : AccountArrayAdapter.OnDeleteListener {
            override fun onDelete(account: Account) {
                listener?.accountsProvider()?.remove(account.id)
                reloadList()
            }

        })
        accountList?.adapter = accountListContent

        reloadList()

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AccountListListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    interface AccountListListener {
        fun openAddAccountDialog()
        fun accountsProvider(): AccountsProvider
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AccountListFragment().apply {
                arguments = Bundle()
            }
    }
}
