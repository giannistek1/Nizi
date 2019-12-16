package nl.stekkinger.nizi.classes

// Inside DoctorLogin (basically the same as PatientLogin but differently named
data class Auth (
    val guid: Int,
    val token: Token
)