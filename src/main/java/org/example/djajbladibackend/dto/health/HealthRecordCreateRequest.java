package org.example.djajbladibackend.dto.health;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HealthRecordCreateRequest {

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Diagnosis is required")
    @Size(max = 255)
    private String diagnosis;

    @Size(max = 2000)
    private String treatment;

    @NotNull(message = "Examination date is required")
    private LocalDate examinationDate;

    private LocalDate nextVisitDate;

    private Integer mortalityCount;

    private BigDecimal treatmentCost;

    private Boolean isDiseaseReported;

    @Size(max = 2000)
    private String notes;
}
