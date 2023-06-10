package com.singularity.trackmyvehicle.testhelper

import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Imran Chowdhury on 8/9/2018.
 */

class TestHelper {
    companion object {

        fun getDate(): String {
            val format = "yyyy-MM-dd"
            val date = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat(format)
            return dateFormat.format(date)
        }

        fun getDateByDateTime(): DateTime {
            val format = "yyyy-MM-dd"
            val date = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat(format)
            return DateTime.parse(dateFormat.format(date))
        }

        fun getUUID(): String {
            val uuid = UUID.randomUUID()
            return uuid.toString()
        }

        fun getPreviousDate(): String {
            val format = "yyyy-MM-dd hh:mm:ss"
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -1)
            val date = cal.time
            val dateFormat = SimpleDateFormat(format)
            return dateFormat.format(date)
        }
    }
}