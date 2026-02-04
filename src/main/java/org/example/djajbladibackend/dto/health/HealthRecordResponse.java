package org.example.djajbladibackend.dto.health;

import lombok.Builder;
import lombok.Data;
import org.example.djajbladibackend.models.ApprovalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class HealthRecordResponse {
    private Long id;
    private Long batchId;
    private String batchNumber;
    private Long veterinarianId;
    private String veterinarianName;
    private String diagnosis;
    private String treatment;
    private LocalDate examinationDate;
    private LocalDate nextVisitDate;
    private Integer mortalityCount;
    private BigDecimal treatmentCost;
    private Boolean requiresApproval;
    private ApprovalStatus approvalStatus;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
