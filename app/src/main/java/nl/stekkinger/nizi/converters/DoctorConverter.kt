package nl.stekkinger.nizi.converters

import nl.stekkinger.nizi.classes.doctor.Doctor
import nl.stekkinger.nizi.classes.doctor.DoctorShort

object DoctorConverter {
    fun convertAllDoctors(doctors: ArrayList<Doctor>) : ArrayList<DoctorShort> {
        val doctorsShort = ArrayList<DoctorShort>()

        for (doctor in doctors) {
            doctorsShort.add(convert(doctor))
        }

        return doctorsShort
    }

    fun convert(doctor: Doctor) : DoctorShort {
        return DoctorShort(doctor.id, doctor.location, doctor.user.id)
    }
}