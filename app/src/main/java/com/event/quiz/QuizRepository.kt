package com.event.quiz

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.event.quiz.com.event.databaselocal.QuizDatabase

import java.lang.IllegalStateException
import java.util.concurrent.Executors

private const val DATABASE_NAME = "quiz-database"

class QuizRepository private constructor(context: Context){

    private val database: QuizDatabase = Room.databaseBuilder(
        context.applicationContext,
        QuizDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val quizDao = database.quizDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    fun getQuizes(): LiveData<List<Quiz>> = quizDao.getQuizes()
    fun addQuiz(quiz: Quiz){
        executor.execute {
            quizDao.addQuiz(quiz)
        }
    }


    companion object{
        private var INSTANCE: QuizRepository? = null

        fun initialize(context: Context){
            if(INSTANCE == null){
                INSTANCE = QuizRepository(context)
            }
        }

        fun get(): QuizRepository {
            return INSTANCE ?:
                    throw IllegalStateException("QuestionRepository must be initialized")
        }
    }
}