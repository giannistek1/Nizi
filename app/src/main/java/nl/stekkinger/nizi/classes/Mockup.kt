package nl.stekkinger.nizi.classes

import nl.stekkinger.nizi.classes.diary.ConsumptionShort
import nl.stekkinger.nizi.classes.diary.MyFood
import nl.stekkinger.nizi.classes.dietary.DietaryManagementShort
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
import java.time.LocalDate
import java.util.Date

// Reverse order
object Mockup {
    const val jwt: String = "admin"

    val weightUnits: ArrayList<WeightUnit> = arrayListOf(WeightUnit(1, "gram","gr"), WeightUnit(2, "kilogram","kg"), WeightUnit(3, "liter","L"))

    val feedbacksShort: ArrayList<FeedbackShort>? = arrayListOf()
    val dietaryManagementsShort: ArrayList<DietaryManagementShort>? = arrayListOf()
    val myFoods: ArrayList<MyFood>? = arrayListOf()
    val consumptions: ArrayList<ConsumptionShort>? = arrayListOf()

    val role: Role = Role(0, "admin", "admin", "admin")
    val patientShort: PatientShort = PatientShort(0, "male", "0-0-0000 00:00:00", 0, 0, feedbacksShort, dietaryManagementsShort, myFoods, consumptions)
    val doctorShort: DoctorShort = DoctorShort(0, "location", 0)

    val feedbacks: ArrayList<Feedback> = arrayListOf(Feedback(0, "Samenvatting", "Even beter op uw eiwitinname letten! Ga naar www.letopuwvoeding.com voor advies.", Date("1/1/2000"), false, Mockup.patientShort, Mockup.doctorShort),
        Feedback(0, "Samenvatting", "Goed gedaan, ga zo door!.", Date("2/2/2002"), false, Mockup.patientShort, Mockup.doctorShort))

    val userLogin: UserLogin = UserLogin(0, "admin","admin@admin.nl", "Admin", "Admin", role, patientShort, doctorShort, null, null, "admin", confirmed = false, blocked = false)
    val user: User = User(email = "admin@admin.nl", first_name = "admin", last_name = "admin", role = 0, username = "admin")
    val testUser1: User = User(email = "admin@admin.nl", first_name = "Bas", last_name = "Test", role = 0, username = "admin")
    val testUser2: User = User(email = "admin@admin.nl", first_name = "Bram", last_name = "Testo", role = 0, username = "admin")
    val doctor: Doctor = Doctor(0, "location", user, null, null)

    val patients: ArrayList<Patient> = arrayListOf(Patient(1, "male", "0-0-0000 00:00:00", doctorShort, testUser1), Patient(2, "male", "0-0-0000 00:00:00", doctorShort, testUser2))
}