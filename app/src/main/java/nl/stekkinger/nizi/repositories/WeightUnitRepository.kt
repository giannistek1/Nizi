package nl.stekkinger.nizi.repositories

import nl.stekkinger.nizi.classes.WeightUnit
import nl.stekkinger.nizi.classes.dietary.*

class WeightUnitRepository : Repository() {

    private val TAG = "WeightUnitRepository"

    fun getWeightUnits() : ArrayList<WeightUnit>? {
        return service.getWeightUnits(authHeader).execute().body()
    }
}