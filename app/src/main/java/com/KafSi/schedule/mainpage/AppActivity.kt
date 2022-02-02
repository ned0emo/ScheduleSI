package com.KafSi.schedule.mainpage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.viewpager.widget.ViewPager
import com.KafSi.schedule.FavoriteActivity
import com.KafSi.schedule.PublicData
import com.KafSi.schedule.R
import com.google.android.material.tabs.TabLayout
import java.io.FileNotFoundException
import java.io.Serializable
import java.net.URL
import java.nio.charset.Charset
import java.util.*

class AppActivity : AppCompatActivity() {

    private var favIndex = -1
    private val filesList = mutableListOf<List<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setVisible(false)
        setContentView(R.layout.main_tabs)

        /**Начало вкладок*/
        val sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        /**Конец вкладок*/

        /**Текущая неделя*/
        /***/
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"))

        var currentDay = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_YEAR)
        //val currentWeekFile = getFileStreamPath("currentWeek")
        currentDay -= ((currentDay / 14) * 14)


        if (currentDay in 3..9) {
            PublicData.currentWeek = 0
        } else {
            PublicData.currentWeek = 1
        }

        /**Открытие раписания при запуске приложения*/
        filesList.clear()
        var fileCount = fileList().size

        var j = -1
        try {
            for (i in fileList()) {
                /**список с этой херней, с текстом из файлов по группам/преподам*/
                if (i != "fav" && i.length > 2) {
                    j++
                    val favFile = getFileStreamPath("fav")

                    filesList.add(getFileStreamPath(i).readLines())

                    if (favFile != null && favFile.readText(Charset.defaultCharset()) == i) {
                        favIndex = j
                    }
                } else {
                    fileCount--
                }
            }
        } catch (e: FileNotFoundException) {
            favIndex = -1
        }


        if (favIndex >= 0) {
            val favList = filesList[favIndex]

            /**для очного*/
            if (favList[0].length > 3) {
                /**Строка, с которой начинается добавление*/
                var k = 4

                val listOfList = mutableListOf<MutableList<String>>()

                for (i in 0..27) {
                    val tmpList = mutableListOf<String>()

                    try {
                        for (j in k..k + 5) {
                            tmpList.add(favList[j])
                        }
                    } catch (e: Exception) {
                        break
                    }

                    k += 7
                    listOfList.add(tmpList)

                    if (k >= favList.size) break
                }

                val intent = Intent(this@AppActivity, FavoriteActivity::class.java)
                    .putExtra("type", favList[0])
                    .putExtra("link", favList[1])
                    .putExtra("name", favList[2])
                    .putExtra("favSchedule", listOfList as Serializable)

                //PublicData.favSchedule = listOfList
                //PublicData.catalog = favList[0]

                startActivity(this@AppActivity, intent, null)

            }
            /**для заочного*/
            else if(favList[0] == "zo1"|| favList[0] == "zo2"){
                /**Строка, с которой начинается добавление*/
                var k = 4

                val listOfList = mutableListOf<MutableList<String>>()

                for (i in 0..27) {
                    val tmpList = mutableListOf<String>()

                    try {
                        for (j in k..k + 7) {
                            tmpList.add(favList[j])
                        }
                    } catch (e: Exception) {
                        break
                    }

                    k += 9
                    listOfList.add(tmpList)

                    if (k >= favList.size) break
                }

                val intent = Intent(this@AppActivity, FavoriteActivity::class.java)
                    .putExtra("type", favList[0])
                    .putExtra("link", favList[1])
                    .putExtra("name", favList[2])
                    .putExtra("favSchedule", listOfList as Serializable)

                //PublicData.favSchedule = listOfList
                //PublicData.catalog = favList[0]

                startActivity(this@AppActivity, intent, null)
            }
            /**Для преподов*/
            else{

                var k = 5
                val listOfList = mutableListOf<MutableList<String>>()

                for (i in 0..11) {
                    val tmpList = mutableListOf<String>()

                    try {
                        for (j in k..k + 7) {
                            tmpList.add(favList[j])
                        }
                    } catch (e: Exception) {
                    }

                    k += 9
                    listOfList.add(tmpList)
                }

                val intent = Intent(this@AppActivity, FavoriteActivity::class.java)
                    .putExtra("name", favList[3])
                    .putExtra("link", favList[1])
                    .putExtra("link2", favList[2])
                    .putExtra("favSchedule", listOfList as Serializable)
                    .putExtra("position", favList[0])

                //PublicData.favSchedule = listOfList
                //PublicData.catalog = ""

                startActivity(this@AppActivity, intent, null)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        this.setVisible(true)
    }
}