package nl.stekkinger.nizi.classes.patient

import java.io.Serializable

// Recycleview Item
data class PatientItem(
    var id: Int,
    var name: String,
    var first_name: String,
    var last_name: String,
    var date_of_birth: String,
    var gender: String,
    var username: String,
    var email: String,
    var role: Int,
    var patient_id: Int,
    var doctor_id: Int
): Serializable