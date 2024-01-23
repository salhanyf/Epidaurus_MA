package com.team11.epidaurus_ma

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class PatientAdapter (val itemList: List<PatientListItem>):RecyclerView.Adapter<PatientAdapter.ViewHolder>() {

    inner class ViewHolder (itemView:View): RecyclerView.ViewHolder(itemView){
        val lstelem_nameTextView = itemView.findViewById<TextView>(R.id.lstelem_nameTextView)
        val lstelem_dobTextView = itemView.findViewById<TextView>(R.id.lstelem_dobTextView)
        val lstelem_roomTextView = itemView.findViewById<TextView>(R.id.lstelem_roomTextView)

        init {
            itemView.setOnClickListener{
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    //TODO: Add logic for accessing patient details: Supabase accesses, etc.
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.patient_list_item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.lstelem_nameTextView.text = currentItem.name
        holder.lstelem_dobTextView.text = currentItem.dateOfBirth
        holder.lstelem_roomTextView.text = currentItem.roomNumber
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}