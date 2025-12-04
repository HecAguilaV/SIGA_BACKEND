package com.siga.backend.exception

import com.siga.backend.utils.ErrorSanitizer
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.HashMap

@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * Maneja errores de validación (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = HashMap<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as? FieldError)?.field ?: error.objectName
            val errorMessage = error.defaultMessage ?: "Error de validación"
            errors[fieldName] = errorMessage
        }
        
        logger.warn("Error de validación: {}", errors)
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf(
            "success" to false,
            "message" to "Error de validación",
            "errors" to errors
        ))
    }
    
    /**
     * Maneja excepciones de integridad de datos (JPA)
     */
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<Map<String, Any>> {
        logger.error("Error de integridad de datos", ex)
        
        val message = when {
            ex.message?.contains("unique constraint") == true -> 
                "El recurso ya existe o viola una restricción única"
            ex.message?.contains("foreign key constraint") == true -> 
                "No se puede eliminar el recurso porque está en uso"
            else -> 
                "Error de integridad de datos"
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf(
            "success" to false,
            "message" to message
        ))
    }
    
    /**
     * Maneja excepciones de Gemini API
     */
    @ExceptionHandler(GeminiApiException::class)
    fun handleGeminiApiException(ex: GeminiApiException): ResponseEntity<Map<String, Any>> {
        logger.error("Error en API de Gemini", ex)
        
        // Sanitizar el mensaje para no exponer API keys
        val sanitizedMessage = ErrorSanitizer.sanitizeException(ex)
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
            "success" to false,
            "message" to "Error al procesar la solicitud con el asistente IA. Por favor, intente más tarde."
        ))
    }
    
    /**
     * Maneja excepciones de validación personalizadas
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<Map<String, Any>> {
        logger.warn("Error de validación: {}", ex.message)
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf(
            "success" to false,
            "message" to (ex.message ?: "Error de validación")
        ))
    }
    
    /**
     * Maneja todas las demás excepciones no controladas
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<Map<String, Any>> {
        logger.error("Error no controlado", ex)
        
        // Sanitizar el mensaje para no exponer información sensible
        val sanitizedMessage = ErrorSanitizer.sanitizeException(ex)
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
            "success" to false,
            "message" to sanitizedMessage
        ))
    }
}

