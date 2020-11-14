package nl.stekkinger.nizi.classes

import nl.stekkinger.nizi.classes.dietary.DietaryManagementModel
import nl.stekkinger.nizi.classes.old.Restrictions

// Response from getting Dietary
data class DietaryView (
    val Restrictions: ArrayList<Restrictions>,
    val Dietarymanagement: ArrayList<DietaryManagementModel>
)