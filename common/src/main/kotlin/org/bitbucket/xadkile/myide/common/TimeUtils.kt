package org.bitbucket.xadkile.myide.common

import arrow.core.Either
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtils {
    val jupterTimeFormat = "YYYY-MM-dd'T'kk:mm:ss.SSSSSS'Z'"
    val dateFormat = SimpleDateFormat("YYYY-MM-dd'T'kk:mm:ss.SSSSSS'Z'")
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
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            return dateFormat.format(calendar.time)
        }
    }

    fun parseJupyterTime(dateStr:String?): Either<ParseException, Date> {
        try{
            return Either.Right(dateFormat.parse(dateStr))
        }catch(e:ParseException){
            return Either.Left(e)
        }
    }
}
