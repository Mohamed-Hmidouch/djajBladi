package org.example.djajbladibackend.dto.vaccination;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class VaccinationAlertResponse {
    private Long batchId;
    private String batchNumber;
    private String strain;
    private Long protocolId;
    private String vaccineName;
    private LocalDate dueDate;
    private Integer daysOverdue;
    private Boolean isOverdue;
}
