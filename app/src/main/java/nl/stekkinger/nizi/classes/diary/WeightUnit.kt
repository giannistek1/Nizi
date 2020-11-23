package nl.stekkinger.nizi.classes.diary
// TODO: Delete this, use other WeightUnit
data class WeightUnit(
        val id: Int,
        val unit: String,
        val short: String,
        val created_at: String? = null,
        val updated_at: String? = null
)