package com.siga.backend.service

import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Service

@Service
class PasswordService {
    private val rounds = 12
    
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(rounds))
    }
    
    fun verifyPassword(password: String, hash: String): Boolean {
        return try {
            BCrypt.checkpw(password, hash)
        } catch (e: Exception) {
            false
        }
    }
}

