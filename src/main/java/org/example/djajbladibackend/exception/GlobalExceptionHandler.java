package org.example.djajbladibackend.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions
 * ✅ Spring Boot Best Practice: @RestControllerAdvice pour gestion centralisée des erreurs
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Les données saisies sont invalides. Veuillez corriger les erreurs ci-dessous."
        );
        problemDetail.setTitle("Erreur de validation");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.error("Validation error: {}", errors);
        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "Email ou mot de passe incorrect."
        );
        problemDetail.setTitle("Échec d'authentification");
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.error("Bad credentials: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setTitle("Ressource introuvable");
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.error("Resource not found: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(RegistrationNotAllowedException.class)
    public ProblemDetail handleRegistrationNotAllowed(RegistrationNotAllowedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            ex.getMessage()
        );
        problemDetail.setTitle("Inscription non autorisée");
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.warn("Registration not allowed: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(InvalidRoleForAdminCreationException.class)
    public ProblemDetail handleInvalidRoleForAdminCreation(InvalidRoleForAdminCreationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Rôle invalide");
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.warn("Invalid role for admin user creation: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateBatchNumberException.class)
    public ProblemDetail handleDuplicateBatchNumber(DuplicateBatchNumberException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Numéro de lot en double");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Duplicate batch number: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Email déjà utilisé");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Email already exists: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateDailyMortalityException.class)
    public ProblemDetail handleDuplicateDailyMortality(DuplicateDailyMortalityException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Mortalité déjà enregistrée");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Duplicate daily mortality: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(HealthRecordNotPendingException.class)
    public ProblemDetail handleHealthRecordNotPending(HealthRecordNotPendingException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Rapport de santé non en attente");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Health record not pending: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(ForbiddenRoleException.class)
    public ProblemDetail handleForbiddenRole(ForbiddenRoleException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            ex.getMessage()
        );
        problemDetail.setTitle("Accès refusé");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Forbidden role: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Requête invalide");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Invalid argument: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(BatchNotActiveException.class)
    public ProblemDetail handleBatchNotActive(BatchNotActiveException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Lot non actif");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Batch not active: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(MortalityExceedsBatchSizeException.class)
    public ProblemDetail handleMortalityExceedsBatchSize(MortalityExceedsBatchSizeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Mortalité dépasse la taille du lot");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Mortality exceeds batch size: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(DateRangeTooLargeException.class)
    public ProblemDetail handleDateRangeTooLarge(DateRangeTooLargeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Plage de dates trop grande");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Date range too large: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(InvalidDataException.class)
    public ProblemDetail handleInvalidData(InvalidDataException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Données invalides");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Invalid data: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String detail = ex.getCause() instanceof JsonProcessingException
                ? "Le format des données envoyées est incorrect. Vérifiez les champs et réessayez."
                : "Le corps de la requête n'a pas pu être lu. Vérifiez le format des données.";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle("Requête malformée");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Malformed request: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Le paramètre '" + ex.getParameterName() + "' est obligatoire et n'a pas été fourni."
        );
        problemDetail.setTitle("Paramètre manquant");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Missing parameter: {}", ex.getParameterName());
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a
                ));
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Les paramètres de la requête sont invalides."
        );
        problemDetail.setTitle("Erreur de validation des paramètres");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Constraint violation: {}", errors);
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = "Le paramètre '" + ex.getName() + "' a une valeur invalide. Type attendu : " +
                (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "inconnu");
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle("Type de paramètre invalide");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Type mismatch: {} = {}", ex.getName(), ex.getValue());
        return problemDetail;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.METHOD_NOT_ALLOWED,
            "La méthode HTTP " + ex.getMethod() + " n'est pas supportée pour cette action."
        );
        problemDetail.setTitle("Méthode non autorisée");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Method not allowed: {}", ex.getMethod());
        return problemDetail;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Le format " + ex.getContentType() + " n'est pas supporté. Utilisez application/json."
        );
        problemDetail.setTitle("Format non supporté");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Unsupported media type: {}", ex.getContentType());
        return problemDetail;
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Stock insuffisant");
        problemDetail.setProperty("stockItemId", ex.getStockItemId());
        problemDetail.setProperty("stockItemName", ex.getStockItemName());
        problemDetail.setProperty("availableKg", ex.getAvailable());
        problemDetail.setProperty("requestedKg", ex.getRequested());
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Insufficient stock: stockItemId={}, available={}, requested={}",
            ex.getStockItemId(), ex.getAvailable(), ex.getRequested());
        return problemDetail;
    }

    @ExceptionHandler(WithdrawalPeriodActiveException.class)
    public ProblemDetail handleWithdrawalPeriodActive(WithdrawalPeriodActiveException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Période de retrait en cours");
        problemDetail.setProperty("batchId", ex.getBatchId());
        problemDetail.setProperty("expirationDate", ex.getExpirationDate());
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Sale blocked by withdrawal period: batchId={}, expirationDate={}",
            ex.getBatchId(), ex.getExpirationDate());
        return problemDetail;
    }

    @ExceptionHandler(BatchNotForSaleException.class)
    public ProblemDetail handleBatchNotForSale(BatchNotForSaleException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Lot non disponible à la vente");
        problemDetail.setProperty("batchId", ex.getBatchId());
        problemDetail.setProperty("currentStatus", ex.getCurrentStatus());
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Batch not for sale: batchId={}, status={}", ex.getBatchId(), ex.getCurrentStatus());
        return problemDetail;
    }

    @ExceptionHandler(OrderQuantityExceedsStockException.class)
    public ProblemDetail handleOrderQuantityExceedsStock(OrderQuantityExceedsStockException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Quantité commandée supérieure au stock");
        problemDetail.setProperty("batchId", ex.getBatchId());
        problemDetail.setProperty("requested", ex.getRequested());
        problemDetail.setProperty("available", ex.getAvailable());
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Order exceeds stock: batchId={}, requested={}, available={}",
            ex.getBatchId(), ex.getRequested(), ex.getAvailable());
        return problemDetail;
    }

    @ExceptionHandler(OrderNotCancellableException.class)
    public ProblemDetail handleOrderNotCancellable(OrderNotCancellableException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Commande non annulable");
        problemDetail.setProperty("orderId", ex.getOrderId());
        problemDetail.setProperty("currentStatus", ex.getCurrentStatus());
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Order not cancellable: orderId={}, status={}", ex.getOrderId(), ex.getCurrentStatus());
        return problemDetail;
    }

    @ExceptionHandler(OrderOwnershipException.class)
    public ProblemDetail handleOrderOwnership(OrderOwnershipException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            ex.getMessage()
        );
        problemDetail.setTitle("Accès refusé à cette commande");
        problemDetail.setProperty("orderId", ex.getOrderId());
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Order ownership denied: orderId={}", ex.getOrderId());
        return problemDetail;
    }

    @ExceptionHandler(MinimumOrderQuantityException.class)
    public ProblemDetail handleMinimumOrderQuantity(MinimumOrderQuantityException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Quantité minimale non atteinte");
        problemDetail.setProperty("batchId", ex.getBatchId());
        problemDetail.setProperty("requested", ex.getRequested());
        problemDetail.setProperty("minimum", ex.getMinimum());
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Min order quantity not met: batchId={}, requested={}, minimum={}",
            ex.getBatchId(), ex.getRequested(), ex.getMinimum());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateVaccinationProtocolException.class)
    public ProblemDetail handleDuplicateVaccinationProtocol(DuplicateVaccinationProtocolException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Protocole de vaccination en double");
        problemDetail.setProperty("strain", ex.getStrain());
        problemDetail.setProperty("vaccineName", ex.getVaccineName());
        problemDetail.setProperty("dayOfLife", ex.getDayOfLife());
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Duplicate vaccination protocol: strain={}, vaccine={}, day={}",
            ex.getStrain(), ex.getVaccineName(), ex.getDayOfLife());
        return problemDetail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        String detail = message != null && (message.contains("unique") || message.contains("duplicate"))
                ? "Un enregistrement identique existe déjà. Veuillez vérifier les données."
                : "Les données sont en conflit avec les enregistrements existants.";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
        problemDetail.setTitle("Conflit de données");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Data integrity violation: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNotFound(NoHandlerFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "Aucune ressource trouvée pour " + ex.getHttpMethod() + " " + ex.getRequestURL()
        );
        problemDetail.setTitle("Page introuvable");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("No handler: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Une erreur interne est survenue. Veuillez réessayer plus tard."
        );
        problemDetail.setTitle("Erreur interne du serveur");
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.error("Unexpected error", ex);
        return problemDetail;
    }
}
