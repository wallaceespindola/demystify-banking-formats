# Article Context: Demystifying Banking Formats and Standards

> Working notes and research brief for a future article (and possibly a series + talk).
> Author: Wallace Espindola, Solution Architect.
> Status: context / pre-draft. Created 2026-11-22.

Original seed note:

> New article - Demystifying Banking Formats and Standards
> CODA, CAMT, MT, MX, CGI, PSD2, ISO 20022, CBPR+, PACS, PAIN

---

## 1. Why this article

Engineers joining a bank, a fintech, a treasury/ERP team or a payment hub get hit by an alphabet soup on day one:
MT103, pacs.008, camt.053, CODA, CBPR+, PSD2. Most of the material out there is either vendor marketing,
800-page specs, or forum answers that mix up the layers. Nobody draws the map first.

The article's job: **give the reader a mental map**. Which of these is a syntax, which is a message, which is a
network rulebook, which is a law, and how a single payment travels through all of them.

### Core thesis

These ten acronyms are not ten competing formats. They sit on different layers:

| Layer | What it answers | Items from the list |
|-------|-----------------|---------------------|
| Standard / methodology | How are financial messages modeled and named? | ISO 20022 |
| Message families (business areas) | What kind of message is it? | PAIN, PACS, CAMT |
| Network syntax | What does it look like on the wire? | MT (legacy SWIFT FIN), MX (ISO 20022 XML on SWIFT) |
| Usage rulebooks | Which subset and rules apply on this network/corridor? | CBPR+, CGI-MP (also SEPA EPC, HVPS+) |
| Local / national formats | What does my bank actually send me here? | CODA (Belgium) |
| Regulation | What must banks legally expose and how is access secured? | PSD2 |

If the reader leaves with that table in their head, the article worked.

### Target audience

- Backend engineers and architects building payment, treasury, reconciliation or ERP integrations.
- Developers moving from MT to ISO 20022 (the migration is the reason many are reading).
- Fintech engineers dealing with PSD2 APIs who wonder how that relates to "the XML stuff".
- Level: intermediate to senior. Skip "what is XML". Explain banking domain terms once.

### Possible titles

- Demystifying Banking Formats: CODA, MT, MX and the ISO 20022 Alphabet Soup
- From MT103 to pacs.008: A Developer's Map of Banking Message Standards
- PAIN, PACS, CAMT and Friends: How Money Actually Moves Between Systems
- ISO 20022 Without Tears: A Field Guide for Engineers
- One Payment, Ten Acronyms: Following a Euro Through the Banking Stack

---

## 2. The topics, expanded

### 2.1 ISO 20022 (the foundation)

- International standard for financial messaging, run by ISO TC68, registration authority is SWIFT.
- It is a **methodology + business model + message catalogue**, not "an XML format". XML (XSD) is the main
  syntax today; JSON and ASN.1 representations exist for some uses (APIs).
- Rich, structured data: separate fields for debtor/creditor names, structured postal addresses, LEIs,
  purpose codes, remittance info, ultimate parties. This is the main business argument vs MT.
- **Message naming convention**: `pain.001.001.09`
  - `pain` = business area
  - `001` = message functionality number
  - `001` = variant (001 = global/base)
  - `09` = version
- Business areas worth naming: `pain` (payments initiation), `pacs` (clearing and settlement),
  `camt` (cash management), `acmt` (account management), `remt` (remittance), `head` (business application
  header), `sese`/`semt`/`setr` (securities), `admi` (administration).
- Envelope: **Business Application Header (BAH, `head.001`)** + Document. Many integration bugs come from
  ignoring the BAH.
- Adoption landmarks (verify dates before publishing):
  - SEPA (SCT, SDD, SCT Inst) uses ISO 20022 since the 2008-2014 rollout.
  - Eurosystem T2 (ex TARGET2) and EURO1 migrated in March 2023.
  - UK CHAPS migrated in June 2023.
  - US Fedwire Funds migrated in July 2025.
  - SWIFT cross-border MT/MX coexistence for payment instructions ended November 2025.

### 2.2 PAIN (Payments Initiation)

Customer-to-bank (C2B) and bank-to-customer status. What an ERP / treasury system sends to its bank.

| Message | Purpose |
|---------|---------|
| pain.001 | Customer Credit Transfer Initiation (the "payment file", batch of transfers) |
| pain.002 | Customer Payment Status Report (accepted, rejected, pending, with reason codes) |
| pain.007 | Customer Payment Reversal |
| pain.008 | Customer Direct Debit Initiation |
| pain.009-012 | Mandate initiation, amendment, cancellation, acceptance |
| pain.013 / pain.014 | Creditor Payment Activation Request / Status (Request to Pay) |

Key structure of pain.001: `GrpHdr` (group header, message id, control sum, number of transactions) →
`PmtInf` (one debtor account, execution date, batch booking) → `CdtTrfTxInf` (each transfer).
Explain the three levels; it is the first thing developers get wrong.

### 2.3 PACS (Payments Clearing and Settlement)

Bank-to-bank (interbank). What travels between banks, through clearing systems or correspondents.

| Message | Purpose | MT ancestor |
|---------|---------|-------------|
| pacs.008 | FI to FI Customer Credit Transfer | MT103 |
| pacs.009 | Financial Institution Credit Transfer (incl. COV variant) | MT202 / MT202COV |
| pacs.002 | FI to FI Payment Status Report | (MT199 free text, partly) |
| pacs.004 | Payment Return | MT103/MT202 return by convention |
| pacs.003 | FI to FI Customer Direct Debit | (none common) |
| pacs.010 | Financial Institution Direct Debit | MT204 |
| pacs.028 | FI to FI Payment Status Request | MT199/MT299 queries |

### 2.4 CAMT (Cash Management)

Account reporting, notifications, exceptions and investigations.

| Message | Purpose | MT ancestor |
|---------|---------|-------------|
| camt.052 | Bank to Customer Account Report (intraday) | MT942 |
| camt.053 | Bank to Customer Statement (end of day) | MT940 / MT950 |
| camt.054 | Bank to Customer Debit/Credit Notification | MT900 / MT910 |
| camt.056 | FI to FI Payment Cancellation Request | MT192 / MT292 |
| camt.029 | Resolution of Investigation | MT196 / MT296 |
| camt.060 | Account Reporting Request | MT920 |
| camt.057 | Notification to Receive | MT210 |

camt.053 is the bridge to CODA (see 2.8): it is the ISO 20022 statement replacing local formats.
Point out the reconciliation gain: structured remittance info and end-to-end IDs survive the whole chain.

### 2.5 MT (SWIFT FIN, legacy)

- SWIFT's proprietary message format since the 1970s, ISO 15022 based. Text, tag-based, fixed character set
  (SWIFT X charset), field length limits (35 chars x 4 lines for party info).
- Structure: blocks `{1:}` basic header, `{2:}` application header, `{3:}` user header (UETR lives in
  field 121 here since gpi), `{4:}` text block with fields like `:20:`, `:32A:`, `:50K:`, `:59:`, `:71A:`,
  `{5:}` trailer.
- Categories: MT1xx customer payments, MT2xx FI transfers, MT3xx treasury/FX, MT5xx securities,
  MT7xx trade finance, MT9xx cash management/statements.
- Famous ones: MT101, MT103, MT202, MT202COV, MT940, MT942, MT950, MT199.
- Show a short annotated MT103 snippet vs the equivalent pacs.008 fragment. Best visual in the article.
- Status: for cross-border payment instructions on SWIFT, MT is retired since the end of coexistence
  (November 2025). MT still lives on in reporting (MT9xx), trade, securities, and inside many banks' cores
  via translation layers. Verify the exact current scope of the SWIFT retirement before publishing.

### 2.6 MX

- "MX" is **SWIFT's name for ISO 20022 XML messages exchanged on the SWIFT network** (FINplus service).
- MX is not a separate standard. pacs.008 on SWIFT = an MX message. Same XML in SEPA = just ISO 20022.
- Clarify common confusion: "MT vs MX" = old SWIFT syntax vs ISO 20022 on SWIFT. "MT vs ISO 20022" is the
  more precise phrasing.
- Translation: SWIFT publishes MT↔MX translation rules; in-flow translation was offered during coexistence.
  Lossy in the MX→MT direction (truncation of rich data). Great "gotcha" section.

### 2.7 CBPR+ (Cross-Border Payments and Reporting Plus)

- SWIFT working group and **usage guidelines** defining how ISO 20022 messages are used for cross-border and
  correspondent banking on SWIFT.
- Restricts the base ISO message: which fields are mandatory, cardinalities, character sets, code lists.
  Published on SWIFT MyStandards. Validation against plain ISO XSD is not enough; you need the CBPR+ usage
  guideline (or a library implementing it).
- Coexistence: started March 2023, ended November 2025 for payment instructions (MT103/MT202 family).
- Structured / hybrid postal address requirements: fully unstructured addresses phased out around
  November 2026. **That is now; verify the exact rule and date at publication time.** Good hook for the
  article ("this month, your free-text addresses start bouncing").
- Sister guidelines: **HVPS+** (high-value payment systems: T2, CHAPS, Fedwire alignment).

### 2.8 CODA (Belgium)

- **COded statement of Account / COded DAta**, Belgian standard by Febelfin for bank statements,
  used by accountants and ERPs to import statements and reconcile automatically.
- Fixed-width text, **128 characters per line**, record types identified by first digit:
  - `0` header
  - `1` old balance
  - `2` movement records (`21`, `22`, `23`)
  - `3` information records (`31`, `32`, `33`)
  - `4` free communication
  - `8` new balance
  - `9` trailer
- Structured communication (Belgian "+++123/4567/89012+++" OGM/VCS reference) parsed for auto-matching.
- Current version 2.6 (verify). Febelfin pushes CAMT.053 as the successor; many banks deliver both.
- Why include it: a concrete example of **national legacy formats** (others: MT940 dialects in the
  Netherlands and Germany, AEB43 in Spain, CFONB120 in France, BAI2 in the US). The pattern is the same:
  fixed-width or tag-based local format → camt.053.
- Nice code demo: parse a CODA file in ~50 lines of Java 25 (records + switch pattern matching on record
  type) and map to the same domain model as camt.053.

### 2.9 CGI (Common Global Implementation)

- **CGI-MP (Common Global Implementation - Market Practice)**: industry initiative (banks, corporates,
  vendors, hosted under SWIFT) that publishes harmonized implementation guidelines for **corporate-to-bank**
  ISO 20022 messages: pain.001, pain.002, pain.008, camt.052, camt.053, camt.054.
- Goal: one pain.001 that works with all your banks instead of one dialect per bank.
- Reality check for the article: banks still publish their own "CGI-based" guides with deviations. Treasury
  teams still test file formats bank by bank.
- Relation: CBPR+ governs bank-to-bank on SWIFT; CGI-MP governs corporate-to-bank. Same ISO messages,
  different rulebooks.
- Disambiguate: not "CGI" the IT services company, not Common Gateway Interface.

### 2.10 PSD2 (Payment Services Directive 2)

- EU Directive (EU) 2015/2366, applied from January 2018; SCA RTS applied from September 2019.
- **Not a message format.** It is regulation that forced banks to open access to accounts via APIs
  (XS2A, access to account) for licensed third parties:
  - AISP: Account Information Service Provider
  - PISP: Payment Initiation Service Provider
  - CBPII: Card-Based Payment Instrument Issuer
- Strong Customer Authentication (SCA): two of knowledge / possession / inherence, plus dynamic linking.
- eIDAS certificates (QWAC, QSealC) identify TPPs.
- PSD2 itself does not define an API. Market standards fill the gap: **Berlin Group NextGenPSD2**
  (most of the EU), **STET** (France), **UK Open Banking** (UK, separate regime post-Brexit).
  These are REST/JSON, but their payment payloads borrow ISO 20022 data elements.
- What is next: **PSD3 + Payment Services Regulation (PSR)** and **FIDA** (Financial Data Access).
  Check legislative status at publication time.
- Related EU items worth one line each: Instant Payments Regulation (EU) 2024/886 and
  **Verification of Payee** (VoP, live since October 2025 in the eurozone).

---

## 3. One payment, end to end (narrative backbone)

Use a single story to tie everything together. Example: a Belgian company pays a US supplier in USD.

1. ERP generates a **pain.001** (CGI-MP compliant) and uploads it to its Belgian bank (EBICS, host-to-host,
   or SWIFT FileAct).
2. Bank replies with **pain.002** (accepted / rejected with reason code).
3. Bank sends **pacs.008** over SWIFT under **CBPR+** rules to its correspondent (cover via **pacs.009 COV**
   if serial vs cover method). UETR tracks it end to end (SWIFT gpi).
4. Correspondent forwards to the US beneficiary bank; domestic leg may go over Fedwire (ISO 20022 since 2025).
5. Problems? **camt.056** cancellation request, **camt.029** resolution, **pacs.004** return.
6. Debtor gets **camt.054** notification intraday, then **camt.053** statement (or **CODA** if the
   accountant's software still expects it). Reconciliation matches on EndToEndId.
7. Separately, a fintech budgeting app reads the same account via a **PSD2** AIS API (Berlin Group).

Diagram this as a sequence diagram. It is the centerpiece figure.

---

## 4. Diagrams to produce

1. **Layer map** (stack diagram): Regulation (PSD2) / Rulebooks (CBPR+, CGI-MP, EPC SEPA, HVPS+) /
   Messages (pain, pacs, camt) / Standard (ISO 20022) / Syntax (MT, MX XML, CODA fixed-width, JSON APIs).
2. **End-to-end payment sequence**: Corporate → Debtor bank → Correspondent → Creditor bank → Creditor,
   with the message name on each arrow.
3. **MT → MX migration timeline**: 2023 coexistence start, T2/CHAPS 2023, Fedwire 2025, end of coexistence
   Nov 2025, structured address Nov 2026.
4. **Message family map**: who talks to whom (C2B = pain, B2B = pacs, B2C reporting = camt).
5. Optional: CODA record structure vs camt.053 element tree side by side.

Tools: `diagram-mermaid` or `diagram-plantuml`, rendered to PNG. Sources kept in the repo.

---

## 5. Code examples (candidate repo content)

Keep examples small, runnable, Java 25 (plus maybe one Python snippet).

- **CODA parser**: records + sealed interface + switch pattern matching on record type.
- **MT103 parse/build** with Prowide Core (open source, Apache 2.0).
- **pacs.008 / camt.053 parse** with Prowide ISO 20022 (open source edition) or JAXB from the XSDs.
- **MT103 → pacs.008 mapping** demo, highlighting lossy fields (party name/address truncation, charset).
- **pain.001 generator** producing a CGI-style file, validated against the XSD.
- Python alternative for the reader: `lxml` + XSD validation; `mt-940` library for MT940 parsing.

Sample data must be synthetic. Fake IBANs that pass checksum (e.g., documented test IBANs), no real names.

---

## 6. Pitfalls and "lessons from the trenches" section

- XSD-valid is not network-valid: CBPR+, SEPA EPC and bank-specific guides add rules on top.
- Character sets: SWIFT X charset in MT vs UTF-8 in ISO 20022 (but CBPR+ restricts it). Accents and
  non-Latin names break on translation.
- Truncation in MX → MT translation: 140-char remittance, 4x35 addresses. Data loss = compliance risk
  (sanctions screening, FATF Recommendation 16 / travel rule).
- Identifiers: MsgId vs InstrId vs EndToEndId vs TxId vs UETR. Explain each once; a table helps.
- Amounts and decimals: currency exponent (JPY 0 decimals), control sums in group headers.
- Dates/times: ISO date vs ISODateTime with offset; cut-off times; value date vs booking date.
- Statement edge cases: reversals, intraday vs end-of-day duplicates, batch booking hiding transactions.
- Versioning: pain.001.001.03 vs .09 still both in the wild; banks support different versions.
- Treating PSD2 as "the new format" instead of an access channel.

---

## 7. Glossary seed (for docs/glossary.md)

AIS, AISP, BAH, BIC, CBPR+, CBPII, CGI-MP, CODA, CAMT, EBICS, EPC, eIDAS, FIN, FINplus, gpi, HVPS+, IBAN,
ISO 15022, ISO 20022, LEI, MT, MX, OGM/VCS, PACS, PAIN, PIS, PISP, PSD2, PSD3, PSR, QWAC, QSealC, SCA,
SCT, SCT Inst, SDD, SEPA, SWIFT, T2, TPP, UETR, VoP, XS2A, XSD.

---

## 8. Sources to check before writing (verify, do not cite from memory)

- ISO 20022 official site: iso20022.org (message catalogue, naming).
- SWIFT: ISO 20022 programme pages, CBPR+ usage guidelines on MyStandards, MT/MX translation rules.
- Febelfin: CODA specification (current version, CAMT migration guidance).
- CGI-MP: implementation guides published via SWIFT.
- European Commission / EUR-Lex: PSD2 (2015/2366), SCA RTS (2018/389), PSD3/PSR status, IPR 2024/886.
- European Payments Council (EPC): SEPA rulebooks, Verification of Payee scheme.
- Berlin Group NextGenPSD2 framework.
- ECB: T2 migration documentation. Federal Reserve: Fedwire ISO 20022 implementation.
- Prowide open source libraries docs (for code samples).

Every date, version number and deadline in this file must be rechecked at writing time. Several items
(CBPR+ address rules, SWIFT MT retirement scope, PSD3/PSR status, CODA version) move fast.

---

## 9. Platform and series ideas

- Fits a cross-platform series around this repo: DZone (practical guide, human-written prose),
  InfoQ (architect angle: migration strategy, canonical data model, translation layer), Dev.to
  (code-first: parse CODA and camt.053 in Java), Medium (narrative "one payment, ten acronyms"),
  LinkedIn (short map + diagram), JavaPro (Java 25 parsing and mapping deep dive).
- Talk version: "Ten Acronyms, One Payment" with the layer map and the end-to-end sequence as key slides.
- Follow-up articles:
  - Designing a canonical payment model to survive MT, MX and local formats.
  - Bank statement reconciliation with camt.053: matching strategies.
  - PSD2 / Berlin Group API integration in Spring Boot.
  - ISO 20022 validation pipelines: XSD + usage guidelines + business rules.

---

## 10. Article checklist

- [ ] Banner image (`article-image-generator`), house style, 0-4 words.
- [ ] At least the layer map and end-to-end sequence diagrams, rendered to PNG.
- [ ] Code snippets identical to repo code.
- [ ] Glossary file covering every acronym.
- [ ] All dates and versions verified against primary sources.
- [ ] `article-review` pass before delivery.
- [ ] No AI-assistance statement; DZone version written in the author's own voice.
