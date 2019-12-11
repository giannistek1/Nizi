package nl.stekkinger.nizi.classes

import androidx.lifecycle.ViewModel
// PatientLogin has authLogin, DoctorLogin has auth = inconsistency
data class PatientLogin (
    var account: Account?,
    var patient: Patient,
    var doctor: Doctor?,
    var authLogin: AuthLogin?)
    : ViewModel()