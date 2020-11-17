package nl.stekkinger.nizi.repositories

import android.content.Context
import android.content.SharedPreferences
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientLogin
import nl.stekkinger.nizi.classes.patient.PatientUpdateUserIdRequest

// 1e API TEST = Patiënt
// 3e API TEST = Dcotor


class PatientRepository : Repository(){

    private val TAG = "PatientRepository"

    var preferences: SharedPreferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    var accessToken = preferences.getString(GeneralHelper.PREF_TOKEN, null)
    var authHeader = "Bearer " + accessToken

    fun registerPatient(patient: PatientLogin) : Patient?
    {
        return service.registerPatient(authHeader, patient).execute().body()
    }

    fun getPatientsForDoctor(doctorId: Int) : ArrayList<Patient>?
    {
        return service.getPatientsForDoctor(authHeader, doctorId).execute().body()
    }

    fun getPatient(patientId: Int) : Patient?
    {
        return service.getPatient(authHeader, patientId).execute().body()
    }

    // DoB = "YYYY-MM-DDT10:55:38:00"

    fun updatePatientUserId(patientId: Int, userId: Int) : Patient? {

        return service.updatePatientUserId(authHeader, patientId, PatientUpdateUserIdRequest(userId)).execute().body()
    }

    fun updatePatient(patient: PatientLogin) : Patient?
    {
        return service.updatePatient(authHeader, patient.id, patient).execute().body()
    }


}