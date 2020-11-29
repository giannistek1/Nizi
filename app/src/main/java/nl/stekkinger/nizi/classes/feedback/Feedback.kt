package nl.stekkinger.nizi.classes.feedback

import nl.stekkinger.nizi.classes.doctor.DoctorShort
import nl.stekkinger.nizi.classes.patient.PatientShort
import java.io.Serializable
import java.util.*

data class Feedback(
    val id: Int = 0,
    val title: String,
    val comment: String,
    val date: Date,
    val is_read: Boolean = false,
    val patient: PatientShort,
    val doctorShort: DoctorShort,

    // Unimportant stuff
    val created_at: String? = null,
    val updated_at: String? = null
) : Serializable