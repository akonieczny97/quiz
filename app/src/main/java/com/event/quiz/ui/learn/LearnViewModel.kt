package com.event.quiz.ui.learn

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.event.quiz.DatabaseHelper
import com.event.quiz.Question

class LearnViewModel : ViewModel(){
    private val database = DatabaseHelper()
    var questionsListLiveData : LiveData<ArrayList<Question>> = database.questionsLiveData
    var title =""

    init {
        database.open()


    }
    fun retrieveQuestions(){
        database.retrieveQuestions(title)
    }

}