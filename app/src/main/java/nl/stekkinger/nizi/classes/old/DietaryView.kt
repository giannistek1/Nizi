package nl.stekkinger.nizi.classes.old

import nl.stekkinger.nizi.classes.old.DietaryManagementModel
import nl.stekkinger.nizi.classes.old.Restrictions

// Response from getting Dietary
data class DietaryView (
    val Restrictions: ArrayList<Restrictions>,
    val Dietarymanagement: ArrayList<DietaryManagementModel>
)