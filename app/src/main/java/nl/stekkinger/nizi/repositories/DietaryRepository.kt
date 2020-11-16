package nl.stekkinger.nizi.repositories

import android.content.Context
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.dietary.DietaryManagementShort
import nl.stekkinger.nizi.classes.dietary.DietaryRestriction

class DietaryRepository : Repository() {

    private val TAG = "DietaryRepository"

    private val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val accessToken = preferences.getString("TOKEN", null)
    private val authHeader = "Bearer " + accessToken

    fun getDietaries() : ArrayList<DietaryRestriction>? {
        return service.getDietaryRestrictions(authHeader).execute().body()
    }

    fun getDietary(patientId: Int) : DietaryView? {
        return service.getDietary(authHeader, patientId).execute().body()
    }

    fun addDietary(dietaryManagement: DietaryManagementShort) : DietaryManagement? {
        return service.addDietary(authHeader, dietaryManagement).execute().body()
    }
    fun updateDietary(dietaryManagementId: Int) : DietaryManagement? {
        return service.updateDietary(authHeader, dietaryManagementId).execute().body()
    }
}