package nl.stekkinger.nizi.classes

import nl.stekkinger.nizi.classes.diary.ConsumptionResponse
import nl.stekkinger.nizi.classes.diary.ConsumptionShort
import nl.stekkinger.nizi.classes.diary.FoodMealComponent
import nl.stekkinger.nizi.classes.diary.MyFood
import nl.stekkinger.nizi.classes.diary.PatientDiary
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.dietary.DietaryManagementShort
import nl.stekkinger.nizi.classes.dietary.DietaryRestrictionShort
import nl.stekkinger.nizi.classes.doctor.Doctor
import nl.stekkinger.nizi.classes.doctor.DoctorShort
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.feedback.FeedbackShort
import nl.stekkinger.nizi.classes.login.Role
import nl.stekkinger.nizi.classes.login.User
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientShort
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit
import nl.stekkinger.nizi.converters.DoctorConverter
import nl.stekkinger.nizi.converters.PatientConverter
import java.util.Date

// Reverse order, top = most independent
object LocalDb {
    private const val dob: String = "2000-01-01"
    private const val genderMale = "male"
    private const val genderFemale = "female"

    const val jwt: String = "admin"

    private val feedbacksShort: ArrayList<FeedbackShort>? = arrayListOf()
    private val dietaryManagementsShort: ArrayList<DietaryManagementShort>? = arrayListOf()
    private val myFoods: ArrayList<MyFood> = arrayListOf()
    private val consumptionsShort: ArrayList<ConsumptionShort>? = arrayListOf()

    private val testWeightUnit1 = WeightUnit(1, "kilocalorien","kcal")
    private val testWeightUnit2 = WeightUnit(2, "gram","gr")
    private val testWeightUnit3 = WeightUnit(3, "milligram","mg")
    private val testWeightUnit4 = WeightUnit(4, "milliliter","ml")

    val testUser1 = User(id = 1, email = "admin@admin.nl", first_name = "Bas", last_name = "Bastest", role = 1, username = "basbastest")
    val testUser2 = User(id = 2, email = "admin@admin.nl", first_name = "Abraham", last_name = "Abtesto", role = 1, username = "abrahamabtesto")
    val testUser3 = User(id = 3, email = "admin@admin.nl", first_name = "Loesie", last_name = "Poesie", role = 1, username = "loesiepoesie")
    // Doctor
    val testUser4 = User(id = 4, email = "doctor@admin.nl", first_name = "doctor", last_name = "admin", role = 2, username = "doctoradmin")


    val users = arrayListOf<User>(testUser1, testUser2, testUser3, testUser4)

    val weightUnits: ArrayList<WeightUnit> = arrayListOf(testWeightUnit1, testWeightUnit2, testWeightUnit3, testWeightUnit4)

    val role: Role = Role(0, "admin", "admin", "admin")
    private val patientShort: PatientShort = PatientShort(0, genderMale, dob, 0, 0, feedbacksShort, dietaryManagementsShort, myFoods, consumptionsShort)
    private val doctorShort: DoctorShort = DoctorShort(0, "location", 0)

    //val testDietaryRestriction1: DietaryRestriction = DietaryRestriction(1, "Eiwit","Eiwitten", weight_unit = testWeightUnit2)
    //val testDietaryRestriction2: DietaryRestriction = DietaryRestriction(2, "Kalium","Kalium", weight_unit = testWeightUnit3)

    val testDietaryRestrictionShort1 = DietaryRestrictionShort(1, "Calorieinname","Kilocalorien", weight_unit = 1)
    val testDietaryRestrictionShort2 = DietaryRestrictionShort(2, "Eiwitinname","Eiwitten", weight_unit = 2)
    val testDietaryRestrictionShort3 = DietaryRestrictionShort(3, "Kaliuminname","Kalium", weight_unit = 3)
    val testDietaryRestrictionShort4 = DietaryRestrictionShort(4, "Vochtinname","Vocht", weight_unit = 4)
    val testDietaryRestrictionShort5 = DietaryRestrictionShort(5, "Natriuminname","Natrium", weight_unit = 3)
    val testDietaryRestrictionShort6 = DietaryRestrictionShort(6, "Vezelinname","Vezels", weight_unit = 2)

    val testDietaryManagement1 = DietaryManagement(1, testDietaryRestrictionShort1, 0, 2500, true, patientShort)
    val testDietaryManagement2 = DietaryManagement(2, testDietaryRestrictionShort2, 70, 150, true, patientShort)
    val testDietaryManagement3 = DietaryManagement(3, testDietaryRestrictionShort3, 2000, 3000, true, patientShort)
    val testDietaryManagement4 = DietaryManagement(4, testDietaryRestrictionShort4, 2000, 3000, true, patientShort)
    val testDietaryManagement5 = DietaryManagement(5, testDietaryRestrictionShort5, 0, 6000, true, patientShort)
    val testDietaryManagement6 = DietaryManagement(6, testDietaryRestrictionShort6, 2000, 3000, false, patientShort)

    val dietaryManagements: ArrayList<DietaryManagement> = arrayListOf(
        testDietaryManagement1, testDietaryManagement2, testDietaryManagement3, testDietaryManagement4,
        testDietaryManagement5, testDietaryManagement6
    )

    val patientDiary = PatientDiary(1, genderMale, dob)

    val testfoodMealComponent1 = FoodMealComponent(1,"Appel (Elstar)", "Elstar appel", 25f, 0f, 0f, 0f, 100f, 0f, 25f, "https://fruitmasters.com/wp-content/uploads/2020/04/2020_Packshot_Elstar_500x500px-300x300.png", 1)
    val testfoodMealComponent2 = FoodMealComponent(2,"Volle melk (Campina)", "Volle melk van Campina", 2f, 0f, 0f, 0f, 225f, 0f, 225f, "https://d3r3h30p75xj6a.cloudfront.net/artikelen/155532_1_407418_638149077088485279.png?width=500&height=500&mode=crop", 2)

    val testfoodMealComponent3 = FoodMealComponent(3,"Appel (Elstar)", "Elstar appel", 250f, 0f, 0f, 0f, 1000f, 0f, 250f, "https://fruitmasters.com/wp-content/uploads/2020/04/2020_Packshot_Elstar_500x500px-300x300.png", 3)
    val testfoodMealComponent4 = FoodMealComponent(4,"Grote Appel (Elstar)", "Grote Elstar appel", 2500f, 0f, 0f, 0f, 100f, 0f, 2500f, "https://fruitmasters.com/wp-content/uploads/2020/04/2020_Packshot_Elstar_500x500px-300x300.png", 4)
    val testfoodMealComponent5 = FoodMealComponent(5,"Mega Appel (Elstar)", "Mega Elstar appel", 25000f, 0f, 0f, 0f, 100f, 0f, 25000f, "https://fruitmasters.com/wp-content/uploads/2020/04/2020_Packshot_Elstar_500x500px-300x300.png", 5)


    val testConsumptionResponse1 = ConsumptionResponse(1, 1f, "05-31-2023", "Ontbijt", patientDiary, testWeightUnit1, testfoodMealComponent1)
    val testConsumptionResponse2 = ConsumptionResponse(2, 1f, "05-31-2023", "Ontbijt", patientDiary, testWeightUnit4, testfoodMealComponent2)
    val testConsumptionResponse3 = ConsumptionResponse(3, 1f, "05-31-2023", "Lunch", patientDiary, testWeightUnit1, testfoodMealComponent1)
    val testConsumptionResponse4 = ConsumptionResponse(4, 1f, "05-31-2023", "Avondeten", patientDiary, testWeightUnit1, testfoodMealComponent1)
    val testConsumptionResponse5 = ConsumptionResponse(5, 1f, "05-31-2023", "Snack", patientDiary, testWeightUnit1, testfoodMealComponent1)

    val testConsumptionResponse6 = ConsumptionResponse(6, 1f, "05-31-2023", "Snack", patientDiary, testWeightUnit1, testfoodMealComponent3)
    val testConsumptionResponse7 = ConsumptionResponse(7, 1f, "05-31-2023", "Snack", patientDiary, testWeightUnit1, testfoodMealComponent4)
    val testConsumptionResponse8 = ConsumptionResponse(8, 1f, "05-31-2023", "Snack", patientDiary, testWeightUnit1, testfoodMealComponent5)

    val feedbacks: ArrayList<Feedback> = arrayListOf(
        Feedback(0, "Gesprek samenvatting", "Even beter op uw eiwit-inname letten! Ga naar www.letopuwvoeding.com voor advies.", Date("1/1/2000"), false, LocalDb.patientShort, LocalDb.doctorShort),
        Feedback(1, "Gesprek samenvatting", "Goed gedaan, ga zo door!.", Date("2/2/2002"), false, LocalDb.patientShort, LocalDb.doctorShort)
    )

    val consumptionResponses: ArrayList<ConsumptionResponse> = arrayListOf(
        testConsumptionResponse1, testConsumptionResponse2, testConsumptionResponse3, testConsumptionResponse4, testConsumptionResponse5,
        testConsumptionResponse6, testConsumptionResponse7, testConsumptionResponse8
    )

    val userLogin = UserLogin(0, "admin","admin@admin.nl", "Admin", "Admin", role, patientShort, doctorShort, null, null, "admin", confirmed = false, blocked = false)
    val testDoctor1: Doctor = Doctor(0, "location", testUser4, null, null)

    val doctors: ArrayList<Doctor> = arrayListOf(
        testDoctor1
    )

    val patients = arrayListOf(
        Patient(1, genderMale, dob, doctorShort, testUser1),
        Patient(2, genderMale, dob, doctorShort, testUser2),
        Patient(3, genderFemale, dob, doctorShort, testUser3)
    )

    val patientsShort = PatientConverter.convertAllPatients(patients)
    val doctorsShort = DoctorConverter.convertAllDoctors(doctors)

    //region Get Random
    public fun getRandomConsumptionResponses(numberOfElements: Int) : ArrayList<ConsumptionResponse> {
        var number = numberOfElements

        if (number > consumptionResponses.count())
            number = consumptionResponses.count()

        return ArrayList(consumptionResponses.shuffled().take(number))
    }
    //endregion
}