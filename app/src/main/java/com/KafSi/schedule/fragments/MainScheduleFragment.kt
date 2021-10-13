package com.KafSi.schedule.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.KafSi.schedule.*
import com.KafSi.schedule.students.StudentsScheduleData
import com.KafSi.schedule.teachers.DepScheduleData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*

class MainScheduleFragment : Fragment(), AdapterView.OnItemSelectedListener {

    var localData: MutableList<MutableList<String>> = mutableListOf()

    private lateinit var studSchedule: StudentsScheduleData
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var tabsCollectionAdapter: ScheduleTabsViewPagerAdapter
    private lateinit var reqActivity: HostViewActivity
    private lateinit var listOfSchedule: MutableList<MutableList<String>>
    private var linkString = ""

    private lateinit var hmmImage: ImageView
    private lateinit var hmmText: TextView
    private lateinit var favoriteFloatButton: FloatingActionButton
    private lateinit var groupSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        reqActivity = requireActivity() as HostViewActivity

        hmmImage = view.findViewById(R.id.hmmImage)
        hmmText = view.findViewById(R.id.hmmText)
        favoriteFloatButton = view.findViewById(R.id.favoriteFloatButton)
        groupSpinner = view.findViewById(R.id.groupSpinner)

        /**Настраиваем видимость элементов*/
        hmmImage.visibility = View.INVISIBLE
        hmmText.visibility = View.INVISIBLE
        favoriteFloatButton.visibility = View.VISIBLE
        groupSpinner.visibility = View.VISIBLE

        viewPager = view.findViewById(R.id.pagerlol)
        tabLayout = view.findViewById(R.id.tabs_main)

        /**Если не удалось загрузить страницу со списком групп.
         *
         * PublicData.siteText содержит или страницу со списком кафедр
         * или страницу расписания со всеми группами будь то очники или заочники*/
        if (PublicData.siteText.indexOf("1 курс", 0, true) < 0
            && PublicData.siteText.indexOf("Факультет", 0, true) < 0
        ) {
            Toast.makeText(
                reqActivity,
                "Ошибка при загрузке расписания",
                Toast.LENGTH_SHORT
            )
                .show()

            groupSpinner.visibility = View.GONE
            favoriteFloatButton.visibility = View.GONE
            //scheduleProgressBar.visibility = View.GONE
            return
        }

        if (reqActivity.courseNum > -1) {
            studLoad()
        } else {
            teachersLoad()
        }
    }

    private fun studLoad() {

        object : Thread() {
            override fun run() {
                /**Получаем расписание всех групп выбранного курса*/
                studSchedule = StudentsScheduleData()

                reqActivity.runOnUiThread {
                    run {
                        /**заполняем спиннер группами*/
                        val adapter = ArrayAdapter(
                            reqActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            studSchedule.groupNameList
                        )
                        groupSpinner.adapter = adapter
                        groupSpinner.onItemSelectedListener = this@MainScheduleFragment

                        /**Если список групп пустой выдаем ХМММ*/
                        if (groupSpinner.count == 0) {
                            hmmImage.visibility = View.VISIBLE
                            hmmText.visibility = View.VISIBLE
                            favoriteFloatButton.visibility = View.GONE
                            groupSpinner.visibility = View.GONE
                        }
                    }
                }
            }
        }.start()
    }

    private fun teachersLoad() {
        object : Thread() {
            override fun run() {

                val depScheduleData = DepScheduleData(
                    reqActivity.cafLinkPairArray[reqActivity.depNum].first,
                    reqActivity.cafLinkPairArray[reqActivity.depNum + 1].first
                )

                val siteText1 = depScheduleData.cafSiteText1
                val siteText2 = depScheduleData.cafSiteText2

                if(siteText1 == "Failed" || siteText2 == "Failed"){
                    reqActivity.runOnUiThread {
                        run(){
                            Toast.makeText(
                                reqActivity,
                                "Ошибка при загрузке расписания",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                            groupSpinner.visibility = View.GONE
                            favoriteFloatButton.visibility = View.GONE
                        }
                    }

                    return
                }

                listOfSchedule = depScheduleData.listOfSchedule

                reqActivity.runOnUiThread {
                    run(){

                        val adapter = ArrayAdapter(
                            reqActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            depScheduleData.listOfTeachers
                        )
                        groupSpinner.adapter = adapter
                        groupSpinner.onItemSelectedListener = this@MainScheduleFragment
                    }
                }
            }
        }.start()

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    /**конкретный студент*/
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if(reqActivity.courseNum > -1){
            onStudItemSelected()
        }
        else{
            onTeacherItemSelected()
        }

    }

    private fun onStudItemSelected(){
        linkString = "https://portal.esstu.ru/${PublicData.catalog}/" +
                studSchedule.linkNamePairList[groupSpinner.selectedItemPosition].first

        /**Цвет звездочки на плавающей кнопке*/
        for (i in reqActivity.fileList()) {
            if (i == groupSpinner.selectedItem.toString()) {
                favoriteFloatButton.setImageResource(R.drawable.star_on)
                break
            } else {
                favoriteFloatButton.setImageResource(R.drawable.star_off)
            }
        }

        /**поток по загрузке страницы с расписанием*/
        object : Thread() {
            override fun run() {

                val siteScheduleText =
                    studSchedule.getCurrentStudentPage(groupSpinner.selectedItemPosition)

                reqActivity.runOnUiThread {
                    run {
                        /**Если инет отрубился после загрузки страницы со списком групп*/
                        if (siteScheduleText == "Failed" || siteScheduleText.indexOf("Пары") < 0) {
                            Toast.makeText(
                                reqActivity,
                                "Ошибка при загрузке расписания",
                                Toast.LENGTH_SHORT
                            ).show()

                            favoriteFloatButton.visibility = View.GONE
                            //groupSpinner.visibility = View.GONE

                            return@runOnUiThread
                        } else {
                            /**Если он врубился)))0)*/
                            favoriteFloatButton.visibility = View.VISIBLE
                            groupSpinner.visibility = View.VISIBLE
                        }

                        if (siteScheduleText == "Failed") {
                            return@runOnUiThread
                        }

                        /**Список с расписанием по дням недели для конкретного студента*/
                        localData = studSchedule.getCurrentStudentSchedule()

                        val itemCount = when (localData.size) {
                            7 -> 1
                            21 -> 3
                            28 -> 4
                            else -> 2
                        }

                        viewPager.isUserInputEnabled = try {
                            PreferenceManager.getDefaultSharedPreferences(reqActivity)
                                .all["swipeSetting"] as Boolean
                        } catch (e: Exception) {
                            true
                        }

                        val isWeekPosFalse = try {
                            PreferenceManager.getDefaultSharedPreferences(reqActivity)
                                .all["weekPosSetting"] as Boolean
                        } catch (e: Exception) {
                            false
                        }

                        /**----------Вкладки---------------------*/
                        var currentTab = TabLayout.Tab()

                        tabsCollectionAdapter =
                            ScheduleTabsViewPagerAdapter(
                                this@MainScheduleFragment,
                                itemCount
                            )
                        viewPager.adapter = tabsCollectionAdapter

                        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                            tab.text = "Неделя ${(position + 1)}"

                            /**Вкладка по неделе--------------------------------------*/
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
                    }
                }
            }
        }.start()

        /**Кнопка избранное*/
        favoriteFloatButton.setOnClickListener {
            val groupName = StringBuilder(groupSpinner.selectedItem.toString())

            try {
                groupName[groupName.indexOf('/')] = '.'
            } catch (e: Exception) {
            }

            try {
                reqActivity.openFileOutput(groupName.toString(), Context.MODE_APPEND)
            } catch (e: Exception) {
                Toast.makeText(
                    reqActivity,
                    "Ошибка при добавлении в избранное",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            AddToFavButtonHandler.addToFavButtonClick(
                favoriteFloatButton, reqActivity,
                localData as MutableList<List<String>>,
                0, groupName.toString(), linkString
            )
        }
    }

    private fun onTeacherItemSelected(){
        //заполняем лист с расписанием по текущему преподу------------------------------------------
        localData.clear()
        for (i in groupSpinner.selectedItemPosition * 12..groupSpinner.selectedItemPosition * 12 + 11) {
            localData.add(listOfSchedule[i])
        }

        /**КНопка избранное*/
        favoriteFloatButton.setOnClickListener {
            val linkString1 = "https://portal.esstu.ru/bakalavriat/${reqActivity.cafLinkPairArray[reqActivity.depNum].first}.htm"
            val linkString2 = "https://portal.esstu.ru/spezialitet/${reqActivity.cafLinkPairArray[reqActivity.depNum+1].first}.htm"
            AddToFavButtonHandler.addToFavButtonClick(
                favoriteFloatButton,
                reqActivity,
                localData as MutableList<List<String>>,
                groupSpinner.selectedItemPosition,
                groupSpinner.selectedItem.toString(),
                linkString1,
                linkString2
            )
        }

        /**Цвет звездочки*/
        for (i in reqActivity.fileList()) {
            if (i == groupSpinner.selectedItem.toString()) {
                favoriteFloatButton.setImageResource(R.drawable.star_on)
                break
            } else {
                favoriteFloatButton.setImageResource(R.drawable.star_off)
            }
        }

        /**херачим вкладки*/
        var currentTab = TabLayout.Tab()
        tabsCollectionAdapter = ScheduleTabsViewPagerAdapter(this, 2)

        val isWeekPosFalse = try {
            PreferenceManager.getDefaultSharedPreferences(reqActivity)
                .all["weekPosSetting"] as Boolean
        } catch (e: Exception) {
            false
        }

        viewPager.adapter = tabsCollectionAdapter
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
    }
}

class ScheduleTabsViewPagerAdapter(
    fragment: Fragment,
    private val itemCount: Int
) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = itemCount

    override fun createFragment(position: Int): Fragment {

        val fragment = MyTabFragment()
        val bundle = Bundle()
        bundle.putInt("pos", position)
        fragment.arguments = bundle

        return fragment
    }
}