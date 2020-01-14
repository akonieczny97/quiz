package com.event.quiz.com.event.quiz.account

data class User(var uid: String = "",  var email: String = "", var username: String = "", var admin: String = "no", var name: String="", var surname: String ="", var friendNumber: String =""){
    val photoFileName
        get() = "IMG_$uid.jpg"
}