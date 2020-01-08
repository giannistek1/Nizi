package nl.stekkinger.nizi

import nl.stekkinger.nizi.classes.wont_use.AccessTokenResult
import nl.stekkinger.nizi.classes.DoctorLogin
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.PatientLogin
import retrofit2.Call
import retrofit2.http.*
import java.text.SimpleDateFormat

// Swagger: https://appnizi-api.azurewebsites.net/api/swagger/ui#/

interface ApiService {

    //region TEST AccessToken
    @POST("https://appnizi.eu.auth0.com/oauth/token")
    fun getAccessToken(
        @Header("content-type:application/json")
        @Field("client_id") client_id: String,
        @Field("client_secret") client_secret: String,
        @Field("audience") audience: String,
        @Field("grant_type") grant_type: String
    ) : Call<AccessTokenResult>

    //endregion

    //region Consumption
    @POST("v1/consumptions")
    fun addConsumption(
        @Header("Authorization") authHeader : String,
        @Body body: Consumption
    ) : Call<Unit>

    @GET("v1/consumptions")
    fun fetchConsumptions(
        @Header("Authorization") authHeader : String,
        @Query("patientId") patientId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ) : Call<Consumptions.Result>

    @GET("v1/consumption/{consumptionId}")
    fun fetchConsumptionById(
        @Path("consumptionId") consumptionId: Int
    ) : Call<Unit>

    @PUT("v1/consumption/{consumptionId}")
    fun updateConsumptionById(
        @Path("consumptionId") consumptionId: Int
    ) : Call<Unit>

    @DELETE("v1/consumption/{consumptionId}")
    fun deleteConsumption(
        @Header("Authorization") authHeader : String,
        @Path("consumptionId") consumptionId: Int
    ) : Call<Unit>
    //endregion

    //region Food
    @POST("v1/food/favorite")
    fun addFavoriteFood(
        @Header("Authorization") authHeader : String,
        @Query("patientId") patientId: Int,
        @Query("foodId") foodId: Int
    ) : Call<Unit>

    @GET("v1/food/{foodId}")
    fun getFood(
        @Path("foodId") foodId: Int
    ) : Call<Unit>

    @GET("v1/food/partial/{foodName}/{count}")
    fun searchFoodDB(
        @Header("Authorization") authHeader : String,
        @Path("foodName") foodName: String,
        @Path("count") count: Int
    ) : Call<ArrayList<Food>>

    @GET("v1/food/favorite/{patientId}")
    fun getFavoriteFood(
        @Header("Authorization") authHeader : String,
        @Path("patientId") patientId: Int
    ) : Call<ArrayList<Food>>

    // Staat hier ook een fout in bij swagger
    @DELETE("v1/food/favorite")
    fun deleteFavoriteFood(
        @Header("Authorization") authHeader : String,
        @Query("patientId") patientId: Int,
        @Query("foodId") foodId: Int
    ) : Call<Unit>
    //endregion

    //region WaterConsumption
    @POST("v1/waterconsumption")
    fun addWaterConsumption(

    ) : Call<Unit>

    @GET("v1/waterconsumption/{waterId}")
    fun getWaterConsumption(
        @Path("waterId") waterId: Int
    ) : Call<Unit>

    @PUT("v1/waterconsumption/{waterId}")
    fun updateWaterConsumption(
        @Path("waterId") waterId: Int
    ) : Call<Unit>

    @DELETE("v1/waterconsumption/{waterId}")
    fun deleteWaterConsumption(
        @Path("waterId") waterId: Int
    ) : Call<Unit>

    @GET("v1/waterconsumption/daily/{patientId}")
    fun getWaterConsumptionByDate(
        @Path("patientId") patientId: Int,
        @Query("date") date: SimpleDateFormat
    ) : Call<Unit>

    @GET("v1/waterconsumption/period/{patientId}")
    fun getWaterConsumptionByPeriod(
        @Path("patientId") patientId: Int,
        @Query("beginDate") beginDate: SimpleDateFormat,
        @Query("endDate") endDate: SimpleDateFormat
    ) : Call<Unit>
    //endregion

    //region Patient
    // Staat twee fouten in Swagger
    @GET("v1/patients")
    fun getPatients(
        @Header("Authorization") authHeader : String
    ) : Call<List<Patient>>

    @POST("v1/patient")
    fun registerPatient(
        @Header("Authorization") authHeader : String,
        @Body body: PatientLogin
    ) : Call<Unit>

    @GET("v1/patient/{patientId}")
    fun getPatient(
        @Path("patientId") patientId: Int
    ) : Call<Unit>

    @DELETE("v1/patient/{patientId}")
    fun deletePatient(
        @Path("patientId") patientId: Int
    ) : Call<Unit>


    // Get User As Patient from Access Token
    @GET("v1/login/patient")
    fun loginAsPatient(
        @Header("Authorization") authHeader : String
    ) : Call<PatientLogin>
    //endregion

    //region Meal
    // Staat fout in swagger
    @POST("v1/meal/{patientId}")
    fun createMeal(
        @Header("Authorization") authHeader : String,
        @Path("patientId") patientId: Int,
        @Body body: Meal
    ) : Call<Unit>

    @GET("v1/meal/{patientId}")
    fun getMeals(
        @Header("Authorization") authHeader : String,
        @Path("patientId") patientId: Int
    ) : Call<ArrayList<Meal>>

    @PUT("v1/meal/{patientId}/{mealId}")
    fun updateMeal(
        @Path("patientId") patientId: Int,
        @Path("mealId") mealId: Int
    ) : Call<Unit>

    @DELETE("v1/meal")
    fun deleteMeal(
        @Header("Authorization") authHeader : String,
        @Query("patientId") patientId: Int,
        @Query("mealId") mealId: Int
    ) : Call<Unit>

    //endregion

    //region Doctor
    // /v1/doctor? niet /v1/doctors?
    @GET("v1/doctor")
    fun getDoctors(

    ) : Call<Unit>

    @POST("v1/doctor")
    fun addDoctor(

    ) : Call<Unit>

    @GET("v1/doctor/{doctorId}")
    fun getDoctor(
        @Path("doctorId") doctorId: Int
    ) : Call<Unit>

    @DELETE("v1/doctor/{doctorId}")
    fun deleteDoctor(
        @Path("doctorId") doctorId: Int
    ) : Call<Unit>

    @GET("v1/doctor/{doctorId}/patients")
    fun getPatientsFromDoctor(
        @Header("Authorization") authHeader : String,
        @Path("doctorId") doctorId: Int
    ) : Call<ArrayList<Patient>>

    // Descriptie fout
    @GET("v1/login/doctor")
    fun loginAsDoctor(
        @Header("Authorization") authHeader : String
    ) : Call<DoctorLogin>
    //endregion

    //region DietaryManagement
    @POST("v1/dietaryManagement")
    fun addDietary(

    ) : Call<Unit>

    @GET("v1/dietaryManagement/{patientId}")
    fun getDietary(
        @Path("patientId") patientId: Int
    ) : Call<Unit>

    @PUT("v1/dietaryManagement/{dietId}")
    fun updateDietary(
        @Path("dietId") dietId: Int
    ) : Call<Unit>

    @DELETE("v1/dietaryManagement/{dietId}")
    fun deleteDietary(
        @Path("dietId") dietId: Int
    ) : Call<Unit>
    //endregion
}