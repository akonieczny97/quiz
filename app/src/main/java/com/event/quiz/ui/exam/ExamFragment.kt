package com.event.quiz.ui.exam

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.event.quiz.R
import com.event.quiz.ui.choiceGameMode.GameModeFragment
import com.facebook.share.widget.ShareDialog.show
import com.google.firebase.auth.FirebaseAuth
import java.util.*

private const val ARG_QUIZ_ID = "quiz_id"
private const val ARG_SCORE = "score"
private const val ARG_HIGHSCORE = "highscore"
private const val REQUEST_SCORE = 0
private const val DIALOG_SCORE = "score"
private const val ARG_IS_TIME_ENDED = "is_time_ended"




class ExamFragment : Fragment(), ScoreDialogFragment.Callbacks {
    override fun onOptionSelected(which: Int) {
        Log.d("123", "jeszcze")
        if(which==0)callbacks?.backToGameModes(examViewModel.title)
        else reset()
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
    private lateinit var timerTextView: TextView
    private lateinit var nextQuestionButton: ImageButton
    private lateinit var timer: CountDownTimer
    private lateinit var callButton: ImageButton
    private lateinit var helpButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private var callbacks: Callbacks? = null

    private val examViewModel: ExamViewModel by lazy {
        ViewModelProviders.of(this).get(ExamViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exam, container, false)
        callButton = view.findViewById(R.id.call_button)
        helpButton = view.findViewById(R.id.help_button)
        questionNumberTextView = view.findViewById(R.id.question_number)
        questionTextView = view.findViewById(R.id.question)
        answer1RadioButton = view.findViewById(R.id.answer1_radio)
        answer2RadioButton = view.findViewById(R.id.answer2_radio)
        answer3RadioButton = view.findViewById(R.id.answer3_radio)
        answer4RadioButton = view.findViewById(R.id.answer4_radio)
        answersRadioGroup = view.findViewById(R.id.answer_group)
        timerTextView = view.findViewById(R.id.timer)
        nextQuestionButton = view.findViewById(R.id.next_question_button)
        examViewModel.title = arguments!!.getString(ARG_QUIZ_ID)!!
        auth = FirebaseAuth.getInstance()
        examViewModel.retrieveQuestions()
        examViewModel.getUser(auth!!.currentUser!!.uid)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        examViewModel.questionsListLiveData.observe(
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
        examViewModel.userLiveData.observe(
            viewLifecycleOwner,
            Observer { questions ->
                questions?.let {
                    Log.d("123", "Got friend number")
                    setUpCallButton(it.friendNumber)
                }
            }
        )
    }

    fun setUpCallButton(number: String){
        callButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_DIAL).apply{
                data = Uri.parse("tel:${number}")
            }
            setOnClickListener {
                Log.d("QUERY", number)
                pickContactIntent.data = Uri.parse("tel:${number}")
                startActivity(pickContactIntent)
            }
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)

            if(resolvedActivity == null || number.isBlank()){
                Log.d("QUERY", "INVISIBLE: ${number}")
                isEnabled = false
            }
        }
    }
    override fun onStart() {
        super.onStart()
        nextQuestionButton.setOnClickListener {
            nextQuestion()
        }
        answersRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            nextQuestionButton.isEnabled = true
        }
        helpButton.setOnClickListener {
            make50Answer()
        }

    }

    fun make50Answer(){

        val correctAnswerNumber = when(examViewModel.currentQuestion.correctAnswer){
            answer1RadioButton.text -> 1
            answer2RadioButton.text -> 2
            answer3RadioButton.text -> 3
            else -> 4
        }

        val rand = Random()
        var answerNumber = rand.nextInt(4) + 1
        while(answerNumber == correctAnswerNumber)answerNumber = rand.nextInt(4) + 1

        if(answerNumber !=1 && correctAnswerNumber != 1)answer1RadioButton.isEnabled = false
        if(answerNumber !=2 && correctAnswerNumber != 2)answer2RadioButton.isEnabled = false
        if(answerNumber !=3 && correctAnswerNumber != 3)answer3RadioButton.isEnabled = false
        if(answerNumber !=4 && correctAnswerNumber != 4)answer4RadioButton.isEnabled = false

        /*var number=0

        if(examViewModel.currentQuestion.correctAnswer != answer1RadioButton.text && number<2){
            number++
            answer1RadioButton.isEnabled = false
        }
        if(examViewModel.currentQuestion.correctAnswer != answer2RadioButton.text && number<2){
            answer2RadioButton.isEnabled = false
            number++
        }

        if(examViewModel.currentQuestion.correctAnswer != answer3RadioButton.text && number<2){
            answer3RadioButton.isEnabled = false
            number++
        }
        if(examViewModel.currentQuestion.correctAnswer != answer4RadioButton.text && number<2){
            answer4RadioButton.isEnabled = false
        }*/
    }
    fun setEnable(){
        answer1RadioButton.isEnabled = true
        answer2RadioButton.isEnabled = true
        answer3RadioButton.isEnabled = true
        answer4RadioButton.isEnabled = true
    }
    private fun makeVisible() {
        questionNumberTextView.visibility = View.VISIBLE
        questionTextView.visibility = View.VISIBLE
        answer1RadioButton.visibility = View.VISIBLE
        answer2RadioButton.visibility = View.VISIBLE
        answer3RadioButton.visibility = View.VISIBLE
        answer4RadioButton.visibility = View.VISIBLE
        answersRadioGroup.visibility = View.VISIBLE
        timerTextView.visibility = View.VISIBLE
        nextQuestionButton.visibility = View.VISIBLE


    }

    fun startTimer(){
        timer = object: CountDownTimer(examViewModel.timeLeft, 1000){
            override fun onFinish() {
                Log.d("123", "Timer end")
                ScoreDialogFragment.newInstance(examViewModel.points.toString(), examViewModel.questionRange.toString(), 1).apply {
                    setTargetFragment(this@ExamFragment, REQUEST_SCORE)
                    show(this@ExamFragment.requireFragmentManager(), DIALOG_SCORE)
                }
                stopTimer()
            }

            override fun onTick(millisUntilFinished: Long) {
                examViewModel.timeLeft=millisUntilFinished
                Log.d("123", "Tick: ${examViewModel.timeLeft} left")
                updateTimerUI()
            }

        }.start()
    }
    fun stopTimer(){
        timer.cancel()
    }
    fun updateTimerUI(){
        val minutes = ((examViewModel.timeLeft/1000)%3600)/60
        val sec =(examViewModel.timeLeft/1000)%60

        timerTextView.text = "$minutes:${if(sec<10)"0$sec" else sec}"
    }
    private fun nextQuestion() {
        val choosenAnswer = when (answersRadioGroup.checkedRadioButtonId) {

            R.id.answer1_radio -> answer1RadioButton.text
            R.id.answer2_radio -> answer2RadioButton.text
            R.id.answer3_radio -> answer3RadioButton.text
            R.id.answer4_radio -> answer4RadioButton.text
            else -> ""
        }
        examViewModel.checkAnswer(choosenAnswer.toString())
        if (examViewModel.isItEnd()) {
            stopTimer()
            ScoreDialogFragment.newInstance(examViewModel.points.toString(), examViewModel.questionRange.toString(), 0).apply {
                setTargetFragment(this@ExamFragment, REQUEST_SCORE)
                show(this@ExamFragment.requireFragmentManager(), DIALOG_SCORE)
            }

        }
        else
        {
            examViewModel.nextQuestion()
            setEnable()
            updateUI()


        }

    }

    fun reset(){
        examViewModel.makeQuestions()
        startTimer()
        updateUI()
    }

    private fun updateUI() {
        var answersList = examViewModel.returnAnswers()
        Log.d("123", "answer ${examViewModel.currentQuestion.answer1}")
        answer1RadioButton.text = answersList[0]
        answer2RadioButton.text = answersList[1]
        answer3RadioButton.text = answersList[2]
        answer4RadioButton.text = answersList[3]
        questionTextView.text = examViewModel.currentQuestion.question
        questionNumberTextView.text = "Pytanie: ${examViewModel.questionNumber} / ${examViewModel.questionRange}"
        answersRadioGroup.clearCheck()
        nextQuestionButton.isEnabled = false
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

