package org.example.djajbladibackend.services.health;

import org.example.djajbladibackend.dto.health.HealthRecordCreateRequest;
import org.example.djajbladibackend.dto.health.HealthRecordResponse;
import org.example.djajbladibackend.exception.BatchNotActiveException;
import org.example.djajbladibackend.exception.ForbiddenRoleException;
import org.example.djajbladibackend.exception.InvalidDataException;
import org.example.djajbladibackend.exception.HealthRecordNotPendingException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.ApprovalStatus;
import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.HealthRecord;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.HealthRecordRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HealthRecordService {

    private final HealthRecordRepository healthRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;

    @Value("${app.supervision.expensive-treatment-threshold:5000}")
    private BigDecimal expensiveTreatmentThreshold;

    public HealthRecordService(HealthRecordRepository healthRepository,
                               BatchRepository batchRepository,
                               UserRepository userRepository) {
        this.healthRepository = healthRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public HealthRecordResponse create(HealthRecordCreateRequest req, String userEmail) {
        User vet = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        if (vet.getRole() != RoleEnum.Veterinaire && vet.getRole() != RoleEnum.Admin) {
            throw new ForbiddenRoleException("Only Veterinaire or Admin can create health records");
        }
        if (req.getExaminationDate().isAfter(LocalDate.now())) {
            throw new InvalidDataException("Examination date cannot be in the future");
        }
        if (req.getMortalityCount() != null && req.getMortalityCount() < 0) {
            throw new InvalidDataException("Mortality count must be >= 0");
        }
        if (req.getTreatmentCost() != null && req.getTreatmentCost().signum() < 0) {
            throw new InvalidDataException("Treatment cost cannot be negative");
        }
        Batch batch = batchRepository.findById(req.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + req.getBatchId()));
        if (batch.getStatus() != org.example.djajbladibackend.models.BatchStatus.Active) {
            throw new BatchNotActiveException("Health records can only be created for active batches. Current status: " + batch.getStatus());
        }

        boolean requiresApproval = Boolean.TRUE.equals(req.getIsDiseaseReported())
                || (req.getTreatmentCost() != null && req.getTreatmentCost().compareTo(expensiveTreatmentThreshold) >= 0);

        HealthRecord record = HealthRecord.builder()
                .batch(batch)
                .veterinarian(vet)
                .diagnosis(req.getDiagnosis().trim())
                .treatment(req.getTreatment() != null ? req.getTreatment().trim() : null)
                .examinationDate(req.getExaminationDate())
                .nextVisitDate(req.getNextVisitDate())
                .mortalityCount(req.getMortalityCount() != null ? req.getMortalityCount() : 0)
                .treatmentCost(req.getTreatmentCost())
                .requiresApproval(requiresApproval)
                .approvalStatus(requiresApproval ? ApprovalStatus.PENDING_APPROVAL : null)
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .build();
        HealthRecord saved = healthRepository.save(record);

        return toResponse(saved);
    }

    @Transactional
    public HealthRecordResponse approve(Long id, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + adminEmail));
        if (admin.getRole() != RoleEnum.Admin) {
            throw new ForbiddenRoleException("Only Admin can approve health records");
        }
        HealthRecord record = healthRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health record not found: " + id));
        if (!Boolean.TRUE.equals(record.getRequiresApproval())) {
            throw new HealthRecordNotPendingException("This health record does not require approval");
        }
        if (record.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
            throw new HealthRecordNotPendingException("Health record is not pending approval. Status: " + record.getApprovalStatus());
        }

        record.setApprovalStatus(ApprovalStatus.APPROVED);
        record.setApprovedBy(admin);
        record.setApprovedAt(LocalDateTime.now());
        HealthRecord saved = healthRepository.save(record);

        return toResponse(saved);
    }

    @Transactional
    public HealthRecordResponse reject(Long id, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + adminEmail));
        if (admin.getRole() != RoleEnum.Admin) {
            throw new ForbiddenRoleException("Only Admin can reject health records");
        }
        HealthRecord record = healthRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health record not found: " + id));
        if (!Boolean.TRUE.equals(record.getRequiresApproval())) {
            throw new HealthRecordNotPendingException("This health record does not require approval");
        }
        if (record.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
            throw new HealthRecordNotPendingException("Health record is not pending approval. Status: " + record.getApprovalStatus());
        }

        record.setApprovalStatus(ApprovalStatus.REJECTED);
        record.setApprovedBy(admin);
        record.setApprovedAt(LocalDateTime.now());
        HealthRecord saved = healthRepository.save(record);

        return toResponse(saved);
    }

    public List<HealthRecordResponse> findPendingApproval() {
        return healthRepository.findPendingApprovalWithRelations().stream().map(this::toResponse).toList();
    }

    private HealthRecordResponse toResponse(HealthRecord h) {
        return HealthRecordResponse.builder()
                .id(h.getId())
                .batchId(h.getBatch().getId())
                .batchNumber(h.getBatch().getBatchNumber())
                .veterinarianId(h.getVeterinarian().getId())
                .veterinarianName(h.getVeterinarian().getFullName())
                .diagnosis(h.getDiagnosis())
                .treatment(h.getTreatment())
                .examinationDate(h.getExaminationDate())
                .nextVisitDate(h.getNextVisitDate())
                .mortalityCount(h.getMortalityCount())
                .treatmentCost(h.getTreatmentCost())
                .requiresApproval(h.getRequiresApproval())
                .approvalStatus(h.getApprovalStatus())
                .approvedById(h.getApprovedBy() != null ? h.getApprovedBy().getId() : null)
                .approvedByName(h.getApprovedBy() != null ? h.getApprovedBy().getFullName() : null)
                .approvedAt(h.getApprovedAt())
                .notes(h.getNotes())
                .createdAt(h.getCreatedAt())
                .updatedAt(h.getUpdatedAt())
                .build();
    }
}
