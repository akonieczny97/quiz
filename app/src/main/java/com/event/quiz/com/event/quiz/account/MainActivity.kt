package com.event.quiz.com.event.quiz.account

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.event.quiz.R

class MainActivity : AppCompatActivity(), LoginFragment.Callbacks, RegisterFragment.Callbacks {
    override fun changeToRegisterFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, RegisterFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    override fun changeToLoginFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, LoginFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if(currentFragment == null){
            val fragment = RegisterFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }

    }

}
