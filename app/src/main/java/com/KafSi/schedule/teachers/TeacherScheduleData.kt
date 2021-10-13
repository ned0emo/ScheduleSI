package com.KafSi.schedule.teachers

import com.KafSi.schedule.PublicData
import java.net.URL
import java.nio.charset.Charset

class TeacherScheduleData {

    var siteText1: String = try {
        URL("https://portal.esstu.ru/bakalavriat/craspisanEdt.htm").readText(
            Charset.forName(
                "Windows-1251"
            )
        )
    } catch (e: Exception) {
        "Failed"
    }

    var siteText2: String = try {
        URL("https://portal.esstu.ru/spezialitet/craspisanEdt.htm").readText(
            Charset.forName(
                "Windows-1251"
            )
        )
    } catch (e: Exception) {
        "Failed"
    }

    fun getFacDepList(): MutableList<Pair<List<String>, List<String>>>{

        PublicData.siteText = siteText1

        //списки с факультетами и кафедрами---------------------------------------------------------
        val list1 = siteText1.split("ID", ignoreCase = true) as MutableList<String>
        val list2 = siteText2.split("ID", ignoreCase = true) as MutableList<String>
        val facDepList = mutableListOf<Pair<List<String>, List<String>>>()

        //разбиваем списки на факультеты и кафедры (с ссылками) в разные строки-----------------------------------
        //и херачим их попарно в еще один список-----------------------------------------------------
        for (i in 0..8) {
            val tmpList1 = list1[i].split("href")
            val tmpList2 = list2[i].split("href")

            facDepList.add(Pair(tmpList1.drop(0), tmpList2.drop(0)))
        }

        return facDepList
    }
}

