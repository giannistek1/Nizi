package nl.stekkinger.nizi.classes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import nl.stekkinger.nizi.repositories.FoodRepository
import nl.stekkinger.nizi.repositories.PatientRepository
import java.text.SimpleDateFormat

data class PatientListViewModel (
    private val mRepository: PatientRepository = PatientRepository(),
    private var doctorId: MutableLiveData<Int> = MutableLiveData()
) : ViewModel() {
    // Contains patientList
    private var mPatientList: LiveData<ArrayList<Patient>> = Transformations.switchMap<Int, ArrayList<Patient>>(
            doctorId
        ) { id ->  getPatients(id) }

        private fun getPatients(did: Int): MutableLiveData<ArrayList<Patient>?> {
            return mRepository.getPatientsFromDoctor2(did)
        }

        fun setDoctorId(id: Int) {
            doctorId.value = id
        }

        fun loadPatients(): LiveData<ArrayList<Patient>> {
            return mPatientList
        }
}