package com.KafSi.schedule.students

import com.KafSi.schedule.PublicData
import java.net.URL
import java.nio.charset.Charset

class StudentsScheduleData(siteText: String = "") {

    var linkNamePairList: MutableList<Pair<String, String>> = mutableListOf()
    var groupNameList: MutableList<String> = mutableListOf()
    private var siteScheduleText: String = "Failed"

    init {
        if (siteText == "") {
            linkNamePairList = getListOfGroups()
            linkNamePairList.removeAll(listOf(Pair("1.htm", "")))

            for (i in linkNamePairList) {
                groupNameList.add(
                    try {
                        if (PublicData.catalog != "zo1" && PublicData.catalog != "zo2")
                            i.second.substring(0, i.second.indexOf(' '))
                        else i.second
                    } catch (e: StringIndexOutOfBoundsException) {
                        i.second
                    }
                )
            }
        } else {
            /**Это при создании объекта во время проверки сохраненного расписания на корректность
             * Тип в конструктор передаем подгруженный оттудава в фоне сайт,
             * а не тот, что в паблик дату захерачен.
             *
             * Стоп, а нахера?
             *
             * А потому, что нам до лампочки на список групп, так как проверка
             * только одной группы идет*/
            siteScheduleText = siteText
        }
    }

    /**Получаем список групп с уже загруженной страницы с таблицей из этих групп*/
    private fun getListOfGroups(): MutableList<Pair<String, String>> {

        val courseNum =
            if (PublicData.courseNum > 5) PublicData.courseNum - 6
            else PublicData.courseNum

        val siteSplitList: MutableList<String> =
            PublicData.siteText.split("><A") as MutableList<String>

        val linkNamePairList: MutableList<Pair<String, String>> = mutableListOf()

        for (i in 1 + courseNum..400 + courseNum step 6) {
            linkNamePairList.add(
                Pair(
                    siteSplitList[i].substring(7, siteSplitList[i].indexOf("htm") + 3),
                    try {
                        siteSplitList[i].substring(
                            siteSplitList[i].indexOf("n\">") + 3,
                            siteSplitList[i].indexOf("</")
                        )
                    } catch (e: Exception) {
                        ""
                    }
                )
            )
        }
        return linkNamePairList
    }

    fun getCurrentStudentPage(groupPos: Int): String {
        siteScheduleText = try {
            URL(
                "https://portal.esstu.ru/${PublicData.catalog}/" +
                        this.linkNamePairList[groupPos].first
            ).readText(Charset.forName("Windows-1251"))
        } catch (e: Exception) {
            "Failed"
        }

        return siteScheduleText
    }

    /**Расписание конкретного студента*/
    fun getCurrentStudentSchedule(): MutableList<MutableList<String>> {
        val list = siteScheduleText.split("CENTER") as MutableList<String>
        val listOfList = mutableListOf<MutableList<String>>()
        val tmpList = mutableListOf<String>()
        var k = 19

        for (i in 0..27) {
            for (j in k..k + 7) {
                tmpList.add(list[j].substring(2, list[j].indexOf('<')))
            }
            listOfList.add(tmpList.drop(0) as MutableList<String>)
            tmpList.clear()
            k += 9

            if (k >= list.size - 2) break
            if (list[k].indexOf("Пары") > 0) k += 18
        }

        return listOfList
    }
}