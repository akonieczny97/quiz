package com.event.quiz.com.event.quiz.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.event.quiz.DrawerActivity
import com.event.quiz.R
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    interface Callbacks {
        fun changeToRegisterFragment()
    }
    private var callbacks: Callbacks? = null

    private lateinit var emailTextView: EditText
    private lateinit var passwordTextView: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var loginButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        emailTextView = view.findViewById(R.id.email)
        passwordTextView = view.findViewById(R.id.password)
        progressBar = view.findViewById(R.id.progressBar)
        loginButton = view.findViewById(R.id.btn_login)

        return view

    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.GONE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    companion object{
        fun newInstance(): LoginFragment{
            return LoginFragment()
        }
    }


    override fun onStart() {
        super.onStart()
        if(auth.currentUser!=null){
            startActivity(Intent(requireContext(), DrawerActivity::class.java))
        }

        loginButton.setOnClickListener {

            var emailInput = emailTextView.text.toString()
            val passwordInput = passwordTextView.text.toString()

            if(TextUtils.isEmpty(emailInput)){
                Toast.makeText(requireContext(), "Enter email!", Toast.LENGTH_SHORT).show()

            }
            if(TextUtils.isEmpty(passwordInput)){
                Toast.makeText(requireContext(), "Enter password!", Toast.LENGTH_SHORT).show()
            }
            progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(requireActivity()){ task ->
                    progressBar.visibility= View.GONE
                    if(!task.isSuccessful){
                        Toast.makeText(requireContext(), "Nie udadalo sie zalogowac", Toast.LENGTH_SHORT).show()

                    }
                    else {
                        startActivity(Intent(requireContext(), DrawerActivity::class.java))
                    }

                }
        }

    }



}