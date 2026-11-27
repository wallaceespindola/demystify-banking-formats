# Banking formats map: series definition

What this repository explains, the story every article follows, what the examples prove, and the facts the
articles may state. Research notes and wider background are in [../context.md](../context.md); when the two
disagree, this file wins.

## Problem

Developers who touch payments meet a pile of acronyms at once: ISO 20022, pain, pacs, camt, MT, MX, CBPR+,
CGI-MP, CODA, PSD2. Most material is vendor marketing or a 200-page usage guideline. Nobody shows how the
pieces fit, so people treat them as competing formats when they are layers of one stack.

## The idea in one paragraph

The acronyms answer different questions. ISO 20022 is the standard. pain, pacs and camt are message families
inside it (customer to bank, bank to bank, bank to customer). MT and MX are syntaxes on the Swift network: the
old text format and ISO 20022 XML. CBPR+ and CGI-MP are usage rules that restrict the same ISO messages for a
given path. CODA (and its cousins CFONB 120, Norma 43, CBI RH, MT940) are national or legacy statement files
that camt.053 replaces. PSD2 is a regulation that opened account APIs, not a message format.

| Layer | Question it answers | Examples |
| --- | --- | --- |
| Standard | Which rulebook defines the data? | ISO 20022 |
| Message family | Who talks to whom about what? | pain (customer to bank), pacs (bank to bank), camt (reporting) |
| Syntax on Swift | What does it look like on the wire? | MT (FIN text), MX (ISO 20022 XML) |
| Usage rules | Which fields, lengths and codes are allowed here? | CBPR+ (cross-border, bank to bank), CGI-MP (corporate to bank), HVPS+ |
| National and legacy files | What does my ERP still import? | CODA (BE), CFONB 120 (FR), MT940 dialects (DE), Norma 43 (ES), CBI RH (IT) |
| Regulation and APIs | Who may access the account, and how? | PSD2, Berlin Group NextGenPSD2, STET |

## The story

One payment and one statement, used in every article, slide and sample:

1. ACME NV in Brussels pays GLOBEX in the US USD 12,500.00. Its ERP sends a **pain.001**
   ([sample](../samples/payment.pain001.xml)) and the bank answers with a **pain.002**.
2. The bank sends a **pacs.008** ([sample](../samples/payment.pacs008.xml)) over Swift under **CBPR+**,
   tracked by its UETR.
3. If something has to be translated back to **MT103**, data is lost: the example translator shows the creditor
   name and the building number cut at 35 characters and the EndToEndId dropped.
4. ACME's account receives 250.00 EUR from DUPONT SA with the Belgian structured reference
   `+++123/4567/89002+++`. The bank reports it as **CODA** ([sample](../samples/statement.coda)),
   **MT940** ([sample](../samples/statement.mt940)) and **camt.053** ([sample](../samples/statement.camt053.xml)).
5. A fintech app reads the same account through a **PSD2** account information API.

BIC codes `BANKBEBB` and `BANKUS33` are fictional. IBAN `BE68539007547034` is the common documentation example.

## What the examples prove

The Java 25 module in [`examples/`](../../examples) (JDK only, JUnit 5 tests):

| Class | Shows |
| --- | --- |
| `Statement` | One canonical model (record) for a statement, whatever the file format |
| `CodaParser` | Fixed-width parsing by position: 128-character records, record type in column 1, switch with patterns |
| `Mt940Parser` | Tag parsing, plus the `:86:` problem: free text that every bank structures differently |
| `Camt053Parser` | ISO 20022 XML with the JDK DOM parser, namespace-version agnostic, XXE disabled |
| `Pacs008ToMt103` | Why ISO 20022 to MT is lossy: 35-character lines, flattened addresses, no EndToEndId |
| `Main` | Runs everything on `docs/samples` and prints `Same statement in all three formats: true` |

`make examples` builds and tests it, `make run` prints the output.

## Facts the articles may state (checked 2026-11-27)

Only these dates and versions go into articles. Anything else needs a source first.

| Fact | Status | Source |
| --- | --- | --- |
| Swift MT/MX coexistence for cross-border payment instructions ended on 22 November 2025 | Confirmed | [Swift: ISO 20022 in bytes, the journey continues](https://www.swift.com/insights/news/iso-20022-bytes-payments-journey-continues) |
| Fedwire Funds moved to ISO 20022 in a single-day cutover on 14 July 2025 | Confirmed | [Federal Reserve Financial Services announcement](https://frbservices.org/news/press-releases/071525-iso20022-migration-announcement) |
| T2 (Eurosystem) migrated in March 2023, CHAPS in June 2023 | Widely documented; cite the operator if stated | none linked |
| CBPR+ was due to reject unstructured postal addresses from 14 November 2026 (town and country in their own fields at minimum, hybrid allows 2 address lines of 70 characters) | Rule confirmed; the date was deferred | [Swift: removal of unstructured address](https://www.swift.com/standards/iso-20022/removal-unstructured-address) |
| In August 2026 Swift accepted a community request to extend the structured address migration; the deferred SR 2026 release moves to 12 June 2027, and the address timing is under consultation (update expected by December 2026) | Confirmed; final address date open | [Swift news](https://www.swift.com/news-events/news/swift-accepts-community-request-extend-structured-address-migration-iso-20022-payment-messages), [Finextra](https://www.finextra.com/newsarticle/48318/swift-accepts-request-to-extend-structured-address-migration-for-iso-20022-messages) |
| camt.052, camt.053 and camt.054 are excluded from the CBPR+ structured address rule | Per Swift FAQ | [Swift FAQ](https://www.swift.com/standards/iso-20022/iso-20022-faqs/iso-20022-removal-unstructured-address) |
| Current CODA specification is version 2.7 (2.6 dated February 2018) | Documents found on Febelfin's site | [Febelfin CODA 2.7](https://febelfin.be/media/pages/publicaties/2023/febelfin-standaarden-voor-online-bankieren/5d601609cd-1754302976/standard-coda-2.7-en.pdf) |
| PSD3 and the Payment Services Regulation (PSR): provisional political agreement on 27 November 2025; Council and ECON approvals in spring 2026; formal adoption and Official Journal publication not confirmed when checked | Say "agreed, not yet in force" | [Norton Rose Fulbright](https://www.nortonrosefulbright.com/en/knowledge/publications/cedd39c6/psd3-and-psr-from-provisional-agreement-to-2026-readiness) |
| PSD2 is Directive (EU) 2015/2366 | Confirmed | [EUR-Lex](https://eur-lex.europa.eu/eli/dir/2015/2366/oj) |
| Verification of Payee is mandatory for euro credit transfers in the eurozone since 9 October 2025 (Instant Payments Regulation (EU) 2024/886) | Confirmed | [EUR-Lex](https://eur-lex.europa.eu/eli/reg/2024/886/oj) |

No adoption percentages, error rates or cost figures: there is no data for them in this repo, and the articles say
so instead of inventing numbers.

## Out of scope

Securities (sese, semt), trade finance (MT7xx), card schemes, and a production-grade CBPR+ translator. The
translator here is a teaching example and says so in its Javadoc.
