package nl.stekkinger.nizi.activities.doctor

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_doctor_main.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.BaseActivity
import nl.stekkinger.nizi.adapters.PatientAdapter
import nl.stekkinger.nizi.adapters.PatientAdapterListener
import nl.stekkinger.nizi.classes.LocalDb
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientItem
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.PatientRepository
import java.util.*


class DoctorMainActivity : BaseActivity(), AdapterView.OnItemSelectedListener  {

    private var TAG = "DoctorMain"

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"

    // For activity result
    private val ADD_PATIENT_REQUEST_CODE = 0

    //region Repositories
    private val authRepository: AuthRepository = AuthRepository()
    private val patientRepository: PatientRepository = PatientRepository()
    //endregion

    private lateinit var user: UserLogin

    private var patientList: MutableList<PatientItem> = arrayListOf()
    private var filteredList: MutableList<PatientItem> = arrayListOf()
    private var doctorId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /// setup UI
        setContentView(R.layout.activity_doctor_main)

        // Set toolbar
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.patients)
        loader = activity_doctor_main_loader

        // Setup custom toast
        val parent: RelativeLayout = activity_doctor_main_rl
        toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
        parent.addView(toastView)

        // Get User and doctorId
        user = GeneralHelper.getUser()
        doctorId = GeneralHelper.getDoctorId()

        activity_doctor_main_et_searchPatients.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(activity_doctor_main_et_searchPatients.text.toString())
            }
        })

        // Add patient button
        activity_doctor_main_btn_addPatient.setOnClickListener {
            doAddPatient()
        }
        activity_doctor_main_txt_patient.setOnClickListener {
            doAddPatient()
        }

        // Check internet connection
        if (!GeneralHelper.hasInternetConnection(this, toastView, toastAnimation)) return

        // Get patients
        if (GeneralHelper.isAdmin()) {
            getPatientsForDoctorMockup(); return
        }

        getPatientsForDoctorAsyncTask().execute()
    }

    //region Toolbar
    // Inflates toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId)
        {
            R.id.menu_toolbar_logout -> {
                authRepository.logout(this,this)
            }
        }
        return true
    }
    //endregion

    //region Needed for AdapterView.onSelectedItemListener
    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
         parent.getItemAtPosition(pos)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }
    //endregion

    private fun setupRecyclerView() {

        // Listener for recycleview
        val listener = object: PatientAdapterListener
        {
            override fun onItemClick(position: Int) {
                // Open detail page when clicked
                val intent = Intent(this@DoctorMainActivity, PatientDetailActivity::class.java)

                // Prevents duplicating activity
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                intent.putExtra(GeneralHelper.EXTRA_PATIENT, patientList[position])
                intent.putExtra(EXTRA_DOCTOR_ID, doctorId)
                startActivity(intent)
            }
        }

        // Create adapter (data)
        activity_doctor_main_rv.adapter = PatientAdapter(this, filteredList, listener)

        // Create Linear Layout Manager
        activity_doctor_main_rv.layoutManager = LinearLayoutManager(this)

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(activity_doctor_main_rv)
    }

    //region SwipeToDelete
    private val itemTouchHelperCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
    {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped( viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val patient = filteredList[viewHolder.adapterPosition]

            val builder = AlertDialog.Builder(this@DoctorMainActivity)
            builder.setTitle(getString(R.string.delete_patient))
            builder.setMessage(getString(R.string.delete_patient_prompt, patient.first_name))

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                if (GeneralHelper.isAdmin()) {
                    deletePatientAndUserMockup(patient.patient_id)
                } else {
                    deletePatientAsyncTask(patient.patient_id).execute()
                }
                filteredList.removeAt(viewHolder.adapterPosition)
                activity_doctor_main_rv.adapter!!.notifyDataSetChanged()
            }

            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                // Put patient back
                activity_doctor_main_rv.adapter!!.notifyDataSetChanged()
            }

            builder.show()
        }
    }
    //endregion

    // region Filter
    fun filter(text: String) {
        var text = text

        filteredList.clear()

        if (text.isEmpty()) {
            filteredList.addAll(patientList)
        } else {
            text = text.toLowerCase(Locale.getDefault())
            for (item in patientList) {
                if (item.name.toLowerCase(Locale.getDefault()).contains(text)) {
                    filteredList.add(item)
                }
            }
        }
        if (activity_doctor_main_rv.adapter != null)
            activity_doctor_main_rv.adapter!!.notifyDataSetChanged()
    }
    //endregion

    private fun doAddPatient()
    {
        val intent = Intent(this@DoctorMainActivity, AddPatientActivity::class.java)

        // Prevents multiple activities
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        intent.putExtra(EXTRA_DOCTOR_ID, doctorId)
        startActivityForResult(intent, ADD_PATIENT_REQUEST_CODE)
    }

    //region getPatients
    inner class getPatientsForDoctorAsyncTask() : AsyncTask<Void, Void, ArrayList<Patient>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<Patient>? {
            return try {
                patientRepository.getPatientsForDoctor(doctorId)
            }  catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: ArrayList<Patient>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.api_is_down)); return }
            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.get_patients_fail));
                return }

            // Feedback
            GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.get_patients_success))

            //GeneralHelper.createToast(baseContext, customToastLayout, getString(R.string.get_patients_success))

            // Clear
            patientList.clear()
            filteredList.clear()
            
            // Fill
            (0 until result.count()).forEach {
                if (result[it].user == null) return@forEach

                val pi = PatientItem(
                    it + 1,
                    result[it].user!!.first_name + " " + result[it].user!!.last_name,
                    result[it].user!!.first_name,
                    result[it].user!!.last_name,
                    result[it].date_of_birth,
                    result[it].gender,
                    result[it].user!!.username,
                    result[it].user!!.email,
                    result[it].user!!.role,
                    result[it].id!!,
                    result[it].doctor.id!!
                )
                patientList.add(pi)
            }

            filteredList.addAll(patientList)

            setupRecyclerView()
        }
    }
    //endregion

    //region deletePatient
    inner class deletePatientAsyncTask(val patientId: Int) : AsyncTask<Void, Void, Patient>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Patient? {
            return try {
                patientRepository.deletePatient(patientId)
            }  catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: Patient?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.api_is_down)); return }
            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.patient_delete_fail))
                return }

            // Feedback
            GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.patient_deleted))

            deleteUserAsyncTask(result.user!!.id).execute()
        }
    }
    //endregion

    //region deleteUser
    inner class deleteUserAsyncTask(val userId: Int) : AsyncTask<Void, Void, UserLogin>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): UserLogin? {
            return try {
                authRepository.deleteUser(userId)
            }  catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: UserLogin?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.api_is_down)); return }
            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.user_delete_fail));
                return }

            // Feedback
            //Toast.makeText(baseContext, R.string.user_deleted, Toast.LENGTH_SHORT).show()
        }
    }
    //endregion

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_PATIENT_REQUEST_CODE && resultCode == RESULT_OK) {
            // refresh activity (recyclerview)
            recreate()
        }
    }

    //region HideKeyboard function
    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    //endregion

    //region Mockups
    private fun getPatientsForDoctorMockup() {
        // Feedback
        GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.get_patients_success))

        // Clear
        patientList.clear()
        filteredList.clear()

        val _patients = LocalDb.patients

        // Fill
        (0 until _patients.count()).forEach {
            if (_patients[it].user == null) return@forEach

            val pi = PatientItem(
                it + 1,
                _patients[it].user!!.first_name + " " + LocalDb.patients[it].user!!.last_name,
                _patients[it].user!!.first_name,
                _patients[it].user!!.last_name,
                _patients[it].date_of_birth,
                _patients[it].gender,
                _patients[it].user!!.username,
                _patients[it].user!!.email,
                _patients[it].user!!.role,
                _patients[it].id!!,
                _patients[it].doctor.id!!
            )
            patientList.add(pi)
        }

        filteredList.addAll(patientList)

        setupRecyclerView()
    }

    private fun deletePatientAndUserMockup(patientId: Int) {
        val deletedPatient = patientRepository.deletePatientLocally(patientId)
        authRepository.deleteUserLocally(deletedPatient!!.user!!.id)

        // Feedback
        GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.patient_deleted))
    }
    //endregion
}
