package com.team11.epidaurus_ma

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ToolbarAdapter(private val context: Context, private val menuItems: List<MenuItem>) : BaseAdapter() {
    override fun getCount(): Int = menuItems.size
    override fun getItem(position: Int): MenuItem = menuItems[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.dropdown_toolbar, parent, false)
        val menuItem = getItem(position)
        view.findViewById<TextView>(R.id.menu_item_text).text = menuItem.title
        return view
    }
}

data class MenuItem(val icon: Int, val title: String)