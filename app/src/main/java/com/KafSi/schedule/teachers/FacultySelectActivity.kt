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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty)

        object : Thread() {
            override fun run() {
                val teachersScheduleData = TeacherScheduleData()
                val siteText1 = teachersScheduleData.siteText1
                val siteText2 = teachersScheduleData.siteText2

                this@FacultySelectActivity.runOnUiThread {
                    run {

                        findViewById<ProgressBar>(R.id.facActivityProgressBar).visibility =
                            View.GONE

                        if (siteText1 == "Failed" || siteText2 == "Failed" ||
                            siteText1.indexOf("занятий") < 0 || siteText2.indexOf("занятий") < 0
                        ) {
                            Toast.makeText(
                                this@FacultySelectActivity,
                                "Ошибка при загрузке расписания",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            return@runOnUiThread
                        }

                        findViewById<RecyclerView>(R.id.facRecyclerView).apply {
                            setHasFixedSize(true)
                            layoutManager = LinearLayoutManager(this@FacultySelectActivity)
                            adapter = FacultySelectButtonsViewAdapter(
                                teachersScheduleData.getFacDepList(), this@FacultySelectActivity
                            )
                        }
                    }
                }
            }
        }.start()
    }
}