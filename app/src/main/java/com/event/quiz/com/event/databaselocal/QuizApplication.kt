package com.event.quiz.com.event.databaselocal

import android.app.Application
import com.event.quiz.QuizRepository

class QuizApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        QuizRepository.initialize(this)
    }
}

