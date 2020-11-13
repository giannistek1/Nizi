package nl.stekkinger.nizi.classes.user

data class Role(
    val id: Int,
    val name: String,
    val description: String,
    val type: String
)