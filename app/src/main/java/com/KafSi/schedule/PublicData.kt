package com.KafSi.schedule

class PublicData {
    companion object {
        var isTeacher = 0
        var siteText = ""
        var courseNum = 0
        var catalog = ""
        var favSchedule = mutableListOf<MutableList<String>>()
        var currentWeek = 1
    }
}