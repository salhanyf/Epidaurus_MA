package com.team11.epidaurus_ma

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class AvatarAdapter(private val context: Context, private val avatarList: List<Int>) : BaseAdapter() {
    override fun getCount(): Int = avatarList.size
    override fun getItem(position: Int): Any = avatarList[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView: ImageView
        if (convertView == null) {
            imageView = ImageView(context)
            imageView.layoutParams = ViewGroup.LayoutParams(100, 100) // Size of each grid item
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            imageView = convertView as ImageView
        }

        imageView.setImageResource(avatarList[position])
        return imageView
    }
}