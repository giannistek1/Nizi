package nl.stekkinger.nizi

import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.diary.*
import nl.stekkinger.nizi.classes.login.*
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.dietary.DietaryRestriction
import nl.stekkinger.nizi.classes.patient.*
import nl.stekkinger.nizi.classes.dietary.DietaryManagementShort
import nl.stekkinger.nizi.classes.doctor.Doctor
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.feedback.FeedbackShort
import nl.stekkinger.nizi.classes.password.*
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit
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

    @POST("auth/forgot-password")
    fun forgotPassword(
        @Body body: ForgotPasswordRequest
    ) : Call<ForgotPasswordResponse>

    @POST("auth/reset-password")
    fun resetPassword(
        @Body body: ResetPasswordRequest
    ) : Call<ResetPasswordResponse>
    //endregion

    //region patients
    @POST("patients")
    fun registerPatient(
        @Header("Authorization") authHeader : String,
        @Body body: PatientShort
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
        @Body body: PatientShort
    ) : Call<Patient>

    @DELETE("v1/patient/{patientId}")
    fun deletePatient(
        @Path("patientId") patientId: Int
    ) : Call<Unit>
    //endregion

    //region consumptions
    @POST("consumptions")
    fun addConsumption(
        @Header("Authorization") authHeader : String,
        @Body body: Consumption
    ) : Call<Unit>

    @PUT("consumptions")
    fun editConsumption(
        @Header("Authorization") authHeader : String,
        @Body body: Consumption
    ) : Call<Unit>

    @GET("consumptions")
    fun fetchConsumptions(
        @Header("Authorization") authHeader : String,
        @Query("patient.id") patientId: Int,
        @Query("date") date: String
    ) : Call<ArrayList<ConsumptionResponse>>

    @GET("consumptions")
    fun fetchConsumptionsByWeek(
        @Header("Authorization") authHeader : String,
        @Query("patient.id") patientId: Int,
        @Query("date_gte") startDate: String,
        @Query("date_lte") endDate: String
    ) : Call<ArrayList<ConsumptionResponse>>

    @GET("v1/consumption/{consumptionId}")
    fun fetchConsumptionById(
        @Path("consumptionId") consumptionId: Int
    ) : Call<Unit>

    @PUT("v1/consumption/{consumptionId}")
    fun updateConsumptionById(
        @Path("consumptionId") consumptionId: Int
    ) : Call<Unit>

    @DELETE("consumptions/{id}")
    fun deleteConsumption(
        @Header("Authorization") authHeader : String,
        @Path("id") consumptionId: Int
    ) : Call<Unit>
    //endregion

    //region foods
    @POST("my-foods")
    fun addFavoriteFood(
        @Header("Authorization") authHeader : String,
        @Body body: MyFoodRequest
    ) : Call<Unit>

    @GET("v1/food/{foodId}")
    fun getFood(
        @Path("foodId") foodId: Int
    ) : Call<Unit>

    @GET("v1/food/partial/{foodName}/{count}")
    fun searchFoodDBBU(
        @Header("Authorization") authHeader : String,
        @Path("foodName") foodName: String,
        @Path("count") count: Int
    ) : Call<ArrayList<Food>>

    @GET("foods")
    fun searchFoodDB(
        @Header("Authorization") authHeader : String,
        @Query("name_contains") foodName: String
    ) : Call<ArrayList<FoodResponse>>

    @GET("my-foods")
    fun getFavoriteFood(
        @Header("Authorization") authHeader : String,
        @Query("patients_ids.id") patientId: Int
    ) : Call<ArrayList<MyFoodResponse>>

    // Staat hier ook een fout in bij swagger
    @DELETE("my-foods/{id}")
    fun deleteFavoriteFood(
        @Header("Authorization") authHeader : String,
        @Path("id") id: Int
    ) : Call<Unit>
    //endregion

    //region feedbacks
    @POST("feedbacks")
    fun addFeedback(
        @Header("Authorization") authHeader : String,
        @Body body: FeedbackShort
    ) : Call<Feedback>

    @GET("feedbacks")
    fun fetchFeedbacks(
        @Header("Authorization") authHeader : String,
        @Query("patient.id") patientId: Int,
        @Query("_sort") sortProp: String
    ) : Call<ArrayList<Feedback>>
    //endregion

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

    //region weight-units
    @GET("weight-units")
    fun getWeightUnits(
        @Header("Authorization") authHeader : String
    ) : Call<ArrayList<WeightUnit>>
    //endregion
}