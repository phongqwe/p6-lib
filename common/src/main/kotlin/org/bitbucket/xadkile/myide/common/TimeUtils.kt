package org.bitbucket.xadkile.myide.common

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtils {
    val jupterTimeFormat = "YYYY-MM-dd'T'kk:mm:ss.SSSSSS'Z'"
    val dateFormat = SimpleDateFormat("YYYY-MM-dd'T'kk:mm:ss.SSSSSS'Z'")

    fun parseJupyterTime(dateStr:String?): Result<Date,ParseException> {
        try{
            return Ok(dateFormat.parse(dateStr))
        }catch(e:ParseException){
            return Err(e)
        }
    }
}
