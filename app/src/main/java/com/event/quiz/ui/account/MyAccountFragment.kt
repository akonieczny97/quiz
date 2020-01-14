package com.event.quiz.ui.account

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.event.quiz.ImageFragment

import com.event.quiz.R
import com.event.quiz.com.event.quiz.account.MainActivity
import com.event.quiz.getScaledBitmap
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

private const val DIALOG_ZOOM = "DialogZoom"
private const val REQUEST_CHANGES = 0
private const val REQUEST_PHOTO = 1
private const val REQUEST_ZOOM = 3
private const val STORAGE_PERMISSION_CODE = 4
private const val READ_CONTACTS_PERMISSION_CODE = 5
private const val REQUEST_GALLERY = 2
private const val REQUEST_CONTACT = 6
private const val DIALOG_CHANGES = "change_data"
class MyAccountFragment : Fragment(){

    interface Callbacks{
        fun changeAccountData(accountUid: String)
    }
    private var photoFile: File? = null
    private lateinit var photoUri: Uri
    private lateinit var profilePhotoView: ImageView
    private lateinit var makePhotoButton: ImageButton
    private lateinit var choosePhotoButton: Button
    private lateinit var changeAccountDataButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView
    private lateinit var nameText: TextView
    private lateinit var surnameText: TextView
    private lateinit var friendButton: Button
    private var auth: FirebaseAuth? = null
    private var callbacks: Callbacks? = null
    private lateinit var filesDir : File
    private val myAccountViewModel : MyAccountViewModel by lazy {
        ViewModelProviders.of(this).get(MyAccountViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_account, container, false)
        makePhotoButton = view.findViewById(R.id.make_image_button)
        choosePhotoButton = view.findViewById(R.id.choose_photo_button)
        usernameText = view.findViewById(R.id.username)
        nameText = view.findViewById(R.id.name)
        surnameText = view.findViewById(R.id.surname)
        emailText = view.findViewById(R.id.email)
        profilePhotoView = view.findViewById(R.id.profile_photo)
        changeAccountDataButton = view.findViewById(R.id.change_account_data_button)
        deleteAccountButton = view.findViewById(R.id.delete_account_button)
        friendButton = view.findViewById(R.id.friend_number_button)
        auth = FirebaseAuth.getInstance()
        myAccountViewModel.databaseHelper.getUser(auth!!.currentUser!!.uid)
        downloadImage()
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        filesDir = context.applicationContext.filesDir
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_GALLERY  && data!= null-> {
                val imageUri = data.data
                CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(requireContext(), this)
            }
            requestCode == REQUEST_PHOTO ->{
                val imageBitmap = data!!.extras!!.get("data") as Bitmap

                CropImage.activity(getImageUri(requireContext(), imageBitmap))
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(requireContext(), this)
                profilePhotoView.setImageBitmap(imageBitmap)
                uploadImageAndSaveUri(imageBitmap)
            }

            requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ->{
                val result = CropImage.getActivityResult(data)
                Log.d("12345", "cropped photo")

                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, result.uri)
                profilePhotoView.setImageBitmap(bitmap)
                uploadImageAndSaveUri(bitmap)
            }

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                //Specify which fields you want your query to return values for
                val queryFields =
                    arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.NAME_RAW_CONTACT_ID)
                //Perform your query - the contactUri is like a "where" clause here
                val cursor = contactUri?.let {
                    requireActivity().contentResolver
                        .query(it, queryFields, null, null, null)
                }

                cursor?.use {
                    //verify cursor contains at least one result
                    if (it.count == 0) {
                        return
                    }

                    //Pull out the first column of the first row of data -
                    //that is your suspect's name
                    it.moveToFirst()
                    val friend = it.getString(0)
                    val id = it.getString(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID))
                    setPhoneNumber(id)

                    friendButton.text = friend

                }

            }
        }
    }
    private fun setPhoneNumber(numberId: String){
        val contentResolver = requireActivity().contentResolver
        val cursorNumber = contentResolver
            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null ,null ,null , null)

        // Log.d("QUERY", id)
        cursorNumber?.use {
            if (it.count == 0) {
                return
            }
            while(it.moveToNext()){
                val id = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                if(id==numberId){
                    val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    Log.d("QUERY", "id: $numberId  ID: $id  number: $number")
                    myAccountViewModel.databaseHelper.updateUserFriendNumber(auth!!.currentUser!!.uid, number)
                    break
                }

            }

        }



    }
    fun downloadImage(){
        val localFile = File.createTempFile("pics", "jpg")
        val storageRef = FirebaseStorage.getInstance()
            .reference.child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")
        storageRef.getFile(localFile).addOnSuccessListener(object:OnSuccessListener<FileDownloadTask.TaskSnapshot> {
            override fun onSuccess(p0: FileDownloadTask.TaskSnapshot?) {
                photoFile = localFile
                updatePhotoView()
            }
        })
    }
    private fun updatePhotoView() {
        if(photoFile != null){
            val bitmap = getScaledBitmap(photoFile!!.path, requireActivity())
            profilePhotoView.setImageBitmap(bitmap)
        } else {
            profilePhotoView.setImageDrawable(null)
        }
    }
    fun uploadImageAndSaveUri(bitmap: Bitmap){
        val baos = ByteArrayOutputStream()
        val storageRef = FirebaseStorage.getInstance()
            .reference.child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        val upload = storageRef.putBytes(image)

        upload.addOnCompleteListener {uploadTask ->
            if(uploadTask.isSuccessful){
                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                    urlTask.result?.let{
                        photoUri = it
                        Log.d("12345", photoUri.toString())
                    }
                }
            }else{
                uploadTask.exception?.let{
                    Log.d("12345", it.message)
                }
            }

        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myAccountViewModel.userLiveDate.observe(
            viewLifecycleOwner,
            Observer { user ->
                user?.let {
                    Log.d("123", "Got user")
                    updateUI()
                }
            }
        )
    }
    fun updateUI(){
        val user = myAccountViewModel.userLiveDate.value
        usernameText.text = user!!.username
        nameText.text = user!!.name
        surnameText.text = user!!.surname
        emailText.text = user!!.email
        updatePhotoView()
    }

    fun getImageUri(inContext: Context , inImage : Bitmap ): Uri {

        val outImage = Bitmap.createScaledBitmap(inImage, 1000, 1000,true)
        val path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), outImage, "Title", null)
        return Uri.parse(path)
    }
    fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            AlertDialog.Builder(requireContext())
                .setTitle("Permission needed")
                .setMessage("Permission is needed")
                .setPositiveButton("ok", object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        ActivityCompat.requestPermissions(requireActivity(), Array(1){Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE)
                    }
                })
                .setNegativeButton("cancel", object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        dialog.dismiss()
                    }
                })
                .create().show()
        }else{
            ActivityCompat.requestPermissions(requireActivity(), Array(1){Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when{
            requestCode == STORAGE_PERMISSION_CODE ->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
            }
            requestCode == READ_CONTACTS_PERMISSION_CODE ->{
                if (permissions[0].equals(Manifest.permission.READ_CONTACTS)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContactActivity()
                }
            }
        }

    }
    override fun onStart() {
        super.onStart()
        makePhotoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            setOnClickListener{
                if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    //jest zgoda
                }else{
                    requestStoragePermission()
                }

                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        startActivityForResult(takePictureIntent, REQUEST_PHOTO)
                    }
                }
            }
        }
        choosePhotoButton.setOnClickListener {
            val galleryIntent = Intent().apply{
                action = Intent.ACTION_GET_CONTENT
                type = "image/*"
            }
            startActivityForResult(galleryIntent, REQUEST_GALLERY)
        }
        changeAccountDataButton.setOnClickListener {
            callbacks?.changeAccountData(auth!!.currentUser!!.uid)
        }

        profilePhotoView.setOnClickListener {
            if(photoFile!= null){
                ImageFragment.newInstance(photoFile!!.path).apply {
                    setTargetFragment(this@MyAccountFragment, REQUEST_ZOOM)
                    show(this@MyAccountFragment.requireFragmentManager(), DIALOG_ZOOM)
                }
            }
        }

        deleteAccountButton.setOnClickListener {
            val user = auth!!.currentUser

            user?.delete()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("123", "User account deleted.")
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    }
                }
        }
        friendButton.setOnClickListener {
            requestContactPermission()
        }
    }

    private fun requestContactPermission(){
        if (ActivityCompat.checkSelfPermission(this!!.getContext()!!, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions( //Method of Fragment
                arrayOf(Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_PERMISSION_CODE
            )
        } else {
            startContactActivity()

        }

    }
    private fun startContactActivity(){
        val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(pickContactIntent, REQUEST_CONTACT)
    }

}