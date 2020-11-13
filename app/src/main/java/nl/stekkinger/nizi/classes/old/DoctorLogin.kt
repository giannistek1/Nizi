package nl.stekkinger.nizi.classes.old

import nl.stekkinger.nizi.classes.Doctor

data class DoctorLogin (
    var account: Account,
    var doctor: Doctor,
    var auth: AuthLogin
)