package com.event.quiz

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.event.quiz.com.event.quiz.account.User
import com.google.firebase.database.*

class DatabaseHelper {

    private lateinit var database: DatabaseReference

    val quizesLiveData = MutableLiveData<ArrayList<String>>()
    val questionsLiveData = MutableLiveData<ArrayList<Question>>()
    val userLiveData = MutableLiveData<User>()


    init {
        quizesLiveData.value = ArrayList()
        questionsLiveData.value = ArrayList()
    }

    fun retrieve(query: String = ""): LiveData<ArrayList<String>> {
        database.child("quizes").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.e("123", "Succeded to read user")

                if(query.isBlank()){
                    fetchData(dataSnapshot)
                }else{
                    searchData(dataSnapshot, query)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("123", "Failed to read user")
            }

        })
        Log.d("123", "got: ${quizesLiveData.value!!.size}")
        return quizesLiveData
    }

    fun fetchData(dataSnapshot: DataSnapshot){
        var quizList = ArrayList<String>()
        for(myQuiz in dataSnapshot.children){
            val quiz = myQuiz.key.toString()
            quizList.add(quiz)
        }
        quizesLiveData.value=quizList
    }

    fun searchData(dataSnapshot: DataSnapshot, title: String){
        var quizList = ArrayList<String>()
        val regex = title.toRegex()
        for(myQuiz in dataSnapshot.children){
            val quiz = myQuiz.key.toString()
            val match = regex.find(quiz)
            if(match != null) quizList.add(quiz)
        }
        quizesLiveData.value=quizList
    }
    fun getUser(uid: String): LiveData<User>{
        database.child("users").child(uid).addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onCancelled(p0: DatabaseError) {
                Log.e("123", "Failed to read user")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.e("123", "Succeded to read user")
                val user = dataSnapshot.getValue(User::class.java)
                userLiveData.value = user
            }

        })
        return userLiveData
    }
    fun open() {
        database = FirebaseDatabase.getInstance().reference
    }
    fun addUser(user: User){

        //val key = database.child("users").push().key
        database.child("users").child(user.uid).child("email").setValue(user.email)
        database.child("users").child(user.uid).child("username").setValue(user.username)
        database.child("users").child(user.uid).child("admin").setValue(user.admin)
        database.child("users").child(user.uid).child("name").setValue(user.name)
        database.child("users").child(user.uid).child("surname").setValue(user.surname)

    }
    fun updateUser(user: User){

        if(user.username.isNotBlank())database.child("users").child(user.uid).child("username").setValue(user.username)
        if(user.name.isNotBlank())database.child("users").child(user.uid).child("name").setValue(user.name)
        if(user.surname.isNotBlank())database.child("users").child(user.uid).child("surname").setValue(user.surname)
        //database.child("users").child(user.uid).child("photoFileName").setValue(user.photoFileName)

    }

    fun updateUserFriendNumber(uid: String, number: String){
        database.child("users").child(uid).child("friendNumber").setValue(number)
    }
    fun add(title: String) {
        val key = database.child("quizes").push().key
        database.child("quizes").child(key!!).child("title").setValue(title)
    }

    fun delete(_id: String) {
        database.child("quizes").child(_id).removeValue()
    }

    fun updateQuestion(title: String, question: Question){
        database.child("quizes").child(title).child(question.id).child("question").setValue(question.question)
        database.child("quizes").child(title).child(question.id).child("answer1").setValue(question.answer1)
        database.child("quizes").child(title).child(question.id).child("answer2").setValue(question.answer2)
        database.child("quizes").child(title).child(question.id).child("answer3").setValue(question.answer3)
        database.child("quizes").child(title).child(question.id).child("answer4").setValue(question.answer4)
        database.child("quizes").child(title).child(question.id).child("number").setValue(question.number)
        database.child("quizes").child(title).child(question.id).child("correctAnswer").setValue(question.correctAnswer)
    }
    fun addQuestion(title: String, question: Question): String{

        val key = database.child("quizes").child(title).push().key!!
        database.child("quizes").child(title).child(key).child("question").setValue(question.question)
        database.child("quizes").child(title).child(key).child("answer1").setValue(question.answer1)
        database.child("quizes").child(title).child(key).child("answer2").setValue(question.answer2)
        database.child("quizes").child(title).child(key).child("answer3").setValue(question.answer3)
        database.child("quizes").child(title).child(key).child("answer4").setValue(question.answer4)
        database.child("quizes").child(title).child(key).child("number").setValue(question.number)
        database.child("quizes").child(title).child(key).child("correctAnswer").setValue(question.correctAnswer)
        return key

    }
    fun retrieveQuestions(title: String): LiveData<ArrayList<Question>> {
        var questionList = ArrayList<Question>()
        database.child("quizes").child(title).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.e("123", "Failed to read questions")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.e("123", "Succeded to read questions")

                for(myQuestion in dataSnapshot.children){
                    val question = myQuestion.getValue(Question::class.java)
                    questionList.add(question!!)
                }
                questionsLiveData.value = questionList
                Log.d("123", "${questionList.size}")

            }

        })
        Log.d("123", "Succeded to read ${questionList.size}")
        return questionsLiveData
    }


    companion object{
        fun newInstance(): DatabaseHelper{
            return DatabaseHelper()
        }
    }
}
