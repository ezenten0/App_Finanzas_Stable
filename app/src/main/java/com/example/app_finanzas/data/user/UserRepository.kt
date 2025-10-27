package com.example.app_finanzas.data.user

import com.example.app_finanzas.data.local.user.UserDao
import com.example.app_finanzas.data.local.user.UserEntity

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(name: String, email: String, password: String): Result<UserProfile> {
        val normalizedEmail = email.trim().lowercase()
        val existingUser = userDao.getUserByEmail(normalizedEmail)
        if (existingUser != null) {
            return Result.failure(IllegalStateException("Ya existe un usuario con este correo."))
        }

        val passwordHash = PasswordHasher.hash(password)
        val userId = userDao.insert(
            UserEntity(
                name = name.trim(),
                email = normalizedEmail,
                passwordHash = passwordHash
            )
        )

        return Result.success(
            UserProfile(
                id = userId.toInt(),
                name = name.trim(),
                email = normalizedEmail
            )
        )
    }

    suspend fun authenticate(email: String, password: String): Result<UserProfile> {
        val normalizedEmail = email.trim().lowercase()
        val passwordHash = PasswordHasher.hash(password)
        val user = userDao.authenticate(normalizedEmail, passwordHash)
            ?: return Result.failure(IllegalArgumentException("Correo o contraseña incorrectos."))

        return Result.success(
            UserProfile(
                id = user.id,
                name = user.name,
                email = user.email
            )
        )
    }
}
