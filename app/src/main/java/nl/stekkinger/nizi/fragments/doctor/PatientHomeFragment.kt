package nl.stekkinger.nizi.fragments.doctor


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_patient_home.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.doctor.EditPatientActivity
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelper
import nl.stekkinger.nizi.classes.patient.PatientData
import java.util.*

class PatientHomeFragment : Fragment() {

    // For activity result
    private val REQUEST_CODE = 0

    // Does not work in fragments
    /*private var weekNumber by Delegates.observable(0) { property, oldValue, newValue ->
        fragment_patient_home_week.text = "Week ${newValue}"
    }*/

    private lateinit var mCurrentDate: String
    private lateinit var patientData: PatientData
    private lateinit var model: DiaryViewModel
    private val sdf = GeneralHelper.getDateFormat()

    // Gets called one time, you CANT use view references in here
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_patient_home, container, false)

        // Get patient data from bundle
        val bundle: Bundle = this.arguments!!
        patientData = bundle.getSerializable(GeneralHelper.EXTRA_PATIENT) as PatientData

        // Header
        val fullName = "${patientData!!.user.first_name} ${patientData.user.last_name}"
        view.fragment_patient_home_average_of_patient.text = getString(R.string.average_of, fullName)

        // Edit Button
        view.fragment_patient_home_btn_edit.setOnClickListener {
            val intent = Intent(activity, EditPatientActivity::class.java)

            // Prevents multiple activities
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

            intent.putExtra(GeneralHelper.EXTRA_PATIENT, patientData)
            intent.putExtra(GeneralHelper.EXTRA_DOCTOR_ID, intent.getIntExtra(GeneralHelper.EXTRA_DOCTOR_ID, 1))
            startActivityForResult(intent, REQUEST_CODE)
        }

        // Preparation
        // Get current date
        val calendar: Calendar = Calendar.getInstance()
        // Clear time from date
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        // Get first day of week and save it
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val currentFirstDayOfWeek = calendar.time
        var selectedFirstDayOfWeek = currentFirstDayOfWeek

        // Get last day of week and save it
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek+6);
        val currentLastDayOfWeek = calendar.time
        var selectedLastDayOfWeek = currentLastDayOfWeek

        view.fragment_patient_home_btn_previousWeek.setOnClickListener {
            // Set calendar date to first day of selected week,  substract 7, get date
            calendar.time = selectedFirstDayOfWeek
            calendar.add(Calendar.DATE, -7)
            selectedFirstDayOfWeek = calendar.time

            calendar.time = selectedLastDayOfWeek
            calendar.add(Calendar.DATE, -7)
            selectedLastDayOfWeek = calendar.time

            // Update UI
            view.fragment_patient_home_week.text = "${sdf.format(selectedFirstDayOfWeek)} - ${sdf.format(selectedLastDayOfWeek)}"
            view.fragment_patient_home_btn_nextWeek.imageAlpha = 255

            refreshGuidelines(view)
        }

        view.fragment_patient_home_btn_nextWeek.setOnClickListener {
            // Guard
            if (selectedFirstDayOfWeek == currentFirstDayOfWeek) { return@setOnClickListener }

            calendar.time = selectedFirstDayOfWeek
            calendar.add(Calendar.DATE, 7)
            selectedFirstDayOfWeek = calendar.time

            calendar.time = selectedLastDayOfWeek
            calendar.add(Calendar.DATE, 7)
            selectedLastDayOfWeek = calendar.time

            if (selectedFirstDayOfWeek == currentFirstDayOfWeek) {
                // Update UI
                view.fragment_patient_home_week.text = "${sdf.format(selectedFirstDayOfWeek)} - ${sdf.format(selectedLastDayOfWeek)}"
                view.fragment_patient_home_btn_nextWeek.imageAlpha = 20
            }

            refreshGuidelines(view)
        }

        /*// setting date for diary
        val startDate: String = getDay(mCurrentDate, 0)
        val endDate: String = getDay(mCurrentDate, 1)
        model.setDiaryDate(startDate + "/" + endDate)*/

        view.fragment_patient_home_week.text = "${sdf.format(selectedFirstDayOfWeek)} - ${sdf.format(selectedLastDayOfWeek)}"
        view.fragment_patient_home_btn_nextWeek.imageAlpha = 20

        refreshGuidelines(view)

        return view
    }

    // Gets called when visible
    override fun onStart() {
        super.onStart()

        /*model.getDiary().observe(viewLifecycleOwner, Observer { result ->
            // update total intake values of the day (guidelines)
            dietaryGuidelines?.forEachIndexed { index, element ->
                if (element.description.contains("Calorie"))
                    dietaryGuidelines[index].amount = result.KCalTotal.toInt()
                else if (element.description.contains("Vocht"))
                    dietaryGuidelines[index].amount = (round(result.WaterTotal * 100) / 100)
                else if (element.description.contains("Natrium"))
                    dietaryGuidelines[index].amount = (round(result.SodiumTotal * 100) / 100)
                else if (element.description.contains("Kalium"))
                    dietaryGuidelines[index].amount = (round(result.CaliumTotal * 100) / 100)
                else if (element.description.contains("Eiwit"))
                    dietaryGuidelines[index].amount = (round(result.ProteinTotal * 100) / 100)
                else if (element.description.contains("Vezel"))
                    dietaryGuidelines[index].amount = (round(result.FiberTotal * 100) / 100)
            }


        })*/
        /*fragment_home_btn_yesterday.setOnClickListener {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val newDate = sdf.parse(getDay(mCurrentDate, -1))
            mCurrentDate = sdf.format(newDate)

            fragment_home_txt_day.text = mCurrentDate
            refreshGuidelines()
        }

        fragment_home_btn_tomorrow.setOnClickListener {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val newDate = sdf.parse(getDay(mCurrentDate, 1))
            // if new date is NOT after Today
            if (!newDate.after(Date())) {
                mCurrentDate = sdf.format(newDate)

                fragment_home_txt_day.text = mCurrentDate
                refreshGuidelines()
            }
        }

        fragment_home_txt_day.text = mCurrentDate*/


    }

    fun refreshGuidelines(view: View)
    {
        if (patientData != null) {
            GuidelinesHelper.initializeGuidelines(activity, view.fragment_patient_home_ll_guidelines, patientData.diets)
        }
    }

    fun getDay(date: String, daysAdded: Int): String {
        Log.d("AAAAAA", "BBBBB")
        var newDate = date
        val c = Calendar.getInstance()
        c.time = sdf.parse(newDate)
        c.add(Calendar.DATE, daysAdded)
        val resultdate = Date(c.timeInMillis)
        newDate = sdf.format(resultdate)
        return newDate
    }
}
