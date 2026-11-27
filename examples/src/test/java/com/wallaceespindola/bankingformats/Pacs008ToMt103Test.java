package com.wallaceespindola.bankingformats;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class Pacs008ToMt103Test {

    private Pacs008ToMt103.Result translate() throws Exception {
        try (var in = Files.newInputStream(Path.of("../docs/samples/payment.pacs008.xml"))) {
            return new Pacs008ToMt103().translate(in);
        }
    }

    @Test
    void mapsTheCoreFields() throws Exception {
        String mt = translate().mt103();
        assertTrue(mt.contains("{121:eb6305c9-1f7f-49de-aed0-16487c27b42d}"));
        assertTrue(mt.contains(":20:PAY-2026-0042"));
        assertTrue(mt.contains(":32A:261124USD12500,00"));
        assertTrue(mt.contains(":71A:SHA"));
    }

    @Test
    void reportsTheLongCreditorNameAsTruncated() throws Exception {
        assertTrue(translate().losses().stream().anyMatch(l -> l.startsWith(":59: name truncated")));
    }

    @Test
    void everyMtLineFitsIn35Characters() throws Exception {
        translate().mt103().lines()
                .filter(l -> !l.startsWith("{") && !l.startsWith("-"))
                .map(l -> l.replaceFirst("^:\\w+:", "")) // check the content after the tag too
                .forEach(l -> assertTrue(l.length() <= 35, "too long: " + l));
    }

    @Test
    void reportsTheMissingEndToEndId() throws Exception {
        assertTrue(translate().losses().stream().anyMatch(l -> l.startsWith("EndToEndId INV-2026-0042")));
    }
}
