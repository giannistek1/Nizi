package nl.stekkinger.nizi.classes

data class DietaryManagementModel (
    val id: Int,
    val Description: String,
    val Amount: Int,
    val IsActive: Boolean,
    val PatientId: Int
)