package nl.stekkinger.nizi.classes

import androidx.lifecycle.ViewModel
import nl.stekkinger.nizi.classes.old.Account
import nl.stekkinger.nizi.classes.old.AuthLogin
import nl.stekkinger.nizi.classes.patient.Patient

// PatientLogin has authLogin, DoctorLogin has auth = inconsistency
data class PatientLogin (
    var account: Account,
    var patient: Patient,
    var doctor: Doctor,
    var authLogin: AuthLogin
)
    : ViewModel()