package nl.stekkinger.nizi.classes

data class PatientR (
    val PatientId: Int,
    val AccountId: Int,
    val DoctorId: Int,
    val FirstName: String,
    val LastName: String,
    val DateOfBirth: String,
    val WeightInKilograms: Float,
    val Guid: String
)