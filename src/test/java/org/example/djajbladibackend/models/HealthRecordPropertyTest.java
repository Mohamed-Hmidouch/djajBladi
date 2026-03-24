package org.example.djajbladibackend.models;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for HealthRecord entity
 * Uses jqwik to verify properties hold across many random inputs
 */
class HealthRecordPropertyTest {

    /**
     * Property 1: Withdrawal Period Storage
     * 
     * **Validates: Requirements 1.1**
     * 
     * For any health record created with withdrawalDays > 0, retrieving that 
     * health record should return the same withdrawalDays value that was stored.
     */
    @Property
    @Label("Property 1: Withdrawal Period Storage - withdrawalDays value is preserved")
    void withdrawalDaysValueIsPreserved(
            @ForAll @IntRange(min = 1, max = 365) int withdrawalDays,
            @ForAll("examinationDates") LocalDate examinationDate) {
        
        // Given: a health record with withdrawalDays > 0
        HealthRecord record = HealthRecord.builder()
                .withdrawalDays(withdrawalDays)
                .examinationDate(examinationDate)
                .isVaccination(false)
                .build();
        
        // When: retrieving the withdrawalDays value
        Integer retrievedWithdrawalDays = record.getWithdrawalDays();
        
        // Then: the retrieved value should match the stored value
        assertThat(retrievedWithdrawalDays).isEqualTo(withdrawalDays);
    }

    /**
     * Provides arbitrary examination dates for property tests
     * Generates dates within a reasonable range (past 2 years to future 1 year)
     */
    @Provide
    Arbitrary<LocalDate> examinationDates() {
        LocalDate minDate = LocalDate.now().minusYears(2);
        LocalDate maxDate = LocalDate.now().plusYears(1);
        
        return Arbitraries.longs()
                .between(minDate.toEpochDay(), maxDate.toEpochDay())
                .map(LocalDate::ofEpochDay);
    }
}
