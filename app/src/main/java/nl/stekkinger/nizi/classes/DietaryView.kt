package nl.stekkinger.nizi.classes

// Response from getting Dietary
data class DietaryView (
    val Restrictions: List<Restrictions>,
    val Dietarymanagement: List<DietaryManagementModel>
)