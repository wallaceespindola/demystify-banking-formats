package com.wallaceespindola.bankingformats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** One bank statement, whatever file format it arrived in. Amounts are signed: credit positive, debit negative. */
public record Statement(
        String iban,
        String currency,
        BigDecimal openingBalance,
        LocalDate openingDate,
        BigDecimal closingBalance,
        LocalDate closingDate,
        List<Entry> entries) {

    /** One booked movement. {@code reference} is the structured creditor reference, digits only. */
    public record Entry(LocalDate bookingDate, BigDecimal amount, String counterparty, String reference) {}
}
