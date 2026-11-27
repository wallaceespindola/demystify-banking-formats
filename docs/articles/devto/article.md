---
title: "pain, pacs, camt, MT, MX: a field guide to banking formats"
published: false
description: "Open five real-looking bank files (pain.001, pacs.008, CODA, MT940, camt.053), learn what to look at in each and how to tell them apart, then run the Java examples."
tags: java, fintech, xml, tutorial
---

![Banner with flowing light ribbons converging on one point and the words Read the formats](banner.png)

If you join a payments, treasury or ERP team, the files arrive before the explanations do. Someone drops a folder on you with an XML file, a block of text full of colons and a file where every line is exactly 128 characters long, and asks you to "just import the statement". The acronyms come with it: pain, pacs, camt, MT, MX, CBPR+, CODA, PSD2.

Most of what you'll find online is either vendor marketing or a usage guideline the size of a phone book. Neither tells you how to look at a file and say what it is.

So I built a small repo around one payment and one bank statement, written out in every format that matters for the story: [demystify-banking-formats on GitHub](https://github.com/wallaceespindola/demystify-banking-formats). This article covers the hands-on part: you open each sample file, I point at the lines that matter, and at the end you run the Java examples and watch three different statement files turn into the same object.

## The map in one minute

The acronyms aren't competing formats. They answer different questions, and once you see the layers the rest gets much easier:

![Layered map of banking formats: ISO 20022 is the standard; pain, pacs and camt are message families defined by it; MT and MX are the syntaxes on Swift; CBPR+ and CGI-MP are usage rules that restrict the messages; CODA, CFONB 120, MT940 dialects, Norma 43 and CBI RH are national and legacy files replaced by camt.053; PSD2 and the API standards borrow ISO 20022 data](diagrams/format-layers.png)

- **ISO 20022:** the standard, a data dictionary plus a catalogue of messages.
- **pain, pacs, camt:** message families inside it. pain is customer to bank, pacs is bank to bank, camt is the bank reporting back to you.
- **MT and MX:** how messages look on the Swift network. MT is the old tag-based text format, MX is ISO 20022 XML on Swift.
- **CBPR+ and CGI-MP:** usage rules. Same ISO messages, fewer allowed fields and stricter lengths for a given path.
- **CODA and friends:** national statement files that camt.053 replaces.
- **PSD2:** an EU directive ([Directive (EU) 2015/2366](https://eur-lex.europa.eu/eli/dir/2015/2366/oj)) that opened account APIs. It's not a message format at all.

## Which format am I looking at?

You rarely need more than the first line or two to know what you're holding. This is the decision flow I use:

![Decision flow for identifying a bank file: XML with a Document namespace is ISO 20022, and the namespace tells you pain.001, pacs.008 or camt.053; a file starting with a {1:F01 block and :20: tags is Swift MT, and the type in the {2:} block tells you MT103 or MT940; fixed-width 128-character lines starting with 0 are CODA; JSON from an HTTPS call is a PSD2 account API](diagrams/which-format.png)

All the sample files live in `docs/samples/`. They're trimmed teaching samples: real structure, fictional parties (the BICs `BANKBEBB` and `BANKUS33` are made up), and not something you'd send to a bank without validating it against the bank's own guideline.

The story is the same everywhere. ACME NV in Brussels pays GLOBEX in the US USD 12,500.00. Separately, ACME's account receives 250.00 EUR from DUPONT SA with a Belgian structured reference, `+++123/4567/89002+++`.

## File 1: the payment order (pain.001)

Open `payment.pain001.xml`. The first thing to read is the namespace on `Document`:

```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.09">
```

That string tells you everything about the type. `pain` is the business area, `001` the message (credit transfer initiation), the next `001` the variant and `09` the version. When a bank says "we accept pain.001.001.09", this is the field they mean.

The thing that trips people up is that a pain.001 has three levels. Here they are, trimmed from the sample:

```xml
  <CstmrCdtTrfInitn>
    <GrpHdr>
      <MsgId>ACME-20261124-01</MsgId>
      <CreDtTm>2026-11-24T09:00:00</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
      <CtrlSum>12500.00</CtrlSum>
      <InitgPty><Nm>ACME NV</Nm></InitgPty>
    </GrpHdr>
    <PmtInf>
      <PmtInfId>BATCH-01</PmtInfId>
      <PmtMtd>TRF</PmtMtd>
      <ReqdExctnDt><Dt>2026-11-24</Dt></ReqdExctnDt>
      <Dbtr><Nm>ACME NV</Nm></Dbtr>
      <DbtrAcct><Id><IBAN>BE68539007547034</IBAN></Id></DbtrAcct>
      <DbtrAgt><FinInstnId><BICFI>BANKBEBB</BICFI></FinInstnId></DbtrAgt>
      <CdtTrfTxInf>
        <PmtId><EndToEndId>INV-2026-0042</EndToEndId></PmtId>
        <Amt><InstdAmt Ccy="USD">12500.00</InstdAmt></Amt>
        ...
      </CdtTrfTxInf>
    </PmtInf>
  </CstmrCdtTrfInitn>
```

`GrpHdr` describes the file: an ID, a timestamp, how many transactions and their control sum. `PmtInf` is a batch with one debtor account and one execution date. `CdtTrfTxInf` is one transfer. A file can carry many batches and a batch many transfers, so if you're generating these, `NbOfTxs` and `CtrlSum` have to add up across all of them or the bank rejects the file.

Keep an eye on `EndToEndId`. It's ACME's own reference (`INV-2026-0042`), and ISO 20022 lets it travel with the money and come back on the statement. The bank's answer to this file is a pain.002 status report, which points back at the same IDs.

## File 2: the bank-to-bank message (pacs.008)

Open `payment.pacs008.xml`. Same XML style, different namespace: `pacs.008.001.08`. This is what ACME's bank sends to GLOBEX's bank. On Swift it's called an MX message, but it's the same ISO 20022 XML you'd see anywhere else.

What to look at here is the identifiers:

```xml
      <PmtId>
        <InstrId>PAY-2026-0042</InstrId>
        <EndToEndId>INV-2026-0042</EndToEndId>
        <UETR>eb6305c9-1f7f-49de-aed0-16487c27b42d</UETR>
      </PmtId>
```

`InstrId` is the bank's reference, `EndToEndId` is the customer's reference carried over from the pain.001, and `UETR` is a UUID that tracks the payment across every bank it passes through. Further down you'll also see a fully structured address for the creditor (`StrtNm`, `BldgNb`, `PstCd`, `TwnNm`, `Ctry`), which matters for CBPR+, the usage rules for cross-border payments on Swift.

Two dates matter here. Swift's [coexistence period between MT and MX for cross-border payment instructions ended on 22 November 2025](https://www.swift.com/insights/news/iso-20022-bytes-payments-journey-continues), so for those, pacs.008 is the message now and MT103 is the legacy one. And CBPR+ was due to reject unstructured postal addresses from 14 November 2026, but in August 2026 [Swift accepted a community request to extend that migration](https://www.swift.com/news-events/news/swift-accepts-community-request-extend-structured-address-migration-iso-20022-payment-messages) and the final address date is still under consultation. So the date moved, but the address data in your systems still has to be split into those fields.

## File 3: the Belgian statement (CODA)

Now the statements. All three files describe the same day on ACME's account: opening balance 1,000.00 EUR, one credit of 250.00 from DUPONT SA, closing balance 1,250.00.

Open `statement.coda`. It has six lines of exactly 128 characters and nothing else (scroll right in the block below):

```text
0000024112672505        0000000000ACME NV                   BANKBEBB   00000000000                                             2
12001BE68539007547034                  EUR0000000001000000231126ACME NV                   CURRENT ACCOUNT                    001
21000100000000000000000000000000000000000250000241126001500001101123456789002                                      24112600100 1
2300010000BE71096123456769                     DUPONT SA                                                                     0 0
8001BE68539007547034                  EUR0000000001250000241126                                                                0
9               000004000000000000000000000000250000                                                                           2
```

CODA is the Febelfin standard for Belgian bank statements ([current version 2.7](https://febelfin.be/media/pages/publicaties/2023/febelfin-standaarden-voor-online-bankieren/5d601609cd-1754302976/standard-coda-2.7-en.pdf)). The record type is the first digit: `0` header, `1` old balance, `2x` movement, `3x` information, `4` free communication, `8` new balance, `9` trailer. Everything else is a position, and you need the spec open next to you to read it. Using the spec's 1-based positions:

- **Record 1 (old balance):** IBAN in 6-39, currency `EUR` in 40-42, sign in 43 (`0` = credit), amount in 44-58 as `000000001000000`, date in 59-64 as `231126`.
- **Record 21 (movement):** sign in 32, amount in 33-47 as `000000000250000`, entry date in 116-121. Position 62 is `1`, which says a structured communication follows: `101` and then `123456789002`.
- **Record 23 (movement, part 3):** the counterparty name `DUPONT SA` in 48-82.
- **Record 8 (new balance):** the closing `000000001250000` for 24 November.

Two details catch everyone. Amounts have three implied decimals, so `000000000250000` is 250.000. And dates are `DDMMYY`, so `231126` is 23 November 2026. The 12 digits `123456789002` are the Belgian structured reference without its `+++` dressing, which is what an accounting package uses to match the payment to an invoice.

## File 4: the Swift-style statement (MT940)

Open `statement.mt940`. It's short, so here's all of it:

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

The `{1:}` and `{2:}` blocks are the Swift headers. `O940` in the second block says "output, message type 940", and that's how you tell an MT940 from an MT103 without reading further. The data sits in block 4 as tags:

- `:25:` is the account.
- `:60F:` and `:62F:` are the opening and closing balances: `C` for credit, the date as `YYMMDD` (note: the opposite order from CODA), currency, then the amount with a comma as decimal separator.
- `:61:` is one statement line: value date `261124`, entry date `1124`, `C` for credit, `R` as the optional funds code, `250,00`, then the transaction type `NTRF` and references.
- `:86:` is the free text for the account owner.

That last tag is the problem. `:86:` is free text, and every bank structures it in its own way (German banks, for example, use `?20` to `?63` subfields). In this sample the counterparty and the structured reference are just mashed together: `DUPONT SA +++123/4567/89002+++`. The parser in the repo pulls them apart with a regex, and its Javadoc says plainly that this is one bank's dialect. "We support MT940" usually means "we support MT940 from the banks we've tested".

## File 5: the ISO statement (camt.053)

Open `statement.camt053.xml`. The namespace says `camt.053.001.08`, a bank-to-customer statement. Balances sit in `Bal` blocks with type codes (`OPBD` for opening booked, `CLBD` for closing booked), and each movement is an `Ntry`. Here's the entry, trimmed from the sample:

```xml
      <Ntry>
        <Amt Ccy="EUR">250.00</Amt>
        <CdtDbtInd>CRDT</CdtDbtInd>
        <Sts><Cd>BOOK</Cd></Sts>
        <BookgDt><Dt>2026-11-24</Dt></BookgDt>
        <ValDt><Dt>2026-11-24</Dt></ValDt>
        <BkTxCd><Domn><Cd>PMNT</Cd><Fmly><Cd>RCDT</Cd><SubFmlyCd>ESCT</SubFmlyCd></Fmly></Domn></BkTxCd>
        <NtryDtls>
          <TxDtls>
            ...
            <RltdPties>
              <Dbtr><Pty><Nm>DUPONT SA</Nm></Pty></Dbtr>
              <DbtrAcct><Id><IBAN>BE71096123456769</IBAN></Id></DbtrAcct>
            </RltdPties>
            <RmtInf>
              <Strd>
                <CdtrRefInf>
                  <Tp><CdOrPrtry><Cd>SCOR</Cd></CdOrPrtry><Issr>BBA</Issr></Tp>
                  <Ref>123456789002</Ref>
                </CdtrRefInf>
              </Strd>
            </RmtInf>
          </TxDtls>
        </NtryDtls>
      </Ntry>
```

Compare it with the other two. The amount has a real decimal point and an explicit credit/debit indicator. The dates are ISO dates. The counterparty name has its own element instead of hiding in free text, and the structured reference is marked as one (`SCOR`, issued by `BBA`, the Belgian scheme) instead of sitting at a fixed position or inside `:86:`. The `BkTxCd` block tells you what kind of transaction it was (a received SEPA credit transfer) with codes instead of a bank-specific type.

That's why camt.053 replaces CODA, MT940 and the other national files: each piece of data gets a field with a name.

## Run the examples

Reading files is half the job. The repo has a small Java 25 module in `examples/` (Java 25 and Maven, no dependencies beyond the JDK, JUnit 5 tests) with one parser per statement format and a single `Statement` record they all produce. From the repo root:

```bash
git clone https://github.com/wallaceespindola/demystify-banking-formats.git
cd demystify-banking-formats
make examples   # build and run the tests
make run        # parse docs/samples and print the result
```

Here's the first part of the `make run` output:

```text
CODA     -> Statement[iban=BE68539007547034, currency=EUR, openingBalance=1000.00, openingDate=2026-11-23, closingBalance=1250.00, closingDate=2026-11-24, entries=[Entry[bookingDate=2026-11-24, amount=250.00, counterparty=DUPONT SA, reference=123456789002]]]
MT940    -> Statement[iban=BE68539007547034, currency=EUR, openingBalance=1000.00, openingDate=2026-11-23, closingBalance=1250.00, closingDate=2026-11-24, entries=[Entry[bookingDate=2026-11-24, amount=250.00, counterparty=DUPONT SA, reference=123456789002]]]
camt.053 -> Statement[iban=BE68539007547034, currency=EUR, openingBalance=1000.00, openingDate=2026-11-23, closingBalance=1250.00, closingDate=2026-11-24, entries=[Entry[bookingDate=2026-11-24, amount=250.00, counterparty=DUPONT SA, reference=123456789002]]]
Same statement in all three formats: true
```

Three files that look nothing alike produce equal records. So parse each format at the edge, and let the rest of your code see only your own model.

The second part translates the pacs.008 back into an MT103 and lists what got lost on the way. Trimmed from the same output:

```text
:59:/123456789
Globex Corporation International Ho
North Industrial Park Boulevard 120
62701 Springfield IL US
...
Lost in translation:
  - :50K: structured address (StrtNm, BldgNb, PstCd, TwnNm, Ctry) flattened to free text
  - :59: name truncated, lost "ldings LLC"
  - :59: street truncated, lost "0"
  - :59: structured address (StrtNm, BldgNb, PstCd, TwnNm, Ctry) flattened to free text
  - EndToEndId INV-2026-0042 has no MT103 field (kept only if the bank maps it to :70:)
```

MT lines are 35 characters, so the creditor's name loses its tail and the building number `1200` becomes `120`. Nothing crashes. You just get a payment to a slightly different address with no end-to-end reference. The translator is a teaching example, not a production CBPR+ mapper, but the losses it reports are the kind you should look for in any real one.

## Things that will trip you up

**Namespace versions.** The same message comes in several versions (`camt.053.001.02`, `.08` and so on), and banks don't all move at once. The camt parser in the repo matches elements by local name and ignores the version namespace, so a version bump doesn't break it. Decide on purpose whether you want that.

**Dates and decimals.** CODA uses `DDMMYY` and three implied decimals, MT940 uses `YYMMDD` and a decimal comma, ISO 20022 uses ISO dates and a decimal point. Write a test for each, because a swapped day and month still parses on the first twelve days of every month.

**Field 86.** Budget one adapter per bank for MT940, not one per format.

**XML security.** These files come from outside your system. The camt parser disables DOCTYPE declarations on the JDK's `DocumentBuilderFactory` to shut out XXE, and you should do the same in your own code.

**PSD2 is not a file.** If someone asks for "the PSD2 format", they mean an account information or payment API over HTTPS. The data borrows ISO 20022 names, and your code calls it like any other HTTP API.

## Recap

- Read the first line. An XML namespace, a Swift `{2:}` block or a 128-character fixed-width record tells you the format.
- pain.001 has three levels: file, batch, transfer. pacs.008 is the bank-to-bank twin, with `EndToEndId` and `UETR` carried along.
- CODA is positions, MT940 is tags with a free-text `:86:`, camt.053 is named fields.
- Parse each format at the edge into one model, and test that every format produces the same result.
- Translating ISO 20022 down to MT loses data. Know what, before it happens in production.

## Try it yourself

Clone [the repo](https://github.com/wallaceespindola/demystify-banking-formats), run `make run`, then change one amount in `docs/samples/statement.mt940` and run `make examples` again to watch the MT940 test fail against the expected statement while the CODA and camt.053 tests keep passing. If your bank sends you a different dialect, a new `:86:` rule in `Mt940Parser` is the smallest place to start.

Which bank file format gives you the most trouble at work, and is it the format itself or each bank's flavour of it? I'd like to hear in the comments.

**Examples repository:** every example in this article (the pain.001, pacs.008, CODA, MT940 and camt.053 samples, the Java parsers and translator, and the diagrams) is in <https://github.com/wallaceespindola/demystify-banking-formats>.

Need more tech insights?
Check out my GitHub, LinkedIn, and Speaker Deck.
Happy coding!

Wallace Espindola, Solution Architect

- GitHub: <https://github.com/wallaceespindola/>
- LinkedIn: <https://www.linkedin.com/in/wallaceespindola/>
- Speaker Deck: <https://speakerdeck.com/wallacese>
