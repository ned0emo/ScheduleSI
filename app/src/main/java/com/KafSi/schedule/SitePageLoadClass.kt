package com.KafSi.schedule

import java.net.URL
import java.nio.charset.Charset

class SitePageLoadClass {
    fun getSiteText(link: String = "https://portal.esstu.ru/${PublicData.catalog}/raspisan.htm"): String {
        return try {
            URL(link).readText(
                Charset.forName(
                    "Windows-1251"
                )
            )
        } catch (e: Exception) {
            "Failed"
        }
    }
}