package nl.stekkinger.nizi.classes

data class DietaryManagementModel (
    val id: Int,
    val description: String,
    val amount: Int,
    val isActive: Boolean,
    val patientId: Int
)