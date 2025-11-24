package com.siga.backend.services

import org.mindrot.jbcrypt.BCrypt

object PasswordService {
    private const val ROUNDS = 12
    
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(ROUNDS))
    }
    
    fun verifyPassword(password: String, hash: String): Boolean {
        return try {
            BCrypt.checkpw(password, hash)
        } catch (e: Exception) {
            false
        }
    }
}