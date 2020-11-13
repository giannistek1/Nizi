package nl.stekkinger.nizi.classes

import nl.stekkinger.nizi.classes.old.Account
import nl.stekkinger.nizi.classes.old.AuthLogin

data class DoctorLogin (
    var account: Account,
    var doctor: Doctor,
    var auth: AuthLogin
)
{

}