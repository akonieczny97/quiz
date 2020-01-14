package com.event.quiz.ui.addquiz

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.event.quiz.DatabaseHelper
import com.event.quiz.Question
import com.event.quiz.R

class AddQuizFragment: Fragment() {

    interface Callbacks{
        fun goToQuizList()

    }

    private lateinit var titleEditText: EditText
    private lateinit var questionEditText: EditText
    private lateinit var answer2EditText: EditText
    private lateinit var answer3EditText: EditText
    private lateinit var answer4EditText: EditText
    private lateinit var correctAnswerEditText: EditText
    private lateinit var nextQuestionButton: Button
    private lateinit var prevQuestionButton: Button
    private lateinit var endAddingButton: Button
    private lateinit var saveQuestionButton: Button
    private lateinit var questionNumberTextView: TextView
    private var callbacks: Callbacks? = null

    private val addQuizListViewModel : AddQuizViewModel by lazy {
        ViewModelProviders.of(this).get(AddQuizViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_quiz, container, false)
        titleEditText = view.findViewById(R.id.title_text)
        questionEditText = view.findViewById(R.id.question_text)
        answer2EditText = view.findViewById(R.id.answer2_text)
        answer3EditText = view.findViewById(R.id.answer3_text)
        answer4EditText = view.findViewById(R.id.answer4_text)
        correctAnswerEditText = view.findViewById(R.id.correct_answer_text)
        nextQuestionButton = view.findViewById(R.id.add_next_question_button)
        prevQuestionButton = view.findViewById(R.id.prev_question_button)
        endAddingButton = view.findViewById(R.id.end_adding_quiz_button)
        questionNumberTextView = view.findViewById(R.id.question_number_text)
        saveQuestionButton = view.findViewById(R.id.save_question_button)
        updateUi()
        return view
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                 addQuizListViewModel.title= sequence.toString()

            }

            override fun afterTextChanged(s: Editable?) {

            }
        }
        titleEditText.addTextChangedListener(titleWatcher)

        val questionWatcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                addQuizListViewModel.currentQuestion.question = sequence.toString()

            }

            override fun afterTextChanged(s: Editable?) {

            }
        }
        questionEditText.addTextChangedListener(questionWatcher)

        val correctAnswerWatcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                addQuizListViewModel.currentQuestion.correctAnswer = sequence.toString()
                addQuizListViewModel.currentQuestion.answer1 = sequence.toString()

            }

            override fun afterTextChanged(s: Editable?) {

            }
        }
        correctAnswerEditText.addTextChangedListener(correctAnswerWatcher)

        val answer2Watcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                addQuizListViewModel.currentQuestion.answer2 = sequence.toString()

            }

            override fun afterTextChanged(s: Editable?) {

            }
        }
        answer2EditText.addTextChangedListener(answer2Watcher)

        val answer3Watcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                addQuizListViewModel.currentQuestion.answer3 = sequence.toString()

            }

            override fun afterTextChanged(s: Editable?) {

            }
        }
        answer3EditText.addTextChangedListener(answer3Watcher)

        val answer4Watcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                addQuizListViewModel.currentQuestion.answer4 = sequence.toString()

            }

            override fun afterTextChanged(s: Editable?) {

            }
        }
        answer4EditText.addTextChangedListener(answer4Watcher)

        nextQuestionButton.setOnClickListener {
           nextQuestion()
        }

        prevQuestionButton.setOnClickListener {
            prevQuestion()
        }

        endAddingButton.setOnClickListener {
            endAddingQuiz()
        }
        saveQuestionButton.setOnClickListener {
            addQuizListViewModel.saveQuestion()
            nextQuestionButton.isEnabled=true

        }

    }
    private fun updateUi(){
        prevQuestionButton.isEnabled =
        if(addQuizListViewModel.questionNumber==1)false else true

        if(addQuizListViewModel.currentQuestion.id.isBlank())nextQuestionButton.isEnabled=false
        else nextQuestionButton.isEnabled=true

        questionEditText.setText(addQuizListViewModel.currentQuestion.question)
        answer2EditText.setText(addQuizListViewModel.currentQuestion.answer2)
        answer3EditText.setText(addQuizListViewModel.currentQuestion.answer3)
        answer4EditText.setText(addQuizListViewModel.currentQuestion.answer4)
        correctAnswerEditText.setText(addQuizListViewModel.currentQuestion.correctAnswer)
        questionNumberTextView.text ="Pytanie nr ${addQuizListViewModel.questionNumber}"
    }

    private fun nextQuestion(){
        if(addQuizListViewModel.questionNumber==1){
            titleEditText.visibility = View.GONE
        }
        addQuizListViewModel.goToNextQuestion()
        updateUi()
    }

    private fun prevQuestion(){
        addQuizListViewModel.goToPrevQuestion()
        updateUi()
    }

    private fun endAddingQuiz(){
        addQuizListViewModel.saveQuestion()
        callbacks?.goToQuizList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

}