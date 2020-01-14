package com.event.quiz

import androidx.lifecycle.ViewModel

class DrawerViewModel : ViewModel(){
    val databaseHelper: DatabaseHelper = DatabaseHelper()
    val userLiveData = databaseHelper.userLiveData

    init {
        databaseHelper.open()
    }

    fun getUser(uid: String){
        databaseHelper.getUser(uid)
    }
}