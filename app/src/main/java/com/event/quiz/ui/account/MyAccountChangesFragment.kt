package com.event.quiz.ui.account

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders

import com.google.firebase.auth.FirebaseAuth


import com.google.firebase.auth.UserProfileChangeRequest


import android.util.Log
import com.event.quiz.R
import com.event.quiz.com.event.quiz.account.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task


class MyAccountChangesFragment : Fragment(){

    interface Callbacks{
        fun backToMyAccountFragment()
    }
    private lateinit var usernameText: EditText
    private lateinit var nameText: EditText
    private lateinit var surnameText: EditText
    private lateinit var password1Text: EditText
    private lateinit var password2Text: EditText
    private lateinit var saveChangesButton: Button
    private var auth: FirebaseAuth? = null
    private var callbacks : Callbacks? = null

    private val myAccountViewModel: MyAccountViewModel by lazy {
        ViewModelProviders.of(this).get(MyAccountViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_account_data, container, false)
        usernameText = view.findViewById(R.id.username)
        nameText = view.findViewById(R.id.name)
        surnameText = view.findViewById(R.id.surname)
        password1Text = view.findViewById(R.id.password1)
        password2Text = view.findViewById(R.id.password2)
        saveChangesButton = view.findViewById(R.id.save_changes_button)
        auth = FirebaseAuth.getInstance()
        myAccountViewModel.databaseHelper.getUser(auth!!.currentUser!!.uid)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onStart() {
        super.onStart()
        saveChangesButton.setOnClickListener {
            saveChanges()
        }
    }
    fun saveChanges(){
        var nameInput = nameText.text.toString().trim()
        var surnameInput = surnameText.text.toString().trim()
        var password1Input = password1Text.text.toString().trim()
        var password2Input = password2Text.text.toString().trim()
        var usernameInput = usernameText.text.toString().trim()

        if(password1Input.length < 6){
            Toast.makeText(requireContext(), "Password too short!", Toast.LENGTH_SHORT).show()
            return
        }
        if(password1Input!=password2Input){
            Toast.makeText(requireContext(), "Nie pasują do siebie hasła", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(if(usernameInput.isNotBlank()) usernameInput else currentUser!!.displayName)
            .build()

        currentUser!!.updateProfile(profileUpdates)
            .addOnCompleteListener(object : OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    Log.d("123", "Completed user update")
                }
            })
        currentUser!!.updatePassword(password1Input)
        val user = User(currentUser.uid, "", usernameInput, "no", nameInput, surnameInput)
        myAccountViewModel.databaseHelper.updateUser(user)
        callbacks?.backToMyAccountFragment()

    }
    companion object{
        fun newInstance(): MyAccountChangesFragment{

            return  MyAccountChangesFragment()
        }
    }
}