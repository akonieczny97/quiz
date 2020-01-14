package com.event.quiz.ui.mock
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.event.quiz.R
import com.event.quiz.ui.choiceGameMode.GameModeFragment
import com.facebook.share.widget.ShareDialog.show
import java.util.*

private const val ARG_QUIZ_ID = "quiz_id"
private const val ARG_SCORE = "score"
private const val ARG_HIGHSCORE = "highscore"
private const val REQUEST_SCORE = 0
private const val DIALOG_SCORE = "score"
private const val ARG_IS_TIME_ENDED = "is_time_ended"




class MockExamFragment : Fragment(), ScoreDialogFragment.Callbacks {
    override fun onOptionSelected(which: Int) {
        Log.d("123", "jeszcze")
        //if(which==0)callbacks?.backToGameModes(mockExamViewModel.title)
        //else reset()
    }
    interface Callbacks{
        fun backToGameModes(quizId: String)
    }

    private lateinit var questionNumberTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var answer1RadioButton: RadioButton
    private lateinit var answer2RadioButton: RadioButton
    private lateinit var answer3RadioButton: RadioButton
    private lateinit var answer4RadioButton: RadioButton
    private lateinit var answersRadioGroup: RadioGroup
    private lateinit var nextQuestionButton: ImageButton
    private lateinit var checkAnswerButton: ImageButton
    private lateinit var progressBar: ProgressBar
    //private var callbacks: Callbacks? = null

    private val mockExamViewModel: MockExamViewModel by lazy {
        ViewModelProviders.of(this).get(MockExamViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mock, container, false)
        questionNumberTextView = view.findViewById(R.id.question_number)
        questionTextView = view.findViewById(R.id.question)
        answer1RadioButton = view.findViewById(R.id.answer1_radio)
        answer2RadioButton = view.findViewById(R.id.answer2_radio)
        answer3RadioButton = view.findViewById(R.id.answer3_radio)
        answer4RadioButton = view.findViewById(R.id.answer4_radio)
        answersRadioGroup = view.findViewById(R.id.answer_group)
        nextQuestionButton = view.findViewById(R.id.next_question_button)
        checkAnswerButton = view.findViewById(R.id.check_answer_button)
        mockExamViewModel.title = arguments!!.getString(ARG_QUIZ_ID)!!
        mockExamViewModel.retrieveQuestions()

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        callbacks = context as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
      //  callbacks = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mockExamViewModel.questionsListLiveData.observe(
            viewLifecycleOwner,
            Observer { questions ->
                questions?.let {
                    Log.d("123", "Got questions ${questions.size}")
                    if (questions.size > 0){
                        reset()
                        makeVisible()
                    }


                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        nextQuestionButton.setOnClickListener {
            nextQuestion()
        }

        checkAnswerButton.setOnClickListener {
           checkAnswer()
            nextQuestionButton.isEnabled = true
            checkAnswerButton.isEnabled = false
            for(radioButton in answersRadioGroup.children) radioButton.isClickable = false
        }
    }

    private fun makeVisible() {
        questionNumberTextView.visibility = View.VISIBLE
        questionTextView.visibility = View.VISIBLE
        answer1RadioButton.visibility = View.VISIBLE
        answer2RadioButton.visibility = View.VISIBLE
        answer3RadioButton.visibility = View.VISIBLE
        answer4RadioButton.visibility = View.VISIBLE
        answersRadioGroup.visibility = View.VISIBLE
        nextQuestionButton.visibility = View.VISIBLE
        checkAnswerButton.visibility = View.VISIBLE


    }


    private fun nextQuestion() {

        if (mockExamViewModel.isItEnd()) {
            /*ScoreDialogFragment.newInstance(examViewModel.points.toString(), examViewModel.questionRange.toString(), 0).apply {
                setTargetFragment(this@ExamFragment, REQUEST_SCORE)
                show(this@ExamFragment.requireFragmentManager(), DIALOG_SCORE)
            }*/
        }
        else
        {
            mockExamViewModel.nextQuestion()
            updateUI()
        }

    }

    fun checkAnswer(){
        val choosenRadio = when (answersRadioGroup.checkedRadioButtonId) {

            R.id.answer1_radio -> answer1RadioButton
            R.id.answer2_radio -> answer2RadioButton
            R.id.answer3_radio -> answer3RadioButton
            R.id.answer4_radio -> answer4RadioButton
            else -> answer1RadioButton
        }
        if(mockExamViewModel.checkAnswer(choosenRadio.text.toString())){
            choosenRadio.setBackgroundColor(Color.GREEN)
        }else{
            choosenRadio.setBackgroundColor(Color.RED)
            when(mockExamViewModel.currentQuestion.correctAnswer){
                answer1RadioButton.text -> answer1RadioButton.setBackgroundColor(Color.GREEN)
                answer2RadioButton.text -> answer2RadioButton.setBackgroundColor(Color.GREEN)
                answer3RadioButton.text -> answer3RadioButton.setBackgroundColor(Color.GREEN)
                answer4RadioButton.text -> answer4RadioButton.setBackgroundColor(Color.GREEN)
            }
        }

    }
    fun reset(){
        mockExamViewModel.makeQuestions()
        updateUI()
    }

    private fun updateUI() {
        var answersList = mockExamViewModel.returnAnswers()
        Log.d("123", "answer ${mockExamViewModel.currentQuestion.answer1}")
        answer1RadioButton.text = answersList[0]
        answer2RadioButton.text = answersList[1]
        answer3RadioButton.text = answersList[2]
        answer4RadioButton.text = answersList[3]
        questionTextView.text = mockExamViewModel.currentQuestion.question
        questionNumberTextView.text = "Pytanie: ${mockExamViewModel.questionNumber} / ${mockExamViewModel.questionRange}"
        answersRadioGroup.clearCheck()
        nextQuestionButton.isEnabled = false
        for(radioButton in answersRadioGroup.children) radioButton.isClickable = true
        checkAnswerButton.isEnabled = true
        answer1RadioButton.setBackgroundColor(Color.WHITE)
        answer2RadioButton.setBackgroundColor(Color.WHITE)
        answer3RadioButton.setBackgroundColor(Color.WHITE)
        answer4RadioButton.setBackgroundColor(Color.WHITE)
    }


}
class ScoreDialogFragment: DialogFragment(){


    interface Callbacks {
        fun onOptionSelected(which: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val score = arguments?.getSerializable(ARG_SCORE) as String
        val highScore = arguments?.getSerializable(ARG_HIGHSCORE) as String
        val timeLeft = arguments?.getSerializable(ARG_IS_TIME_ENDED) as Int
        val builder = AlertDialog.Builder(activity)
        builder.apply {
            setTitle("Score")
            setMessage("${if(timeLeft==1) "TIME'S PASSED\n" else ""}Your Score: $score / $highScore")
            setNegativeButton("Koniec") { dialog, which ->
                targetFragment?.let { fragment ->
                    (fragment as Callbacks).onOptionSelected(0)
                }
            }
            setPositiveButton("Jeszcze raz") { dialog, which ->
                targetFragment?.let { fragment ->
                    (fragment as Callbacks).onOptionSelected(1)
                }
            }
        }

        return builder.create()

    }


    companion object {
        fun newInstance(score: String, highScore: String, option: Int): ScoreDialogFragment {
            val args = Bundle().apply {
                putSerializable(ARG_SCORE, score)
                putSerializable(ARG_HIGHSCORE, highScore)
                putSerializable(ARG_IS_TIME_ENDED, option)
            }
            return ScoreDialogFragment().apply {
                arguments = args
            }
        }
    }
}

