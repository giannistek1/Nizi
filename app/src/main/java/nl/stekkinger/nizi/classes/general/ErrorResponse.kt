package nl.stekkinger.nizi.classes.general

data class ErrorResponse (
    val statusCode: String,
    val error: String,
    val message: ArrayList<Messages>
)