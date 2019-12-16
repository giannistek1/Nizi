package nl.stekkinger.nizi.classes

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import nl.stekkinger.nizi.repositories.PatientRepository
import java.text.SimpleDateFormat

data class PatientListViewModel (private val mRepository: PatientRepository = PatientRepository()) : ViewModel() {


    /*fun getPatientList(): LiveData<ArrayList<Patient>> {
        return null
    }*/
}