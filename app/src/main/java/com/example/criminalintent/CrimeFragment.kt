package com.example.criminalintent

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Point
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


private const val CRIME_ID = "crimeId"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val DIALOG_PHOTO = "DialogPhoto"
const val DIALOG_DATE_ID = "DialogDateId"
const val DATE_ARG = "DateArg"
private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment : Fragment() {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var checkBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var callButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoImage: ImageView
    private lateinit var photoFile: File
    private lateinit var photoSize: Point
    private lateinit var photoUri: Uri
    private val crimeFragmentViewModel: CrimeFragmentViewModel by lazy {
        ViewModelProvider(this).get(CrimeFragmentViewModel::class.java)
    }
    private val executor = Executors.newSingleThreadExecutor()
    private var liveBitmap: MutableLiveData<Bitmap> = MutableLiveData()     //getting scaled bitmap

    private class ContactsActivityContract: ActivityResultContract<Unit, Uri?>() {
        override fun createIntent(context: Context, input: Unit?): Intent {
            return Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return when (resultCode) {
                Activity.RESULT_OK -> {
                    intent?.data
                }
                else -> null
            }

        }
    }

    //This value is an activityLauncher, that later is launched to start new Activity. It is using contract,
    //that is declaring earlier. Methods of this contracts are calling when Activity is creating and returning results
    private val contactsActivityLauncher = registerForActivityResult(ContactsActivityContract()) { contactUri ->
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)   //fields, that we want to check
        val cursor = if (contactUri != null)       //returns a cursor to the contact list with fields, we have declared earlier
            requireActivity().contentResolver.query(contactUri, queryFields,
            null, null, null)
        else
            null
        cursor?.use {
            if (it.count != 0) {
                it.moveToFirst()      //moving to the first contact(first row)
                val suspect = it.getString(0)  //getting it name(field from 0 column)
                crime.suspect = suspect
                crimeFragmentViewModel.saveCrime(crime)
                callButton.isEnabled = true
            }
        }
    }

    private val imageActivityLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it)
            needPhotoUpdate()
        requireActivity().revokeUriPermission(
            photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.save_crime_menu_item->{
                parentFragmentManager.popBackStack()
                true
            }
            R.id.delete_crime_menu_item->{
                crimeFragmentViewModel.deleteCrime(crime)
                parentFragmentManager.popBackStack()
                true
            }
            else-> super.onContextItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        crime = Crime()
        val uuid: UUID = arguments?.get(CRIME_ID) as UUID
        crimeFragmentViewModel.loadCrime(uuid)
        requireActivity().requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.text_edit) as EditText
        dateButton = view.findViewById(R.id.date_button) as Button
        timeButton = view.findViewById(R.id.time_button) as Button
        checkBox = view.findViewById(R.id.is_solved_checkbox) as CheckBox
        reportButton = view.findViewById(R.id.report_button) as Button
        suspectButton = view.findViewById(R.id.suspect_button) as Button
        callButton = view.findViewById(R.id.call_button) as Button
        photoButton = view.findViewById(R.id.camera_button) as ImageButton
        photoImage = view.findViewById(R.id.crime_photo) as ImageView
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeFragmentViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { crime->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeFragmentViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(
                        requireActivity(),
                        "com.example.criminalintent.fileProvider",
                        photoFile
                    )
                    updateUI()
                }
            }
        )
        photoImage.viewTreeObserver.addOnGlobalLayoutListener(object:ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {         //this code is starting when photoImage's size will exist
                photoSize = Point(photoImage.width, photoImage.height)
                needPhotoUpdate()
                photoImage.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

    }

    @SuppressLint("Range")
    override fun onStart() {
        super.onStart()
        val titleWatcher = object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                crime.title = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }

        checkBox.setOnCheckedChangeListener { _, isSolved ->
            crime.isSolved = isSolved
        }

        titleField.addTextChangedListener(titleWatcher)

        dateButton.setOnClickListener {         //call dialog menu
            DatePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)  //first argument is fragment manager, that will call this dialog-fragment,
                                                                            //second one is id for fragment manager to identify it
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {          //constructor gets running action
                type = "text/plain"                     //type of data
                putExtra(Intent.EXTRA_TEXT, getReport())        //setting text of report
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))      //setting name of intent
            }.also {intent ->
                val intentChooser = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(intentChooser)
            }
        }

        suspectButton.apply {
            val contactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                contactsActivityLauncher.launch(null)
            }
            val packageManager: PackageManager = requireActivity().packageManager   //package manager knows about all existing components
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(   //we are trying to find activity, that is able to do this intent
                contactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null)           //if there is no such activity, user does not have contacts app
                isEnabled = false
        }

        callButton.apply {
            isEnabled = false

            setOnClickListener {
                val cursor = requireActivity().contentResolver.query(    //Opening contact-database to get contact with suspect's name
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    "DISPLAY_NAME = '" + crime.suspect + "'",
                    null,
                    null
                )

                cursor?.use {
                    if (it.count > 0) {
                        cursor.moveToFirst()
                        val suspectId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                        val secondCursor = requireActivity().contentResolver.query(    //Open database with contact's phone
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + suspectId,
                            null,
                            null
                        )
                        secondCursor?.use {secondCursor->
                            if (secondCursor.count > 0) {
                                secondCursor.moveToFirst()
                                val suspectPhone = secondCursor.getString(0)
                                requireActivity().startActivityFromFragment(
                                    this@CrimeFragment,
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$suspectPhone")),  //Starting new Activity to phone suspect
                                    1
                                )
                            }
                        }
                    }
                }
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity = packageManager.resolveActivity(
                captureImage,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            if (resolvedActivity == null) //checking if photo applications exist
                isEnabled = false
            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities = requireActivity().packageManager.queryIntentActivities( //finding all suitable activities
                    captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(       //giving permission to all activities to save photos to this URI
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                imageActivityLauncher.launch(photoUri)
            }
        }

        photoImage.apply {
            setOnClickListener{
                val imageWidth = (requireActivity().windowManager.defaultDisplay.width * 0.9).toInt()
                val imageHeight = (requireActivity().windowManager.defaultDisplay.height * 0.9).toInt()
                PhotoZoomDialog.getInstance(getScaledBitmap(
                    photoFile.path,
                    imageWidth,
                    imageHeight
                    ),
                    getPhotoOrientation()
                ).show(
                    this@CrimeFragment.parentFragmentManager,
                    DIALOG_PHOTO
                )
            }
        }

        parentFragmentManager.setFragmentResultListener(    //subscribing on getting results by id
            DIALOG_DATE_ID,
            viewLifecycleOwner
        ) {_, result->
            crime.date = result.getSerializable(DATE_ARG) as Date
            updateUI()
        }

    }

    override fun onStop() {
        super.onStop()
        crimeFragmentViewModel.saveCrime(crime)
    }

    private fun getReport(): String {  //this function returns a string, which generates based on current crime
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        val suspectString = if (crime.suspect.isBlank())
            getString(R.string.crime_report_no_suspect)    //getString() fills template
        else
            getString(R.string.crime_report_suspect, crime.suspect)

        val isSolvedString = if(crime.isSolved)
            getString(R.string.crime_report_solved)
        else
            getString(R.string.crime_report_unsolved)

        return getString(
            R.string.crime_report,
            crime.title,
            dateString,
            isSolvedString,
            suspectString
        )
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = SimpleDateFormat.getDateInstance().format(crime.date)
        timeButton.text = SimpleDateFormat.getTimeInstance().format(crime.date)
        checkBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()    //delete checkbox animation after getting crime from database
        }
        if (crime.suspect.isNotBlank()) {
            suspectButton.text = crime.suspect
            callButton.isEnabled = true
        }
        liveBitmap.observe(viewLifecycleOwner) {
            photoImage.setImageBitmap(it)
            photoImage.rotation = getPhotoOrientation()
            photoImage.contentDescription = getString(R.string.crime_photo_image_description)
        }
        needPhotoUpdate()
    }

    private fun getPhotoOrientation(): Float {
        requireActivity().contentResolver.notifyChange(photoUri, null)  //notifying about something???
        val exif = ExifInterface(photoFile.absolutePath)  //creating exif file(exif is a standard, allowing to add different info about mediafile)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,  //getting photo orientation
            ExifInterface.ORIENTATION_NORMAL)
        val rotate = when(orientation) {
            ExifInterface.ORIENTATION_ROTATE_270->270
            ExifInterface.ORIENTATION_ROTATE_180->180
            ExifInterface.ORIENTATION_ROTATE_90->90
            else->0
        }
        return rotate.toFloat()
    }

    private fun needPhotoUpdate() {  //this function is changing live data and then bitmap is setting
        if (::photoSize.isInitialized && ::photoFile.isInitialized && photoFile.exists())
            executor.execute{
                liveBitmap.postValue(getScaledBitmap(photoFile.path, photoSize.x, photoSize.y))
            }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(
            photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    companion object {
        fun newInstance(uuid: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(CRIME_ID, uuid)
            }
            val fragment = CrimeFragment()
            fragment.arguments = args      //adding arguments to the fragment, they will be saved during configuration's changing
            return fragment
        }
    }
}