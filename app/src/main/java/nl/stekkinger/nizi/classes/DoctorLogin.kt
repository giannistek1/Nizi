package nl.stekkinger.nizi.classes

import androidx.lifecycle.ViewModel

data class DoctorLogin (
    var account: Account,
    var doctor: Doctor,
    var auth: AuthLogin)
{

}