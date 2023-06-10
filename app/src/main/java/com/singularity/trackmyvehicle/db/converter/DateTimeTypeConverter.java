package com.singularity.trackmyvehicle.db.converter;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import org.joda.time.DateTime;

/**
 * Created by Sadman Sarar on 10/25/18.
 */
public class DateTimeTypeConverter {

    @TypeConverter
    @Nullable
    public DateTime fromTimestamp(Long value) {
        return value == null ? null : new DateTime(value);
    }

    @TypeConverter
    @Nullable
    public Long dateToTimestamp(DateTime date) {
        if (date == null) {
            return null;
        } else {
            return date.getMillis();
        }
    }
}


