package com.team11.epidaurus_ma

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class AdapterIssues(context: Context,
                    resource: Int,
                    private val issues: List<String>,
                    private val hint: String
                    ) : ArrayAdapter<String>(context, resource, issues) {

    private var currentSelection: String? = hint

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.dropdown_menu, parent, false)
        val textView = view.findViewById<TextView>(R.id.repeatedItem)
        textView.text = currentSelection ?: hint
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.dropdown_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.customSpinnerItem)
        textView.text = issues[position]
        return view
    }

    fun setCurrentSelection(selection: String) {
        currentSelection = selection
        notifyDataSetChanged()
    }
}