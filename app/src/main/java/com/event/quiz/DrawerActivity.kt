package com.event.quiz

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.provider.MediaStore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.TextureView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import com.event.quiz.com.event.quiz.account.MainActivity
import com.event.quiz.com.event.quiz.account.User
import com.event.quiz.ui.account.MyAccountChangesFragment
import com.event.quiz.ui.account.MyAccountFragment
import com.event.quiz.ui.addquiz.AddQuizFragment
import com.event.quiz.ui.choiceGameMode.GameModeFragment
import com.event.quiz.ui.exam.ExamFragment
import com.event.quiz.ui.quizlist.QuizListFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.nav_header_drawer.*
import java.io.File

private const val ARG_QUIZ_ID = "quiz_id"
private const val ARG_USER_ID = "user_id"

class DrawerActivity : AppCompatActivity(), QuizListFragment.Callbacks, AddQuizFragment.Callbacks,
    GameModeFragment.Callbacks, ExamFragment.Callbacks, MyAccountFragment.Callbacks, MyAccountChangesFragment.Callbacks {
    override fun backToMyAccountFragment() {
        navController.navigate(R.id.nav_my_account)
    }

    override fun changeAccountData(accountUid: String) {
        var bundle = Bundle().apply {
            putString(ARG_USER_ID, accountUid)
        }
        navController.navigate(R.id.nav_my_account_settings, bundle)
    }

    override fun backToGameModes(quizId: String) {
        var bundle = Bundle().apply {
            putString(ARG_QUIZ_ID, quizId)
        }
        navController.navigate(R.id.nav_game_mode_quiz, bundle)
    }

    override fun goToMode(layoutId: Int, quizId: String) {
        var bundle = Bundle().apply {
            putString(ARG_QUIZ_ID, quizId)
        }

        navController.navigate(layoutId, bundle)
    }

    override fun goToAddingQuestion() {
        navController.navigate(R.id.nav_add_quiz)
    }

    override fun onQuizSelected(quizId: String) {
        var bundle = Bundle().apply {
            putString(ARG_QUIZ_ID, quizId)
        }
        navController.navigate(R.id.nav_game_mode_quiz, bundle)
    }

    override fun goToQuizList() {
        navController.navigate(R.id.nav_gallery)

    }
    private var auth: FirebaseAuth? = null
    internal lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var emailText: TextView
    private lateinit var usernameText: TextView
    private lateinit var profileImage: ImageView
    private val drawerViewModel : DrawerViewModel by lazy{
        ViewModelProviders.of(this).get(DrawerViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

       /* val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        emailText = navView.getHeaderView(0).findViewById(R.id.email_adress)
        usernameText = navView.getHeaderView(0).findViewById(R.id.username)
        profileImage = navView.getHeaderView(0).findViewById(R.id.profile_photo)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_gallery, R.id.nav_game_mode_quiz, R.id.nav_add_quiz,
                R.id.nav_learn_quiz, R.id.nav_exam_quiz, R.id.nav_mock_quiz,
                R.id.nav_my_account

            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        auth = FirebaseAuth.getInstance()
        downloadImage()
        drawerViewModel.getUser(auth!!.currentUser!!.uid)

        drawerViewModel.userLiveData.observe(
            this,
            Observer { user ->
                user?.let {
                    emailText.text = user.email
                    usernameText.text = user.username
                }
            }
        )
        emailText.text = auth!!.currentUser!!.email
        usernameText.text = auth!!.currentUser!!.displayName
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.drawer, menu)
        return true
    }

    fun downloadImage(){
        val localFile = File.createTempFile("pics", "jpg")
        val storageRef = FirebaseStorage.getInstance()
            .reference.child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")
        storageRef.getFile(localFile).addOnSuccessListener(object:
            OnSuccessListener<FileDownloadTask.TaskSnapshot> {
            override fun onSuccess(p0: FileDownloadTask.TaskSnapshot?) {
                if(localFile != null){
                    val bitmap = getScaledBitmap(localFile!!.path, this@DrawerActivity)
                    profileImage.setImageBitmap(bitmap)
                } else {
                    profileImage.setImageDrawable(null)
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        /*photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            if(resolvedActivity == null)
                isEnabled = false

            setOnClickListener{
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

                for(cameraActivity in cameraActivities){
                    requireActivity().grantUriPermission(cameraActivity.activityInfo.packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }*/

       /* profileImage.setOnClickListener {
            if(photoFile.exists()){
                ImageFragment.newInstance(photoFile.path).apply {
                    setTargetFragment(this@CrimeFragment, REQUEST_ZOOM)
                    show(this@CrimeFragment.requireFragmentManager(), DIALOG_ZOOM)
                }
            }

        }*/
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                if(auth?.currentUser != null){
                    auth?.signOut()
                    googleSignInClient.signOut()
                    googleSignInClient.revokeAccess()
                    startActivity(Intent(this, MainActivity::class.java))
                }
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}
