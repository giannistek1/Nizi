package nl.stekkinger.nizi.repositories

import android.content.Context
import android.content.SharedPreferences
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientLogin
import nl.stekkinger.nizi.classes.PatientUpdateModel

// 1e API TEST = Patiënt
// 3e API TEST = Dcotor


class PatientRepository : Repository(){

    private val TAG = "PatientRepository"

    var preferences: SharedPreferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    var accessToken = preferences.getString(GeneralHelper.PREF_TOKEN, null)
    var authHeader = "Bearer " + accessToken

    fun registerPatient(patient: PatientLogin) : Patient?
    {
        // DoB = "YYYY-MM-DDT10:55:38:00"
        return service.registerPatient(authHeader, patient).execute().body()
    }

    fun updatePatient(patientId: Int?, doctorId: Int?, firstName: String, lastName: String, dateOfBirth: String)
    {
        //val randomGuid = Random.nextInt(1000000).toString()

        val patientToUpdate =
            PatientUpdateModel(
                patientId,
                doctorId,
                firstName,
                lastName,
                dateOfBirth
            )

        // DoB = "YYYY-MM-DDT10:55:38:00"
        service.updatePatient(authHeader, patientToUpdate).execute()
    }

    fun getPatientsForDoctor(doctorId: Int) : ArrayList<Patient>?
    {
        return service.getPatientsForDoctor(authHeader, doctorId).execute().body()
    }

    /*fun getPatientsFromDoctor2(doctorId: Int) : MutableLiveData<ArrayList<Patient>?> {
        val patientList: ArrayList<Patient> = arrayListOf()
        val result = MutableLiveData<ArrayList<Patient>?>()

        service.getPatientsFromDoctor(authHeader = authHeader, doctorId = doctorId).enqueue(object: Callback<ArrayList<Patient>> {
            override fun onResponse(call: Call<ArrayList<Patient>>, response: Response<ArrayList<Patient>>) {
                if (response.isSuccessful && response.body() != null) {
                    for (patient: Patient in response.body()!!) {
                        Log.i(TAG, patient.toString())
                        patientList.add(patient)
                    }
                    result.value = patientList
                } else {
                    d(TAG, "response, not successful: ${response.body()}")
                }
            }
            override fun onFailure(call: Call<ArrayList<Patient>>, t: Throwable) {
                d(TAG, "onFailure")
            }
        })
        return result
    }*/
}