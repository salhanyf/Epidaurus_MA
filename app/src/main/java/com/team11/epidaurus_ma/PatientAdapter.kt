package com.team11.epidaurus_ma

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
class PatientAdapter (val itemList: List<PatientDBResponse>):RecyclerView.Adapter<PatientAdapter.ViewHolder>() {

    inner class ViewHolder (itemView:View): RecyclerView.ViewHolder(itemView){
        val lstelem_nameTextView = itemView.findViewById<TextView>(R.id.lstelem_nameTextView)
        val lstelem_dobTextView = itemView.findViewById<TextView>(R.id.lstelem_dobTextView)
        val lstelem_roomTextView = itemView.findViewById<TextView>(R.id.lstelem_roomTextView)

        init {
            itemView.setOnClickListener{
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    val context = itemView.context
                    //We can access the clicked item using itemList[position]
                    val patientDetailsIntent = Intent(context, PatientDetailsActivity::class.java)
                    patientDetailsIntent.putExtra("id", itemList[position].id)
                    patientDetailsIntent.putExtra("name", itemList[position].name)
                    patientDetailsIntent.putExtra("dob", itemList[position].date_of_birth)
                    patientDetailsIntent.putExtra("department", itemList[position].department)
                    patientDetailsIntent.putExtra("roomNumber", itemList[position].room_number)
                    patientDetailsIntent.putExtra("symptoms", itemList[position].symptoms)
                    patientDetailsIntent.putExtra("medicalHistory", itemList[position].medical_history)
                    patientDetailsIntent.putExtra("medicalDiagnosis", itemList[position].medical_diagnosis)
                    context.startActivity(patientDetailsIntent)
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
        holder.lstelem_dobTextView.text = currentItem.date_of_birth
        holder.lstelem_roomTextView.text = currentItem.room_number
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}