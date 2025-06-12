package com.example.contactapp.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uuid: String,

    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val phone: String,
    val photoUrl: String,


    val email: String,
    val nationality: String,
    val gender: String,
    val age: Int,
    val street: String,
    val city: String,
    val state: String,
    val country: String,
)

// Helper to convert User to JSON String (place in User.kt or a util file)
fun User.toJsonString(): String {
    val userDataJson = JSONObject().apply {
        put("uuid", this@toJsonString.uuid)
        put("firstName", this@toJsonString.firstName)
        put("lastName", this@toJsonString.lastName)
        put("address", this@toJsonString.birthDate ?: JSONObject.NULL)
        put("email", this@toJsonString.email ?: JSONObject.NULL) // Handle nullables
        put("phone", this@toJsonString.phone ?: JSONObject.NULL)
        put("gender", this@toJsonString.gender ?: JSONObject.NULL)
        put("nationality", this@toJsonString.nationality ?: JSONObject.NULL)
        put( "age", this@toJsonString.age ?: JSONObject.NULL)
        put("street", this@toJsonString.street ?: JSONObject.NULL)
        put("city", this@toJsonString.city ?: JSONObject.NULL)
        put("state", this@toJsonString.state ?: JSONObject.NULL)
        put("country", this@toJsonString.country ?: JSONObject.NULL)

        put("photoUrl", this@toJsonString.photoUrl ?: JSONObject.NULL)
        // Add other fields
    }
    val rootJson = JSONObject().apply {
        put("appIdentifier", "com.example.contactapp.user")
        put("version", 1)
        put("userData", userDataJson)
    }
    return rootJson.toString()
}