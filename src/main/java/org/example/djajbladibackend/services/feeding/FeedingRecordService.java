package org.example.djajbladibackend.services.feeding;

import lombok.extern.slf4j.Slf4j;
import org.example.djajbladibackend.dto.feeding.FeedingRecordRequest;
import org.example.djajbladibackend.dto.feeding.FeedingRecordResponse;
import org.example.djajbladibackend.exception.BatchNotActiveException;
import org.example.djajbladibackend.exception.DateRangeTooLargeException;
import org.example.djajbladibackend.exception.ForbiddenRoleException;
import org.example.djajbladibackend.exception.InsufficientStockException;
import org.example.djajbladibackend.exception.InvalidDataException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.BatchStatus;
import org.example.djajbladibackend.models.FeedingRecord;
import org.example.djajbladibackend.models.StockItem;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.FeedingRecordRepository;
import org.example.djajbladibackend.repository.StockItemRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class FeedingRecordService {

    private final FeedingRecordRepository feedingRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final StockItemRepository stockItemRepository;

    @Value("${app.supervision.max-date-range-days:366}")
    private int maxDateRangeDays;

    public FeedingRecordService(FeedingRecordRepository feedingRepository,
                                BatchRepository batchRepository,
                                UserRepository userRepository,
                                StockItemRepository stockItemRepository) {
        this.feedingRepository = feedingRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
        this.stockItemRepository = stockItemRepository;
    }

    /**
     * Cree un enregistrement d'alimentation avec deduction atomique du stock.
     *
     * Logique ACID :
     *   1. Valider la requete (role, date, lot actif, stockItemId obligatoire)
     *   2. Acquerir un PESSIMISTIC_WRITE lock sur le StockItem -> bloque les
     *      acces concurrents jusqu'a la fin de la transaction
     *   3. Verifier disponibilite : stock.quantity >= req.quantity
     *   4. Si insuffisant -> InsufficientStockException (rollback automatique)
     *   5. Deduire : stock.quantity -= req.quantity
     *   6. Sauvegarder le FeedingRecord lie au StockItem
     *   -> Les etapes 5 et 6 sont atomiques : meme @Transactional
     */
    @Transactional
    public FeedingRecordResponse create(FeedingRecordRequest req, String userEmail) {
        // --- Validation role ---
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        if (user.getRole() != RoleEnum.Ouvrier && user.getRole() != RoleEnum.Admin) {
            throw new ForbiddenRoleException("Only Ouvrier or Admin can create feeding records");
        }

        // --- Validation metier ---
        if (req.getFeedingDate().isAfter(LocalDate.now())) {
            throw new InvalidDataException("Feeding date cannot be in the future");
        }
        if (req.getFeedType() == null || req.getFeedType().trim().isEmpty()) {
            throw new InvalidDataException("Feed type is required and cannot be blank");
        }
        if (req.getQuantity() == null || req.getQuantity().signum() <= 0) {
            throw new InvalidDataException("Quantity must be greater than 0");
        }

        Batch batch = batchRepository.findById(req.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + req.getBatchId()));
        if (batch.getStatus() != BatchStatus.Active) {
            throw new BatchNotActiveException("Feeding can only be recorded for active batches. Status: " + batch.getStatus());
        }

        // --- Tracabilite stock obligatoire ---
        // findByIdForUpdate pose un PESSIMISTIC_WRITE lock (SELECT ... FOR UPDATE)
        // -> toute autre transaction voulant modifier ce StockItem sera bloquee
        // jusqu'a la fin de cette transaction (commit ou rollback)
        StockItem stockItem = stockItemRepository.findByIdForUpdate(req.getStockItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + req.getStockItemId()));

        // --- Verification disponibilite (en kg) ---
        BigDecimal requested = req.getQuantity();
        BigDecimal available = stockItem.getQuantity();

        if (available.compareTo(requested) < 0) {
            // Le lock sera libere automatiquement au rollback
            throw new InsufficientStockException(
                    stockItem.getId(),
                    stockItem.getName() != null ? stockItem.getName() : stockItem.getType().name(),
                    available,
                    requested
            );
        }

        // --- Deduction atomique du stock ---
        stockItem.setQuantity(available.subtract(requested));
        stockItemRepository.save(stockItem);

        log.info("Stock deduction: stockItemId={}, name='{}', deducted={}kg, remaining={}kg, batchId={}",
                stockItem.getId(), stockItem.getName(), requested,
                stockItem.getQuantity(), batch.getId());

        // --- Creation du FeedingRecord lie au stock ---
        FeedingRecord record = FeedingRecord.builder()
                .batch(batch)
                .stockItem(stockItem)
                .feedType(req.getFeedType().trim())
                .quantity(requested)
                .feedingDate(req.getFeedingDate())
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .recordedBy(user)
                .build();
        FeedingRecord saved = feedingRepository.save(record);

        return toResponse(saved);
    }

    public List<FeedingRecordResponse> findByDateRange(LocalDate start, LocalDate end, Long batchId) {
        if (start.isAfter(end)) {
            throw new InvalidDataException("Start date must be before or equal to end date");
        }
        if (ChronoUnit.DAYS.between(start, end) > maxDateRangeDays) {
            throw new DateRangeTooLargeException("Date range cannot exceed " + maxDateRangeDays + " days");
        }
        List<FeedingRecord> records;
        if (batchId != null) {
            records = feedingRepository.findByBatchIdAndDateRangeWithRelations(batchId, start, end);
        } else {
            records = feedingRepository.findByFeedingDateBetweenWithRelations(start, end);
        }
        return records.stream().map(this::toResponse).toList();
    }

    private FeedingRecordResponse toResponse(FeedingRecord r) {
        return FeedingRecordResponse.builder()
                .id(r.getId())
                .batchId(r.getBatch().getId())
                .batchNumber(r.getBatch().getBatchNumber())
                .stockItemId(r.getStockItem() != null ? r.getStockItem().getId() : null)
                .stockItemName(r.getStockItem() != null ? r.getStockItem().getName() : null)
                .feedType(r.getFeedType())
                .quantity(r.getQuantity())
                .feedingDate(r.getFeedingDate())
                .notes(r.getNotes())
                .recordedById(r.getRecordedBy().getId())
                .recordedByName(r.getRecordedBy().getFullName())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
