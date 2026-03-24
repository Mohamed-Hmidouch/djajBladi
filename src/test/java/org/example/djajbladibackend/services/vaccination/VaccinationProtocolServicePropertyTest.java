package org.example.djajbladibackend.services.vaccination;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotBlank;
import org.example.djajbladibackend.dto.vaccination.VaccinationProtocolRequest;
import org.example.djajbladibackend.exception.DuplicateVaccinationProtocolException;
import org.example.djajbladibackend.models.VaccinationProtocol;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.HealthRecordRepository;
import org.example.djajbladibackend.repository.VaccinationProtocolRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for VaccinationProtocolService.
 * Requirements: 6.1, 6.2, 6.5
 */
class VaccinationProtocolServicePropertyTest {

    private final VaccinationProtocolRepository protocolRepository =
            Mockito.mock(VaccinationProtocolRepository.class);
    private final BatchRepository batchRepository = Mockito.mock(BatchRepository.class);
    private final HealthRecordRepository healthRecordRepository = Mockito.mock(HealthRecordRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);

    private final VaccinationProtocolService protocolService = new VaccinationProtocolService(
            protocolRepository, batchRepository, healthRecordRepository, userRepository);

    /**
     * Property 18: findByStrain returns only protocols matching the given strain.
     * Requirements: 6.5
     */
    @Property
    @Label("Property 18: findByStrain returns only matching protocols")
    void findByStrainReturnsOnlyMatchingProtocols(@ForAll @NotBlank String strain) {
        List<VaccinationProtocol> protocols = List.of(
                VaccinationProtocol.builder()
                        .strain(strain).vaccineName("VaccineA").dayOfLife(7).build(),
                VaccinationProtocol.builder()
                        .strain(strain).vaccineName("VaccineB").dayOfLife(14).build()
        );
        when(protocolRepository.findByStrainOrderByDayOfLifeAsc(strain)).thenReturn(protocols);

        var result = protocolService.findByStrain(strain);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getStrain().equals(strain));
    }

    /**
     * Property 19: findByStrain returns empty list for unknown strain.
     * Requirements: 6.5
     */
    @Property
    @Label("Property 19: findByStrain returns empty list for unknown strain")
    void findByStrainReturnsEmptyForUnknownStrain(@ForAll @NotBlank String strain) {
        when(protocolRepository.findByStrainOrderByDayOfLifeAsc(strain)).thenReturn(Collections.emptyList());

        var result = protocolService.findByStrain(strain);

        assertThat(result).isEmpty();
    }

    /**
     * Property 26: create throws DuplicateVaccinationProtocolException when duplicate exists.
     * Requirements: 6.2
     */
    @Property
    @Label("Property 26: create throws on duplicate strain+vaccine+day combination")
    void createThrowsOnDuplicate(
            @ForAll @NotBlank String strain,
            @ForAll @NotBlank String vaccineName,
            @ForAll @IntRange(min = 1, max = 365) int dayOfLife) {

        when(protocolRepository.existsByStrainAndVaccineNameAndDayOfLife(
                anyString(), anyString(), anyInt())).thenReturn(true);

        VaccinationProtocolRequest request = new VaccinationProtocolRequest();
        request.setStrain(strain);
        request.setVaccineName(vaccineName);
        request.setDayOfLife(dayOfLife);

        assertThatThrownBy(() -> protocolService.create(request, "admin@test.com"))
                .isInstanceOf(DuplicateVaccinationProtocolException.class);
    }
}
