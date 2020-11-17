package nl.stekkinger.nizi.classes.old

// Kan weg na DietaryView rework
data class DietaryManagementModel (
    val id: Int,
    val Description: String,
    val Amount: Int,
    val IsActive: Boolean,
    val PatientId: Int
)