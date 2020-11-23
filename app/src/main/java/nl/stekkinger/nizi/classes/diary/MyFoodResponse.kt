package nl.stekkinger.nizi.classes.diary

data class MyFoodResponse(
    val id: Int = 0,
    val food: FoodResponseShort,
    val patients_ids: ArrayList<PatientDiary> = arrayListOf()
)