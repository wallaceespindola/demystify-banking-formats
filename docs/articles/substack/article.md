---
title: "Keep one statement model, whatever the bank sends"
platform: Substack
newsletter: Code & Coffee
author: Wallace Espindola
description: "CODA, MT940 and camt.053 all describe the same bank statement. One canonical Java record, one adapter per format and one test that proves they agree keep the rest of your code out of the format mess."
tags: [banking, iso-20022, camt053, java]
---

**Suggested subject line:** Three bank files, one statement, one test

*Preview text: The habit that keeps bank formats out of your business code.*

---

![Banner for the Code and Coffee issue on keeping one bank statement model across CODA, MT940 and camt.053](banner.png)

# Keep one statement model, whatever the bank sends

Hey folks,

Some file archaeology first. If your company has a bank account in Belgium, the statement probably reaches your ERP as a CODA file: fixed-width lines of 128 characters, where the meaning of a field depends on the column it starts in. Open a second account somewhere else and you may get MT940, the Swift text format with tags like `:61:` and `:86:`. Ask the bank for something modern and they offer camt.053, the ISO 20022 XML statement that is meant to replace both.

All three describe the same account and the same money. The usual result is three import paths, each one leaking its format into the code around it: a reconciliation job that knows about column 43, a matching rule that greps `:86:` for a reference, a report that walks XML nodes. Every new bank or format change touches all of them.

I put the whole banking formats map in a repo, [demystify-banking-formats](https://github.com/wallaceespindola/demystify-banking-formats), with samples and small Java 25 examples. This issue is the coffee-length version of one habit from it: keep one statement model, write one adapter per format, and keep one test that proves the adapters agree.

## The habit: one record, many adapters

The idea is old (people call it a canonical model, or an anti-corruption layer if they want to sound serious). It works as a habit when the model is small, lives in code and has a test holding it in place.

Here's the canonical record from the repo, without the package and import lines:

```java
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

Two decisions in there do most of the work. Amounts are signed (credit positive, debit negative), so nobody downstream has to know that CODA uses `0` and `1` for the sign, MT940 uses `C` and `D`, and camt.053 uses `CRDT` and `DBIT`. The reference is digits only, so the Belgian structured reference `+++123/4567/89002+++` becomes `123456789002` no matter how the file wrote it.

Because it's a Java record, equality comes for free. That matters later, when the test compares three parsed statements with one `equals`.

## What each adapter deals with

Each format has its own way of being annoying, and the adapter is where that annoyance stays.

- **CodaParser:** reads by position. Every line must be exactly 128 characters, the record type is in the first column, and amounts carry three implied decimals. The parser cuts substrings at 1-based positions copied from the [Febelfin CODA specification](https://febelfin.be/media/pages/publicaties/2023/febelfin-standaarden-voor-online-bankieren/5d601609cd-1754302976/standard-coda-2.7-en.pdf), so you can check it against the spec line by line.
- **Mt940Parser:** reads by tag. Balances and movements are easy. The hard part is `:86:`, a free-text field that every bank structures differently. The repo's parser handles one bank's dialect, and its Javadoc says so, because that's the honest scope of any MT940 parser.
- **Camt053Parser:** reads XML with the JDK's DOM parser, ignores the namespace version so a newer camt.053 doesn't break it, and turns off DOCTYPE declarations to block XXE. The structured reference has its own element here, so no regex is needed.

![Adapter sketch: statement.coda, statement.mt940 and statement.camt053.xml each go through their own parser (positions, tags with the :86: dialect, DOM without XXE) into one Statement record, which feeds reconciliation and ERP import, while a test checks that all three parse to the same expected statement](diagrams/adapter-sketch.png)

Nothing to the right of the `Statement` box knows which file came in. I'd draw this on a whiteboard before writing any parser.

## The test that keeps it honest

A canonical model without a test drifts. Someone adds a field in one adapter and forgets the others, or one parser starts returning the reference with slashes. The repo pins it with one expected `Statement` and three tests that parse each sample file and compare the result with it. The sample files are trimmed teaching samples for the same account and the same 250.00 EUR credit from DUPONT SA, so they should all produce the same record.

You can see it from the command line too. Run it from the repo root:

```bash
make run
```

After printing the parsed statement from each format, the last line of that block is the one I care about:

```text
Same statement in all three formats: true
```

That line costs almost nothing to produce, and the JUnit tests behind `make examples` check the same thing in every build. When it turns `false`, the diff between the three printed records tells you which adapter moved. (The same `make run` also shows the pacs.008 to MT103 translation losses, which is another newsletter.)

So every time you add a format or a bank, you add a sample file for a statement you already know, and you add it to the test. If the new adapter can't produce the same `Statement`, you find out on your laptop, not in the month-end reconciliation.

## Why this matters more now

For years MT940 felt like the safe default, because every bank could produce it. Swift [ended the MT/MX coexistence period for cross-border payment instructions on 22 November 2025](https://www.swift.com/insights/news/iso-20022-bytes-payments-journey-continues), and ISO 20022 is now the language of the payment side. That cut-off is about payment instructions between banks. What your ERP imports is a separate question, and plenty of companies will keep reading CODA or MT940 next to camt.053 for a while.

That "for a while" is exactly where a canonical model pays off. You can switch one account from MT940 to camt.053, run both in parallel, and compare `Statement` objects instead of comparing ERP screens. The business code doesn't change at all during the migration. I don't have numbers on how long these parallel runs usually take, and I won't guess.

## Where it doesn't pay off

A canonical model means deciding what to drop, and that has a cost.

The repo's `Statement` is deliberately small. Look at the camt.053 sample and you'll find things it drops: the EndToEndId of the payment, the debtor's IBAN, the bank transaction code (`PMNT`, `RCDT`, `ESCT`), the value date. Some of that is in the older files too (CODA and MT940 both carry the value date and a transaction code, and CODA has the counterparty account), but the EndToEndId and the ISO transaction codes only arrive with camt.053. If your canonical model is the lowest common denominator of your oldest format, you throw away exactly the data the new format was supposed to give you. So the model has to grow with your needs, and fields that only some formats can fill have to be optional, with that rule written down.

A few more cases where I wouldn't bother:

- **One bank, one format, no plans to change:** a direct parser into your domain is fine. The extra layer buys nothing yet.
- **You already buy a bank connectivity product that normalizes statements:** then its output model is your canonical model, and your job is to test against it, not to write another one.
- **The MT940 `:86:` field is your real problem:** a canonical model doesn't fix bank dialects. You still need one rule set per bank, the canonical model just keeps those rules in one place.

And the test only proves what the samples cover. Three tidy samples with one credit each won't catch a debit with a reversal code or a multi-page statement. You still need real (anonymized) files from each bank before you trust an adapter in production.

## Try this on Monday

You don't need a migration project to start. One afternoon is enough to see whether the habit fits your code:

1. Find every place your code reads a bank statement file. Write down the format and the bank for each one.
2. Write a small `Statement` record with only the fields your downstream code actually uses today. Sign the amounts, normalize the reference.
3. Move one existing parser behind it, so it returns `Statement` and nothing else leaves the adapter.
4. Take one real statement (anonymized) that you know well, write the expected `Statement` by hand, and add a test that the parser produces it.
5. When the next format shows up, add its file for the same statement to that test before you write the adapter.

Then clone [the repo](https://github.com/wallaceespindola/demystify-banking-formats), run `make examples` to build and test it, and compare its three parsers with yours. The samples and the record are small on purpose, so they're easy to copy and adapt.

## Wrapping up

Banks will keep sending whatever format they send, and for a while it will be more than one. Your code doesn't have to care. Keep one statement model, push every format quirk into its own adapter, and keep the test that says the adapters agree. When that test prints `true`, the format is just a detail of how the file got to you.

My question for you: how many statement formats does your code read today, and does anything check that they agree? Hit reply or drop a comment. I read every one.

**Examples repository:** every example in this article (the `Statement` record, the CODA, MT940 and camt.053 parsers, the sample files, the tests and the diagram) is in <https://github.com/wallaceespindola/demystify-banking-formats>.

Need more tech insights?
Check out my GitHub, LinkedIn, and Speaker Deck.
Happy coding!

Wallace Espindola, Solution Architect

- GitHub: <https://github.com/wallaceespindola/>
- LinkedIn: <https://www.linkedin.com/in/wallaceespindola/>
- Speaker Deck: <https://speakerdeck.com/wallacese>
