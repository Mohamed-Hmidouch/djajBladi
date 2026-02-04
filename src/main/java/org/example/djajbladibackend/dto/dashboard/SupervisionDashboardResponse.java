package org.example.djajbladibackend.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SupervisionDashboardResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<BatchDailySummary> batchSummaries;
    private List<HealthAlertSummary> pendingAlerts;

    @Data
    @Builder
    public static class BatchDailySummary {
        private Long batchId;
        private String batchNumber;
        private LocalDate date;
        private BigDecimal totalQuantityEaten;
        private Integer mortalityCount;
        private String recordedByName;
        private Boolean abnormalConsumption;
    }

    @Data
    @Builder
    public static class HealthAlertSummary {
        private Long healthRecordId;
        private Long batchId;
        private String batchNumber;
        private String diagnosis;
        private String treatment;
        private BigDecimal treatmentCost;
        private LocalDate examinationDate;
        private String veterinarianName;
        private java.time.LocalDateTime createdAt;
    }
}
