package com.event.quiz

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


data class Question(var id: String ="",
                    var number: String="",
                 var question: String = "",
                 var answer1: String = "",
                 var answer2: String = "",
                 var answer3: String = "",
                 var answer4: String = "",
                 var correctAnswer: String = "")