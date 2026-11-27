package com.wallaceespindola.bankingformats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the CODA records this example needs (Febelfin layout, 128 characters per line).
 * Positions in the comments are 1-based, as in the Febelfin specification.
 */
public final class CodaParser {

    private static final DateTimeFormatter DDMMYY = DateTimeFormatter.ofPattern("ddMMyy");

    public Statement parse(List<String> lines) {
        String iban = null;
        String currency = null;
        BigDecimal opening = null;
        LocalDate openingDate = null;
        BigDecimal closing = null;
        LocalDate closingDate = null;
        List<Statement.Entry> entries = new ArrayList<>();
        Movement pending = null;

        for (String line : lines) {
            if (line.length() != 128) {
                throw new IllegalArgumentException("CODA line must be 128 characters, got " + line.length());
            }
            switch (line.substring(0, 2)) {
                case String r when r.startsWith("0") -> { } // header: creation date, bank, addressee
                case String r when r.startsWith("1") -> {     // old balance
                    iban = cut(line, 6, 39).strip();             // account structure 2: IBAN in 6-39
                    currency = cut(line, 40, 42);
                    opening = amount(line.charAt(42), cut(line, 44, 58)); // sign in 43
                    openingDate = date(cut(line, 59, 64));
                }
                case "21" -> {                                 // movement, part 1
                    if (pending != null) {
                        entries.add(pending.toEntry(null));    // previous movement had no part 3
                    }
                    pending = new Movement(
                            amount(line.charAt(31), cut(line, 33, 47)),  // sign in 32
                            date(cut(line, 116, 121)),                    // entry date
                            structuredReference(line));
                }
                case "22" -> { }                               // movement, part 2: BIC, purpose codes
                case "23" -> {                                 // movement, part 3: counterparty
                    if (pending == null) {
                        throw new IllegalArgumentException("CODA record 23 without a preceding 21");
                    }
                    String counterparty = cut(line, 48, 82).strip();
                    entries.add(pending.toEntry(counterparty));
                    pending = null;
                }
                case String r when r.startsWith("8") -> {     // new balance
                    if (pending != null) {
                        entries.add(pending.toEntry(null));
                        pending = null;
                    }
                    closing = amount(line.charAt(41), cut(line, 43, 57)); // sign in 42
                    closingDate = date(cut(line, 58, 63));
                }
                default -> { }                                 // 3x information, 4 free text, 9 trailer
            }
        }
        return new Statement(iban, currency, opening, openingDate, closing, closingDate, List.copyOf(entries));
    }

    /** Communication type 1 in position 62, then "101" + 12 digits: the Belgian OGM/VCS reference. */
    private static String structuredReference(String line) {
        if (line.charAt(61) == '1' && cut(line, 63, 65).equals("101")) {
            return cut(line, 66, 77);
        }
        return null;
    }

    /** CODA amounts: 15 digits, 3 implied decimals; sign 0 = credit, 1 = debit. A non-zero third decimal fails fast. */
    private static BigDecimal amount(char sign, String digits) {
        BigDecimal value = new BigDecimal(digits).movePointLeft(3).setScale(2);
        return sign == '1' ? value.negate() : value;
    }

    private static LocalDate date(String ddmmyy) {
        return LocalDate.parse(ddmmyy, DDMMYY);
    }

    /** Substring by 1-based, inclusive positions, the way the spec writes them. */
    private static String cut(String line, int from, int to) {
        return line.substring(from - 1, to);
    }

    private record Movement(BigDecimal amount, LocalDate bookingDate, String reference) {
        Statement.Entry toEntry(String counterparty) {
            return new Statement.Entry(bookingDate, amount, counterparty, reference);
        }
    }
}
