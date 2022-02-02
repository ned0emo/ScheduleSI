package com.KafSi.schedule.teachers

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.KafSi.schedule.PublicData
import com.KafSi.schedule.R
import java.net.URL
import java.nio.charset.Charset

class FacultySelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty)

        object : Thread() {
            override fun run() {
                val facDepList = mutableListOf<Pair<List<String>, List<String>>>()

                val siteText1: String = try {
                    URL("https://portal.esstu.ru/bakalavriat/craspisanEdt.htm").readText(
                        Charset.forName(
                            "Windows-1251"
                        )
                    )
                } catch (e: Exception) {
                    "Failed"
                }

                val siteText2: String = try {
                    URL("https://portal.esstu.ru/spezialitet/craspisanEdt.htm").readText(
                        Charset.forName(
                            "Windows-1251"
                        )
                    )
                } catch (e: Exception) {
                    "Failed"
                }

                if (siteText1 != "Failed" && siteText2 != "Failed") {
                    PublicData.siteText = siteText1

                    val list1 = siteText1.split("ID", ignoreCase = true) as MutableList<String>
                    val list2 = siteText2.split("ID", ignoreCase = true) as MutableList<String>

                    //разбиваем списки на факультеты и кафедры (с ссылками) в разные строки-----------------------------------
                    //и херачим их попарно в еще один список-----------------------------------------------------
                    for (i in 0..8) {
                        val tmpList1 = list1[i].split("href")
                        val tmpList2 = list2[i].split("href")

                        facDepList.add(Pair(tmpList1.drop(0), tmpList2.drop(0)))
                    }
                } else {
                    this@FacultySelectActivity.runOnUiThread {
                        run{
                            findViewById<ProgressBar>(R.id.facActivityProgressBar).visibility =
                                View.GONE

                            Toast.makeText(
                                this@FacultySelectActivity,
                                "Ошибка при загрузке расписания",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    return
                }

                this@FacultySelectActivity.runOnUiThread {
                    run {

                        findViewById<ProgressBar>(R.id.facActivityProgressBar).visibility =
                            View.GONE

                        findViewById<RecyclerView>(R.id.facRecyclerView).apply {
                            setHasFixedSize(true)
                            layoutManager = LinearLayoutManager(this@FacultySelectActivity)
                            adapter = FacultySelectButtonsViewAdapter(
                                facDepList, this@FacultySelectActivity
                            )
                        }
                    }
                }
            }
        }.start()
    }
}