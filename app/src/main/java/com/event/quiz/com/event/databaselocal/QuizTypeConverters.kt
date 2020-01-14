package com.event.quiz.com.event.databaselocal

import androidx.room.TypeConverter
import java.util.*

class QuizTypeConverters {
    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }
}