package com.KafSi.schedule.classes

import java.net.URL
import java.nio.charset.Charset

class ClassesSchedule {
    private val classesScheduleList = mutableMapOf<String, MutableList<MutableList<String>>>()
    private val buildingsList =
        listOf("1-", "2", "3", "4", "5", "6", "7", "8", "9", "0", "11", "12", "13", "14", "15")
    private var isLoad = false
    private val sitesList = mutableListOf<String>()
    private val daysOfWeekList = listOf("Пнд", "Втр", "Срд", "Чтв", "Птн", "Сбт")

    var isReady = false
    var isError = false

    fun loadSchedule() {
        object : Thread() {
            override fun run() {
                val siteText1: String = try {
                    URL("https://portal.esstu.ru/bakalavriat/craspisanEdt.htm").readText(
                        Charset.forName(
                            "Windows-1251"
                        )
                    )
                } catch (e: Exception) {
                    isError = true
                    isReady = true
                    return
                }

                val splitSiteText1 = siteText1.split("Caf", ignoreCase = false)

                for (i in splitSiteText1) {
                    if (i[0].isDigit() && i.indexOf("htm") > 0) {
                        try {
                            sitesList.add(
                                URL(
                                    "https://portal.esstu.ru/bakalavriat/Caf" +
                                            i.substring(0, i.indexOf("htm") + 3)
                                ).readText(
                                    Charset.forName(
                                        "Windows-1251"
                                    )
                                )
                            )
                        } catch (e: Exception) {
                        }
                    }
                }

                if (isLoad) {
                    createClassesMap()
                } else {
                    isLoad = true
                }
            }
        }.start()

        object : Thread() {
            override fun run() {
                val siteText2: String = try {
                    URL("https://portal.esstu.ru/spezialitet/craspisanEdt.htm").readText(
                        Charset.forName(
                            "Windows-1251"
                        )
                    )
                } catch (e: Exception) {
                    isError = true
                    isReady = true
                    return
                }
                val splitSiteText2 = siteText2.split("Caf", ignoreCase = false)
                //val classesList = mutableMapOf<String, Int>()

                for (i in splitSiteText2) {
                    if (i[0].isDigit() && i.indexOf("htm") > 0) {
                        try {
                            sitesList.add(
                                URL(
                                    "https://portal.esstu.ru/spezialitet/Caf" +
                                            i.substring(0, i.indexOf("htm") + 3)
                                ).readText(
                                    Charset.forName(
                                        "Windows-1251"
                                    )
                                )
                            )
                        } catch (e: Exception) {
                        }
                    }
                }

                if (isLoad) {
                    createClassesMap()
                } else {
                    isLoad = true
                }
            }
        }.start()
    }

    private fun createClassesMap() {
        for (i in sitesList) {
            if (i.indexOf("а.") < 0) {
                continue
            }

            var tmp = i

            /**Имя препода находится перед этим цветовым кодом*/
            while (tmp.indexOf("ff00ff") > -1) {
                tmp = tmp.substring(tmp.indexOf("ff00ff"))
                val teacherName = tmp.substring(tmp.indexOf(' '), tmp.indexOf("</"))

                /**6 дней недели два раза*/
                for (j in 0..11) {
                    /**Переходим к текущему дню недели*/
                    tmp = tmp.substring(tmp.indexOf(daysOfWeekList[j % 6]))
                    /**Последний символ подстроки названия предмета
                     * для возможности перехода к след паре*/
                    var lastSubstringIndex = tmp.indexOf('<', tmp.indexOf("CENTER"))

                    /**Название пары необработанное*/
                    var lesson = tmp.substring(
                        tmp.indexOf("CENTER") + 8,
                        lastSubstringIndex
                    )

                    /**От 1 до 6 пары*/
                    for (k in 0..5) {
                        /**Если в названиии пары указана аудитория
                         * Конструируем название и номер пары, номер аудитрии,
                         * и фигачим их в Мэп*/
                        if (lesson.indexOf("а.") > -1) {
                            val aIndex = lesson.indexOf("а.")
                            var className =
                                lesson.substring(aIndex + 2, lesson.indexOf(' ', aIndex))

                            var lessonName =
                                teacherName + " " + lesson.replace("а.$className", "")

                            while (lessonName.indexOf("  ") > -1) {
                                lessonName = lessonName.replace("  ", " ")
                            }

                            if(className.indexOf("и/д") > -1){
                                className = className.replace("и/д", "")
                            }

                            if (!classesScheduleList.containsKey(className)) {
                                classesScheduleList[className] =
                                    MutableList(12) { MutableList(6) { "" } }
                            }

                            if (lessonName.length > classesScheduleList[className]!![j][k].length)
                                classesScheduleList[className]!![j][k] = lessonName
                        }

                        /**Запоминаем текущий индекс CENTER для перехода к следующему*/
                        val newCenterIndex = tmp.indexOf("CENTER", lastSubstringIndex)

                        lesson = tmp.substring(
                            newCenterIndex + 8,
                            tmp.indexOf('<', newCenterIndex)
                        )

                        lastSubstringIndex = tmp.indexOf('<', newCenterIndex)
                    }
                }
            }

            /**Временный цикл для составления списка классов
             * tmp изначально должен равняться i*/
            /*while(tmp.indexOf("а.") > -1){
                tmp = tmp.substring(tmp.indexOf("а."))

                val className = tmp.substring(2, tmp.indexOf(' '))

                if(classesList.containsKey(className)){
                    classesList[className] = classesList[className]!! + 1
                }
                else{
                    classesList[className] = 0
                }

                tmp = tmp.drop(5)
            }*/
        }

        isReady = true
        return
    }

    fun getClassesSchedule(buildNumber: Int): MutableMap<String, MutableList<MutableList<String>>> {
        val classScheduleList = mutableMapOf<String, MutableList<MutableList<String>>>()

        for (i in classesScheduleList) {
            if (i.key.indexOf(buildingsList[buildNumber]) == 0) {
                classScheduleList[i.key] = i.value
            }
        }

        return classScheduleList
    }
}