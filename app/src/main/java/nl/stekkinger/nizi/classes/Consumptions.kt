package nl.stekkinger.nizi.classes

import java.text.SimpleDateFormat
class Consumptions {

    data class Result (
        val Consumptions: ArrayList<Consumption> = ArrayList(),
        val KCalTotal: Float,
        val ProteinTotal: Float,
        val FiberTotal: Float,
        val CaliumTotal: Float,
        val SodiumTotal: Float
    )

    data class Consumption (
        val ConsumptionId: Int,
        val FoodName: String,
        val KCal: Float,
        val Protein: Float,
        val Fiber: Float,
        val Calium: Float,
        val Sodium: Float,
        val Amount: Int,
        val Weight: WeightUnit,
        val Date: String,
        val Valid: Boolean
    )
    data class WeightUnit (
        val Id: Int,
        val Unit: String,
        val Short: String
    )
}
