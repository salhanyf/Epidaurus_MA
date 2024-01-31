package com.team11.epidaurus_ma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext

class NotesActivity : AppCompatActivity(), CoroutineScope {
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        var fabAddNote = findViewById<FloatingActionButton>(R.id.fabAddNote)
        var patientId:Int = 0
        var data:List<NoteEntry>? = null
        var nurseName:String = ""
        val supabase = createSupabaseClient(
            "https://faafgdjvgcpjbchmbmhg.supabase.co",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU",
        ){
            install(Postgrest)
            install(Auth)
        }

        if (intent.hasExtra("patientId")){
            patientId = intent.getIntExtra("patientId",0)
            Log.e("PatientID", patientId.toString())
        }


        launch{
            data = getRows(supabase, patientId)
            val notesList = mutableListOf<NoteEntry>()
            data?.forEach { notesList.add(it) }
            val recyclerView = findViewById<RecyclerView>(R.id.notes_RecyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this@NotesActivity)
            recyclerView.adapter = NoteAdapter(notesList)

            val metadata = supabase.auth.retrieveUserForCurrentSession().userMetadata
            nurseName = metadata?.get("name").toString()
            nurseName = nurseName.substring(1,nurseName.length-1) //Removes the quotation marks

            fabAddNote.setOnClickListener{
                addNote(nurseName, patientId, notesList, recyclerView, supabase)
            }
        }
    }

    private fun addNote(
        nurseName: String,
        patientId: Int,
        notesList: MutableList<NoteEntry>,
        recyclerView: RecyclerView,
        supabase: SupabaseClient
    ) {
        val builder = AlertDialog.Builder(this@NotesActivity)
        builder.setTitle("Add a New Note")

        val input = EditText(this@NotesActivity)
        builder.setView(input)

        builder.setPositiveButton("Confirm") { dialog, _ ->
            val observationText = input.text.toString()
            if (observationText.isNotBlank()) {
                val timeDate = getCurrentDateTimeAsString()
                val noteEntry = NoteEntry(
                    author = nurseName,
                    patient_id = patientId,
                    time_date = timeDate,
                    observation = observationText
                )
                notesList.add(noteEntry)
                recyclerView.adapter?.notifyItemInserted(notesList.size - 1)
                recyclerView.scrollToPosition(notesList.size - 1)
                launch{
                    addRows(supabase,noteEntry)
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private suspend fun getRows(supabase:SupabaseClient, patientId:Int): List<NoteEntry>?{
        val response = supabase.from("Notes").select(){
            filter {
                eq("patient_id", patientId)
            }
        }.decodeList<NoteEntry>()
        return response
    }
    private suspend fun addRows(supabase:SupabaseClient, entry: NoteEntry){
        supabase.from("Notes").insert(entry)
    }

    fun getCurrentDateTimeAsString(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDateTime.format(formatter)
    }
}