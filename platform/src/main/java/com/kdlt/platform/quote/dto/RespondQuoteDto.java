package com.kdlt.platform.quote.dto;

import com.kdlt.platform.quote.entity.QuoteStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class RespondQuoteDto {

    @NotNull
    private QuoteStatus status; // ANSWERED ou REJECTED

    @PositiveOrZero
    private java.math.BigDecimal proposedPrice; // requis si ANSWERED

    @Size(max = 2000)
    private String staffResponse;

    public QuoteStatus getStatus() { return status; }
    public void setStatus(QuoteStatus status) { this.status = status; }
    public java.math.BigDecimal getProposedPrice() { return proposedPrice; }
    public void setProposedPrice(java.math.BigDecimal proposedPrice) { this.proposedPrice = proposedPrice; }
    public String getStaffResponse() { return staffResponse; }
    public void setStaffResponse(String staffResponse) { this.staffResponse = staffResponse; }
}