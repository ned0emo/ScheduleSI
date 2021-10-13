package com.KafSi.schedule

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.KafSi.schedule.fragments.MyTabFragment
import com.KafSi.schedule.students.StudentsScheduleData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FavoriteActivity : AppCompatActivity() {

    var localData: MutableList<MutableList<String>> = mutableListOf()

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
        localData = PublicData.favSchedule//intent.getSerializableExtra("favSchedule") as MutableList<MutableList<String>>
        viewPager = findViewById(R.id.pagerlol)
        tabLayout = findViewById(R.id.tabs_main)
        title = intent.getStringExtra("name")

        favoriteFloatButton.setImageResource(R.drawable.star_on)

        itemCount = when (localData.size) {
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
        }
        catch(e: Exception){
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

            if(isWeekPosFalse) {
                if (PublicData.currentWeek % 2 != position) currentTab = tab
            }
            else{
                if (PublicData.currentWeek % 2 == position) currentTab = tab
            }
        }.attach()

        try {
            currentTab.select()
        } catch (e: IllegalArgumentException) {
        }

        updateSchedule()

        /**Кнопка избранное*/
        favoriteFloatButton.setOnClickListener {
            //val groupName = intent.getStringExtra("name")
            val linkString1 = intent.getStringExtra("link")
            val linkString2 = intent.getStringExtra("link2")
            val position = intent.getStringExtra("position")

            AddToFavButtonHandler.addToFavButtonClick(favoriteFloatButton, this,
                localData as MutableList<List<String>>,
                position?.toInt() ?: 0,
                intent.getStringExtra("name")!!,
                linkString1!!, linkString2 ?: "_"
            )
        }
    }

    /**обновление расписания с сайта*/
    private fun updateSchedule() {
        val link1 = intent.getStringExtra("link") ?: ""
        val link2 = intent.getStringExtra("link2") ?: ""
        var isError = false

        object : Thread() {
            override fun run() {
                var newScheduleList = mutableListOf<MutableList<String>>()

                val siteTextLoad = SitePageLoadClass()
                val siteText = siteTextLoad.getSiteText(link1)
                val siteText2 = siteTextLoad.getSiteText(link2)

                if (siteText == "Failed" || siteText.indexOf("Пары") < 0) {
                    return
                }

                /**Избавляемся от точек в названии группы*/
                val nameWithoutDot = try {
                    intent.getStringExtra("name").toString()
                        .substring(0, intent.getStringExtra("name").toString().indexOf('.'))
                } catch (e: Exception) {
                    intent.getStringExtra("name").toString()
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
                                    .setPositiveButton("OK") { dialog, id ->
                                        val file = getFileStreamPath(title as String?)
                                        file.delete()
                                        this@FavoriteActivity.finish()
                                    }.create()
                            attentionDialog.show()
                            isError = true
                        }
                    }
                }


                if (link2 == "") {/**загрузка расписания студента---------------------------*/
                    newScheduleList = StudentsScheduleData(siteText).getCurrentStudentSchedule()

                } else {/**загрузка расписания преподов---------------------------------*/
                    //val depSchedData = DepScheduleData()
                    createScheduleList(siteText.split("занятий"))
                    createScheduleList(siteText2.split("занятий"))

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
                    for (i in intent.getStringExtra("position")!!
                        .toInt() * 12..intent.getStringExtra(
                        "position"
                    )!!.toInt() * 12 + 11) {
                        newScheduleList.add(listOfSchedule[i])
                    }
                }

                /**Вывод снэкбара при различных списках*/
                if (newScheduleList.toString() != PublicData.favSchedule.toString() && !isError) {
                    this@FavoriteActivity.runOnUiThread {
                        run {
                            Snackbar.make(
                                this@FavoriteActivity.findViewById<ConstraintLayout>(R.id.rootLayout),
                                "Доступно новое расписание",
                                Snackbar.LENGTH_LONG
                            ).setAction("Обновить") {
                                localData = newScheduleList

                                val groupName = intent.getStringExtra("name")
                                getFileStreamPath(groupName).delete()
                                val fileOutput = openFileOutput(groupName, Context.MODE_APPEND)

                                fileOutput.write((link1 + '\n').toByteArray())
                                if (link2 != "") {
                                    fileOutput.write((link2 + '\n').toByteArray())
                                    fileOutput.write((intent.getStringExtra("position") + '\n').toByteArray())
                                }
                                fileOutput.write((groupName + '\n').toByteArray())

                                var count = 1
                                for (i in localData) {
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