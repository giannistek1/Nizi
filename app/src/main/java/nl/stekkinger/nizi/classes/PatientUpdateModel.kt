package nl.stekkinger.nizi.classes

data class PatientUpdateModel (
    val PatientId: Int?,
    val DoctorId: Int?,
    val FirstName: String,
    val LastName: String,
    val DateOfBirth: String
)