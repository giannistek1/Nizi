package nl.stekkinger.nizi.classes

// Response from getting Dietary
data class DietaryView (
    val Restrictions: ArrayList<Restrictions>,
    val Dietarymanagement: ArrayList<DietaryManagementModel>
)