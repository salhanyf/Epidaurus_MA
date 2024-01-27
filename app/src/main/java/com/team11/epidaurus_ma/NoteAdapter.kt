package com.team11.epidaurus_ma

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(private val notes: List<NoteEntry>): RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val authorTV = view.findViewById<TextView>(R.id.author)
        val timeDateTV = view.findViewById<TextView>(R.id.time_date)
        val observationTV = view.findViewById<TextView>(R.id.observation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.authorTV.text = note.author
        holder.timeDateTV.text = note.time_date
        holder.observationTV.text = note.observation
    }

    override fun getItemCount(): Int {
        return notes.size
    }
}