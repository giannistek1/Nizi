package nl.stekkinger.nizi.classes

import nl.stekkinger.nizi.classes.dietary.DietaryGuideline
import java.io.Serializable

data class EditDietaryModel(
    val list: ArrayList<DietaryGuideline>?
) : Serializable