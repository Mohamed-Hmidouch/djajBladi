package org.example.djajbladibackend.services.mortality;

import org.example.djajbladibackend.dto.mortality.DailyMortalityRequest;
import org.example.djajbladibackend.exception.DuplicateDailyMortalityException;
import org.example.djajbladibackend.exception.ForbiddenRoleException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.DailyMortalityRecord;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.DailyMortalityRecordRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DailyMortalityService Unit Tests")
class DailyMortalityServiceTest {

    @Mock
    private DailyMortalityRecordRepository mortalityRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DailyMortalityService service;

    private User ouvrier;
    private Batch batch;
    private DailyMortalityRequest request;

    @BeforeEach
    void setUp() {
        ouvrier = User.builder().id(1L).email("ouvrier@test.com").role(RoleEnum.Ouvrier).fullName("Ouvrier").build();
        batch = Batch.builder().id(1L).batchNumber("BL-001").status(org.example.djajbladibackend.models.BatchStatus.Active).chickenCount(1000).build();
        request = new DailyMortalityRequest();
        request.setBatchId(1L);
        request.setRecordDate(LocalDate.now().minusDays(1));
        request.setMortalityCount(2);
    }

    @Test
    @DisplayName("record should succeed when Ouvrier and batch active")
    void record_Ouvrier_Success() {
        when(userRepository.findByEmail("ouvrier@test.com")).thenReturn(Optional.of(ouvrier));
        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(mortalityRepository.existsByBatchIdAndRecordDate(1L, request.getRecordDate())).thenReturn(false);
        DailyMortalityRecord saved = DailyMortalityRecord.builder().id(1L).batch(batch).recordDate(request.getRecordDate()).mortalityCount(2).recordedBy(ouvrier).build();
        when(mortalityRepository.save(any(DailyMortalityRecord.class))).thenReturn(saved);

        var resp = service.record(request, "ouvrier@test.com");

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals(2, resp.getMortalityCount());
        verify(mortalityRepository, times(1)).save(any(DailyMortalityRecord.class));
    }

    @Test
    @DisplayName("record should throw ForbiddenRoleException when Client")
    void record_Client_Throws() {
        User client = User.builder().id(2L).email("client@test.com").role(RoleEnum.Client).build();
        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(client));

        assertThrows(ForbiddenRoleException.class, () -> service.record(request, "client@test.com"));
        verify(mortalityRepository, never()).save(any());
    }

    @Test
    @DisplayName("record should throw when recordDate in future")
    void record_FutureDate_Throws() {
        request.setRecordDate(LocalDate.now().plusDays(1));
        when(userRepository.findByEmail("ouvrier@test.com")).thenReturn(Optional.of(ouvrier));

        assertThrows(org.example.djajbladibackend.exception.InvalidDataException.class, () -> service.record(request, "ouvrier@test.com"));
        verify(batchRepository, never()).findById(any());
        verify(mortalityRepository, never()).save(any());
    }

    @Test
    @DisplayName("record should throw DuplicateDailyMortality when already exists")
    void record_Duplicate_Throws() {
        when(userRepository.findByEmail("ouvrier@test.com")).thenReturn(Optional.of(ouvrier));
        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(mortalityRepository.existsByBatchIdAndRecordDate(1L, request.getRecordDate())).thenReturn(true);

        assertThrows(DuplicateDailyMortalityException.class, () -> service.record(request, "ouvrier@test.com"));
        verify(mortalityRepository, never()).save(any());
    }

    @Test
    @DisplayName("record should throw when batch not found")
    void record_BatchNotFound_Throws() {
        when(userRepository.findByEmail("ouvrier@test.com")).thenReturn(Optional.of(ouvrier));
        when(batchRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.record(request, "ouvrier@test.com"));
        verify(mortalityRepository, never()).save(any());
    }
}
