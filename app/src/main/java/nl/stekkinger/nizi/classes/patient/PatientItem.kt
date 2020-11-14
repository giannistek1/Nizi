package nl.stekkinger.nizi.classes.patient

import java.io.Serializable

data class PatientItem(
    var id: Int,
    var name: String,
    var firstName: String,
    var lastName: String,
    var dateOfBirth: String,
    //var weight: Float,
    var patientId: Int,
    var doctorId: Int
): Serializable