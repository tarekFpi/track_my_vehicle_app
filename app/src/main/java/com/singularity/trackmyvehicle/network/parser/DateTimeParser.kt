package com.singularity.trackmyvehicle.network.parser

import com.google.gson.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.lang.reflect.Type


class DateTimeParser : JsonDeserializer<DateTime>, JsonSerializer<DateTime> {

    private val dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")


    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DateTime? =
            try {
                DateTime.parse(json?.asString, dateTimeFormat)
            } catch (ex: Exception) {
                null
            }


    override fun serialize(src: DateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.toString(dateTimeFormat))
    }
}
