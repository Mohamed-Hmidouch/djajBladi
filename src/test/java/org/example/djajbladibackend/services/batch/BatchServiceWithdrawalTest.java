package org.example.djajbladibackend.services.batch;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.example.djajbladibackend.exception.WithdrawalPeriodActiveException;
import org.example.djajbladibackend.models.HealthRecord;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.BuildingRepository;
import org.example.djajbladibackend.repository.HealthRecordRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for BatchService withdrawal period enforcement.
 * Requirements: 1.2, 1.3, 1.4, 1.5, 7.1, 7.2, 7.3, 7.4
 */
class BatchServiceWithdrawalTest {

    private final BatchRepository batchRepository = Mockito.mock(BatchRepository.class);
    private final BuildingRepository buildingRepository = Mockito.mock(BuildingRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final HealthRecordRepository healthRecordRepository = Mockito.mock(HealthRecordRepository.class);

    private final BatchService batchService = new BatchService(
            batchRepository, buildingRepository, userRepository, healthRecordRepository);

    /**
     * Property 2: isBatchSellable returns false when active withdrawal records exist.
     * Requirements: 1.2, 7.1
     */
    @Property
    @Label("Property 2: isBatchSellable is false when active withdrawal periods exist")
    void isBatchSellableReturnsFalseWhenWithdrawalActive(
            @ForAll @IntRange(min = 1, max = 30) int daysRemaining) {

        Long batchId = 1L;
        HealthRecord activeRecord = HealthRecord.builder()
                .withdrawalDays(daysRemaining + 10)
                .examinationDate(LocalDate.now().minusDays(5))
                .isVaccination(false)
                .build();

        when(healthRecordRepository.findActiveWithdrawalPeriods(batchId))
                .thenReturn(List.of(activeRecord));

        boolean sellable = batchService.isBatchSellable(batchId);

        assertThat(sellable).isFalse();
    }

    /**
     * Property 3: isBatchSellable returns true when no active withdrawal records exist.
     * Requirements: 1.5, 7.3
     */
    @Property
    @Label("Property 3: isBatchSellable is true when no active withdrawal periods")
    void isBatchSellableReturnsTrueWhenNoWithdrawal(@ForAll Long batchId) {
        when(healthRecordRepository.findActiveWithdrawalPeriods(batchId))
                .thenReturn(Collections.emptyList());

        boolean sellable = batchService.isBatchSellable(batchId);

        assertThat(sellable).isTrue();
    }

    /**
     * Property 4: validateStatusTransition throws WithdrawalPeriodActiveException
     * when selling batch with active withdrawal period.
     * Requirements: 1.3, 7.2
     */
    @Property
    @Label("Property 4: validateStatusTransition throws when withdrawal active for SOLD")
    void validateStatusTransitionThrowsForSoldWithActiveWithdrawal(
            @ForAll @IntRange(min = 1, max = 30) int daysUntilExpiration) {

        Long batchId = 42L;
        LocalDate expirationDate = LocalDate.now().plusDays(daysUntilExpiration);
        HealthRecord activeRecord = HealthRecord.builder()
                .withdrawalDays(50)
                .examinationDate(LocalDate.now().minusDays(10))
                .isVaccination(false)
                .build();

        when(healthRecordRepository.findActiveWithdrawalPeriods(batchId))
                .thenReturn(List.of(activeRecord));
        when(healthRecordRepository.findLatestWithdrawalExpiration(batchId))
                .thenReturn(expirationDate);

        assertThatThrownBy(() -> batchService.validateStatusTransition(
                batchId, org.example.djajbladibackend.models.BatchStatus.SOLD))
                .isInstanceOf(WithdrawalPeriodActiveException.class);
    }

    /**
     * Property 25: getWithdrawalExpirationDate returns the latest expiration date.
     * Requirements: 1.4, 7.4
     */
    @Property
    @Label("Property 25: getWithdrawalExpirationDate returns correct latest expiration")
    void getWithdrawalExpirationDateReturnsLatest(
            @ForAll @IntRange(min = 1, max = 60) int futureDays) {

        Long batchId = 99L;
        LocalDate expectedDate = LocalDate.now().plusDays(futureDays);

        when(healthRecordRepository.findLatestWithdrawalExpiration(batchId))
                .thenReturn(expectedDate);

        Optional<LocalDate> result = batchService.getWithdrawalExpirationDate(batchId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedDate);
    }
}
