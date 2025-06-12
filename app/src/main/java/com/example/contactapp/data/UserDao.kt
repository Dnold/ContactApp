package com.example.contactapp.data // This declares the package for AppDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.contactapp.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // CRITICAL for upsert behavior
    suspend fun insert(user: User)

    @Query("SELECT * FROM users ORDER BY firstName ASC")
    fun getAllUsers(): Flow<List<User>> // Good practice to return Flow

    @Query("DELETE FROM users")
    suspend fun clearAll()

    @Query("DELETE FROM users") // IMPORTANT: 'users_table' MUST match your @Entity tableName
    suspend fun deleteAllUsers()
    // You might also have:
    // @Query("SELECT * FROM users WHERE uuid = :uuid LIMIT 1")
    // suspend fun getUserByUuid(uuid: String): User?
    /**
     * Selects and returns the user with the given UUID.
     *
     * @param uuid The UUID of the user. (Note: It's common practice to name parameters
     *              without leading underscores unless they clash with a class property name,
     *              e.g., just 'uuid' or 'userId' would be more conventional).
     * @return A [User] object if found, otherwise null.
     */
    @Query("SELECT * FROM users WHERE uuid = :uuid") // :_uuid refers to the function parameter
    suspend fun getUserById(uuid: String): User? // <<<< KEY CHANGE: Added return type User?

}