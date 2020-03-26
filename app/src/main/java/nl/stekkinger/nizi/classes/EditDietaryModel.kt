package nl.stekkinger.nizi.classes

import java.io.Serializable

data class EditDietaryModel(
    val list: ArrayList<DietaryGuideline>?
) : Serializable