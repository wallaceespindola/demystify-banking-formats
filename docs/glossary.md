# Glossary

Every acronym used in this repository's Markdown files (README, `CLAUDE.md`, `docs/`, the articles and the slides),
sorted alphabetically. When you introduce a new one, add a row here.

| Acronym | Stands for | In this project |
| --- | --- | --- |
| AC04 | ISO 20022 status reason code "closed account number" | The rejection reason in the pain.002 example in `docs/context.md` |
| AEB | Asociación Española de Banca | Publisher of Norma 43 (also written AEB 43), the Spanish statement format |
| AI | Artificial intelligence | Mentioned only for platform rules on AI-written text |
| AIS, AISP | Account Information Service, Account Information Service Provider | PSD2 role that reads account data; the fintech app in the story |
| API | Application programming interface | PSD2 account and payment APIs (Berlin Group, STET, UK Open Banking) |
| ASCII | American Standard Code for Information Interchange | Fixed-width samples in Markdown use ASCII `...` and `<-` so columns stay aligned |
| ASN.1 | Abstract Syntax Notation One | One of the non-XML syntaxes ISO 20022 allows |
| B2B, B2C, C2B | Business to business, business to customer, customer to business | Directions of message flows: pain is C2B, camt reporting is B2C |
| BAH | Business Application Header (`head.001`) | The envelope in front of every MX message on Swift |
| BAI2 | Bank Administration Institute format, version 2 | US cash reporting file, mentioned for contrast with CODA |
| BBA | Belgian Bankers' Association | Issuer code for the Belgian structured reference in the camt.053 sample |
| BIC | Business Identifier Code (ISO 9362) | Bank identifiers; `BANKBEBB` and `BANKUS33` are fictional |
| camt, CAMT | Cash management | ISO 20022 business area for reporting: camt.052, camt.053, camt.054 |
| CBI, CBI RH | Corporate Banking Interbancario; RH is its reporting (rendicontazione) file | Italian statement format; CBI also defines RiBa collections |
| CBPII | Card-Based Payment Instrument Issuer | PSD2 role |
| CBPR+ | Cross-Border Payments and Reporting Plus | Swift usage guidelines for ISO 20022 between banks; owns the structured address rule |
| CFONB | Comité Français d'Organisation et de Normalisation Bancaires | Publisher of CFONB 120 statements and the LCR collection layout |
| CGI-MP | Common Global Implementation - Market Practice | Usage guidelines for corporate-to-bank ISO 20022 (pain.001, camt.053) |
| CHAPS | Clearing House Automated Payment System | UK high-value payment system, on ISO 20022 since 2023 |
| CI | Continuous integration | `.github/workflows/ci.yml`: Java build, Markdown lint, link check |
| CODA | Coded statement of account | Belgian statement file from Febelfin, 128-character records; the main legacy example |
| COV | Cover | MT202COV and pacs.009 COV, the cover leg of a payment |
| DDMMYY, YYMMDD, YYYYMMDD | Date layouts: day-month-year, year-month-day | CODA, CFONB and CBI use DDMMYY; MT940 and Norma 43 use YYMMDD; Norma 19.14 uses YYYYMMDD |
| DOM | Document Object Model | The JDK XML API `Camt053Parser` and `Pacs008ToMt103` use |
| DOCTYPE, DTD | Document type declaration, Document Type Definition | Disallowed in the XML parsers to block XXE |
| DTAZV | Datenträgeraustausch Auslandszahlungsverkehr | German file for non-SEPA foreign payments, still in use |
| EBICS | Electronic Banking Internet Communication Standard | Channel German, French, Swiss and Austrian corporates use to exchange bank files |
| ECB | European Central Bank | Operator documentation for T2 |
| ECON | European Parliament Committee on Economic and Monetary Affairs | Approved the PSD3 and PSR texts in 2026 |
| eIDAS | Electronic identification, authentication and trust services (EU regulation) | Source of the QWAC and QSealC certificates PSD2 TPPs use |
| EPC | European Payments Council | Owner of the SEPA rulebooks |
| ERP | Enterprise resource planning | The system that sends pain.001 and imports statements |
| EU | European Union | PSD2, PSD3, PSR, Instant Payments Regulation |
| EUR, USD, JPY | ISO 4217 currency codes | Euro, US dollar, Japanese yen |
| EURO1 | EBA Clearing's euro high-value payment system | Migrated to ISO 20022 with T2 |
| F01 | Swift basic header application and service identifier | Prefix in `{1:F01...}` of the MT samples |
| FAQ | Frequently asked questions | Swift's address-rule FAQ in the spec's sources |
| FATF | Financial Action Task Force | Recommendation 16 (travel rule) is one reason structured party data matters |
| FI | Financial institution | pacs messages are "FI to FI" |
| FIDA | Financial Data Access (EU regulation proposal) | Mentioned as what may follow PSD2 account access |
| FIN, FINplus | Swift's MT messaging service; Swift's ISO 20022 messaging service | MT travels on FIN, MX on FINplus |
| FX | Foreign exchange | MT3xx treasury and FX messages |
| gpi | Swift global payments innovation | Introduced the UETR tracking reference |
| HTML, HTTP, HTTPS | Hypertext Markup Language, Hypertext Transfer Protocol (Secure) | Deck exports and PSD2 API calls |
| HVPS+ | High Value Payments Systems Plus | Usage guidelines for T2, CHAPS and Fedwire on ISO 20022 |
| IBAN | International Bank Account Number | `BE68539007547034`, the common documentation example |
| ID | Identifier | EndToEndId, UETR, message IDs |
| IPR | Instant Payments Regulation (EU) 2024/886 | Made Verification of Payee mandatory in the eurozone |
| ISO | International Organization for Standardization | ISO 20022 (financial messaging), ISO 15022 (MT), ISO 9362 (BIC), ISO 11649 (RF reference) |
| JAXB | Jakarta XML Binding | Code-generation alternative to the DOM parsing used here |
| JDK, JEP | Java Development Kit, JDK Enhancement Proposal | The examples use only the JDK; JEP 441 is pattern matching for `switch` |
| JPEG, PNG, PDF, PPTX | Image, document and slide file formats | Banners, diagrams and deck exports |
| JSON | JavaScript Object Notation | PSD2 API payloads |
| KB | Kilobyte | DZone asks for images of 100 to 200 KB |
| LCR | Lettre de change relevé | French bill-of-exchange collection, CFONB 160 layout, still in use |
| LEI | Legal Entity Identifier | Structured party identifier ISO 20022 can carry and MT cannot |
| MIT | Massachusetts Institute of Technology (licence) | This repository's licence |
| MT | Message Type | Swift FIN text messages; MT103 (customer transfer), MT202 (bank transfer), MT940/MT942 (statements) |
| MX | Swift's name for ISO 20022 XML messages | pacs.008 on Swift is an MX message |
| OGM, VCS | Overschrijving met gestructureerde mededeling, virement avec communication structurée | Belgian structured reference `+++123/4567/89002+++` |
| OWASP | Open Worldwide Application Security Project | Source of the XXE guidance |
| pacs, PACS | Payments clearing and settlement | ISO 20022 bank-to-bank area: pacs.008, pacs.009, pacs.002, pacs.004 |
| pain, PAIN | Payments initiation | ISO 20022 customer-to-bank area: pain.001, pain.002, pain.008 |
| PIS, PISP | Payment Initiation Service, Payment Initiation Service Provider | PSD2 role that starts a payment |
| PRD | Product requirements document | `docs/spec/PRD-banking-formats-map.md` |
| PSD2, PSD3 | Second and third Payment Services Directive | PSD2 is Directive (EU) 2015/2366; PSD3 was agreed in 2025, not yet in force when checked |
| PSR | Payment Services Regulation | Companion regulation to PSD3 |
| QWAC, QSealC | Qualified Website Authentication Certificate, Qualified electronic Seal Certificate | eIDAS certificates that identify PSD2 TPPs |
| REST | Representational state transfer | Style of the PSD2 APIs |
| RTS | Regulatory Technical Standards | The PSD2 SCA rules (Regulation (EU) 2018/389) |
| SCA | Strong Customer Authentication | PSD2 two-factor rule |
| SCOR | ISO 20022 code for a structured creditor reference | Reference type in the camt.053 sample |
| SCT, SCT Inst, SDD | SEPA Credit Transfer, SEPA Instant Credit Transfer, SEPA Direct Debit | The euro schemes built on ISO 20022 |
| SEPA | Single Euro Payments Area | First large ISO 20022 rollout |
| SR | Standards Release | Swift's yearly standards release; SR 2026 was deferred to June 2027 |
| STET | Systèmes Technologiques d'Échange et de Traitement | French PSD2 API standard |
| SWIFT, Swift | Society for Worldwide Interbank Financial Telecommunication | The network that carries MT and MX; publishes CBPR+ |
| T2, TARGET2 | Eurosystem real-time gross settlement system (TARGET2 was its previous name) | Migrated to ISO 20022 in March 2023 |
| TC68 | ISO Technical Committee 68, financial services | Committee responsible for ISO 20022 |
| TL;DR | Too long; didn't read | Summary line in DZone metadata |
| TPP | Third-party provider | PSD2 AISP or PISP |
| UETR | Unique end-to-end transaction reference | The gpi UUID in pacs.008 and in MT103 field 121 |
| UK, US | United Kingdom, United States | CHAPS and UK Open Banking; Fedwire and BAI2 |
| UML | Unified Modeling Language | The PlantUML class diagram in the JavaPro article |
| UTF-8 | Unicode Transformation Format, 8-bit | ISO 20022 XML encoding; MT uses the narrower Swift X character set |
| UUID | Universally unique identifier | The format of the UETR |
| VoP | Verification of Payee | Name check before euro credit transfers, mandatory since 9 October 2025 |
| XML | Extensible Markup Language | ISO 20022's main syntax |
| XS2A | Access to account | The PSD2 obligation to open account APIs |
| XSD | XML Schema Definition | The base schema; CBPR+ and CGI-MP add rules on top |
| XXE | XML external entity | Attack the parsers block with `disallow-doctype-decl` |

Names that look like acronyms but are not: ACME NV, DUPONT SA and GLOBEX (fictional companies in the samples), NV, SA,
SL, SPA, SRL and LLC (company legal forms), BE, DE, ES, FR and IT (country codes), RiBa (ricevuta bancaria), and
CLAUDE and README (file names).
