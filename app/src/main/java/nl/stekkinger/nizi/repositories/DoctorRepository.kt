package nl.stekkinger.nizi.repositories

import nl.stekkinger.nizi.classes.doctor.Doctor

class DoctorRepository : Repository() {

    private val TAG = "DoctorRepository"

    fun getDoctor(doctorId: Int) : Doctor?
    {
        return service.getDoctor(authHeader, doctorId).execute().body()
    }
}