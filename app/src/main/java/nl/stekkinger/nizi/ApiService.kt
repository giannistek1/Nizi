package nl.stekkinger.nizi

import nl.stekkinger.nizi.classes.DoctorLogin
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.PatientLogin
import nl.stekkinger.nizi.classes.login.LoginRequest
import nl.stekkinger.nizi.classes.login.LoginResponse
import retrofit2.Call
import retrofit2.http.*
import java.text.SimpleDateFormat

interface ApiService {

    //region Login
    @POST("auth/local")
    fun login(
        @Body body: LoginRequest
    ) : Call<LoginResponse>
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
    fun fetchConversations(
        @Header("Authorization") authHeader : String,
        @Path("patientId") patientId: Int,
        @Query("beginDate") beginDate: String,
        @Query("endDate") endDate: String
    ) : Call<ArrayList<Conversation>>

//    @GET("v1/waterconsumption/period/{patientId}")
//    fun fetchConversations(
//        @Header("Authorization") authHeader : String,
//        @Path("patientId") patientId: Int,
//        @Query("beginDate") beginDate: String,
//        @Query("endDate") endDate: String
//    ) : Call<ArrayList<String>>
    //endregion

    //region Patient
    // Staat twee fouten in Swagger
    @GET("v1/patients") // werkt niet met doctor token
    fun getPatients(
        @Header("Authorization") authHeader : String
    ) : Call<List<Patient>>

    @POST("v1/patient")
    fun registerPatient(
        @Header("Authorization") authHeader : String,
        @Body body: PatientLogin
    ) : Call<PatientRegisterResponse>

    @PUT("v1/patient")
    fun updatePatient(
        @Header("Authorization") authHeader : String,
        @Body body: PatientUpdateModel
    ) : Call<Unit>

    @GET("v1/patient/{patientId}")
    fun getPatient(
        @Path("patientId") patientId: Int
    ) : Call<Unit>

    @DELETE("v1/patient/{patientId}")
    fun deletePatient(
        @Path("patientId") patientId: Int
    ) : Call<Unit>

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

    //region DietaryManagement
    @POST("v1/dietaryManagement")
    fun addDietary(
        @Header("Authorization") authHeader : String,
        @Body body: DietaryManagementModel
    ) : Call<Unit>

    @GET("v1/dietaryManagement/{patientId}")
    fun getDietary(
        @Header("Authorization") authHeader : String,
        @Path("patientId") patientId: Int
    ) : Call<DietaryView>

    @PUT("v1/dietaryManagement/{dietId}")
    fun updateDietary(
        @Header("Authorization") authHeader : String,
        @Path("dietId") dietId: Int
    ) : Call<Unit>

    @DELETE("v1/dietaryManagement/{dietId}")
    fun deleteDietary(
        @Header("Authorization") authHeader : String,
        @Path("dietId") dietId: Int
    ) : Call<Unit>
    //endregion
}