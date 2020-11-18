package nl.stekkinger.nizi.classes.doctor

data class DoctorShort (
    val id: Int? = null,
    val location: String,
    val user: Int,

    // Unimportant
    val created_at: String? = null,
    val updated_at: String? = null
)