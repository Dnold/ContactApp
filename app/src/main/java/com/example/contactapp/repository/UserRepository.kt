package com.example.contactapp.repository

import android.util.Log
import com.example.contactapp.data.UserDao

import com.example.contactapp.model.User
import com.example.contactapp.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val dao: UserDao) {
    val users = dao.getAllUsers() // This likely returns a Flow<List<User>>

    suspend fun fetchAndStoreUsers() {
        try {
            val response = ApiClient.api.getUsers()
            val userList = response.results.map {
                User(
                    uuid = it.login.uuid,
                    firstName = it.name.first,
                    lastName = it.name.last,
                    birthDate = it.dob.date,
                    phone = it.phone,
                    photoUrl = it.picture.large,
                    email = it.email,
                    gender = it.gender,
                    age = it.dob.age,
                    nationality = it.nat,
                    street = "${it.location.street.number} ${it.location.street.name}",
                    city = it.location.city,
                    state = it.location.state,
                    country = it.location.country
                )
            }

            withContext(Dispatchers.IO) {
                // If dao.insert is set up for OnConflictStrategy.REPLACE, this is fine.
                // Otherwise, you might need separate insert and update methods in DAO.
                userList.forEach { dao.insert(it) }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to fetch and store users", e)
            // Handle error appropriately, e.g., by emitting an error state to the UI
        }
    }

    /**
     * Inserts a new user or updates an existing one if the UUID matches.
     * Assumes the UserDao's insert method uses OnConflictStrategy.REPLACE.
     */
    suspend fun addOrUpdateUser(user: User) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "Inserting/updating user: ${user.firstName} (UUID: ${user.uuid})")
                dao.insert(user) // Assumes this is an upsert operation
            } catch (e: Exception) {
                Log.e("UserRepository", "Error inserting/updating user", e)
                // Handle database error
            }
        }
    }

    suspend fun clearUsers() {
        withContext(Dispatchers.IO) {
            dao.clearAll()
        }
    }
    suspend fun getUserById(userId: String): User? {
        return withContext(Dispatchers.IO) { // Example, adjust dispatcher as needed
            dao.getUserById(userId) // Assuming UserDao has this
        }
    }
}