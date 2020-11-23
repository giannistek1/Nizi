package nl.stekkinger.nizi.classes.diary

data class PatientDiary(
    val id: Int = 0,
    val gender: String,
    val date_of_birth: String,
    val doctor: Int = 0,
    val user: Int = 0
)