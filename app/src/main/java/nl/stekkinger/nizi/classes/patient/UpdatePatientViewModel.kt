package nl.stekkinger.nizi.classes.patient

import nl.stekkinger.nizi.classes.DietaryGuideline
import java.io.Serializable

data class UpdatePatientViewModel (
    var patient: PatientItem,
    var diets: ArrayList<DietaryGuideline>
) : Serializable