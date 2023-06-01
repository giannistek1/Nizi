package nl.stekkinger.nizi.converters

import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientShort

object PatientConverter {
    fun convertAllPatients(patients: ArrayList<Patient>) : ArrayList<PatientShort> {
        val patientsShort = ArrayList<PatientShort>()

        for (patient in patients) {
            patientsShort.add(convert(patient))
        }

        return patientsShort
    }

    fun convert(patient: Patient) : PatientShort {
        return PatientShort(patient.id!!, patient.gender, patient.date_of_birth, patient.doctor.id!!,
            user = patient.user!!.id,
            feedbacks = patient.feedbacks,
            dietary_managements = patient.dietary_managements,
            my_foods = patient.my_foods,
            consumptions = patient.consumptions
        )
    }
}