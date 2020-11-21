package nl.stekkinger.nizi.classes.weight_unit

import java.io.Serializable

data class WeightUnitHolder (
    var weightUnits: ArrayList<WeightUnit> = arrayListOf()
) : Serializable