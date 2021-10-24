package org.bitbucket.xadkile.taiga

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtils {
    object now{
        fun fromInstant():String{
            return DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        }

        fun fromCalendar(): String {
           return fromDate(Date())
        }

        fun fromDate(date:Date):String{
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.timeZone = TimeZone.getTimeZone("UTC")
            val dateFormat = SimpleDateFormat("YYYY-MM-dd'T'kk:mm:ss.SSSSSS'Z'")
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            return dateFormat.format(calendar.time)
        }
    }
}
