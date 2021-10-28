package com.KafSi.schedule

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.awaitAll

class HostViewActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var loadDialog: Dialog
    private lateinit var navController: NavController
    private var buttonIndex = -1
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    lateinit var classesSchedule: ClassesScheduleClass
    var cafLinkPairArray = arrayOf(Pair("", ""))
    var depNum = -1
    var courseNum = -1
    var isClasses = false
    var buildingNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**Если это активити вызвано с активити кнопок факультетов,
         * то грузим преподов и переходим в функцию ниже*/
        if (intent.getBooleanExtra("isTeachers", false)) {

            PublicData.isTeacher = 1

            cafLinkPairArray =
                intent.getSerializableExtra("cafLinkPairArray") as Array<Pair<String, String>>

            teachersNavView(cafLinkPairArray)

            return
        }

        if(intent.getIntExtra("data", 0) == 4){
            classesNavView()

            return
        }

        PublicData.isTeacher = 0
        courseNum = 0

        buttonIndex = intent.getIntExtra("data", 0)
        PublicData.catalog = when (buttonIndex) {
            0 -> "bakalavriat"
            1 -> "spezialitet"
            2 -> "zo1"
            3 -> "zo2"
            else -> ""
        }

        loadDialog = Dialog(this)
        loadDialog.setContentView(layoutInflater.inflate(R.layout.dialog_load, null))
        loadDialog.show()

        /**поток загрузки сайта*/
        object : Thread() {
            override fun run() {
                val siteText = SitePageLoadClass().getSiteText()

                this@HostViewActivity.runOnUiThread {
                    run {
                        loadDialog.dismiss()
                        siteTextChanged(siteText)
                    }
                }
            }
        }.start()
    }

    /**после загрузки сайта*/
    private fun siteTextChanged(siteText: String) {
        PublicData.siteText = siteText
        navViewCreate(false)

        when (buttonIndex) {
            /**Магистратура*/
            1 -> {
                PublicData.courseNum = 6
                courseNum = 6

                for (i in 0..5) {
                    navView.menu[i].setOnMenuItemClickListener {
                        PublicData.courseNum = i + 6
                        courseNum = i + 6
                        false
                    }
                }

                navView.menu[0].title = "Колледж 1 курс"
                navView.menu[1].title = "Колледж 2 курс"
                navView.menu[2].title = "Колледж 3 курс"
                navView.menu[3].title = "Колледж 4 курс"
                navView.menu[4].title = "Магистратура 1 курс"
                navView.menu[5].title = "Магистратура 2 курс"

                navController.graph.toList()[0].label = "Колледж 1 курс"
                navController.graph.toList()[1].label = "Колледж 2 курс"
                navController.graph.toList()[2].label = "Колледж 3 курс"
                navController.graph.toList()[3].label = "Колледж 4 курс"
                navController.graph.toList()[4].label = "Магистратура 1 курс"
                navController.graph.toList()[5].label = "Магистратура 2 курс"
            }
            /**Бакалавриат*/
            else -> {
                courseNum = 0
                PublicData.courseNum = 0

                for (i in 0..5) {
                    navView.menu[i].setOnMenuItemClickListener {
                        PublicData.courseNum = i
                        courseNum = i
                        false
                    }
                }
            }
        }

        for (i in 6..19) {
            navView.menu[i].isVisible = false
            navView.menu[i].isEnabled = false
        }
    }

    private fun teachersNavView(currentFacDepPair: Array<Pair<String, String>>) {

        depNum = 0
        navViewCreate(true)

        for (i in 0 until currentFacDepPair.size / 2) {
            navView.menu[i].title =
                if (currentFacDepPair[i * 2].second.length > 25) {
                    abbreviation(currentFacDepPair[i * 2].second)
                } else {
                    currentFacDepPair[i * 2].second
                }

            navController.graph.toList()[i].label = currentFacDepPair[i * 2].second
        }

        for (i in 0..19) {
            if (navView.menu[i].title.indexOf("курс") > -1) {
                navView.menu[i].isEnabled = false
                navView.menu[i].isVisible = false
            }
        }

        for (i in 0..19) {
            navView.menu[i].setOnMenuItemClickListener {
                depNum = i * 2
                false
            }
        }

        //setupActionBarWithNavController(navController, appBarConfiguration)
        //navView.setupWithNavController(navController)
    }

    private fun classesNavView(){

        PublicData.isTeacher = 1
        classesSchedule = ClassesScheduleClass()
        isClasses = true

        loadDialog = Dialog(this)
        loadDialog.setContentView(layoutInflater.inflate(R.layout.dialog_load, null))
        loadDialog.show()

        object: Thread(){
            override fun run() {
                classesSchedule.loadSchedule()

                while(!classesSchedule.isReady){
                    sleep(100)
                }

                runOnUiThread {
                    run{
                        navViewCreate(true)

                        for(i in 0..14){
                            navView.menu[i].title = "${i+1} корпус"
                            navController.graph.toList()[i].label = "${i+1} корпус"

                            navView.menu[i].setOnMenuItemClickListener {
                                buildingNumber = i
                                false
                            }
                        }

                        for(i in 15..19){
                            navView.menu[i].isEnabled = false
                            navView.menu[i].isVisible = false
                        }

                        loadDialog.dismiss()
                    }
                }
            }
        }.start()
    }

    private fun navViewCreate(isNeedManyTabs: Boolean){
        setContentView(R.layout.activity_host_view)
        setSupportActionBar(findViewById(R.id.toolbar))

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)

        if(isNeedManyTabs){
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_01, R.id.nav_02,
                    R.id.nav_03, R.id.nav_04,
                    R.id.nav_05, R.id.nav_06,
                    R.id.nav_07, R.id.nav_08,
                    R.id.nav_09, R.id.nav_10,
                    R.id.nav_11, R.id.nav_12,
                    R.id.nav_13, R.id.nav_14,
                    R.id.nav_15, R.id.nav_16,
                    R.id.nav_17, R.id.nav_18,
                    R.id.nav_19, R.id.nav_20
                ), drawerLayout
            )
        }
        else{
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_01,
                    R.id.nav_02,
                    R.id.nav_03,
                    R.id.nav_04,
                    R.id.nav_05,
                    R.id.nav_06
                ), drawerLayout
            )
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    /**Сокращение названий кафедр*/
    private fun abbreviation(str: String): String {

        var newStr = str[0].toString()

        for (i in 1 until str.length - 1) {
            if (str[i] == ' ' || str[i] == '-') {
                newStr += when {
                    str[i + 2] == ' ' -> str[i + 1]
                    else -> str[i + 1].uppercaseChar()
                }
            }
        }

        return newStr
    }

    override fun onBackPressed() = finish()

    override fun onSupportNavigateUp(): Boolean = findNavController(R.id.nav_host_fragment)
        .navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}