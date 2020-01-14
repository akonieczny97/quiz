package com.event.quiz.ui.addquiz

import androidx.lifecycle.ViewModel
import com.event.quiz.DatabaseHelper
import com.event.quiz.Question
import com.event.quiz.Quiz

class AddQuizViewModel : ViewModel(){
    private val databaseHelper = DatabaseHelper()
    var currentQuestion = Question()
    var title: String =""
    private var listOfQuestions = mutableListOf<Question>()
    var questionNumber = 1
    init {
        databaseHelper.open()
    }


    fun goToPrevQuestion(){
        questionNumber--
        currentQuestion = listOfQuestions[questionNumber-1]
    }
    fun saveQuestion(){
        if(questionNumber>listOfQuestions.size){
            currentQuestion.number=questionNumber.toString()
            currentQuestion.id=databaseHelper.addQuestion(title, currentQuestion)
            listOfQuestions.add(currentQuestion)
        }
        else
        {
            saveEditedQuestion()
        }


    }
    fun saveEditedQuestion(){
        databaseHelper.updateQuestion(title, currentQuestion)
    }

    fun goToNextQuestion(){

        questionNumber++
        currentQuestion =
            if(questionNumber>listOfQuestions.size) Question()
            else listOfQuestions[questionNumber-1]



    }
}