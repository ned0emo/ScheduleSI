package com.KafSi.schedule.teachers

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.KafSi.schedule.R

class FacultySelectActivity : AppCompatActivity() {

    private lateinit var siteText1: String
    private lateinit var siteText2: String
    private var facDepList: MutableList<Pair<List<String>, List<String>>> = mutableListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty)

        object : Thread() {
            override fun run() {
                val teachersScheduleData = TeacherScheduleData()
                siteText1 = teachersScheduleData.siteText1
                siteText2 = teachersScheduleData.siteText2

                this@FacultySelectActivity.runOnUiThread {
                    run {

                        findViewById<ProgressBar>(R.id.facActivityProgressBar).visibility = View.GONE

                        if (siteText1 == "Failed" || siteText2 == "Failed" ||
                            siteText1.indexOf("занятий") < 0 || siteText2.indexOf("занятий") < 0
                        ) {
                            Toast.makeText(this@FacultySelectActivity, "Ошибка при загрузке расписания", Toast.LENGTH_LONG)
                                .show()
                            return@runOnUiThread
                        }

                        facDepList = teachersScheduleData.getFacDepList()

                        viewManager = LinearLayoutManager(this@FacultySelectActivity)
                        //создается объект адаптера для ресайклера--------------------------------------------------
                        //он находится в файле FacDepAdapter
                        viewAdapter = FacultySelectButtonsViewAdapter(facDepList, this@FacultySelectActivity)

                        recyclerView = findViewById<RecyclerView>(R.id.facRecyclerView).apply {
                            setHasFixedSize(true)
                            layoutManager = viewManager
                            adapter = viewAdapter
                        }
                    }
                }
            }
        }.start()
    }
}