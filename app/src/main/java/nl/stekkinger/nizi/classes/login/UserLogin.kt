package nl.stekkinger.nizi.classes.login

import nl.stekkinger.nizi.classes.doctor.DoctorShort
import nl.stekkinger.nizi.classes.patient.PatientShort
import java.io.Serializable

data class UserLogin(
    val id: Int,
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val role: Role,
    var patient: PatientShort?,
    val doctor: DoctorShort?,

    // Unimportant stuff
    val created_at: String?,
    val updated_at: String?,
    val provider: String?,
    val confirmed: Boolean?,
    val blocked: Boolean?
) : Serializable