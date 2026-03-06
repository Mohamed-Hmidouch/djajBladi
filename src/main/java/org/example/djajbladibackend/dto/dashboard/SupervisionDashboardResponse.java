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
    private List<BatchFcrSummary> fcrSummaries;

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
        // FCR enrichment fields
        private Integer ageInDays;
        private BigDecimal estimatedWeightKg;
        private BigDecimal actualWeightKg;
        private BigDecimal cumulativeFeedKg;
        private BigDecimal cumulativeFcr;
        private BigDecimal dailyFcr;
        private Boolean fcrAlert;
    }

    @Data
    @Builder
    public static class BatchFcrSummary {
        private Long batchId;
        private String batchNumber;
        private String strain;
        private Integer ageInDays;
        private Integer aliveChickens;
        private BigDecimal totalFeedConsumedKg;
        private BigDecimal estimatedWeightKg;
        private BigDecimal actualWeightKg;
        private BigDecimal cumulativeFcr;
        private Boolean fcrAlert;
        private BigDecimal fcrThreshold;
        private String fcrStatus;
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
