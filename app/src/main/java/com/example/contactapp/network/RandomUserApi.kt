package com.example.contactapp.network


import retrofit2.http.GET

data class RandomUserResponse(val results: List<ApiUser>)
data class ApiUser(
    val gender: String,
    val name: Name,
    val login: Login,
    val dob: Dob,
    val phone: String,
    val email: String,
    val nat: String,
    val location: Location,
    val picture: Picture
)

data class Location(
    val street: Street,
    val city: String,
    val state: String,
    val country: String,
)

data class Street(val number: Int, val name: String)

data class Name(val first: String, val last: String)
data class Login(val uuid: String)
data class Dob(
    val date: String,
    val age: Int
)
data class Picture(val large: String)

interface RandomUserApi {
    @GET("api/?results=10")
    suspend fun getUsers(): RandomUserResponse
}