package nl.stekkinger.nizi.repositories

import nl.stekkinger.nizi.classes.LocalDb
import nl.stekkinger.nizi.classes.patient.*

class PatientRepository : Repository() {

    private val TAG = "PatientRepository"

    fun registerPatient(patient: PatientShort) : Patient?
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

    fun updatePatient(patient: PatientShort) : Patient?
    {
        return service.updatePatient(authHeader, patient.id, patient).execute().body()
    }

    fun deletePatient(patientId: Int) : Patient?
    {
        return service.deletePatient(authHeader, patientId).execute().body()
    }

    //region Local/mockup
    fun getPatientLocally(patientId: Int) : Patient?
    {
        return LocalDb.patients.firstOrNull { it.id == patientId }
    }

    fun deletePatientLocally(patientId: Int) : Patient?
    {
        val patient = LocalDb.patients.firstOrNull { it.id == patientId }
        LocalDb.patients.removeAt(LocalDb.patients.indexOfFirst { it.id == patientId })

        return patient
    }
    //endregion
}