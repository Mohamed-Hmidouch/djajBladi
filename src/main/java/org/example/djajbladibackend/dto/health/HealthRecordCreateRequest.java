package org.example.djajbladibackend.dto.health;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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

    @Min(value = 0, message = "Withdrawal days must be >= 0")
    private Integer withdrawalDays;

    private Boolean isVaccination;

    private Long stockItemId;

    @DecimalMin(value = "0.0001", message = "Quantity used must be > 0 when stock item is provided")
    private BigDecimal quantityUsed;
}
