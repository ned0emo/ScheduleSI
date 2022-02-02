package com.KafSi.schedule.students

import java.net.URL
import java.nio.charset.Charset
import kotlin.math.min

class StudentsSchedule(scheduleType: Int) {

    val link = when (scheduleType) {
        0 -> "bakalavriat"
        1 -> "spezialitet"
        2 -> "zo1"
        3 -> "zo2"
        else -> ""
    }

    private val listOfSchedule = mutableListOf<MutableList<String>>()
    private val daysOfWeekList = listOf("Пнд", "Втр", "Срд", "Чтв", "Птн", "Сбт", "Вск")
    private var readyCheck = 0
    var catalogLink = ""

    val siteText = try {
        URL("https://portal.esstu.ru/$link/raspisan.htm").readText(
            Charset.forName(
                "Windows-1251"
            )
        )
    } catch (e: Exception) {
        "Failed"
    }

    var linkNamePairList = generateLinkGroupList()

    fun getStudentSchedule(catalog: String): MutableList<MutableList<String>> {
        if (catalogLink == catalog)
            return listOfSchedule

        var siteScheduleText = try {
            URL(
                "https://portal.esstu.ru/$link/$catalog"
            ).readText(Charset.forName("Windows-1251"))
        } catch (e: Exception) {
            return mutableListOf()
        }

        siteScheduleText = try {
            siteScheduleText.substring(siteScheduleText.indexOf("Пнд"))
        } catch (e: Exception) {
            return mutableListOf()
        }

        catalogLink = catalog

        listOfSchedule.clear()
        val tmpList = mutableListOf<String>()

        if (link == "zo1" || link == "zo2") {
            val k = 28

            for (i in 0 until k) {

                if (siteScheduleText.indexOf("Сбт") < 0) {
                    break
                }

                siteScheduleText = siteScheduleText
                    .substring(siteScheduleText.indexOf(daysOfWeekList[i % 7]))
                val splitedSiteList = siteScheduleText.split("CENTER")

                for (j in 0 until 8) {
                    val lessonString = splitedSiteList[j]
                    val centerIndex = lessonString.indexOf("CENTER")

                    tmpList.add(
                        lessonString.substring(
                            centerIndex + if(j == 0) 1 else 3,
                            lessonString.indexOf("</F") - if(j == 0) 9 else 1
                        )
                    )
                }

                listOfSchedule.add(tmpList.drop(0) as MutableList<String>)
                tmpList.clear()
            }
        } else {
            val k = 12

            for (i in 0 until k) {
                siteScheduleText = siteScheduleText
                    .substring(siteScheduleText.indexOf(daysOfWeekList[i % 6]))
                var splitedSiteList = siteScheduleText.split("CENTER")

                if (k == 12) {
                    splitedSiteList = splitedSiteList.drop(1)
                }

                for (j in 0 until 6) {
                    val lessonString = splitedSiteList[j]
                    val centerIndex = lessonString.indexOf("CENTER")

                    tmpList.add(
                        lessonString.substring(
                            centerIndex + 3,
                            lessonString.indexOf("</F") - 1
                        )
                    )
                }

                listOfSchedule.add(tmpList.drop(0) as MutableList<String>)
                tmpList.clear()
            }
        }

        return listOfSchedule
    }

    fun getListOfGroups(courseNum: Int): MutableList<String> {
        val nameList = mutableListOf<String>()

        /*if(linkNamePairList.isEmpty()){
            linkNamePairList = generateLinkGroupList()
        }*/

        try{
            for (i in linkNamePairList[courseNum]) {
                nameList.add(i.second)
            }
        }
        catch(e: Exception){
            return mutableListOf()
        }

        return nameList
    }

    private fun generateLinkGroupList(): List<MutableList<Pair<String, String>>> {
        if(siteText == "Failed"){
            return listOf()
        }

        val splitedSiteList = try{
            siteText.split("><A").drop(1) as MutableList<String>
        }
        catch(e: Exception){
            return listOf()
        }
        val length = splitedSiteList.size

        linkNamePairList = List(6) { mutableListOf() }

        for (j in 0..5) {
            object : Thread() {
                override fun run() {

                    var emptinessCounter = 0;

                    for (i in 0 until length step 6) {
                        val studentGroup = splitedSiteList[i + j].substring(
                            splitedSiteList[i + j].indexOf("n\">") + 3,
                            splitedSiteList[i + j].indexOf("</")
                        )

                        if (studentGroup == "") {
                            emptinessCounter++

                            if (emptinessCounter > 2) {
                                break
                            }
                            continue
                        }

                        if(studentGroup.replace(".", "") == ""){
                            continue
                        }

                        linkNamePairList[j].add(
                            Pair(
                                splitedSiteList[i + j].substring(
                                    splitedSiteList[i + j].indexOf('=') + 2,
                                    splitedSiteList[i + j].indexOf("htm") + 3
                                ),
                                studentGroup
                            )
                        )
                    }

                    readyCheck++
                }
            }.start()
        }

        while (readyCheck < 6) {
            Thread.sleep(100)
        }

        return linkNamePairList
    }
}