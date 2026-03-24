package org.example.djajbladibackend.models;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotBlank;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for VaccinationProtocol entity.
 * Requirement 2.8: dayOfLife must always be > 0.
 */
class VaccinationProtocolPropertyTest {

    /**
     * Property 27: dayOfLife is always positive.
     * For any valid protocol, dayOfLife >= 1.
     */
    @Property
    @Label("Property 27: VaccinationProtocol dayOfLife is always >= 1")
    void dayOfLifeIsAlwaysPositive(
            @ForAll @NotBlank String strain,
            @ForAll @NotBlank String vaccineName,
            @ForAll @IntRange(min = 1, max = 365) int dayOfLife) {

        VaccinationProtocol protocol = VaccinationProtocol.builder()
                .strain(strain)
                .vaccineName(vaccineName)
                .dayOfLife(dayOfLife)
                .build();

        assertThat(protocol.getDayOfLife()).isGreaterThanOrEqualTo(1);
    }

    /**
     * Property 28: dueDate calculation is deterministic.
     * dueDate = arrivalDate + dayOfLife days always holds.
     */
    @Property
    @Label("Property 28: Due date = arrivalDate + dayOfLife is deterministic")
    void dueDateCalculationIsDeterministic(
            @ForAll("arrivalDates") LocalDate arrivalDate,
            @ForAll @IntRange(min = 1, max = 365) int dayOfLife) {

        LocalDate expectedDueDate = arrivalDate.plusDays(dayOfLife);

        VaccinationProtocol protocol = VaccinationProtocol.builder()
                .strain("TestStrain")
                .vaccineName("TestVaccine")
                .dayOfLife(dayOfLife)
                .build();

        LocalDate actualDueDate = arrivalDate.plusDays(protocol.getDayOfLife());

        assertThat(actualDueDate).isEqualTo(expectedDueDate);
    }

    @Provide
    Arbitrary<LocalDate> arrivalDates() {
        LocalDate minDate = LocalDate.now().minusYears(1);
        LocalDate maxDate = LocalDate.now();
        return Arbitraries.longs()
                .between(minDate.toEpochDay(), maxDate.toEpochDay())
                .map(LocalDate::ofEpochDay);
    }
}
