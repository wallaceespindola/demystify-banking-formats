package com.wallaceespindola.bankingformats;

import java.nio.file.Files;
import java.nio.file.Path;

/** Runs every example against the sample files in docs/samples. */
public final class Main {

    public static void main(String[] args) throws Exception {
        Path samples = Path.of(args.length > 0 ? args[0] : "../docs/samples");

        Statement coda = new CodaParser().parse(Files.readAllLines(samples.resolve("statement.coda")));
        Statement mt940 = new Mt940Parser().parse(Files.readAllLines(samples.resolve("statement.mt940")));
        Statement camt;
        try (var in = Files.newInputStream(samples.resolve("statement.camt053.xml"))) {
            camt = new Camt053Parser().parse(in);
        }
        System.out.println("CODA     -> " + coda);
        System.out.println("MT940    -> " + mt940);
        System.out.println("camt.053 -> " + camt);
        System.out.println("Same statement in all three formats: " + (coda.equals(mt940) && mt940.equals(camt)));

        try (var in = Files.newInputStream(samples.resolve("payment.pacs008.xml"))) {
            Pacs008ToMt103.Result result = new Pacs008ToMt103().translate(in);
            System.out.println();
            System.out.println(result.mt103());
            System.out.println();
            System.out.println("Lost in translation:");
            result.losses().forEach(loss -> System.out.println("  - " + loss));
        }
    }
}
