# Glossary

Every acronym used in this repository's Markdown files (README, `CLAUDE.md`, `docs/`, the articles and the slides),
sorted alphabetically. When you introduce a new one, add a row here.

| Acronym | Stands for | In this project |
| --- | --- | --- |
| AC04 | ISO 20022 status reason code "closed account number" | The rejection reason in the pain.002 example in `docs/context.md` |
| AEB | Asociación Española de Banca | Publisher of Norma 43 (also written AEB 43), the Spanish statement format |
| AI | Artificial intelligence | Mentioned only for platform rules on AI-written text |
| AIS | Account Information Service | PSD2 role that reads account data; the fintech app in the story |
| AISP | Account Information Service Provider | PSD2 role that reads account data; the fintech app in the story |
| API | Application programming interface | PSD2 account and payment APIs (Berlin Group, STET, UK Open Banking) |
| ASCII | American Standard Code for Information Interchange | Fixed-width samples in Markdown use ASCII `...` and `<-` so columns stay aligned |
| ASN | Abstract Syntax Notation | ASN.1, one of the non-XML syntaxes ISO 20022 allows |
| B2B | Business to business | Direction of a message flow |
| B2C | Business to customer | Direction of camt reporting from bank to customer |
| BAH | Business Application Header (`head.001`) | The envelope in front of every MX message on Swift |
| BAI2 | Bank Administration Institute format, version 2 | US cash reporting file, mentioned for contrast with CODA |
| BBA | Belgian Bankers' Association | Issuer code for the Belgian structured reference in the camt.053 sample |
| BE | Belgium | Country or region code (ISO 3166) in addresses, IBANs and tables |
| BIC | Business Identifier Code (ISO 9362) | Bank identifiers; `BANKBEBB` and `BANKUS33` are fictional |
| BICFI | BIC of a financial institution (ISO 20022 element) | Code or record type that appears in the samples |
| BOOK | Booked entry status | Code or record type that appears in the samples |
| C2B | Customer to business | Direction of pain messages from customer to bank |
| camt | Cash management | ISO 20022 business area for reporting: camt.052, camt.053, camt.054 |
| CAMT | Cash management | Upper-case spelling of camt |
| CBI | Corporate Banking Interbancario | Italian interbank corporate banking standard; RH is its reporting file, RiBa its collection |
| CBI RH | Corporate Banking Interbancario; RH is its reporting (rendicontazione) file | Italian statement format; CBI also defines RiBa collections |
| CBPII | Card-Based Payment Instrument Issuer | PSD2 role |
| CBPR | Cross-Border Payments and Reporting Plus (written CBPR+) | Swift usage guidelines for ISO 20022 between banks; owns the structured address rule |
| CFONB | Comité Français d'Organisation et de Normalisation Bancaires | Publisher of CFONB 120 statements and the LCR collection layout |
| CGI | Common Global Implementation | First half of CGI-MP |
| CGI-MP | Common Global Implementation - Market Practice | Usage guidelines for corporate-to-bank ISO 20022 (pain.001, camt.053) |
| CHAPS | Clearing House Automated Payment System | UK high-value payment system, on ISO 20022 since 2023 |
| CI | Continuous integration | `.github/workflows/ci.yml`: Java build, Markdown lint, link check |
| CLBD | Closing booked balance | Code or record type that appears in the samples |
| CODA | Coded statement of account | Belgian statement file from Febelfin, 128-character records; the main legacy example |
| COV | Cover | MT202COV and pacs.009 COV, the cover leg of a payment |
| CRDT | Credit (CdtDbtInd) | Code or record type that appears in the samples |
| DBIT | Debit (CdtDbtInd) | Code or record type that appears in the samples |
| DDMMYY | Date layout: day, month, year (2 digits each) | CODA, CFONB and CBI dates |
| DE | Germany | Country or region code (ISO 3166) in addresses, IBANs and tables |
| DOCTYPE | Document type declaration | Disallowed in the XML parsers to block XXE |
| DOM | Document Object Model | The JDK XML API `Camt053Parser` and `Pacs008ToMt103` use |
| DTAZV | Datenträgeraustausch Auslandszahlungsverkehr | German file for non-SEPA foreign payments, still in use |
| DTD | Document Type Definition | Disallowed in the XML parsers to block XXE |
| EBICS | Electronic Banking Internet Communication Standard | Channel German, French, Swiss and Austrian corporates use to exchange bank files |
| ECB | European Central Bank | Operator documentation for T2 |
| ECON | European Parliament Committee on Economic and Monetary Affairs | Approved the PSD3 and PSR texts in 2026 |
| EF | End of file record (CBI) | Code or record type that appears in the samples |
| eIDAS | Electronic identification, authentication and trust services (EU regulation) | Source of the QWAC and QSealC certificates PSD2 TPPs use |
| EPC | European Payments Council | Owner of the SEPA rulebooks |
| ERP | Enterprise resource planning | The system that sends pain.001 and imports statements |
| ES | Spain | Country or region code (ISO 3166) in addresses, IBANs and tables |
| EU | European Union | PSD2, PSD3, PSR, Instant Payments Regulation |
| EUR | Euro (ISO 4217) | Currency of the statement samples |
| EURO1 | EBA Clearing's euro high-value payment system | Migrated to ISO 20022 with T2 |
| F01 | Swift basic header application and service identifier | Prefix in `{1:F01...}` of the MT samples |
| FAQ | Frequently asked questions | Swift's address-rule FAQ in the spec's sources |
| FATF | Financial Action Task Force | Recommendation 16 (travel rule) is one reason structured party data matters |
| FI | Financial institution | pacs messages are "FI to FI" |
| FIDA | Financial Data Access (EU regulation proposal) | Mentioned as what may follow PSD2 account access |
| FIN | Swift's store-and-forward MT messaging service | MT travels on FIN |
| FINplus | Swift's ISO 20022 messaging service | MX travels on FINplus |
| FR | France | Country or region code (ISO 3166) in addresses, IBANs and tables |
| FX | Foreign exchange | MT3xx treasury and FX messages |
| GMBH | Gesellschaft mit beschränkter Haftung (German private company) | Legal form in the fictional company names of the samples |
| gpi | Swift global payments innovation | Introduced the UETR tracking reference |
| HTML | Hypertext Markup Language | Deck exports |
| HTTP | Hypertext Transfer Protocol | PSD2 API calls |
| HTTPS | HTTP over TLS (secure) | PSD2 API calls |
| HVPS | High Value Payments Systems Plus (written HVPS+) | Usage guidelines for T2, CHAPS and Fedwire on ISO 20022 |
| IB | Header record of a RiBa file (CBI) | Code or record type that appears in the samples |
| IBAN | International Bank Account Number | `BE68539007547034`, the common documentation example |
| ID | Identifier | EndToEndId, UETR, message IDs |
| IL | Illinois (US state code) | Country or region code (ISO 3166) in addresses, IBANs and tables |
| IPR | Instant Payments Regulation (EU) 2024/886 | Made Verification of Payee mandatory in the eurozone |
| ISO | International Organization for Standardization | ISO 20022 (financial messaging), ISO 15022 (MT), ISO 9362 (BIC), ISO 11649 (RF reference) |
| IT | Italy | Country or region code (ISO 3166) in addresses, IBANs and tables |
| JAXB | Jakarta XML Binding | Code-generation alternative to the DOM parsing used here |
| JDK | Java Development Kit | The examples use only the JDK |
| JEP | JDK Enhancement Proposal | JEP 441 is pattern matching for `switch` |
| JPEG | Joint Photographic Experts Group image format | InfoQ and DZone banners and diagrams |
| JPY | Japanese yen (ISO 4217) | Example of a currency with no decimals |
| JSON | JavaScript Object Notation | PSD2 API payloads |
| KB | Kilobyte | DZone asks for images of 100 to 200 KB |
| LCR | Lettre de change relevé | French bill-of-exchange collection, CFONB 160 layout, still in use |
| LEI | Legal Entity Identifier | Structured party identifier ISO 20022 can carry and MT cannot |
| LLC | Limited liability company (US) | Legal form in the fictional company names of the samples |
| MI | Milano (Italian province code) | Country or region code (ISO 3166) in addresses, IBANs and tables |
| MIT | Massachusetts Institute of Technology (licence) | This repository's licence |
| MP | Market Practice | Second half of CGI-MP |
| MT | Message Type | Swift FIN text messages; MT103 (customer transfer), MT202 (bank transfer), MT940/MT942 (statements) |
| MT101 | Swift message type 101 | Request for transfer (corporate to bank) |
| MT103 | Swift message type 103 | Single customer credit transfer; successor pacs.008 |
| MT192 | Swift message type 192 | Request for cancellation (customer payment); successor camt.056 |
| MT196 | Swift message type 196 | Answers (customer payment); successor camt.029 |
| MT199 | Swift message type 199 | Free format message (customer payments) |
| MT202 | Swift message type 202 | General financial institution transfer; successor pacs.009 |
| MT202COV | Swift message type 202COV | Cover payment; successor pacs.009 COV |
| MT204 | Swift message type 204 | Financial markets direct debit; successor pacs.010 |
| MT210 | Swift message type 210 | Notice to receive; successor camt.057 |
| MT292 | Swift message type 292 | Request for cancellation (FI transfer); successor camt.056 |
| MT296 | Swift message type 296 | Answers (FI transfer); successor camt.029 |
| MT299 | Swift message type 299 | Free format message (FI transfers) |
| MT900 | Swift message type 900 | Confirmation of debit; successor camt.054 |
| MT910 | Swift message type 910 | Confirmation of credit; successor camt.054 |
| MT920 | Swift message type 920 | Request message for statements; successor camt.060 |
| MT940 | Swift message type 940 | Customer statement message; successor camt.053 |
| MT941 | Swift message type 941 | Balance report; successor camt.052 |
| MT942 | Swift message type 942 | Interim transaction report; successor camt.052 |
| MT950 | Swift message type 950 | Statement message (bank to bank); successor camt.053 |
| MX | Swift's name for ISO 20022 XML messages | pacs.008 on Swift is an MX message |
| NV | Naamloze vennootschap (Belgian public company) | Legal form in the fictional company names of the samples |
| OGM | Overschrijving met gestructureerde mededeling | Belgian structured reference `+++123/4567/89002+++` (Dutch name) |
| OPBD | Opening booked balance | Code or record type that appears in the samples |
| OWASP | Open Worldwide Application Security Project | Source of the XXE guidance |
| pacs | Payments clearing and settlement | ISO 20022 bank-to-bank area: pacs.008, pacs.009, pacs.002, pacs.004 |
| PACS | Payments clearing and settlement | Upper-case spelling of pacs |
| pain | Payments initiation | ISO 20022 customer-to-bank area: pain.001, pain.002, pain.008 |
| PAIN | Payments initiation | Upper-case spelling of pain |
| PDF | Portable Document Format | Deck export for Speaker Deck |
| PIS | Payment Initiation Service | PSD2 role that starts a payment |
| PISP | Payment Initiation Service Provider | PSD2 role that starts a payment |
| PNG | Portable Network Graphics | Banners and rendered diagrams |
| PPTX | PowerPoint Open XML presentation | Editable deck for PowerPoint, Keynote and Google Slides |
| PRD | Product requirements document | `docs/spec/PRD-banking-formats-map.md` |
| PSD2 | Second Payment Services Directive | Directive (EU) 2015/2366; opened account access APIs |
| PSD3 | Third Payment Services Directive | Provisionally agreed on 27 November 2025, not yet in force when checked |
| PSR | Payment Services Regulation | Companion regulation to PSD3 |
| QSealC | Qualified electronic Seal Certificate | eIDAS certificates that identify PSD2 TPPs |
| QWAC | Qualified Website Authentication Certificate | eIDAS certificates that identify PSD2 TPPs |
| RCUR | Recurring direct debit (sequence type) | Code or record type that appears in the samples |
| REST | Representational state transfer | Style of the PSD2 APIs |
| RH | Rendicontazione (CBI reporting record set) | Italian statement file, written CBI RH |
| RTS | Regulatory Technical Standards | The PSD2 SCA rules (Regulation (EU) 2018/389) |
| SA | Société anonyme / sociedad anónima (public company) | Legal form in the fictional company names of the samples |
| SCA | Strong Customer Authentication | PSD2 two-factor rule |
| SCOR | ISO 20022 code for a structured creditor reference | Reference type in the camt.053 sample |
| SCT | SEPA Credit Transfer | Euro credit transfer scheme on ISO 20022; SCT Inst is the instant version |
| SCT Inst | SEPA Instant Credit Transfer | Instant version of SCT |
| SDD | SEPA Direct Debit | Euro direct debit scheme on pain.008 |
| SEPA | Single Euro Payments Area | First large ISO 20022 rollout |
| SHA | Shared charges (MT103 field 71A) | Code or record type that appears in the samples |
| SHAR | Shared charges (ChrgBr) | Code or record type that appears in the samples |
| SL | Sociedad limitada (Spanish private company) | Legal form in the fictional company names of the samples |
| SPA | Società per azioni (Italian public company) | Legal form in the fictional company names of the samples |
| SR | Standards Release | Swift's yearly standards release; SR 2026 was deferred to June 2027 |
| SRL | Società a responsabilità limitata (Italian private company) | Legal form in the fictional company names of the samples |
| STET | Systèmes Technologiques d'Échange et de Traitement | French PSD2 API standard |
| SWIFT | Society for Worldwide Interbank Financial Telecommunication | The network that carries MT and MX; publishes CBPR+. Written Swift in prose |
| Swift | Society for Worldwide Interbank Financial Telecommunication | The network that carries MT and MX; publishes CBPR+ |
| T2 | Eurosystem real-time gross settlement system | Migrated to ISO 20022 in March 2023 |
| TARGET2 | Previous name of T2 | Trans-European Automated Real-time Gross settlement Express Transfer system |
| TC68 | ISO Technical Committee 68, financial services | Committee responsible for ISO 20022 |
| TL;DR | Too long; didn't read | Summary line in DZone metadata |
| TPP | Third-party provider | PSD2 AISP or PISP |
| UETR | Unique end-to-end transaction reference | The gpi UUID in pacs.008 and in MT103 field 121 |
| UK | United Kingdom | CHAPS and UK Open Banking |
| UML | Unified Modeling Language | The PlantUML class diagram in the JavaPro article |
| US | United States | Fedwire and BAI2 |
| USD | US dollar (ISO 4217) | Currency of the ACME to GLOBEX payment |
| UTF | Unicode Transformation Format | UTF-8 is the ISO 20022 XML encoding; MT uses the narrower Swift X character set |
| UUID | Universally unique identifier | The format of the UETR |
| VCS | Virement avec communication structurée | Same Belgian structured reference (French name) |
| VoP | Verification of Payee | Name check before euro credit transfers, mandatory since 9 October 2025 |
| XML | Extensible Markup Language | ISO 20022's main syntax |
| XS2A | Access to account | The PSD2 obligation to open account APIs |
| XSD | XML Schema Definition | The base schema; CBPR+ and CGI-MP add rules on top |
| XXE | XML external entity | Attack the parsers block with `disallow-doctype-decl` |
| YYMMDD | Date layout: year, month, day (2 digits each) | MT940 and Norma 43 dates |
| YYYYMMDD | Date layout: 4-digit year, month, day | Norma 19.14 dates |

Words that the review script may flag but that are not acronyms: ACME, DUPONT and GLOBEX (fictional companies in
the samples), CORP, RiBa (ricevuta bancaria), CLAUDE and README (file names), and all-caps text inside the
fixed-width and MT samples (street and town names, IBANs, BICs such as `BANKUS33`, references such as
`STARTUMS`).
