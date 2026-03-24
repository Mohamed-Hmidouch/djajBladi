# Implementation Plan: Sanitary & Security Shield

## Overview

This implementation plan breaks down the Sanitary & Security Shield feature into incremental coding tasks. The feature extends existing services (BatchService, HealthRecordService, DailyMortalityService) and introduces new components (VaccinationProtocolService, StockService) to implement withdrawal period enforcement, mortality synchronization, pharmacy integration, and vaccination compliance.

Implementation follows a bottom-up approach: database schema → entities → repositories → services → controllers, with property-based tests validating correctness properties at each layer.

## Tasks

- [ ] 1. Database schema migrations and constraints
  - [x] 1.1 Create migration to extend health_records table
    - Add columns: withdrawal_days (INTEGER), is_vaccination (BOOLEAN), stock_item_id (BIGINT), quantity_used (DECIMAL(12,4))
    - Add foreign key constraint to stock_items table
    - Create indexes on batch_id, withdrawal_days, is_vaccination, stock_item_id
    - _Requirements: 2.1, 2.2, 4.1_

  - [x] 1.2 Create migration to extend daily_mortality_records table
    - Add columns: source (VARCHAR(30)), health_record_id (BIGINT)
    - Add foreign key constraint to health_records table
    - Create indexes on source and health_record_id
    - Set default value for source to 'WORKER_REPORT'
    - _Requirements: 9.1, 9.2, 9.3_

  - [x] 1.3 Create migration to extend batches table
    - Add column: current_count (INTEGER NOT NULL)
    - Initialize current_count = chicken_count for existing batches
    - Create index on current_count
    - _Requirements: 3.1, 3.2_

  - [x] 1.4 Create migration to extend stock_items table
    - Add column: stock_type (VARCHAR(20) NOT NULL DEFAULT 'MEDICATION')
    - Modify unit_price to DECIMAL(12,4) precision
    - Create index on stock_type
    - _Requirements: 4.1, 8.2, 8.3_

  - [x] 1.5 Create migration for vaccination_protocols table
    - Create table with columns: id, strain, vaccine_name, day_of_life, notes, created_by_id, created_at, updated_at
    - Add foreign key to users table for created_by_id
    - Add unique constraint on (strain, vaccine_name, day_of_life)
    - Add check constraint: day_of_life > 0
    - Create indexes on strain and day_of_life
    - _Requirements: 5.1, 10.1, 10.3_

  - [x] 1.6 Create SQL view for vaccination_alerts
    - Create view joining batches and vaccination_protocols
    - Calculate due_date as arrival_date + day_of_life
    - Calculate days_overdue and is_overdue flags
    - Check is_completed by querying health_records with is_vaccination = true
    - Filter for active batches and vaccinations due within 7 days
    - _Requirements: 6.4, 6.5_

- [~] 2. Extend entity models with new fields and methods
  - [x] 2.1 Extend HealthRecord entity
    - Add fields: withdrawalDays (Integer), isVaccination (Boolean), stockItem (ManyToOne), quantityUsed (BigDecimal)
    - Implement getWithdrawalExpirationDate() method
    - Implement hasActiveWithdrawalPeriod() method
    - Add JPA annotations and indexes
    - _Requirements: 2.1, 2.2, 4.1_

  - [x] 2.2 Write property test for HealthRecord withdrawal calculation
    - **Property 1: Withdrawal Period Storage**
    - **Validates: Requirements 1.1**

  - [-] 2.3 Write property test for HealthRecord active withdrawal period logic
    - **Property 5: Null or Zero Withdrawal Has No Effect**
    - **Property 6: Vaccination Records Don't Block Sales**
    - **Validates: Requirements 2.3, 2.4**

  - [~] 2.4 Extend DailyMortalityRecord entity
    - Add fields: source (MortalitySource enum), healthRecord (ManyToOne)
    - Create MortalitySource enum with WORKER_REPORT and VETERINARIAN_EXAMINATION
    - Add JPA annotations and indexes
    - _Requirements: 9.1, 9.2, 9.3_

  - [~] 2.5 Extend Batch entity
    - Add field: currentCount (Integer)
    - Implement @PrePersist method to initialize currentCount = chickenCount
    - Add JPA annotations
    - _Requirements: 3.1_

  - [~] 2.6 Extend StockItem entity
    - Add field: stockType (StockType enum)
    - Create StockType enum with VACCINE, MEDICATION, FEED, EQUIPMENT
    - Modify unitPrice to use DECIMAL(12,4) precision
    - Add JPA annotations
    - _Requirements: 4.1, 8.2_

  - [~] 2.7 Create VaccinationProtocol entity
    - Create entity with fields: id, strain, vaccineName, dayOfLife, notes, createdBy, createdAt, updatedAt
    - Add unique constraint on (strain, vaccineName, dayOfLife)
    - Add validation: dayOfLife > 0
    - Add JPA annotations, indexes, and audit listeners
    - _Requirements: 5.1, 10.1, 10.2, 10.3_

  - [~] 2.8 Write property test for VaccinationProtocol validation
    - **Property 27: Positive Day-of-Life Validation**
    - **Validates: Requirements 10.3**

- [~] 3. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 4. Extend repository interfaces with new query methods
  - [~] 4.1 Extend HealthRecordRepository
    - Add findActiveWithdrawalPeriods(batchId) query method
    - Add findLatestWithdrawalExpiration(batchId) query method
    - Add findVaccinationRecords(batchId, vaccineName) query method
    - Use JPQL with DATE_ADD function for withdrawal calculations
    - _Requirements: 1.2, 6.6, 6.7_

  - [~] 4.2 Extend BatchRepository
    - Add decrementCurrentCount(batchId, mortalityCount) modifying query
    - Use atomic UPDATE with condition: current_count >= mortalityCount
    - Return int to indicate affected rows
    - _Requirements: 3.2, 3.3, 3.4_

  - [~] 4.3 Write property test for atomic batch count decrement
    - **Property 8: Atomic Mortality Decrement**
    - **Validates: Requirements 3.2, 3.3**

  - [~] 4.3 Create VaccinationProtocolRepository
    - Extend JpaRepository<VaccinationProtocol, Long>
    - Add findByStrainOrderByDayOfLifeAsc(strain) method
    - Add existsByStrainAndVaccineNameAndDayOfLife(strain, vaccineName, dayOfLife) method
    - Add findProtocolsByStrain(strain) query method
    - _Requirements: 5.3, 10.5_

  - [~] 4.4 Write property test for vaccination protocol ordering
    - **Property 28: Protocol Ordering by Day-of-Life**
    - **Validates: Requirements 10.5**

  - [~] 4.5 Create DailyMortalityRepository extensions
    - Add findBySourceAndDateBetween(source, startDate, endDate) method
    - Add findByBatchIdAndSource(batchId, source) method
    - _Requirements: 9.4_

- [ ] 5. Create DTOs for request and response objects
  - [~] 5.1 Extend HealthRecordCreateRequest DTO
    - Add fields: withdrawalDays, isVaccination, stockItemId, quantityUsed
    - Add validation annotations
    - _Requirements: 2.1, 2.2, 4.1_

  - [~] 5.2 Create VaccinationProtocolRequest DTO
    - Add fields: strain, vaccineName, dayOfLife, notes
    - Add validation: @NotBlank for strain and vaccineName, @Min(1) for dayOfLife
    - _Requirements: 10.1, 10.3_

  - [~] 5.3 Create VaccinationAlertResponse DTO
    - Add fields: batchId, batchNumber, strain, vaccineName, dueDate, daysOverdue, isOverdue
    - _Requirements: 6.1, 6.2_

  - [~] 5.4 Create VaccinationScheduleResponse DTO
    - Add fields: protocolId, vaccineName, dayOfLife, dueDate, isCompleted, completedHealthRecordId, completedDate
    - _Requirements: 5.3, 5.4_

  - [~] 5.5 Create VaccinationProtocolResponse DTO
    - Add fields: id, strain, vaccineName, dayOfLife, notes, createdBy, createdAt, updatedAt
    - _Requirements: 5.1_

- [ ] 6. Create custom exception classes
  - [~] 6.1 Create WithdrawalPeriodActiveException
    - Extend BusinessException
    - Add field: expirationDate (LocalDate)
    - Include expiration date in error message
    - _Requirements: 1.3, 1.4, 7.3_

  - [~] 6.2 Create InsufficientStockException
    - Extend BusinessException
    - Add fields: stockItemId, requested, available
    - Include stock details in error message
    - _Requirements: 4.3_

  - [~] 6.3 Create VaccinationProtocolNotFoundException
    - Extend ResourceNotFoundException
    - Include strain and vaccine name in error message
    - _Requirements: 5.1_

  - [~] 6.4 Create DuplicateVaccinationProtocolException
    - Extend BusinessException
    - Include strain, vaccine name, and day of life in error message
    - _Requirements: 10.2_

- [ ] 7. Implement StockService for pharmacy inventory management
  - [~] 7.1 Create StockService interface
    - Define methods: isAvailable, deductQuantity, getUnitPrice, findById, findByType
    - _Requirements: 4.2, 4.4, 4.5_

  - [~] 7.2 Implement StockServiceImpl
    - Implement isAvailable(stockItemId, quantity) method
    - Implement deductQuantity(stockItemId, quantity) with atomic UPDATE
    - Implement getUnitPrice(stockItemId) method
    - Use BigDecimal with 4 decimal precision for all calculations
    - Throw InsufficientStockException when stock unavailable
    - _Requirements: 4.2, 4.3, 4.4, 8.2_

  - [~] 7.3 Write property test for stock availability check
    - **Property 12: Insufficient Stock Rejection**
    - **Validates: Requirements 4.2, 4.3**

  - [~] 7.4 Write property test for stock deduction
    - **Property 13: Stock Deduction on Approval**
    - **Validates: Requirements 4.4**

  - [~] 7.5 Write unit tests for StockService edge cases
    - Test concurrent stock deductions
    - Test negative quantity rejection
    - Test non-existent stock item handling

- [ ] 8. Extend DailyMortalityService for mortality synchronization
  - [~] 8.1 Add decrementStock method to DailyMortalityService
    - Implement decrementStock(batchId, mortalityCount, recordDate, healthRecordId)
    - Call BatchRepository.decrementCurrentCount with atomic UPDATE
    - Create DailyMortalityRecord with source = VETERINARIAN_EXAMINATION
    - Link mortality record to health record
    - Throw exception if affected rows = 0 (insufficient stock)
    - _Requirements: 3.1, 3.2, 3.3, 3.5, 3.6, 9.2_

  - [~] 8.2 Write property test for mortality synchronization
    - **Property 7: Mortality Triggers Inventory Update**
    - **Validates: Requirements 3.1, 3.5**

  - [~] 8.3 Write property test for mortality source attribution
    - **Property 9: Mortality Source Attribution**
    - **Property 10: Worker Mortality Source Attribution**
    - **Validates: Requirements 3.6, 9.2, 9.3**

  - [~] 8.4 Write property test for no double-counting
    - **Property 11: No Mortality Double-Counting**
    - **Validates: Requirements 3.7, 9.5**

  - [~] 8.2 Add findBySource method to DailyMortalityService
    - Implement findBySource(source, startDate, endDate)
    - Return filtered mortality records
    - _Requirements: 9.4_

- [~] 9. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 10. Extend BatchService for withdrawal period enforcement
  - [~] 10.1 Implement isBatchSellable method in BatchService
    - Query HealthRecordRepository.findActiveWithdrawalPeriods(batchId)
    - Return true if no active withdrawal periods exist
    - Return false if any health record has active withdrawal period
    - _Requirements: 1.2, 1.5_

  - [~] 10.2 Write property test for batch sellability calculation
    - **Property 2: Batch Sellability Calculation**
    - **Validates: Requirements 1.2, 1.5**

  - [~] 10.3 Implement getWithdrawalExpirationDate method in BatchService
    - Query HealthRecordRepository.findLatestWithdrawalExpiration(batchId)
    - Return Optional<LocalDate> with latest expiration date
    - _Requirements: 1.4, 7.4_

  - [~] 10.4 Write property test for withdrawal expiration date calculation
    - **Property 25: Withdrawal Expiration Date Calculation**
    - **Validates: Requirements 7.4**

  - [~] 10.5 Implement validateStatusTransition method in BatchService
    - Check if new status is SOLD or READY_FOR_SALE
    - Call isBatchSellable(batchId)
    - If not sellable, throw WithdrawalPeriodActiveException with expiration date
    - _Requirements: 1.3, 7.1, 7.2, 7.3_

  - [~] 10.6 Write property test for withdrawal period blocking sales
    - **Property 3: Withdrawal Period Blocks Sale**
    - **Property 4: Withdrawal Error Contains Expiration Date**
    - **Validates: Requirements 1.3, 1.4, 7.1, 7.2, 7.3**

  - [~] 10.7 Integrate validateStatusTransition into BatchService.updateStatus
    - Call validateStatusTransition before updating batch status
    - Handle WithdrawalPeriodActiveException and return appropriate error response
    - _Requirements: 1.3, 7.1, 7.2_

- [ ] 11. Extend HealthRecordService for stock integration and mortality sync
  - [~] 11.1 Add stock validation to HealthRecordService.create
    - If stockItemId is provided, validate quantityUsed > 0
    - Call StockService.isAvailable(stockItemId, quantityUsed)
    - Throw InsufficientStockException if stock unavailable
    - Reject manual treatmentCost entry when stockItemId is provided
    - _Requirements: 4.2, 4.3, 8.8_

  - [~] 11.2 Write property test for insufficient stock rejection
    - **Property 12: Insufficient Stock Rejection**
    - **Validates: Requirements 4.2, 4.3**

  - [~] 11.3 Write property test for manual cost entry rejection
    - **Property 17: Manual Cost Entry Rejection**
    - **Validates: Requirements 8.8**

  - [~] 11.4 Add mortality synchronization to HealthRecordService.create
    - If mortalityCount > 0, call DailyMortalityService.decrementStock
    - Pass batchId, mortalityCount, examinationDate, and saved health record ID
    - Handle exceptions from mortality service
    - _Requirements: 3.1, 3.5_

  - [~] 11.5 Add stock deduction to HealthRecordService.approve
    - If stockItem is not null, call StockService.deductQuantity
    - Calculate treatmentCost = unitPrice × quantityUsed (4 decimal precision)
    - Store calculated cost in health record
    - Use @Transactional to ensure atomicity
    - _Requirements: 4.4, 4.5, 8.4, 8.5, 8.6_

  - [~] 11.6 Write property test for treatment cost calculation
    - **Property 14: Treatment Cost Calculation**
    - **Property 15: Monetary Precision Maintenance**
    - **Validates: Requirements 4.5, 8.2, 8.4, 8.5, 8.6**

  - [~] 11.7 Write property test for historical price immutability
    - **Property 16: Historical Price Immutability**
    - **Validates: Requirements 8.7**

  - [~] 11.8 Write unit tests for HealthRecordService edge cases
    - Test health record with zero mortality doesn't trigger sync
    - Test vaccination records with withdrawal days don't block sales
    - Test transaction rollback on stock deduction failure

- [~] 12. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 13. Implement VaccinationProtocolService for protocol management
  - [~] 13.1 Create VaccinationProtocolService interface
    - Define methods: create, update, delete, findByStrain, getAlertsForToday, getOverdueAlerts, getScheduleForBatch
    - _Requirements: 5.1, 5.3, 6.1, 6.2, 6.3_

  - [~] 13.2 Implement VaccinationProtocolServiceImpl.create
    - Validate dayOfLife > 0
    - Check for duplicate protocol using existsByStrainAndVaccineNameAndDayOfLife
    - Throw DuplicateVaccinationProtocolException if exists
    - Save protocol and return response DTO
    - _Requirements: 10.1, 10.3, 10.4_

  - [~] 13.3 Write property test for duplicate protocol prevention
    - **Property 26: Multiple Protocols Per Strain**
    - **Validates: Requirements 10.2**

  - [~] 13.4 Implement VaccinationProtocolServiceImpl.findByStrain
    - Call repository.findByStrainOrderByDayOfLifeAsc(strain)
    - Map entities to response DTOs
    - Return ordered list
    - _Requirements: 5.3, 10.5_

  - [~] 13.5 Implement VaccinationProtocolServiceImpl.getScheduleForBatch
    - Load batch and get strain
    - Query protocols for strain
    - Calculate due dates: batch.arrivalDate + protocol.dayOfLife
    - Check completion status by querying health records
    - Return schedule with completion flags
    - _Requirements: 5.3, 5.4, 5.5_

  - [~] 13.6 Write property test for vaccination due date calculation
    - **Property 18: Vaccination Due Date Calculation**
    - **Validates: Requirements 5.4**

  - [~] 13.7 Write property test for all protocols returned
    - **Property 19: All Protocols Returned for Strain**
    - **Validates: Requirements 5.5**

- [ ] 14. Implement vaccination alert generation in VaccinationProtocolService
  - [~] 14.1 Implement getAlertsForToday method
    - Query vaccination_alerts SQL view
    - Filter for due_date <= CURRENT_DATE
    - Filter out completed vaccinations (is_completed = true)
    - Map view results to VaccinationAlertResponse DTOs
    - _Requirements: 6.1, 6.3, 6.6_

  - [~] 14.2 Write property test for alert generation
    - **Property 20: Alert Generation for Due Vaccinations**
    - **Validates: Requirements 6.1**

  - [~] 14.3 Write property test for alert filtering by due date
    - **Property 22: Alert Filtering by Due Date**
    - **Validates: Requirements 6.3**

  - [~] 14.4 Write property test for completed vaccinations excluded
    - **Property 23: Completed Vaccinations Excluded from Alerts**
    - **Property 24: Vaccination Completion Removes Alert**
    - **Validates: Requirements 6.6, 6.7**

  - [~] 14.2 Implement getOverdueAlerts method
    - Query vaccination_alerts SQL view
    - Filter for is_overdue = true
    - Filter out completed vaccinations
    - Calculate daysOverdue from view
    - Map to VaccinationAlertResponse DTOs
    - _Requirements: 6.2_

  - [~] 14.3 Write property test for overdue vaccination identification
    - **Property 21: Overdue Vaccination Identification**
    - **Validates: Requirements 6.2**

  - [~] 14.4 Write unit tests for vaccination alert performance
    - Test query performance with 1000+ batches
    - Verify SQL view usage (no N+1 queries)
    - Test caching if implemented

- [ ] 15. Create REST controllers for new endpoints
  - [~] 15.1 Create VaccinationProtocolController
    - POST /api/vaccination-protocols - create protocol
    - PUT /api/vaccination-protocols/{id} - update protocol
    - DELETE /api/vaccination-protocols/{id} - delete protocol
    - GET /api/vaccination-protocols?strain={strain} - list by strain
    - Add @PreAuthorize for admin-only operations
    - Add validation and error handling
    - _Requirements: 10.1, 10.4_

  - [~] 15.2 Create VaccinationAlertController
    - GET /api/vaccination-alerts/today - get today's alerts
    - GET /api/vaccination-alerts/overdue - get overdue alerts
    - GET /api/vaccination-alerts/batch/{batchId}/schedule - get batch schedule
    - Add @PreAuthorize for veterinarian role
    - Add pagination support
    - _Requirements: 6.1, 6.2, 6.3_

  - [~] 15.3 Extend HealthRecordController
    - Update POST /api/health-records to accept new fields
    - Add validation for withdrawal period and stock integration
    - Handle WithdrawalPeriodActiveException and InsufficientStockException
    - Return appropriate error responses with details
    - _Requirements: 2.1, 2.2, 4.1, 4.2, 4.3_

  - [~] 15.4 Extend BatchController
    - Update PUT /api/batches/{id}/status to validate withdrawal periods
    - Handle WithdrawalPeriodActiveException
    - Return error response with expiration date in details
    - _Requirements: 1.3, 1.4, 7.1, 7.2, 7.3_

  - [~] 15.5 Write integration tests for controller endpoints
    - Test withdrawal period blocking batch sale via API
    - Test insufficient stock rejection via API
    - Test vaccination alert retrieval via API
    - Test error response format and status codes

- [~] 16. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 17. Add global exception handler for new exceptions
  - [~] 17.1 Extend GlobalExceptionHandler
    - Add handler for WithdrawalPeriodActiveException
    - Add handler for InsufficientStockException
    - Add handler for VaccinationProtocolNotFoundException
    - Add handler for DuplicateVaccinationProtocolException
    - Return standardized error responses with appropriate HTTP status codes
    - Include error details in response body
    - _Requirements: 1.4, 4.3, 7.3_

  - [~] 17.2 Write unit tests for exception handlers
    - Test error response format
    - Test HTTP status codes
    - Test error details inclusion

- [ ] 18. Add validation and business rule enforcement
  - [~] 18.1 Add validation to HealthRecordCreateRequest
    - If stockItemId provided, quantityUsed must be > 0
    - If withdrawalDays provided, must be >= 0
    - Cannot provide both stockItemId and manual treatmentCost
    - Add custom validator annotations
    - _Requirements: 2.1, 4.1, 8.8_

  - [~] 18.2 Add validation to VaccinationProtocolRequest
    - Strain and vaccineName cannot be blank
    - dayOfLife must be > 0
    - Add @NotBlank and @Min annotations
    - _Requirements: 10.1, 10.3_

  - [~] 18.3 Write unit tests for validation rules
    - Test all validation scenarios
    - Test custom validator behavior
    - Test error messages

- [ ] 19. Add audit logging for critical operations
  - [~] 19.1 Add audit logs for withdrawal period enforcement
    - Log when batch sale is blocked due to withdrawal period
    - Include batch ID, expiration date, and user attempting sale
    - _Requirements: 1.3, 7.1_

  - [~] 19.2 Add audit logs for mortality synchronization
    - Log when veterinarian-recorded mortality updates batch inventory
    - Include batch ID, mortality count, and veterinarian ID
    - _Requirements: 3.1, 9.2_

  - [~] 19.3 Add audit logs for stock deductions
    - Log when medication is deducted from pharmacy inventory
    - Include stock item ID, quantity, health record ID, and user
    - _Requirements: 4.4_

  - [~] 19.4 Add audit logs for vaccination protocol changes
    - Log protocol creation, updates, and deletions
    - Include protocol details and admin user
    - _Requirements: 10.4_

- [ ] 20. Final checkpoint and integration verification
  - [~] 20.1 Run all unit tests and property-based tests
    - Verify all 28 correctness properties pass
    - Verify all unit tests pass
    - Check test coverage meets requirements

  - [~] 20.2 Run integration tests
    - Test end-to-end withdrawal period enforcement flow
    - Test end-to-end mortality synchronization flow
    - Test end-to-end stock integration flow
    - Test end-to-end vaccination alert flow

  - [~] 20.3 Verify database migrations
    - Run migrations on clean database
    - Verify all tables, indexes, and constraints created
    - Verify SQL view created and functional

  - [~] 20.4 Final checkpoint - Ensure all tests pass
    - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional property-based tests that can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property-based tests validate universal correctness properties from the design document
- All monetary calculations use BigDecimal with 4 decimal precision
- Atomic SQL operations prevent race conditions in mortality synchronization
- SQL view optimizes vaccination alert performance
- Checkpoints ensure incremental validation throughout implementation
