package org.example.djajbladibackend.services.vaccination;

import lombok.extern.slf4j.Slf4j;
import org.example.djajbladibackend.dto.vaccination.VaccinationAlertResponse;
import org.example.djajbladibackend.dto.vaccination.VaccinationProtocolRequest;
import org.example.djajbladibackend.dto.vaccination.VaccinationProtocolResponse;
import org.example.djajbladibackend.dto.vaccination.VaccinationScheduleResponse;
import org.example.djajbladibackend.exception.DuplicateVaccinationProtocolException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.exception.VaccinationProtocolNotFoundException;
import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.VaccinationProtocol;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.HealthRecordRepository;
import org.example.djajbladibackend.repository.VaccinationProtocolRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
public class VaccinationProtocolService {

    private final VaccinationProtocolRepository protocolRepository;
    private final BatchRepository batchRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final UserRepository userRepository;

    public VaccinationProtocolService(VaccinationProtocolRepository protocolRepository,
                                      BatchRepository batchRepository,
                                      HealthRecordRepository healthRecordRepository,
                                      UserRepository userRepository) {
        this.protocolRepository = protocolRepository;
        this.batchRepository = batchRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new vaccination protocol for a strain.
     * Requirements: 6.1, 6.2
     */
    @Transactional
    public VaccinationProtocolResponse create(VaccinationProtocolRequest req, String adminEmail) {
        if (protocolRepository.existsByStrainAndVaccineNameAndDayOfLife(
                req.getStrain(), req.getVaccineName(), req.getDayOfLife())) {
            throw new DuplicateVaccinationProtocolException(req.getStrain(), req.getVaccineName(), req.getDayOfLife());
        }
        var createdBy = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + adminEmail));

        VaccinationProtocol protocol = VaccinationProtocol.builder()
                .strain(req.getStrain().trim())
                .vaccineName(req.getVaccineName().trim())
                .dayOfLife(req.getDayOfLife())
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .createdBy(createdBy)
                .build();

        VaccinationProtocol saved = protocolRepository.save(protocol);
        log.info("Vaccination protocol created: strain={}, vaccine={}, day={}, by={}",
                saved.getStrain(), saved.getVaccineName(), saved.getDayOfLife(), adminEmail);
        return toResponse(saved);
    }

    /**
     * Updates an existing vaccination protocol.
     * Requirements: 6.3
     */
    @Transactional
    public VaccinationProtocolResponse update(Long id, VaccinationProtocolRequest req) {
        VaccinationProtocol protocol = protocolRepository.findByIdWithCreatedBy(id)
                .orElseThrow(() -> new VaccinationProtocolNotFoundException(id));

        boolean changed = !protocol.getStrain().equals(req.getStrain())
                || !protocol.getVaccineName().equals(req.getVaccineName())
                || !protocol.getDayOfLife().equals(req.getDayOfLife());

        if (changed && protocolRepository.existsByStrainAndVaccineNameAndDayOfLife(
                req.getStrain(), req.getVaccineName(), req.getDayOfLife())) {
            throw new DuplicateVaccinationProtocolException(req.getStrain(), req.getVaccineName(), req.getDayOfLife());
        }

        protocol.setStrain(req.getStrain().trim());
        protocol.setVaccineName(req.getVaccineName().trim());
        protocol.setDayOfLife(req.getDayOfLife());
        protocol.setNotes(req.getNotes() != null ? req.getNotes().trim() : null);

        VaccinationProtocol saved = protocolRepository.save(protocol);
        log.info("Vaccination protocol updated: id={}, strain={}, vaccine={}, day={}",
                id, saved.getStrain(), saved.getVaccineName(), saved.getDayOfLife());
        return toResponse(saved);
    }

    /**
     * Deletes a vaccination protocol by ID.
     * Requirements: 6.4
     */
    @Transactional
    public void delete(Long id) {
        if (!protocolRepository.existsById(id)) {
            throw new VaccinationProtocolNotFoundException(id);
        }
        protocolRepository.deleteById(id);
        log.info("Vaccination protocol deleted: id={}", id);
    }

    /**
     * Returns all protocols for a given strain, ordered by day of life.
     * Requirements: 6.5
     */
    public List<VaccinationProtocolResponse> findByStrain(String strain) {
        return protocolRepository.findByStrainOrderByDayOfLifeAsc(strain)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Calculates the vaccination schedule for a specific batch based on its strain.
     * For each protocol, computes due date = arrival_date + dayOfLife days.
     * Checks if vaccination has been completed via existing health records.
     * Requirements: 5.1, 5.2, 5.3
     */
    public List<VaccinationScheduleResponse> getScheduleForBatch(Long batchId) {
        Batch batch = batchRepository.findByIdWithCreatedByAndBuilding(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));

        List<VaccinationProtocol> protocols = protocolRepository.findProtocolsByStrain(batch.getStrain());

        return protocols.stream().map(protocol -> {
            LocalDate dueDate = batch.getArrivalDate().plusDays(protocol.getDayOfLife());

            var completedRecords = healthRecordRepository.findVaccinationRecords(batchId, protocol.getVaccineName());
            boolean isCompleted = !completedRecords.isEmpty();
            Long completedHealthRecordId = isCompleted ? completedRecords.get(0).getId() : null;
            LocalDate completedDate = isCompleted ? completedRecords.get(0).getExaminationDate() : null;

            return VaccinationScheduleResponse.builder()
                    .protocolId(protocol.getId())
                    .vaccineName(protocol.getVaccineName())
                    .dayOfLife(protocol.getDayOfLife())
                    .dueDate(dueDate)
                    .isCompleted(isCompleted)
                    .completedHealthRecordId(completedHealthRecordId)
                    .completedDate(completedDate)
                    .build();
        }).toList();
    }

    /**
     * Returns vaccination alerts: protocols due in the next 7 days for active batches.
     * Requirements: 5.4, 5.5
     */
    public List<VaccinationAlertResponse> getAlertsForToday() {
        return buildAlertsForActiveBatches(false);
    }

    /**
     * Returns overdue vaccination alerts for active batches.
     * Requirements: 5.4, 5.6
     */
    public List<VaccinationAlertResponse> getOverdueAlerts() {
        return buildAlertsForActiveBatches(true);
    }

    private List<VaccinationAlertResponse> buildAlertsForActiveBatches(boolean overdueOnly) {
        List<Batch> activeBatches = batchRepository.findByStatusInWithRelations(
                List.of(org.example.djajbladibackend.models.BatchStatus.Active));
        LocalDate today = LocalDate.now();

        return activeBatches.stream().flatMap(batch -> {
            List<VaccinationProtocol> protocols = protocolRepository.findProtocolsByStrain(batch.getStrain());
            return protocols.stream()
                    .flatMap(protocol -> {
                        LocalDate dueDate = batch.getArrivalDate().plusDays(protocol.getDayOfLife());
                        long daysOverdue = today.toEpochDay() - dueDate.toEpochDay();
                        boolean isOverdue = daysOverdue > 0;

                        var completed = healthRecordRepository.findVaccinationRecords(
                                batch.getId(), protocol.getVaccineName());
                        if (!completed.isEmpty()) {
                            return java.util.stream.Stream.empty();
                        }
                        if (overdueOnly && !isOverdue) {
                            return java.util.stream.Stream.empty();
                        }
                        if (!overdueOnly && dueDate.isAfter(today.plusDays(7))) {
                            return java.util.stream.Stream.empty();
                        }

                        VaccinationAlertResponse alert = VaccinationAlertResponse.builder()
                                .batchId(batch.getId())
                                .batchNumber(batch.getBatchNumber())
                                .strain(batch.getStrain())
                                .protocolId(protocol.getId())
                                .vaccineName(protocol.getVaccineName())
                                .dueDate(dueDate)
                                .daysOverdue((int) Math.max(0L, daysOverdue))
                                .isOverdue(isOverdue)
                                .build();
                        return java.util.stream.Stream.of(alert);
                    });
        }).toList();
    }

    private VaccinationProtocolResponse toResponse(VaccinationProtocol p) {
        return VaccinationProtocolResponse.builder()
                .id(p.getId())
                .strain(p.getStrain())
                .vaccineName(p.getVaccineName())
                .dayOfLife(p.getDayOfLife())
                .notes(p.getNotes())
                .createdById(p.getCreatedBy() != null ? p.getCreatedBy().getId() : null)
                .createdByName(p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
