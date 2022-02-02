package com.KafSi.schedule

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.KafSi.schedule.fragments.MyTabFragment
import com.KafSi.schedule.students.StudentsSchedule
import com.KafSi.schedule.teachers.DepScheduleData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

@Suppress("UNCHECKED_CAST")
class FavoriteActivity : AppCompatActivity() {

    var offlineStudentScheduleList: MutableList<MutableList<String>> = mutableListOf()

    private lateinit var viewPager: ViewPager2
    private lateinit var favoriteFloatButton: FloatingActionButton
    private lateinit var tabLayout: TabLayout
    private lateinit var demoCollectionAdapter: FavViewPagerAdapter
    private var itemCount = 0
    private val listOfTeachers = mutableListOf<String>()
    private val listOfSchedule = mutableListOf<MutableList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_schedule)
        findViewById<Spinner>(R.id.groupSpinner).visibility = View.GONE

        favoriteFloatButton = findViewById(R.id.favoriteFloatButton)
        offlineStudentScheduleList = intent.getSerializableExtra("favSchedule")
                as MutableList<MutableList<String>>
        viewPager = findViewById(R.id.pagerlol)
        tabLayout = findViewById(R.id.tabs_main)
        title = intent.getStringExtra("name")

        favoriteFloatButton.setImageResource(R.drawable.star_on)

        itemCount = when (offlineStudentScheduleList.size) {
            7 -> 1
            21 -> 3
            28 -> 4
            else -> 2
        }

        /**Вкладки*/
        var currentTab = TabLayout.Tab()

        val isWeekPosFalse = try {
            PreferenceManager.getDefaultSharedPreferences(this)
                .all["weekPosSetting"] as Boolean
        } catch (e: Exception) {
            false
        }

        viewPager.isUserInputEnabled = try {
            PreferenceManager.getDefaultSharedPreferences(this).all["swipeSetting"] as Boolean
        } catch (e: Exception) {
            true
        }

        /**Вкладки*/
        demoCollectionAdapter = FavViewPagerAdapter(this, itemCount)
        viewPager.adapter = demoCollectionAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = "Неделя ${(position + 1)}"

            if (isWeekPosFalse) {
                if (PublicData.currentWeek % 2 != position) currentTab = tab
            } else {
                if (PublicData.currentWeek % 2 == position) currentTab = tab
            }
        }.attach()

        try {
            currentTab.select()
        } catch (e: IllegalArgumentException) {
        }

        if (intent.getStringExtra("link") == null) {
            classesSchedule()
            return
        }

        updateSchedule()

        /**Кнопка избранное*/
        favoriteFloatButton.setOnClickListener {

            val position = try {
                (intent.getStringExtra("position") ?: "-1").toInt()
            } catch (e: Exception) {
                -1
            }

            val addToFavResult = AddToFavButtonHandler.addToFavButtonClick(
                this,
                offlineStudentScheduleList as MutableList<List<String>>,
                intent.getStringExtra("name")!!,
                intent.getStringExtra("type") ?: "",
                intent.getStringExtra("link")!!,
                intent.getStringExtra("link2") ?: "",
                position
            )

            when (addToFavResult) {
                0 -> {
                    Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show()
                    favoriteFloatButton.setImageResource(R.drawable.star_on)
                }
                1 -> {
                    Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show()
                    favoriteFloatButton.setImageResource(R.drawable.star_off)
                }
                else -> {
                    Toast.makeText(
                        this,
                        "Ошибка добавления в избранное. Код: AddToFavButtonHandler_$addToFavResult",
                        Toast.LENGTH_SHORT
                    ).show()
                    favoriteFloatButton.setImageResource(R.drawable.star_off)
                }
            }
        }
    }

    /**обновление расписания с сайта*/
    private fun updateSchedule() {
        val link1 = intent.getStringExtra("link") ?: ""
        val link2 = intent.getStringExtra("link2") ?: ""
        var isError = false

        object : Thread() {
            override fun run() {

                var studentSchedule: StudentsSchedule? = null
                val name = intent.getStringExtra("name").toString()
                var newScheduleList = mutableListOf<MutableList<String>>()

                when (intent.getStringExtra("type")) {
                    "bakalavriat" -> studentSchedule = StudentsSchedule(0)
                    "spezialitet" -> studentSchedule = StudentsSchedule(1)
                    "zo1" -> studentSchedule = StudentsSchedule(2)
                    "zo2" -> studentSchedule = StudentsSchedule(3)
                    //else ->
                }

                /**фоновая загрузка расписания студентов*/
                if (studentSchedule != null) {
                    val siteText = studentSchedule.siteText
                    newScheduleList = studentSchedule.getStudentSchedule(link1)

                    //val siteTextLoad = SitePageLoadClass()
                    //siteText = siteTextLoad.getSiteText(link1)
                    val siteText2 = ""//siteTextLoad.getSiteText(link2)

                    if (siteText == "Failed" || siteText.indexOf("1.htm") < 0) {
                        return
                    }

                    /**Избавляемся от точек в названии группы*/
                    val nameWithoutDot = try {
                        name.substring(0, name.indexOf('.'))
                    } catch (e: Exception) {
                        name
                    }

                    /**АлертДиалог о добавлении расписания в избранное заново*/
                    if (siteText.indexOf(nameWithoutDot) < 0 && siteText2.indexOf(nameWithoutDot) < 0) {

                        this@FavoriteActivity.runOnUiThread {
                            run {
                                val attentionDialog: AlertDialog =
                                    AlertDialog.Builder(this@FavoriteActivity)
                                        .setCancelable(false)
                                        .setTitle("Внимание")
                                        .setMessage("Ссылка на расписание изменилась, добавьте свое расписание заново")
                                        .setPositiveButton("OK") { _, _ ->
                                            val file = getFileStreamPath(title as String?)
                                            file.delete()
                                            this@FavoriteActivity.finish()
                                        }.create()
                                attentionDialog.show()
                                isError = true
                            }
                        }

                        this.interrupt()
                    }
                }
                /**загрузка расписания преподов---------------------------------*/
                else {

                    val depScheduleData = DepScheduleData(link1, link2)

                    if (depScheduleData.cafSiteText1 == "Failed" || depScheduleData.cafSiteText2 == "Failed"
                        || depScheduleData.cafSiteText1.indexOf("занятий") < 1
                        || depScheduleData.cafSiteText2.indexOf("занятий") < 1) {
                        return
                    }

                    createScheduleList(depScheduleData.cafSiteText1.split("занятий"))
                    createScheduleList(depScheduleData.cafSiteText2.split("занятий"))

                    /**объединение колледжа и бакалавров*/
                    var i = 0
                    while (i < listOfTeachers.size) {
                        for (j in i + 1 until listOfTeachers.size) {
                            if (listOfTeachers[i] == listOfTeachers[j]) {
                                for (k in 0..11) {
                                    for (l in 0..5) {
                                        if (listOfSchedule[i * 12 + k][l].length < listOfSchedule[j * 12 + k][l].length) {
                                            listOfSchedule[i * 12 + k][l] =
                                                listOfSchedule[j * 12 + k][l]
                                        }
                                    }
                                }

                                listOfTeachers.removeAt(j)
                                for (k in 0..11) {
                                    listOfSchedule.removeAt(j * 12)
                                }

                                break
                            }
                        }

                        i++
                    }

                    /**получение расписания текущего препода*/
                    try {
                        for (i in intent.getStringExtra("position")!!
                            .toInt() * 12..intent.getStringExtra(
                            "position"
                        )!!.toInt() * 12 + 11) {
                            newScheduleList.add(listOfSchedule[i])
                        }
                    } catch (e: Exception) {
                        if (getFileStreamPath("fav").canRead()) {
                            val corruptFile =
                                getFileStreamPath(getFileStreamPath("fav").readLines()[0])

                            if (corruptFile.canRead()) {
                                corruptFile.delete()
                            }

                            getFileStreamPath("fav").delete()
                        } else {
                            for (i in fileList()) {
                                /**чистка всего, кроме настройки классов*/
                                if (i != "classesNotify") {
                                    getFileStreamPath(i).delete()
                                }
                            }
                        }

                        this@FavoriteActivity.finish()
                    }
                }

                /**Вывод снэкбара при различных списках*/
                if (newScheduleList != offlineStudentScheduleList && !isError) {
                    this@FavoriteActivity.runOnUiThread {
                        run {
                            Snackbar.make(
                                this@FavoriteActivity.findViewById<ConstraintLayout>(R.id.rootLayout),
                                "Доступно новое расписание",
                                Snackbar.LENGTH_LONG
                            ).setAction("Обновить") {
                                offlineStudentScheduleList = newScheduleList

                                //val groupName = intent.getStringExtra("name")
                                getFileStreamPath(name).delete()
                                val fileOutput = openFileOutput(name, Context.MODE_APPEND)

                                if (link2.length > 3) {
                                    fileOutput.write((intent.getStringExtra("position") + '\n').toByteArray())
                                    fileOutput.write((link1 + '\n').toByteArray())
                                    fileOutput.write((link2 + '\n').toByteArray())
                                } else {
                                    fileOutput.write((intent.getStringExtra("type")!! + '\n').toByteArray())
                                    fileOutput.write((link1 + '\n').toByteArray())
                                }
                                fileOutput.write((name + '\n').toByteArray())

                                var count = 1
                                for (i in offlineStudentScheduleList) {
                                    fileOutput.write("$count;;\n".toByteArray())
                                    count++

                                    for (j in i) {
                                        fileOutput.write((j + '\n').toByteArray())
                                    }
                                }

                                demoCollectionAdapter = FavViewPagerAdapter(
                                    this@FavoriteActivity,
                                    itemCount
                                )

                                viewPager.adapter = demoCollectionAdapter

                                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                                    tab.text = "Неделя ${(position + 1)}"
                                }.attach()

                            }.show()
                        }
                    }
                }
            }
        }.start()
    }

    /**лист расписания для преподов (всех)*/
    private fun createScheduleList(teacherList: List<String>) {
        var flag = true
        for (i in teacherList) {
            //пропускаем первый спличеный фрагмент
            if (flag) {
                flag = false
                continue
            }

            listOfTeachers.add(i.substring(i.indexOf("0f") + 6, i.indexOf("</P")))

            //список с фрагменами сайта по преподам-------------------------------------------------
            val cafSplitList = i.split("TER")
            var counter = 20

            //дополняем список с расписанием по дням недели
            for (j in 0..11) {
                val tmpList = mutableListOf<String>()

                for (k in counter..counter + 7) {
                    tmpList.add(cafSplitList[k].substring(2, cafSplitList[k].indexOf('<')))
                }

                counter += 9
                listOfSchedule.add(tmpList.drop(0) as MutableList<String>)
            }
        }
    }

    private fun classesSchedule() {
        /**Кнопка избранное*/
        favoriteFloatButton.setOnClickListener {

            val addToFavResult = AddToFavButtonHandler.addClassToFavButtonClick(
                this,
                offlineStudentScheduleList as MutableList<List<String>>,
                intent.getStringExtra("name")!!
            )

            when (addToFavResult) {
                0 -> {
                    Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show()
                    favoriteFloatButton.setImageResource(R.drawable.star_on)
                }
                1 -> {
                    Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show()
                    favoriteFloatButton.setImageResource(R.drawable.star_off)
                }
                else -> {
                    Toast.makeText(
                        this,
                        "Ошибка добавления в избранное. Код: AddToFavButtonHandler_$addToFavResult",
                        Toast.LENGTH_SHORT
                    ).show()
                    favoriteFloatButton.setImageResource(R.drawable.star_off)
                }
            }
        }
    }
}

class FavViewPagerAdapter(
    activity: AppCompatActivity,
    private val itemCount: Int
) : FragmentStateAdapter(activity) {

    override fun getItemCount() = itemCount

    override fun createFragment(position: Int): Fragment {

        val fragment = MyTabFragment()
        val bundle = Bundle()
        bundle.putInt("pos", position)
        fragment.arguments = bundle

        return fragment
    }
}