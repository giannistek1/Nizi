package nl.stekkinger.nizi.repositories

import nl.stekkinger.nizi.classes.weight_unit.WeightUnit

class WeightUnitRepository : Repository() {

    private val TAG = "WeightUnitRepository"

    fun getWeightUnits() : ArrayList<WeightUnit>? {
        return service.getWeightUnits(authHeader).execute().body()
    }
}