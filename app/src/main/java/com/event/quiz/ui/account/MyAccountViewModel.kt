package com.event.quiz.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.event.quiz.DatabaseHelper
import com.event.quiz.com.event.quiz.account.User
import java.io.File

class MyAccountViewModel : ViewModel(){
     val databaseHelper: DatabaseHelper = DatabaseHelper()
     val userLiveDate : LiveData<User> = databaseHelper.userLiveData
         init{
             databaseHelper.open()
         }

    fun getPhotoFile(user: User, filesDir : File): File = File(filesDir, user.photoFileName)

}