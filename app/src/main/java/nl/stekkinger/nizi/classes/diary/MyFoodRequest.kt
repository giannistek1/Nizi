package nl.stekkinger.nizi.classes.diary

data class MyFoodRequest(
    val id: Int = 0,
    val food: Int,
    val patients_ids: Int
)