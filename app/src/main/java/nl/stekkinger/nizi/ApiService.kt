package nl.stekkinger.nizi

import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.login.LoginRequest
import nl.stekkinger.nizi.classes.login.LoginResponse
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.dietary.DietaryRestriction
import nl.stekkinger.nizi.classes.patient.*
import nl.stekkinger.nizi.classes.login.User
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.dietary.DietaryManagementShort
import nl.stekkinger.nizi.classes.doctor.Doctor
import nl.stekkinger.nizi.classes.feedback.Feedback
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    //region auth && users
    @POST("auth/local")
    fun login(
        @Body body: LoginRequest
    ) : Call<LoginResponse>

    @POST("users")
    fun registerUser(
        @Header("Authorization") authHeader : String,
        @Body body: User
    ) : Call<UserLogin>

    @PUT("users")
    fun updateUser(
        @Header("Authorization") authHeader : String,
        @Body body: User
    ) : Call<UserLogin>
    //endregion

    //region patients
    @POST("patients")
    fun registerPatient(
        @Header("Authorization") authHeader : String,
        @Body body: PatientLogin
    ) : Call<Patient>

    @GET("patients")
    fun getPatientsForDoctor(
        @Header("Authorization") authHeader : String,
        @Query("doctor.id") doctorId: Int
    ) : Call<ArrayList<Patient>>

    @GET("patients/{patientId}")
    fun getPatient(
        @Header("Authorization") authHeader : String,
        @Path("patientId") patientId: Int
    ) : Call<Patient>

    @PUT("patients/{patientId}")
    fun updatePatientUserId(
        @Header("Authorization") authHeader : String,
        @Path("patientId") patientId: Int,
        @Body body: PatientUpdateUserIdRequest
    ) : Call<Patient>

    @PUT("patients/{patientId}")
    fun updatePatient(
        @Header("Authorization") authHeader : String,
        @Path("patientId") patientId: Int,
        @Body body: PatientLogin
    ) : Call<Patient>

    @DELETE("v1/patient/{patientId}")
    fun deletePatient(
        @Path("patientId") patientId: Int
    ) : Call<Unit>
    //endregion

    //region consumptions
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

    //region foods
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

    //region feedbacks
    @GET("feedbacks")
    fun fetchFeedbacks(
        @Header("Authorization") authHeader : String,
        @Query("patient.id") patientId: Int
    ) : Call<ArrayList<Feedback>>

    /*@GET("v1/waterconsumption/period/{patientId}")
    fun fetchConversations(
        @Header("Authorization") authHeader : String,
        @Path("patientId") patientId: Int,
        @Query("beginDate") beginDate: String,
        @Query("endDate") endDate: String
    ) : Call<ArrayList<Conversation>>*/

    //region meals
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

    //region dietary-restrictions
    @GET("dietary-restrictions")
    fun getDietaryRestrictions(
        @Header("Authorization") authHeader : String
    ) : Call<ArrayList<DietaryRestriction>>
    //endregion

    //region dietary-managements
    @POST("dietary-managements")
    fun addDietaryManagement(
        @Header("Authorization") authHeader : String,
        @Body body: DietaryManagementShort
    ) : Call<DietaryManagement>

    @GET("dietary-managements")
    fun getDietaryManagements(
        @Header("Authorization") authHeader : String,
        @Query("patient.id") patientId: Int
    ) : Call<ArrayList<DietaryManagement>>

    @PUT("dietary-managements/{dietaryManagementId}")
    fun updateDietaryManagement(
        @Header("Authorization") authHeader : String,
        @Body body: DietaryManagementShort,
        @Path("dietaryManagementId") dietaryManagementId: Int
    ) : Call<DietaryManagement>

    @DELETE("dietary-managements/{dietaryManagementId}")
    fun deleteDietary(
        @Header("Authorization") authHeader : String,
        @Path("dietaryManagementId") dietaryManagementId: Int
    ) : Call<DietaryManagement>
    //endregion

    //region doctors
    @GET("doctors/{doctorId}")
    fun getDoctor(
        @Header("Authorization") authHeader : String,
        @Path("doctorId") doctorId: Int
    ) : Call<Doctor>

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
    //endregion
}