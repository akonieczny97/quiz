package com.event.quiz.com.event.databaselocal

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.event.quiz.Quiz

@Database(entities = [ Quiz::class], version=1, exportSchema = false)
@TypeConverters(QuizTypeConverters::class)
abstract class QuizDatabase : RoomDatabase(){

    abstract fun quizDao(): QuizDao
}