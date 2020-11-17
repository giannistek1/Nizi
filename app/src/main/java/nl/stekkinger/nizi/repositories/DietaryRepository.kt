package nl.stekkinger.nizi.repositories

import android.content.Context
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.dietary.DietaryManagementShort
import nl.stekkinger.nizi.classes.dietary.DietaryRestriction
import nl.stekkinger.nizi.classes.old.DietaryView

class DietaryRepository : Repository() {

    private val TAG = "DietaryRepository"

    private val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val accessToken = preferences.getString("TOKEN", null)
    private val authHeader = "Bearer " + accessToken

    fun addDietary(dietaryManagement: DietaryManagementShort) : DietaryManagement? {
        return service.addDietary(authHeader, dietaryManagement).execute().body()
    }

    fun getDietaryRestrictions() : ArrayList<DietaryRestriction>? {
        return service.getDietaryRestrictions(authHeader).execute().body()
    }

    fun getDietaryManagements(patientId: Int) : ArrayList<DietaryManagement>? {
        return service.getDietaryManagements(authHeader, patientId).execute().body()
    }

    fun getDietary(patientId: Int) : DietaryView? {
        //return service.getDietary(authHeader, patientId).execute().body()
        return null
    }

    fun updateDietary(dietaryManagement: DietaryManagementShort) : DietaryManagement? {
        return service.updateDietary(authHeader, dietaryManagement, dietaryManagement.id!!).execute().body()
    }
}