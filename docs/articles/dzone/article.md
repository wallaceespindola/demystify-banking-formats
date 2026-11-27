<!--
DZone submission metadata
TL;DR: Migrate bank statements from CODA or MT940 to camt.053 behind one canonical model, then run old and new feeds in parallel with reconciliation gates.
Article type: Analysis
Summary (meta description): An enterprise pattern for statement-format migration: an anti-corruption layer, one canonical Statement model, adapters per format and a gated parallel run.
Primary zone: Software Design and Architecture. Additional zones: Java, Integration, DevOps.
Tags: iso-20022, banking, java, enterprise-integration, migration, design-patterns
-->

# A canonical model for bank statement migration: CODA and MT940 to camt.053

![Banner for an article on migrating bank statement formats behind one canonical model](banner.jpg)

Most companies that read bank statements automatically do it through a format that was chosen a long time ago. In Belgium that is often CODA, a fixed-width file with 128-character records. Elsewhere it is MT940, the Swift text statement, with a `:86:` field that every bank fills in its own way. The bank now offers camt.053, the ISO 20022 XML statement, and someone has to move the reconciliation, ERP posting and cash position jobs over without breaking month-end close.

Parsing the XML is the easy part. The hard part is that the old format has leaked into everything downstream. Matching rules read the `:86:` text, ERP mappings know column positions, and a cash report somewhere assumes amounts have three implied decimals. Swap the file and all of those break at once, usually in a place nobody tested.

This article proposes a way to run that migration as a gated engineering change instead of a big-bang cutover: an anti-corruption layer around the bank formats, one canonical `Statement` model, an adapter per format, and a parallel run where the old and new feeds must agree before consumers switch. The code is in the companion repository, [demystify-banking-formats](https://github.com/wallaceespindola/demystify-banking-formats), a small Java 25 module with no dependencies beyond the JDK and JUnit 5. It's a proposal. I don't have adoption numbers from a live migration, and I won't invent any.

## Why statement migrations go wrong

Statement formats look interchangeable from a distance. They all carry an account, an opening balance, booked entries and a closing balance. Up close they disagree on almost everything that matters to a parser:

- **CODA:** fixed positions, a record type in the first column, amounts as 15 digits with 3 implied decimals and a separate sign digit. The current specification is [version 2.7, published by Febelfin](https://febelfin.be/media/pages/publicaties/2023/febelfin-standaarden-voor-online-bankieren/5d601609cd-1754302976/standard-coda-2.7-en.pdf).
- **MT940:** tags such as `:60F:` and `:61:`, a comma as decimal separator, and a `:86:` field that is free text in practice.
- **camt.053:** namespaced XML, balance types identified by codes (`OPBD`, `CLBD`), a credit or debit indicator per amount, and much richer detail per entry.

Here is the MT940 sample from the repo. Look at the `:86:` line, because that's where the trouble lives:

```text
{1:F01BANKBEBBAXXX0000000000}{2:O9401700261124BANKBEBBAXXX00000000002611241700N}{4:
:20:STMT-20261124
:25:BE68539007547034
:28C:001/1
:60F:C261123EUR1000,00
:61:2611241124CR250,00NTRFNONREF//0000000000000001
:86:DUPONT SA +++123/4567/89002+++
:62F:C261124EUR1250,00
-}
```

The counterparty name and the Belgian structured reference sit in one line of text. German banks, for example, split `:86:` into numbered subfields (`?20` to `?29` for the purpose, `?32` for the counterparty), and other banks use their own layouts. (The BIC `BANKBEBB` in the repo's samples is fictional.) When downstream matching code reads that text directly, it depends on one bank's dialect of MT940, and the migration has to reproduce that dialect's behavior in a completely different format.

There is also a timing trap. The Swift [MT/MX coexistence period for cross-border payment instructions ended on 22 November 2025](https://www.swift.com/insights/news/iso-20022-bytes-payments-journey-continues), and it's easy to read that as "MT statements are gone too". That date is about payment instructions. It doesn't set a date for the statement file your ERP imports, and in my view a statement migration is usually paced by the bank's product plans and your own platform work. That's another reason to treat it as a controlled change rather than a regulatory fire drill.

## The pattern in one paragraph

Put every bank format behind an **anti-corruption layer**: a set of adapters, one per format (and per bank dialect where needed), whose only job is to turn a file into one canonical `Statement`. Consumers (reconciliation, ERP posting, cash position) depend on `Statement` and nothing else. During the migration both the old and the new feed for the same account and day go through their adapters, and a comparator checks that the two `Statement` values are equal. Consumers switch only after an exit gate is met, and the old adapter is removed afterwards.

![Migration architecture: CODA, MT940 and camt.053 files enter an anti-corruption layer with one parser adapter per format, every adapter produces the same canonical Statement, consumers such as reconciliation, ERP posting and cash position read only Statement, and a temporary comparator receives both feeds during the parallel run](diagrams/migration-architecture.jpg)

The name comes from domain-driven design, and it fits well here. The bank's formats are a model you don't control and can't change. The anti-corruption layer stops that model from shaping yours.

## The canonical model

The model has to be small enough that every format can fill it and rich enough that consumers don't need to reach past it. Here's the record from the repo, unchanged:

```java
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
```

Three decisions in that record carry most of the weight.

**Signed amounts.** CODA uses a sign digit, MT940 uses `C`, `D` and `RC`, camt.053 uses `CRDT` and `DBIT`. The model has one rule (credit positive, debit negative), and every adapter converts to it. Consumers never see a credit/debit flag, so they can't get it backwards.

**Normalized references.** The Belgian structured reference appears as `+++123/4567/89002+++` in MT940 text, as 12 digits after a `101` marker in CODA, and as `<Ref>123456789002</Ref>` in camt.053. The model stores digits only. Matching code compares strings without knowing where they came from.

**A record, so equality is free.** Java records get `equals` from their components, which is what makes the parallel-run comparison a one-liner. There's a catch: `BigDecimal.equals` compares scale, so `250.0` and `250.00` aren't equal. That's why every adapter in the repo calls `setScale(2)` before building the record. If you skip that, the comparator reports differences that aren't real, and people stop trusting it.

The model also leaves things out on purpose. The camt.053 sample carries an `EndToEndId` and an ISO bank transaction code (`PMNT`/`RCDT`/`ESCT`) that the other two formats don't have. The debtor's IBAN is there too, but so is it in CODA record 23, and all three formats carry a value date. `Statement` leaves all of these out. That's a design choice, not a gap in the formats, and in production you'd revisit it (see the trade-offs below).

## Adapters per format

Each adapter is a plain class that takes the raw input and returns `Statement`. The repo has three: `CodaParser` reads fixed positions with a `switch` on the record type, `Mt940Parser` reads tags with regular expressions, and `Camt053Parser` uses the JDK DOM parser, matching elements by local name so any camt.053 version namespace works, with DOCTYPE declarations disabled (no XXE). The comment at the top of the MT940 adapter says plainly where the risk sits:

```java
/**
 * Reads a single-statement MT940 text block. Field 86 is free text, so the counterparty and
 * reference rules below are one bank's dialect: this is the part every bank does differently.
 */
public final class Mt940Parser {
```

The adapters don't share an interface in the repo, on purpose: CODA and MT940 take a list of lines, camt.053 takes an `InputStream`, and an interface with one method per input type would add ceremony to a teaching example. In a production ingestion service I'd put one at the boundary, keyed by format and bank, so a new dialect is a new adapter and a registry entry rather than an `if` in shared code. The rule that matters is the same either way: an adapter may know everything about one format and nothing about consumers.

`Main` shows how thin the wiring is once the adapters exist. This is trimmed to the statement part:

```java
        Statement coda = new CodaParser().parse(Files.readAllLines(samples.resolve("statement.coda")));
        Statement mt940 = new Mt940Parser().parse(Files.readAllLines(samples.resolve("statement.mt940")));
        Statement camt;
        try (var in = Files.newInputStream(samples.resolve("statement.camt053.xml"))) {
            camt = new Camt053Parser().parse(in);
        }
        ...
        System.out.println("Same statement in all three formats: " + (coda.equals(mt940) && mt940.equals(camt)));
```

Run it from the repo root:

```bash
make run
```

The first lines of the output are the three parsed statements and the comparison:

```text
CODA     -> Statement[iban=BE68539007547034, currency=EUR, openingBalance=1000.00, openingDate=2026-11-23, closingBalance=1250.00, closingDate=2026-11-24, entries=[Entry[bookingDate=2026-11-24, amount=250.00, counterparty=DUPONT SA, reference=123456789002]]]
MT940    -> Statement[iban=BE68539007547034, currency=EUR, openingBalance=1000.00, openingDate=2026-11-23, closingBalance=1250.00, closingDate=2026-11-24, entries=[Entry[bookingDate=2026-11-24, amount=250.00, counterparty=DUPONT SA, reference=123456789002]]]
camt.053 -> Statement[iban=BE68539007547034, currency=EUR, openingBalance=1000.00, openingDate=2026-11-23, closingBalance=1250.00, closingDate=2026-11-24, entries=[Entry[bookingDate=2026-11-24, amount=250.00, counterparty=DUPONT SA, reference=123456789002]]]
Same statement in all three formats: true
```

That `true` is the check the migration repeats at scale: the same 250.00 EUR from DUPONT SA, read from three formats, ends up as one value. The sample files are trimmed teaching samples with one entry each, so treat this as the shape of the check, not proof that your bank's files behave.

## The first gate: one expected statement, three formats

Before any parallel run, the adapters need a test that pins them to the same expected value. The repo's test does exactly that. Trimmed to the shared fixture and the three format tests:

```java
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
```

In a real migration you'd ask the bank for the same business days in both formats and turn each pair into a fixture like this. Every edge case you find later (a reversal, a fee entry, a statement split across files) becomes another pair in the same suite. The suite then doubles as documentation of what "the same statement" means for your accounts.

This is gate zero, and it runs in CI on every change to an adapter. If it isn't green, there's no point starting the parallel run.

## The parallel run and its gates

The parallel run catches what the fixtures miss. For a period agreed with the business, the bank delivers both the old and the new feed for the same accounts. Consumers keep reading the old one. Every business day, both feeds go through their adapters and the comparator checks the two `Statement` values.

![Parallel-run flow: when a business day closes, the old CODA or MT940 feed and the new camt.053 feed each go through their adapter to a Statement, a decision checks whether the two statements are equal, a mismatch is classified as an adapter bug, a bank data issue or an expected difference before the next business day, a match records a clean day, and once the exit gate of agreed clean days with no open differences is met consumers switch to the new feed while the old feed is kept as a shadow and then decommissioned](diagrams/parallel-run.jpg)

Equality alone isn't enough for operations. When the comparison fails, someone has to decide what kind of failure it is, and the classification drives who acts:

- **Adapter bug:** the new adapter reads something wrong (a sign, a date, a balance type). The integration team fixes it, adds the day as a fixture and replays it.
- **Bank data issue:** the two files from the bank disagree, for example an entry missing from one of them. The team that owns the bank relationship raises it with the bank. Your adapters can't fix it.
- **Expected difference:** the formats carry different information. The classic case is the counterparty name: MT940 text may be cut or abbreviated where camt.053 has the full `<Nm>`. You write down the rule (for example, compare names after normalization, or only compare the reference) and change the comparator, not the data.

For the comparator itself, record equality is a good first cut, but a diff that names the field is far more useful when the job fails before the morning close. In practice I'd compare balances first, then match entries by booking date, amount and reference, and report unmatched entries on each side. When balances agree and entries don't, it's usually a split or merged entry. When balances disagree, something is wrong, and that should page someone.

Then define the exit gate before you start, in writing, with the people who own the numbers. A reasonable shape:

1. Gate zero green: the fixture suite passes for every format and bank in scope.
2. A number of consecutive clean business days, agreed with finance, that covers at least one month-end close.
3. No open differences classed as adapter bug or bank data issue.
4. Every expected difference documented, with the comparator rule that handles it.
5. A sign-off from the consumer owners (reconciliation, ERP, treasury) that their outputs match on the new feed.

I'm deliberately not giving a number of days. It depends on volume, on how many banks and account types are in scope and on how much month-end variety you need to see, and I have no data that would justify a default.

After the switch, keep the old feed flowing for a while as a shadow. If something goes wrong in the first close on the new feed, you can still compare against it. Remove the old adapter when the old feed stops, so it doesn't rot in the codebase pretending to be supported.

## Who owns the adapters

The pattern moves format knowledge into one place, which only helps if someone owns that place. The split I'd propose:

- **Integration team** (or whoever runs bank connectivity): owns the anti-corruption layer, every adapter, the fixture suite and the comparator. Adding a bank or a dialect is their change, reviewed like any other code.
- **Consumer teams** (reconciliation, ERP posting, cash): own their logic against `Statement` only. A pull request in a consumer that imports a bank-format class is a review finding.
- **Owner of the canonical model:** a named person or small group, because every new field in `Statement` must be filled by every adapter. Model changes go through them.
- **Bank relationship owner:** raises bank data issues, requests sample files and parallel delivery, and agrees the end date for the old feed.
- **Finance or treasury:** agrees the exit gate and signs off the switch. They own the numbers, so they own the decision.

The part I'd want an architect to check is the model owner role. Without it, `Statement` grows a field for every consumer request, adapters start returning `null` for half of them, and you're back to consumers knowing which format a value came from.

## Trade-offs and where it doesn't pay off

The canonical model is a lowest common denominator by default. That's the main cost. All three formats carry more than `Statement` keeps: value dates, transaction codes and the counterparty account exist in CODA and MT940 too, in their own shapes. What camt.053 adds is the `EndToEndId`, ISO bank transaction codes (domain, family, sub-family) and structured remittance data. If your reconciliation would match better with those fields, a model limited to what every format can supply leaves value on the table. The way out is to version the model: run the migration against the small model, and only after the old feed is gone extend `Statement` with what camt.053 alone provides. Don't try to do both at once.

The parallel run costs money and attention. Two feeds per account, a daily comparison and a person who reads its output. For a handful of accounts at one bank, a careful cutover with a few historical days replayed may be enough.

It doesn't pay off at all in some setups:

- **One bank, one format, no change planned.** An anti-corruption layer around a format you will never replace is indirection without a payoff.
- **The ERP already imports camt.053 natively** and nothing else reads the statements. Let the vendor's import do the work and test it on historical files.
- **You need fields only one format has, right now.** If treasury needs the bank transaction codes from day one, a shared model with CODA doesn't help them. Build that consumer on the camt.053 adapter's richer output and keep it out of the comparison.

One more limit: the adapters in the repo are teaching code. They read the records and tags the samples use and skip the rest (CODA record types 3, 4 and 9, MT940 multi-page statements, camt.053 files with more than one `<Stmt>`). A production adapter needs those, plus input validation and a clear error when a file doesn't match the expected dialect.

## Wrapping up

A statement-format migration is mostly a dependency problem. If consumers read the bank's format directly, every one of them has to migrate, and they all break together. Put the formats behind an anti-corruption layer, give consumers one canonical `Statement`, and the migration shrinks to writing a new adapter and proving that it agrees with the old one. The proof comes in two gates: a fixture suite that pins every adapter to the same expected value, and a parallel run with classified differences and an exit gate that finance agrees to before you start.

The full module (the `Statement` record, the three adapters, the tests and the sample files) is in the [companion repository](https://github.com/wallaceespindola/demystify-banking-formats). Run `make run`, then point the adapters at a day of your own bank's files and see where they disagree.

Have you run a statement migration with a parallel run, or did you cut over on a fixed date? I'm curious whether others formalized the reconciliation gates or handled the differences case by case. Let me know in the comments.

**Examples repository:** every example in this article (the `Statement` record, the CODA, MT940 and camt.053 parsers, the parser tests, the sample statement files and the diagrams) is in <https://github.com/wallaceespindola/demystify-banking-formats>.

Need more tech insights?
Check out my GitHub, LinkedIn, and Speaker Deck.
Happy coding!

Wallace Espindola, Solution Architect

- GitHub: <https://github.com/wallaceespindola/>
- LinkedIn: <https://www.linkedin.com/in/wallaceespindola/>
- Speaker Deck: <https://speakerdeck.com/wallacese>
