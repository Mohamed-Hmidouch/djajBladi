package org.example.djajbladibackend.models;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HealthRecord entity
 * Tests the withdrawal period calculation and active withdrawal period logic
 */
class HealthRecordTest {

    @Test
    void getWithdrawalExpirationDate_withValidWithdrawalDays_returnsCorrectDate() {
        // Given
        LocalDate examinationDate = LocalDate.of(2024, 1, 15);
        HealthRecord record = HealthRecord.builder()
                .examinationDate(examinationDate)
                .withdrawalDays(7)
                .isVaccination(false)
                .build();

        // When
        LocalDate expirationDate = record.getWithdrawalExpirationDate();

        // Then
        assertThat(expirationDate).isEqualTo(LocalDate.of(2024, 1, 22));
    }

    @Test
    void getWithdrawalExpirationDate_withNullWithdrawalDays_returnsNull() {
        // Given
        HealthRecord record = HealthRecord.builder()
                .examinationDate(LocalDate.now())
                .withdrawalDays(null)
                .isVaccination(false)
                .build();

        // When
        LocalDate expirationDate = record.getWithdrawalExpirationDate();

        // Then
        assertThat(expirationDate).isNull();
    }

    @Test
    void getWithdrawalExpirationDate_withZeroWithdrawalDays_returnsNull() {
        // Given
        HealthRecord record = HealthRecord.builder()
                .examinationDate(LocalDate.now())
                .withdrawalDays(0)
                .isVaccination(false)
                .build();

        // When
        LocalDate expirationDate = record.getWithdrawalExpirationDate();

        // Then
        assertThat(expirationDate).isNull();
    }

    @Test
    void hasActiveWithdrawalPeriod_withFutureExpiration_returnsTrue() {
        // Given
        LocalDate examinationDate = LocalDate.now().minusDays(3);
        HealthRecord record = HealthRecord.builder()
                .examinationDate(examinationDate)
                .withdrawalDays(7)
                .isVaccination(false)
                .build();

        // When
        boolean hasActive = record.hasActiveWithdrawalPeriod();

        // Then
        assertThat(hasActive).isTrue();
    }

    @Test
    void hasActiveWithdrawalPeriod_withPastExpiration_returnsFalse() {
        // Given
        LocalDate examinationDate = LocalDate.now().minusDays(10);
        HealthRecord record = HealthRecord.builder()
                .examinationDate(examinationDate)
                .withdrawalDays(7)
                .isVaccination(false)
                .build();

        // When
        boolean hasActive = record.hasActiveWithdrawalPeriod();

        // Then
        assertThat(hasActive).isFalse();
    }

    @Test
    void hasActiveWithdrawalPeriod_withVaccination_returnsFalse() {
        // Given
        LocalDate examinationDate = LocalDate.now().minusDays(3);
        HealthRecord record = HealthRecord.builder()
                .examinationDate(examinationDate)
                .withdrawalDays(7)
                .isVaccination(true)
                .build();

        // When
        boolean hasActive = record.hasActiveWithdrawalPeriod();

        // Then
        assertThat(hasActive).isFalse();
    }

    @Test
    void hasActiveWithdrawalPeriod_withNullWithdrawalDays_returnsFalse() {
        // Given
        HealthRecord record = HealthRecord.builder()
                .examinationDate(LocalDate.now())
                .withdrawalDays(null)
                .isVaccination(false)
                .build();

        // When
        boolean hasActive = record.hasActiveWithdrawalPeriod();

        // Then
        assertThat(hasActive).isFalse();
    }

    @Test
    void hasActiveWithdrawalPeriod_withZeroWithdrawalDays_returnsFalse() {
        // Given
        HealthRecord record = HealthRecord.builder()
                .examinationDate(LocalDate.now())
                .withdrawalDays(0)
                .isVaccination(false)
                .build();

        // When
        boolean hasActive = record.hasActiveWithdrawalPeriod();

        // Then
        assertThat(hasActive).isFalse();
    }

    @Test
    void builder_withDefaultValues_setsIsVaccinationToFalse() {
        // Given & When
        HealthRecord record = HealthRecord.builder()
                .examinationDate(LocalDate.now())
                .build();

        // Then
        assertThat(record.getIsVaccination()).isFalse();
    }
}
