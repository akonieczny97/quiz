package com.event.quiz.ui.quizlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.event.quiz.DatabaseHelper
import com.event.quiz.Quiz

class QuizListViewModel : ViewModel() {

    val database = DatabaseHelper()
    var quizListLiveData : LiveData<ArrayList<String>> = database.quizesLiveData
    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value=""
        quizListLiveData = Transformations.switchMap(mutableSearchTerm){searchTerm->
            database.retrieve(searchTerm)
        }
    }

    /*fun addQuiz(quiz: Quiz){
        quizRepository.addQuiz(quiz)
    }*/

    fun fetchQuizes(query: String =""){
        mutableSearchTerm.value = query
    }
}