package com.kdlt.platform.quote.controller;

import com.kdlt.platform.quote.dto.CreateQuoteDto;
import com.kdlt.platform.quote.dto.QuoteDto;
import com.kdlt.platform.quote.dto.RespondQuoteDto;
import com.kdlt.platform.quote.service.QuoteService;
import com.kdlt.platform.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    public ResponseEntity<QuoteDto> createQuote(@AuthenticationPrincipal User user,
                                                @Valid @RequestBody CreateQuoteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteService.createQuote(user, dto));
    }

    @GetMapping("/me")
    public ResponseEntity<List<QuoteDto>> getMyQuotes(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(quoteService.getMyQuotes(user));
    }

    @GetMapping("/me/{quoteId}")
    public ResponseEntity<QuoteDto> getMyQuote(@AuthenticationPrincipal User user,
                                               @PathVariable Long quoteId) {
        return ResponseEntity.ok(quoteService.getMyQuote(user, quoteId));
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<QuoteDto>> getAllQuotes() {
        return ResponseEntity.ok(quoteService.getAllQuotes());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<QuoteDto>> getPendingQuotes() {
        return ResponseEntity.ok(quoteService.getPendingQuotes());
    }

    @PutMapping("/{quoteId}/respond")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<QuoteDto> respondToQuote(@PathVariable Long quoteId,
                                                   @Valid @RequestBody RespondQuoteDto dto) {
        return ResponseEntity.ok(quoteService.respondToQuote(quoteId, dto));
    }
}