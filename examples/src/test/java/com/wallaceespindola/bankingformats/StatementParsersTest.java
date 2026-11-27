package com.wallaceespindola.bankingformats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatementParsersTest {

    private static final Path SAMPLES = Path.of("../docs/samples");

    private static final Statement EXPECTED = new Statement(
            "BE68539007547034", "EUR",
            new BigDecimal("1000.00"), LocalDate.of(2026, 11, 23),
            new BigDecimal("1250.00"), LocalDate.of(2026, 11, 24),
            List.of(new Statement.Entry(LocalDate.of(2026, 11, 24), new BigDecimal("250.00"), "DUPONT SA", "123456789002")));

    @Test
    void codaReadsTheStatement() throws Exception {
        assertEquals(EXPECTED, new CodaParser().parse(Files.readAllLines(SAMPLES.resolve("statement.coda"))));
    }

    @Test
    void mt940ReadsTheStatement() throws Exception {
        assertEquals(EXPECTED, new Mt940Parser().parse(Files.readAllLines(SAMPLES.resolve("statement.mt940"))));
    }

    @Test
    void camt053ReadsTheStatement() throws Exception {
        try (var in = Files.newInputStream(SAMPLES.resolve("statement.camt053.xml"))) {
            assertEquals(EXPECTED, new Camt053Parser().parse(in));
        }
    }

    @Test
    void codaRejectsLinesThatAreNot128Characters() {
        assertThrows(IllegalArgumentException.class, () -> new CodaParser().parse(List.of("0000")));
    }

    @Test
    void codaReadsDebitSign() {
        // record 1, IBAN structure, sign 1 (debit) in position 43, 500.000 with 3 implied decimals
        String oldBalance = "%-128s".formatted(
                "12001" + "%-34s".formatted("BE68539007547034") + "EUR" + "1" + "000000000500000" + "231126");
        Statement s = new CodaParser().parse(List.of(oldBalance));
        assertEquals(new BigDecimal("-500.00"), s.openingBalance());
    }

    @Test
    void codaRejectsPart3WithoutPart1() {
        String part3 = "%-128s".formatted("2300010000");
        assertThrows(IllegalArgumentException.class, () -> new CodaParser().parse(List.of(part3)));
    }

    @Test
    void mt940JoinsMultiLineField86AndSkipsStatementLevelInfo() {
        Statement s = new Mt940Parser().parse(List.of(
                ":20:STMT-20261124",
                ":25:BE68539007547034",
                ":60F:C261123EUR1000,00",
                ":86:STATEMENT LEVEL INFORMATION",
                ":61:2611251124CR250,00NTRFNONREF",
                ":86:DUPONT SA",
                "+++123/4567/89002+++",
                ":62F:C261124EUR1250,00"));
        Statement.Entry entry = s.entries().getFirst();
        assertEquals("DUPONT SA", entry.counterparty());
        assertEquals("123456789002", entry.reference());
        assertEquals(LocalDate.of(2026, 11, 24), entry.bookingDate()); // entry date, not value date 25 Nov
    }
}
