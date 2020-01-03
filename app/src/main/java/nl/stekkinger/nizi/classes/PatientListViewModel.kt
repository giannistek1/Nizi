package nl.stekkinger.nizi.classes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import nl.stekkinger.nizi.repositories.PatientRepository
import java.text.SimpleDateFormat

data class PatientListViewModel (private val mRepository: PatientRepository = PatientRepository()) : ViewModel() {

    // Contains patientList
    private var patientList: MutableLiveData<ArrayList<Patient>>()
            private var patientList:


    fun getPatientList(): LiveData<ArrayList<Patient>> {
        return patientList
    }
}