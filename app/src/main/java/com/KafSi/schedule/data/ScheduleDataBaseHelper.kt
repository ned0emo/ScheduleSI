package com.KafSi.schedule.data

import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScheduleDataBaseHelper(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    constructor(
        context: Context, name: String, type: String, isRead: Boolean
    ) : this(context, name, null, 1)

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQueryString = ("CREATE TABLE teachers ("
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TYPE INTEGER NOT NULL, "
                + "NAME TEXT NOT NULL, "
                + "LINK1 TEXT NOT NULL, "
                + "LINK2 TEXT NOT NULL;")

        db?.execSQL(createTableQueryString)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

    }
}