package com.event.quiz.ui.learn

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.event.quiz.Question
import com.event.quiz.Quiz
import com.event.quiz.R
private const val ARG_QUIZ_ID = "quiz_id"
private const val ADAPTER_POSITION = "adapter_position"
class LearnFragment : Fragment(){

    private val learnViewModel: LearnViewModel by lazy {
        ViewModelProviders.of(this).get(LearnViewModel::class.java)
    }

    private lateinit var questionListRecycler: RecyclerView
    private lateinit var adapter: QuestionAdapter
    private var adapterPosition : Int = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_learn, container, false)
        questionListRecycler = view.findViewById(R.id.question_recycler_view)
        questionListRecycler.layoutManager = LinearLayoutManager(context)

        learnViewModel.title=arguments!!.getString(ARG_QUIZ_ID)!!
        learnViewModel.retrieveQuestions()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        learnViewModel.questionsListLiveData.observe(
            viewLifecycleOwner,
            Observer { questions ->
                questions?.let{
                    Log.d("123", "Got crimes ${questions.size}")

                    updateUI(questions)
                    if(savedInstanceState!=null){
                        questionListRecycler.scrollToPosition(savedInstanceState.getInt(ADAPTER_POSITION))

                    }
                }
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ADAPTER_POSITION, adapterPosition)
        Log.d("123", "position $adapterPosition}")
        super.onSaveInstanceState(outState)
    }
    private fun updateUI(questions: ArrayList<Question>){
        adapter = QuestionAdapter(questions)
        questionListRecycler.adapter = adapter
    }

    private inner class QuestionHolder(view: View) : RecyclerView.ViewHolder(view){
        private lateinit var question: Question
        private val questionTextView: TextView = itemView.findViewById(R.id.question)
        private val correctAnswerTextView: TextView = itemView.findViewById(R.id.correct_answer)
        private val questionNumberTextView: TextView = itemView.findViewById(R.id.question_number)

        init {
            //itemView.setOnClickListener(this)
        }

        fun bind(question: Question){
            this.question = question
            questionNumberTextView.text = "Numer pytania: ${question.number}"
            correctAnswerTextView.text = question.correctAnswer
            questionTextView.text = question.question
        }


    }
    inner class QuestionAdapter(var questions: ArrayList<Question>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return QuestionHolder(layoutInflater.inflate(R.layout.list_item_learn_question, parent, false))
        }

        override fun getItemCount(): Int = questions.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val question = questions[position]
            val questionHolder = holder as QuestionHolder
            adapterPosition = holder.adapterPosition
            questionHolder.bind(question)
        }


    }
}