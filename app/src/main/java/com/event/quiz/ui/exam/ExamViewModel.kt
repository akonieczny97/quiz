package com.event.quiz.ui.exam

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.event.quiz.DatabaseHelper
import com.event.quiz.Question
import com.event.quiz.com.event.quiz.account.User

class ExamViewModel : ViewModel(){
    private val database = DatabaseHelper()
    var questionsListLiveData : LiveData<ArrayList<Question>> = database.questionsLiveData
    var questionsList : ArrayList<Question>
    var userLiveData: LiveData<User> = database.userLiveData
    var points = 0
    var currentQuestion : Question = Question()
    var questionNumber=1
    var questionRange=0
    var title =""
    var running = false
    var timeLeft : Long = 0

    init {
        database.open()
        questionsList = ArrayList()


    }

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
            points = 0
            timeLeft = (questionRange*1000*60).toLong()
            running = true
        }

    }

    fun checkAnswer(answer: String){
        if(answer == currentQuestion.correctAnswer)points++
    }

    fun returnAnswers(): List<String>{
        var answersList = mutableListOf<String>(currentQuestion.answer1, currentQuestion.answer2, currentQuestion.answer3 ,currentQuestion.answer4)
        answersList.shuffle()
        return answersList
    }

    fun nextQuestion(){
        currentQuestion=questionsList[questionNumber-1]
    }
    fun getUser(uid: String){
        database.getUser(uid)
    }
}