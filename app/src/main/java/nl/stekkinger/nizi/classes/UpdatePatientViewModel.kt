package nl.stekkinger.nizi.classes

import java.io.Serializable

data class UpdatePatientViewModel (
    var patient: PatientItem,
    var diets: ArrayList<DietaryGuideline>
) : Serializable