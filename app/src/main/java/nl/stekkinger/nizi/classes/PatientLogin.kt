package nl.stekkinger.nizi.classes

import androidx.lifecycle.ViewModel

data class PatientLogin (
    var account: Account,
    var patient: Patient,
    var doctor: Doctor,
    var authLogin: AuthLogin)
    : ViewModel()
{

}