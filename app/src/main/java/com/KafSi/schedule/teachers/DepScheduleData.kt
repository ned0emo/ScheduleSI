package com.KafSi.schedule.teachers

import java.net.URL
import java.nio.charset.Charset

class DepScheduleData(link1: String, link2: String) {

    val listOfTeachers = mutableListOf<String>()
    val listOfSchedule = mutableListOf<MutableList<String>>()

    var cafSiteText1: String = try {
        URL("https://portal.esstu.ru/bakalavriat/${link1}.htm").readText(
            Charset.forName(
                "Windows-1251"
            )
        )
    } catch (e: Exception) {
        "Failed"
    }

    var cafSiteText2: String = try {
        URL("https://portal.esstu.ru/spezialitet/${link2}.htm").readText(
            Charset.forName(
                "Windows-1251"
            )
        )
    } catch (e: Exception) {
        "Failed"
    }

    init {
        if (cafSiteText1 != "Failed" && cafSiteText2 != "Failed"){
            createScheduleList(cafSiteText1.split("занятий"))
            createScheduleList(cafSiteText2.split("занятий"))

            var i = 0
            while (i < listOfTeachers.size) {
                for (j in i + 1 until listOfTeachers.size) {
                    if (listOfTeachers[i] == listOfTeachers[j]) {
                        for (k in 0..11) {
                            for (l in 0..5) {
                                if (listOfSchedule[i * 12 + k][l].length < listOfSchedule[j * 12 + k][l].length) {
                                    listOfSchedule[i * 12 + k][l] = listOfSchedule[j * 12 + k][l]
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
        }
    }

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