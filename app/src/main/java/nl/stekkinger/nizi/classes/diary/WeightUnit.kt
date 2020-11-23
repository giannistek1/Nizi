package nl.stekkinger.nizi.classes.diary

data class WeightUnit(
        val id: Int,
        val unit: String,
        val short: String,
        val created_at: String? = null,
        val updated_at: String? = null
)