package nl.stekkinger.nizi.classes.dietary

import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientLogin
import java.io.Serializable

// Used as result
data class DietaryManagement(
    val id: Int? = null,
    val dietary_restriction: DietaryRestriction,
    var amount: Int,
    var is_active: Boolean,
    val patient: PatientLogin,

    // Unimportant stuff
    val created_at: String? = null,
    val updated_at: String? = null
) : Serializable