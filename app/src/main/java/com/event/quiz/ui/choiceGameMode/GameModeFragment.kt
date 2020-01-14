package com.event.quiz.ui.choiceGameMode

import android.content.Context
import android.os.Bundle
import android.telecom.Call
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.event.quiz.R
import java.util.*

private const val ARG_QUIZ_ID = "quiz_id"
class GameModeFragment : Fragment(){

    private lateinit var quizId: String
    private lateinit var egzaminButton : Button
    private lateinit var probaButton : Button
    private lateinit var bazaButton : Button
    private var callbacks: Callbacks? = null

    interface Callbacks{
        fun goToMode(layoutId: Int, quizId: String)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        quizId = arguments?.getString(ARG_QUIZ_ID)!!
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_game_mode, container, false)
        egzaminButton = view.findViewById(R.id.egzamin_button)
        probaButton = view.findViewById(R.id.proba_button)
        bazaButton = view.findViewById(R.id.baza_button)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks=null
    }
    override fun onStart() {
        super.onStart()

        egzaminButton.setOnClickListener {
            callbacks?.goToMode(R.id.nav_exam_quiz, quizId!!)
        }
        probaButton.setOnClickListener {
            callbacks?.goToMode(R.id.nav_mock_quiz, quizId!!)

        }
        bazaButton.setOnClickListener {
            callbacks?.goToMode(R.id.nav_learn_quiz, quizId!!)
        }
    }

    companion object{
        fun newInstance():GameModeFragment{
            return GameModeFragment()
        }
    }

}