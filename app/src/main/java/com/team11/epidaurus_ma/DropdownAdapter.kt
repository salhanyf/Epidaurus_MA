package com.team11.epidaurus_ma

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class DropdownAdapter(context: Context,
                      resource: Int,
                      private val items: List<String>,
                      private val hint: String) : ArrayAdapter<String>(context, R.layout.dropdown_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.dropdown_menu, parent, false)
        val textView = view.findViewById<TextView>(R.id.repeatedItem)

        if (position == 0) {
            textView.text = hint
        } else {
            textView.text = items[position]
        }

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.dropdown_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.customSpinnerItem)
        textView.text = items[position]
        return view
    }
}