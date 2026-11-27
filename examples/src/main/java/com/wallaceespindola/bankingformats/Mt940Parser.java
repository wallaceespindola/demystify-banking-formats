package com.wallaceespindola.bankingformats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a single-statement MT940 text block. Field 86 is free text, so the counterparty and
 * reference rules below are one bank's dialect: this is the part every bank does differently.
 */
public final class Mt940Parser {

    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");
    private static final Pattern TAG = Pattern.compile("^:(\\d{2}[A-Z]?):(.*)$");
    private static final Pattern BALANCE = Pattern.compile("([CD])(\\d{6})([A-Z]{3})([\\d,]+)");
    private static final Pattern LINE_61 = Pattern.compile("(\\d{2})(\\d{4})(\\d{4})?(R?[CD])([A-Z])?([\\d,]+).*");
    private static final Pattern OGM = Pattern.compile("\\+\\+\\+(\\d{3})/(\\d{4})/(\\d{5})\\+\\+\\+");

    public Statement parse(List<String> lines) {
        Map<String, String> header = new LinkedHashMap<>();
        List<String[]> statementLines = new ArrayList<>(); // pairs of :61: and :86:

        String lastTag = null;
        for (String line : lines) {
            Matcher tag = TAG.matcher(line);
            if (!tag.matches()) {
                if (line.startsWith("{") || line.startsWith("-}")) {
                    continue; // {1:}{2:}{4: block wrappers and the closing -}
                }
                if ("86".equals(lastTag) && !statementLines.isEmpty()) {
                    statementLines.getLast()[1] += " " + line.strip(); // :86: continues on up to 6 lines
                }
                continue;
            }
            lastTag = tag.group(1);
            switch (lastTag) {
                case "61" -> statementLines.add(new String[] {tag.group(2), ""});
                case "86" -> {
                    if (!statementLines.isEmpty()) {
                        statementLines.getLast()[1] = tag.group(2);
                    } // a :86: before any :61: describes the whole statement; not needed here
                }
                default -> header.put(lastTag, tag.group(2));
            }
        }

        Matcher opening = balance(header.get("60F"));
        Matcher closing = balance(header.get("62F"));
        List<Statement.Entry> entries = statementLines.stream().map(Mt940Parser::entry).toList();

        return new Statement(
                header.get("25"),
                opening.group(3),
                signed(opening.group(1), opening.group(4)),
                LocalDate.parse(opening.group(2), YYMMDD),
                signed(closing.group(1), closing.group(4)),
                LocalDate.parse(closing.group(2), YYMMDD),
                entries);
    }

    private static Statement.Entry entry(String[] pair) {
        Matcher m = LINE_61.matcher(pair[0]);
        if (!m.matches()) {
            throw new IllegalArgumentException("Unreadable :61: line: " + pair[0]);
        }
        String info = pair[1];
        Matcher ogm = OGM.matcher(info);
        String reference = ogm.find() ? ogm.group(1) + ogm.group(2) + ogm.group(3) : null;
        String counterparty = ogm.reset().replaceAll("").strip();
        // :61: starts with the value date (YYMMDD), then an optional entry date (MMDD): the booking date
        String bookingDate = m.group(3) != null ? m.group(1) + m.group(3) : m.group(1) + m.group(2);
        return new Statement.Entry(
                LocalDate.parse(bookingDate, YYMMDD), signed(m.group(4), m.group(6)), counterparty, reference);
    }

    private static Matcher balance(String field) {
        Matcher m = BALANCE.matcher(field);
        if (!m.matches()) {
            throw new IllegalArgumentException("Unreadable balance: " + field);
        }
        return m;
    }

    /** MT amounts use a comma as decimal separator; D and RC are debits. */
    private static BigDecimal signed(String mark, String amount) {
        BigDecimal value = new BigDecimal(amount.replace(',', '.')).setScale(2);
        return mark.equals("D") || mark.equals("RC") ? value.negate() : value;
    }
}
