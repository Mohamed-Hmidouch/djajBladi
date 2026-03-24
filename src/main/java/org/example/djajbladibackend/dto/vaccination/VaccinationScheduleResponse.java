package org.example.djajbladibackend.dto.vaccination;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class VaccinationScheduleResponse {
    private Long protocolId;
    private String vaccineName;
    private Integer dayOfLife;
    private LocalDate dueDate;
    private Boolean isCompleted;
    private Long completedHealthRecordId;
    private LocalDate completedDate;
}
