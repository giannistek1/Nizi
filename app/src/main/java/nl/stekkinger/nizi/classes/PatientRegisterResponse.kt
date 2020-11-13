package nl.stekkinger.nizi.classes

import nl.stekkinger.nizi.classes.old.AccountR
import nl.stekkinger.nizi.classes.old.AuthLoginR
import nl.stekkinger.nizi.classes.old.PatientR

// AccountR, PatientR, DoctorR, AuthLoginR because these variables have to be capitalized and thus a 2nd version
// exists with capitalized variables

data class PatientRegisterResponse (
    var Account: AccountR,
    var Patient: PatientR,
    var Doctor: DoctorR,
    var AuthLogin: AuthLoginR
)