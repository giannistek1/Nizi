package nl.stekkinger.nizi.classes.patient

import nl.stekkinger.nizi.classes.dietary.DietaryGuideline
import nl.stekkinger.nizi.classes.login.User
import java.io.Serializable

data class PatientData (
    var patient: PatientShort,
    var user: User,
    var diets: ArrayList<DietaryGuideline>
) : Serializable