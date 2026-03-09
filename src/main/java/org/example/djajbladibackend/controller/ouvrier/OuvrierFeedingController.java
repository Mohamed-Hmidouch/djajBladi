package org.example.djajbladibackend.controller.ouvrier;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.common.PageResponse;
import org.example.djajbladibackend.dto.feeding.FeedingRecordRequest;
import org.example.djajbladibackend.dto.feeding.FeedingRecordResponse;
import org.example.djajbladibackend.services.feeding.FeedingRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * ✅ Security Best Practice: @PreAuthorize pour ADMIN et OUVRIER
 */
@RestController
@RequestMapping(value = { "/api/ouvrier/feeding", "/api/dashboard/ouvrier/feeding" })
@PreAuthorize("hasAnyRole('ADMIN', 'OUVRIER')")
public class OuvrierFeedingController {

    private final FeedingRecordService feedingService;

    public OuvrierFeedingController(FeedingRecordService feedingService) {
        this.feedingService = feedingService;
    }

    @PostMapping
    public ResponseEntity<FeedingRecordResponse> create(
            @Valid @RequestBody FeedingRecordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedingService.create(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<PageResponse<FeedingRecordResponse>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(feedingService.findByDateRangePaged(startDate, endDate, batchId, page, size));
    }
}
