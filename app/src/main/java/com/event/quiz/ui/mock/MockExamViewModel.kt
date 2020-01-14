package com.event.quiz.ui.mock

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.event.quiz.DatabaseHelper
import com.event.quiz.Question

class MockExamViewModel : ViewModel(){
    private val database = DatabaseHelper()
    var questionsListLiveData : LiveData<ArrayList<Question>> = database.questionsLiveData
    var questionsList : ArrayList<Question>
    var pointsFromAnswers : ArrayList<Int>
    init {
        database.open()
        questionsList = ArrayList()
        pointsFromAnswers = ArrayList()


    }
    var points = 0
    var currentQuestion : Question = Question()
    var questionNumber=1
    var questionRange=0
    var title =""
    var running = false



    fun retrieveQuestions(){
        database.retrieveQuestions(title)
    }

    fun isItEnd() : Boolean{
        if(questionNumber < questionRange) {
            questionNumber++
            return false
        }
        running = false
        return true

    }
    fun makeQuestions(){
        if(!running){
            questionsList=questionsListLiveData.value!!
            questionsList.shuffle()
            currentQuestion=questionsList[0]
            questionRange=if(questionsList.size<10) questionsList.size else 10
            questionNumber=1
            for(i in 1..questionsList.size) pointsFromAnswers.add(0)
            running = true
        }

    }

    fun checkAnswer(answer: String): Boolean{
        if(answer == currentQuestion.correctAnswer) {
            points++
            return true
        }else return false
    }

    fun returnAnswers(): List<String>{
        var answersList = mutableListOf<String>(currentQuestion.answer1, currentQuestion.answer2, currentQuestion.answer3 ,currentQuestion.answer4)
        answersList.shuffle()
        return answersList
    }

    fun nextQuestion(){
        currentQuestion=questionsList[questionNumber-1]
    }
}