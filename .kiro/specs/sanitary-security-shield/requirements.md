# Requirements Document

## Introduction

The Sanitary & Security Shield feature implements critical health and safety compliance controls for poultry farming operations. This feature ensures adherence to veterinary drug residue regulations, maintains accurate mortality tracking, integrates pharmacy inventory management with veterinary treatments, and automates vaccination protocol compliance. The system prevents illegal sale of poultry during antibiotic withdrawal periods, synchronizes mortality data across different recording sources, tracks medication usage and costs, and alerts veterinarians to mandatory vaccination schedules.

## Glossary

- **Withdrawal_Period**: The mandatory waiting time (in days) after antibiotic administration during which poultry cannot be sold due to drug residue regulations

- **Batch**: A group of chickens managed as a single unit with tracking for health, mortality, and sales status

- **Health_Record**: A veterinary examination record documenting diagnosis, treatment, mortality, and medication usage for a batch

- **Sanitary_Lock**: A status flag preventing batch sale operations when an active withdrawal period exists

- **Mortality_Synchronization**: The automatic update of batch living inventory when veterinarian records deaths in examination reports

- **Stock_Item**: A pharmacy inventory item (vaccine or medication) with quantity and unit price tracking

- **Vaccination_Protocol**: A strain-specific schedule defining mandatory vaccination dates for chicken breeds

- **Treatment_Validation**: The verification that required medications are in stock before recording veterinary treatments

- **Batch_Service**: The service component responsible for batch status management and sellability validation

- **Health_Record_Service**: The service component responsible for creating and managing veterinary examination records

- **Daily_Mortality_Service**: The service component responsible for tracking daily death counts and updating batch inventory

- **Vaccination_Protocol_Service**: The service component responsible for managing vaccination schedules and generating compliance alerts

## Requirements

### Requirement 1: Withdrawal Period Enforcement

**User Story:** As a farm administrator, I want the system to prevent sale of batches during antibiotic withdrawal periods, so that we comply with drug residue regulations and avoid legal penalties.

#### Acceptance Criteria

1. WHEN a Health_Record with withdrawalDays > 0 is created, THE Health_Record_Service SHALL store the withdrawal period duration

2. WHEN calculating batch sellability, THE Batch_Service SHALL verify that current date >= (examination date + withdrawal days) for all Health_Records associated with the batch

3. WHEN an administrator attempts to change batch status to SOLD, THE Batch_Service SHALL block the operation if any active withdrawal period exists

4. WHEN a sale is blocked due to withdrawal period, THE Batch_Service SHALL return an error message containing the calculated expiration date

5. THE Batch_Service SHALL provide an isBatchSellable() method that returns true only when no active withdrawal periods exist

### Requirement 2: Withdrawal Period Data Model

**User Story:** As a veterinarian, I want to record withdrawal period requirements when administering antibiotics, so that the system can enforce compliance automatically.

#### Acceptance Criteria

1. THE Health_Record SHALL store a withdrawalDays field as an integer value

2. THE Health_Record SHALL store an isVaccination field as a boolean value

3. WHEN withdrawalDays is null or zero, THE Health_Record_Service SHALL treat the record as having no withdrawal period

4. WHEN isVaccination is true, THE Health_Record_Service SHALL treat the record as a vaccination with no withdrawal period enforcement

### Requirement 3: Mortality Synchronization

**User Story:** As a farm manager, I want veterinarian-recorded deaths to automatically update batch inventory, so that living chicken counts remain accurate without manual reconciliation.

#### Acceptance Criteria

1. WHEN a Health_Record with mortalityCount > 0 is created, THE Health_Record_Service SHALL invoke Daily_Mortality_Service to update batch inventory

2. THE Daily_Mortality_Service SHALL use atomic SQL UPDATE operation to decrement batch inventory: UPDATE batches SET current_count = current_count - :mortalityCount WHERE batch_id = :batchId

3. THE Daily_Mortality_Service SHALL implement a decrementStock method that performs database-level atomic decrement operations to prevent race conditions

4. THE Daily_Mortality_Service SHALL NOT calculate new count values in application code when updating batch inventory

5. THE Health_Record_Service SHALL pass the examination date, batch identifier, and mortality count to Daily_Mortality_Service

6. WHEN mortality synchronization occurs, THE Daily_Mortality_Service SHALL create a mortality record attributed to the veterinarian examination

7. THE Daily_Mortality_Service SHALL prevent double-counting by marking mortality records with their source (worker-reported vs veterinarian-examination)

### Requirement 4: Stock Integration for Treatments

**User Story:** As a veterinarian, I want the system to verify medication availability and automatically deduct used quantities, so that pharmacy inventory remains accurate and treatment costs are calculated correctly.

#### Acceptance Criteria

1. THE Health_Record SHALL store a reference to the Stock_Item used in treatment

2. WHEN a veterinarian creates a Health_Record with medication, THE Health_Record_Service SHALL verify the Stock_Item exists and has sufficient quantity

3. WHEN medication quantity is insufficient, THE Health_Record_Service SHALL return an error message indicating the stock shortage

4. WHEN a Health_Record is approved, THE Health_Record_Service SHALL deduct the used quantity from Stock_Item inventory

5. WHEN calculating treatment cost, THE Health_Record_Service SHALL multiply Stock_Item unit price by quantity used instead of accepting manual cost entry

### Requirement 5: Vaccination Protocol Management

**User Story:** As a veterinarian, I want to define vaccination schedules for each chicken strain, so that the system can track mandatory vaccination requirements.

#### Acceptance Criteria

1. THE Vaccination_Protocol_Service SHALL store vaccination schedules with strain identifier, vaccine name, and day-of-life for administration

2. WHEN a new batch is created, THE Vaccination_Protocol_Service SHALL associate the batch with the appropriate vaccination protocol based on chicken strain

3. THE Vaccination_Protocol_Service SHALL provide a method to retrieve all vaccination requirements for a given batch

4. THE Vaccination_Protocol_Service SHALL calculate due dates by adding protocol day-of-life to batch start date

5. WHERE a vaccination protocol exists for a strain, THE Vaccination_Protocol_Service SHALL return all scheduled vaccinations with their calculated due dates

### Requirement 6: Vaccination Compliance Alerts

**User Story:** As a veterinarian, I want to receive alerts when batches require vaccination, so that I can administer vaccines on schedule and maintain compliance.

#### Acceptance Criteria

1. WHEN a batch reaches a vaccination due date, THE Vaccination_Protocol_Service SHALL generate an alert containing batch identifier, vaccine name, and due date

2. THE Vaccination_Protocol_Service SHALL identify overdue vaccinations where current date > due date and no corresponding Health_Record exists

3. WHEN retrieving alerts for a veterinarian dashboard, THE Vaccination_Protocol_Service SHALL return all vaccinations due today or overdue

4. THE Vaccination_Protocol_Service SHALL use either SQL View for pre-computed vaccination due dates OR lightweight caching service to avoid recalculating alerts on every request

5. THE Vaccination_Protocol_Service SHALL NOT recalculate vaccination alerts on every request to maintain performance efficiency

6. THE Vaccination_Protocol_Service SHALL exclude vaccinations that have already been administered based on existing Health_Records

7. WHEN a Health_Record with isVaccination = true is created, THE Vaccination_Protocol_Service SHALL mark the corresponding protocol vaccination as completed

### Requirement 7: Batch Status Validation

**User Story:** As a system administrator, I want batch status transitions to be validated against sanitary rules, so that illegal operations are prevented automatically.

#### Acceptance Criteria

1. WHEN a batch status change to READY_FOR_SALE is requested, THE Batch_Service SHALL verify no active withdrawal periods exist

2. WHEN a batch status change to SOLD is requested, THE Batch_Service SHALL verify no active withdrawal periods exist

3. IF an active withdrawal period exists during status change, THEN THE Batch_Service SHALL reject the operation and return the withdrawal expiration date

4. THE Batch_Service SHALL calculate withdrawal expiration as examination date + withdrawal days for each Health_Record

5. THE Batch_Service SHALL consider a withdrawal period active when current date < withdrawal expiration date

### Requirement 8: Treatment Cost Automation

**User Story:** As a farm accountant, I want treatment costs to be calculated automatically from pharmacy prices, so that financial records are accurate and manual entry errors are eliminated.

#### Acceptance Criteria

1. WHEN a Health_Record includes medication usage, THE Health_Record_Service SHALL retrieve the Stock_Item unit price

2. THE Health_Record_Service SHALL use BigDecimal with 4 decimal places for all monetary calculations

3. THE Stock_Item SHALL store unit prices with 4 decimal places precision

4. THE Health_Record_Service SHALL calculate treatment cost as Stock_Item unit price multiplied by quantity used

5. THE Health_Record_Service SHALL maintain 4 decimal places precision throughout all treatment cost calculations

6. THE Health_Record_Service SHALL store the calculated treatment cost in the Health_Record with 4 decimal places precision

7. WHEN Stock_Item unit price is updated, THE Health_Record_Service SHALL use the price at the time of treatment for historical records

8. THE Health_Record_Service SHALL reject manual treatment cost entry when a Stock_Item is associated with the Health_Record

### Requirement 9: Mortality Source Attribution

**User Story:** As a data analyst, I want to distinguish between worker-reported and veterinarian-examined mortality, so that I can analyze death patterns and audit data accuracy.

#### Acceptance Criteria

1. THE Daily_Mortality_Service SHALL store a source field indicating whether mortality was reported by worker or veterinarian examination

2. WHEN mortality is recorded through Health_Record, THE Daily_Mortality_Service SHALL set source to "VETERINARIAN_EXAMINATION"

3. WHEN mortality is recorded through daily worker reports, THE Daily_Mortality_Service SHALL set source to "WORKER_REPORT"

4. THE Daily_Mortality_Service SHALL provide a method to retrieve mortality records filtered by source

5. WHEN calculating total mortality for a batch, THE Daily_Mortality_Service SHALL sum mortality from all sources without duplication

### Requirement 10: Vaccination Protocol Data Model

**User Story:** As a system administrator, I want to configure vaccination protocols for different chicken strains, so that the system can enforce breed-specific vaccination requirements.

#### Acceptance Criteria

1. THE Vaccination_Protocol_Service SHALL store protocol entries with strain name, vaccine name, day-of-life, and optional notes

2. THE Vaccination_Protocol_Service SHALL support multiple vaccination entries for a single strain

3. THE Vaccination_Protocol_Service SHALL validate that day-of-life values are positive integers

4. THE Vaccination_Protocol_Service SHALL allow protocol entries to be created, updated, and deleted by administrators

5. WHERE multiple protocols exist for a strain, THE Vaccination_Protocol_Service SHALL return them ordered by day-of-life ascending
