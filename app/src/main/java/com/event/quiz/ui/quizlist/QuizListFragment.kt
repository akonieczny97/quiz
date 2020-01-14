package com.event.quiz.ui.quizlist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.event.quiz.Quiz
import com.event.quiz.R
import java.util.*
import androidx.lifecycle.Observer
import com.event.quiz.DatabaseHelper
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import kotlin.collections.ArrayList

private const val TAG = "QuizListFragment"
class QuizListFragment : Fragment() {



    interface Callbacks{
        fun onQuizSelected(quizId: String)
        fun goToAddingQuestion()
    }

    private val quizListViewModel :QuizListViewModel by lazy {
        ViewModelProviders.of(this).get(QuizListViewModel::class.java)
    }

    private var callbacks: Callbacks? = null
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var noQuizText: TextView
    private lateinit var quizRecyclerView: RecyclerView
    private lateinit var adapter: QuizAdapter
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quiz_list, container, false)
        quizRecyclerView = view.findViewById(R.id.quiz_recycler_view) as RecyclerView
        quizRecyclerView.layoutManager = LinearLayoutManager(context)
        noQuizText = view.findViewById(R.id.no_quiz)
        progressBar = view.findViewById(R.id.progress)
        databaseHelper=quizListViewModel.database
        databaseHelper.open()

        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        quizListViewModel.quizListLiveData.observe(
            viewLifecycleOwner,
            Observer { quizes ->
                quizes?.let{
                    Log.d("123", "Got crimes ${quizes.size}")
                    updateUI(quizes)
                }
            }
        )
    }
    override fun onStart() {
        super.onStart()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_quiz_list, menu)
        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply{
            setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.d("123", "QueryTextSubmit: $query")
                    quizListViewModel.fetchQuizes(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })
            setOnSearchClickListener {
                searchView.setQuery(quizListViewModel.searchTerm, false)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_quiz -> {
                callbacks?.goToAddingQuestion()
                true
            }
            R.id.menu_item_clear -> {
                quizListViewModel.fetchQuizes()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(quizes: ArrayList<String>){

        adapter = QuizAdapter(quizes)
        quizRecyclerView.adapter = adapter

        if(adapter.quizes.isNotEmpty()){
            noQuizText.visibility = View.GONE
            progressBar.visibility = View.GONE
            quizRecyclerView.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Got ${adapter.quizes.size}", Toast.LENGTH_SHORT).show()
        }
        else
        {
            noQuizText.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            quizRecyclerView.visibility = View.GONE
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private inner class QuizHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        private lateinit var quiz: String
        private val titleTextView: TextView = itemView.findViewById(R.id.title_id)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(quiz: String){
            this.quiz = quiz
            titleTextView.text = quiz
        }

        override fun onClick(v: View?) {
            callbacks?.onQuizSelected(quiz)
        }

    }
    inner class QuizAdapter(var quizes: ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return QuizHolder(layoutInflater.inflate(R.layout.list_item_quiz, parent, false))
        }

        override fun getItemCount(): Int = quizes.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val quiz = quizes[position]
            val quizHolder = holder as QuizHolder
            quizHolder.bind(quiz)
        }


    }
}