package nl.stekkinger.nizi.repositories

import android.content.Context
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.dietary.DietaryManagementModel
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
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

        //var result = DietaryView(listOf(Restrictions(0, "")), listOf(DietaryManagementModel(id = 0, )))

        /*service.getDietary(authHeader = authHeader, patientId = patientId).enqueue(object: Callback<DietaryView> {
            override fun onResponse(call: Call<DietaryView>, response: Response<DietaryView>) {
                if (response.isSuccessful && response.body() != null) {
                    result = response.body()
                } else {
                    d(TAG, "response, not successful: ${response.body()}")
                }
            }
            override fun onFailure(call: Call<DietaryView>, t: Throwable) {
                d(TAG, "onFailure")
            }
        })
        return result*/
    }

    fun addDietary(dietaryManamgement: DietaryManagement) : DietaryManagement? {
        return service.addDietary(authHeader, dietaryManamgement).execute().body()
    }
    fun updateDietary(dietaryManagementId: Int) : DietaryManagement? {
        return service.updateDietary(authHeader, dietaryManagementId).execute().body()
    }
}