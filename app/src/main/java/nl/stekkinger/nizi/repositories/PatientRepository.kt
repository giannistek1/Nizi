package nl.stekkinger.nizi.repositories

import android.content.Context
import android.content.SharedPreferences
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientLogin
import nl.stekkinger.nizi.classes.PatientUpdateModel
import nl.stekkinger.nizi.classes.patient.AddPatientRequest
import nl.stekkinger.nizi.classes.patient.PatientUpdateUserIdRequest

// 1e API TEST = Patiënt
// 3e API TEST = Dcotor


class PatientRepository : Repository(){

    private val TAG = "PatientRepository"

    var preferences: SharedPreferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    var accessToken = preferences.getString(GeneralHelper.PREF_TOKEN, null)
    var authHeader = "Bearer " + accessToken

    fun getPatientsForDoctor(doctorId: Int) : ArrayList<Patient>?
    {
        return service.getPatientsForDoctor(authHeader, doctorId).execute().body()
    }

    // DoB = "YYYY-MM-DDT10:55:38:00"

    fun registerPatient(patient: PatientLogin) : Patient?
    {
        return service.registerPatient(authHeader, patient).execute().body()
    }

    fun updatePatientUserId(patientId: Int, userId: Int) : Patient? {

        return service.updatePatientUserId(authHeader, patientId, PatientUpdateUserIdRequest(userId)).execute().body()
    }

    fun updatePatient(patientId: Int?, doctorId: Int?, firstName: String, lastName: String, dateOfBirth: String)
    {
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


}