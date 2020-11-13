package nl.stekkinger.nizi.classes.old

import androidx.lifecycle.ViewModel
import nl.stekkinger.nizi.classes.Doctor
import nl.stekkinger.nizi.classes.patient.Patient

// PatientLogin has authLogin, DoctorLogin has auth = inconsistency
data class PatientLogin (
    var account: Account,
    var patient: Patient,
    var doctor: Doctor,
    var authLogin: AuthLogin
)
    : ViewModel()