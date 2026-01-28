package org.example.djajbladibackend.exception;

/**
 * Exception personnalisée pour les ressources non trouvées
 * ✅ Spring Boot Best Practice: Exception métier spécifique
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
