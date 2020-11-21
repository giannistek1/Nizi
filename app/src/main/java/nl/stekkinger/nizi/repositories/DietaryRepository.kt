package nl.stekkinger.nizi.repositories

import nl.stekkinger.nizi.classes.dietary.*

class DietaryRepository : Repository() {

    private val TAG = "DietaryRepository"

    fun addDietary(dietaryManagement: DietaryManagementShort) : DietaryManagement? {
        return service.addDietaryManagement(authHeader, dietaryManagement).execute().body()
    }

    fun getDietaryRestrictions() : ArrayList<DietaryRestriction>? {
        return service.getDietaryRestrictions(authHeader).execute().body()
    }

    fun getDietaryManagements(patientId: Int) : ArrayList<DietaryManagement>? {
        return service.getDietaryManagements(authHeader, patientId).execute().body()
    }

    fun updateDietary(dietaryManagement: DietaryManagementShort) : DietaryManagement? {
        return service.updateDietaryManagement(authHeader, dietaryManagement, dietaryManagement.id!!).execute().body()
    }

    fun deleteDietary(dietaryManagementId: Int) : DietaryManagement? {
        return service.deleteDietary(authHeader, dietaryManagementId).execute().body()
    }
}