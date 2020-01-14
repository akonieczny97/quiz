package com.event.quiz.com.event.databaselocal

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.event.quiz.Quiz

@Dao
interface QuizDao {

    @Query("SELECT * FROM quiz")
    fun getQuizes(): LiveData<List<Quiz>>

    @Insert
    fun addQuiz(quiz: Quiz)


}