package com.event.quiz.com.event.quiz.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.event.quiz.DatabaseHelper
import com.event.quiz.DrawerActivity
import com.event.quiz.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_register.*

const val RC_SIGN_IN = 9001
class RegisterFragment: Fragment(), View.OnClickListener {

    private var callbacks: Callbacks? =  null

    private var callbackManager: CallbackManager? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var passwordTextView: EditText
    private lateinit var emailTextView: EditText
    private lateinit var usernameTextView: EditText
    private lateinit var loginTextView: TextView
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var singInGoogleButton: SignInButton
    internal lateinit var googleSignInClient: GoogleSignInClient
    private val databaseHelper = DatabaseHelper()

    interface Callbacks{
        fun changeToLoginFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        databaseHelper.open()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        callbackManager = CallbackManager.Factory.create()


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register,container, false)
        passwordTextView = view.findViewById(R.id.password)
        emailTextView = view.findViewById(R.id.email)
        usernameTextView = view.findViewById(R.id.username)
        loginTextView = view.findViewById(R.id.sign_in_button)
        progressBar = view.findViewById(R.id.progressBar)
        registerButton= view.findViewById(R.id.sign_up_button)
        singInGoogleButton = view.findViewById(R.id.sign_in_google_button)

        singInGoogleButton.setSize(SignInButton.SIZE_WIDE)

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

    override fun onStart() {
        super.onStart()

        sing_in_facebook_button.setReadPermissions("email", "public_profile")
        sing_in_facebook_button.registerCallback(callbackManager, object: FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult) {
                Log.d("FACEBOOK", "facebook:onSuccess")
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                Log.d("FACEBOOK", "facebook:onCancel")            }

            override fun onError(error: FacebookException?) {
                Log.d("FACEBOOK", "facebook:onError")
            }
        })
        loginTextView.setOnClickListener {
            callbacks?.changeToLoginFragment()
        }

        val currentUser = auth.currentUser
        singInGoogleButton.setOnClickListener(this)
        if(currentUser != null){
            startActivity(Intent(requireContext(), DrawerActivity::class.java))
        }


        registerButton.setOnClickListener {
            var emailInput = emailTextView.text.toString().trim()
            var passwordInput = passwordTextView.text.toString().trim()
            var usernameInput = usernameTextView.text.toString().trim()

            if(TextUtils.isEmpty(emailInput)){
                Toast.makeText(requireContext(), "Enter email!", Toast.LENGTH_SHORT).show()

            }
            if(TextUtils.isEmpty(passwordInput)){
                Toast.makeText(requireContext(), "Enter password!", Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(usernameInput)){
                Toast.makeText(requireContext(), "Enter username!", Toast.LENGTH_SHORT).show()

            }
            if(passwordInput.length < 6){
                Toast.makeText(requireContext(), "Password too short!", Toast.LENGTH_SHORT).show()

            }
            progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(requireActivity()){ task ->
                    progressBar.visibility= View.GONE
                    if(!task.isSuccessful){
                        Toast.makeText(requireContext(), "Nie udadalo sie zarejestrowac", Toast.LENGTH_SHORT).show()

                    }
                    else
                    {
                        val user = auth.currentUser
                        databaseHelper.addUser(User(user!!.uid, user.email!!, usernameInput))
                        startActivity(Intent(requireContext(), DrawerActivity::class.java))
                    }
                }
        }

    }

    private fun handleFacebookAccessToken(token: AccessToken){
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()){task ->
                if(task.isSuccessful){
                    val user = auth.currentUser
                    startActivity(Intent(requireContext(), DrawerActivity::class.java))
                    Log.d("FACEBOOK", "Authentication Succeded")
                    Toast.makeText(requireContext(), "Authentication Succeded", Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("FACEBOOK", "Authentication failed")
                    Toast.makeText(requireContext(), "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onClick(v: View) {
        val i = v.id
        if(i == R.id.sign_in_google_button){
            signInToGoogle()
        }
    }

    private fun signInToGoogle(){
        val signIntent = googleSignInClient.signInIntent
        startActivityForResult(signIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager!!.onActivityResult(requestCode,resultCode,data)
        if(requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult<ApiException>(ApiException::class.java)
                Toast.makeText(requireContext(), "Google Sing in Succeeded", Toast.LENGTH_SHORT).show()
                firebaseAuthWithGoogle(account!!)
            }catch(e: ApiException){
                Log.w("GOOGLE", "Google sign in failed", e)
                Toast.makeText(requireContext(), "Google Sing in Failed $e", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()){task ->
                if(task.isSuccessful){
                    val user = auth.currentUser!!
                    val username = user.email!!.substringBefore('@')
                    databaseHelper.addUser(User(user!!.uid, user.email!!, username))
                    startActivity(Intent(requireContext(), DrawerActivity::class.java))
                }
                else{
                    Toast.makeText(requireContext(), "Google failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    companion object{
        fun newInstance(): RegisterFragment{
            return RegisterFragment()
        }
    }
}