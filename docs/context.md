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

| Layer                             | What it answers                                           | Items from the list                                |
| --------------------------------- | --------------------------------------------------------- | -------------------------------------------------- |
| Standard / methodology            | How are financial messages modeled and named?             | ISO 20022                                          |
| Message families (business areas) | What kind of message is it?                               | PAIN, PACS, CAMT                                   |
| Network syntax                    | What does it look like on the wire?                       | MT (legacy SWIFT FIN), MX (ISO 20022 XML on SWIFT) |
| Usage rulebooks                   | Which subset and rules apply on this network/corridor?    | CBPR+, CGI-MP (also SEPA EPC, HVPS+)               |
| Local / national formats          | What does my bank actually send me here?                  | CODA (Belgium)                                     |
| Regulation                        | What must banks legally expose and how is access secured? | PSD2                                               |

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

> Small examples below follow the section 3 story: ACME NV (Brussels) pays GLOBEX CORP (US) USD 12,500.00,
> EndToEndId `INV-2026-0042`, UETR `eb6305c9-1f7f-49de-aed0-16487c27b42d`. BICs `BANKBEBB`/`BANKUS33` are
> fictional. Samples are trimmed to show structure; validate against the XSD / usage guideline before reuse.

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

| Message             | Purpose                                                                         |
| ------------------- | ------------------------------------------------------------------------------- |
| pain.001            | Customer Credit Transfer Initiation (the "payment file", batch of transfers)    |
| pain.002            | Customer Payment Status Report (accepted, rejected, pending, with reason codes) |
| pain.007            | Customer Payment Reversal                                                       |
| pain.008            | Customer Direct Debit Initiation                                                |
| pain.009-012        | Mandate initiation, amendment, cancellation, acceptance                         |
| pain.013 / pain.014 | Creditor Payment Activation Request / Status (Request to Pay)                   |

Key structure of pain.001: `GrpHdr` (group header, message id, control sum, number of transactions) →
`PmtInf` (one debtor account, execution date, batch booking) → `CdtTrfTxInf` (each transfer).
Explain the three levels; it is the first thing developers get wrong.

Example pain.001 (three levels visible):

```xml
<CstmrCdtTrfInitn>
  <GrpHdr>                                          <!-- level 1: the file -->
    <MsgId>ACME-20261124-01</MsgId><CreDtTm>2026-11-24T09:00:00</CreDtTm>
    <NbOfTxs>1</NbOfTxs><CtrlSum>12500.00</CtrlSum><InitgPty><Nm>ACME NV</Nm></InitgPty>
  </GrpHdr>
  <PmtInf>                                          <!-- level 2: one debtor account + date -->
    <PmtInfId>BATCH-01</PmtInfId><PmtMtd>TRF</PmtMtd><ReqdExctnDt><Dt>2026-11-24</Dt></ReqdExctnDt>
    <Dbtr><Nm>ACME NV</Nm></Dbtr>
    <DbtrAcct><Id><IBAN>BE68539007547034</IBAN></Id></DbtrAcct>
    <DbtrAgt><FinInstnId><BICFI>BANKBEBB</BICFI></FinInstnId></DbtrAgt>
    <CdtTrfTxInf>                                   <!-- level 3: each transfer -->
      <PmtId><EndToEndId>INV-2026-0042</EndToEndId></PmtId>
      <Amt><InstdAmt Ccy="USD">12500.00</InstdAmt></Amt>
      <CdtrAgt><FinInstnId><BICFI>BANKUS33</BICFI></FinInstnId></CdtrAgt>
      <Cdtr><Nm>GLOBEX CORP</Nm></Cdtr>
      <CdtrAcct><Id><Othr><Id>123456789</Id></Othr></Id></CdtrAcct>
      <RmtInf><Ustrd>INV-2026-0042</Ustrd></RmtInf>
    </CdtTrfTxInf>
  </PmtInf>
</CstmrCdtTrfInitn>
```

Example pain.002 rejection (AC04 = closed account):

```xml
<TxInfAndSts>
  <OrgnlEndToEndId>INV-2026-0042</OrgnlEndToEndId>
  <TxSts>RJCT</TxSts>
  <StsRsnInf><Rsn><Cd>AC04</Cd></Rsn></StsRsnInf>
</TxInfAndSts>
```

### 2.3 PACS (Payments Clearing and Settlement)

Bank-to-bank (interbank). What travels between banks, through clearing systems or correspondents.

| Message  | Purpose                                                   | MT ancestor                      |
| -------- | --------------------------------------------------------- | -------------------------------- |
| pacs.008 | FI to FI Customer Credit Transfer                         | MT103                            |
| pacs.009 | Financial Institution Credit Transfer (incl. COV variant) | MT202 / MT202COV                 |
| pacs.002 | FI to FI Payment Status Report                            | (MT199 free text, partly)        |
| pacs.004 | Payment Return                                            | MT103/MT202 return by convention |
| pacs.003 | FI to FI Customer Direct Debit                            | (none common)                    |
| pacs.010 | Financial Institution Direct Debit                        | MT204                            |
| pacs.028 | FI to FI Payment Status Request                           | MT199/MT299 queries              |

Example pacs.008 transaction (same payment as the MT103 in 2.5; comments show the MT field it replaces):

```xml
<CdtTrfTxInf>
  <PmtId>
    <InstrId>PAY-2026-0042</InstrId>                                  <!-- :20: -->
    <EndToEndId>INV-2026-0042</EndToEndId>                            <!-- no MT home -->
    <UETR>eb6305c9-1f7f-49de-aed0-16487c27b42d</UETR>                 <!-- {3:{121:}} -->
  </PmtId>
  <IntrBkSttlmAmt Ccy="USD">12500.00</IntrBkSttlmAmt>                 <!-- :32A: amount -->
  <IntrBkSttlmDt>2026-11-24</IntrBkSttlmDt>                           <!-- :32A: date -->
  <ChrgBr>SHAR</ChrgBr>                                               <!-- :71A:SHA -->
  <Dbtr><Nm>ACME NV</Nm>                                              <!-- :50K: -->
    <PstlAdr><StrtNm>Rue de la Loi</StrtNm><BldgNb>1</BldgNb><PstCd>1000</PstCd>
      <TwnNm>Bruxelles</TwnNm><Ctry>BE</Ctry></PstlAdr></Dbtr>
  <DbtrAcct><Id><IBAN>BE68539007547034</IBAN></Id></DbtrAcct>
  <DbtrAgt><FinInstnId><BICFI>BANKBEBB</BICFI></FinInstnId></DbtrAgt>
  <CdtrAgt><FinInstnId><BICFI>BANKUS33</BICFI></FinInstnId></CdtrAgt>
  <Cdtr><Nm>GLOBEX CORP</Nm>                                          <!-- :59: -->
    <PstlAdr><TwnNm>Springfield</TwnNm><CtrySubDvsn>IL</CtrySubDvsn><Ctry>US</Ctry></PstlAdr></Cdtr>
  <CdtrAcct><Id><Othr><Id>123456789</Id></Othr></Id></CdtrAcct>
  <RmtInf><Ustrd>INV-2026-0042</Ustrd></RmtInf>                       <!-- :70: -->
</CdtTrfTxInf>
```

### 2.4 CAMT (Cash Management)

Account reporting, notifications, exceptions and investigations.

| Message  | Purpose                                    | MT ancestor   |
| -------- | ------------------------------------------ | ------------- |
| camt.052 | Bank to Customer Account Report (intraday) | MT942         |
| camt.053 | Bank to Customer Statement (end of day)    | MT940 / MT950 |
| camt.054 | Bank to Customer Debit/Credit Notification | MT900 / MT910 |
| camt.056 | FI to FI Payment Cancellation Request      | MT192 / MT292 |
| camt.029 | Resolution of Investigation                | MT196 / MT296 |
| camt.060 | Account Reporting Request                  | MT920         |
| camt.057 | Notification to Receive                    | MT210         |

camt.053 is the bridge to CODA (see 2.8): it is the ISO 20022 statement replacing local formats.
Point out the reconciliation gain: structured remittance info and end-to-end IDs survive the whole chain.

Example camt.054 debit notification to ACME (camt.053 example is in 2.8). The EndToEndId from the
pain.001 comes back, which is what makes auto-matching work:

```xml
<Ntfctn>
  <Acct><Id><IBAN>BE68539007547034</IBAN></Id></Acct>
  <Ntry>
    <Amt Ccy="USD">12500.00</Amt><CdtDbtInd>DBIT</CdtDbtInd><Sts><Cd>BOOK</Cd></Sts>
    <BookgDt><Dt>2026-11-24</Dt></BookgDt>
    <NtryDtls><TxDtls>
      <Refs><EndToEndId>INV-2026-0042</EndToEndId><UETR>eb6305c9-1f7f-49de-aed0-16487c27b42d</UETR></Refs>
    </TxDtls></NtryDtls>
  </Ntry>
</Ntfctn>
```

### 2.5 MT (SWIFT FIN, legacy)

- SWIFT's proprietary message format since the 1970s, ISO 15022 based. Text, tag-based, fixed character set
  (SWIFT X charset), field length limits (35 chars x 4 lines for party info).
- Structure: blocks `{1:}` basic header, `{2:}` application header, `{3:}` user header (UETR lives in
  field 121 here since gpi), `{4:}` text block with fields like `:20:`, `:32A:`, `:50K:`, `:59:`, `:71A:`,
  `{5:}` trailer.
- Categories: MT1xx customer payments, MT2xx FI transfers, MT3xx treasury/FX, MT5xx securities,
  MT7xx trade finance, MT9xx cash management/statements.
- Famous ones: MT101, MT103, MT202, MT202COV, MT940, MT942, MT950, MT199.
- MT94x family (customer cash reporting, the ones corporates actually parse):

  | Message | Name                       | Purpose                                                               | ISO 20022 successor |
  | ------- | -------------------------- | --------------------------------------------------------------------- | ------------------- |
  | MT940   | Customer Statement Message | End-of-day statement: opening/closing balance plus every booked entry | camt.053            |
  | MT941   | Balance Report             | Balances only, no entry detail                                        | camt.052            |
  | MT942   | Interim Transaction Report | Intraday entries (booked and optionally pending) since last report    | camt.052            |

  - Sibling worth naming: MT950 Statement Message (FI-to-FI nostro statement, no `:86:` narrative) → camt.053.
  - Key MT940 fields: `:20:` reference, `:25:` account, `:28C:` statement/sequence number, `:60F:`/`:60M:`
    opening balance, `:61:` statement line (value date, D/C mark, amount, type code, references), `:86:`
    information to account owner (free text, 6x65, where every bank invents its own sub-structure), `:62F:`/
    `:62M:` closing booked balance, `:64:` closing available balance, `:65:` forward available balance.
  - `:86:` is the pitfall: bank-specific dialects (e.g. German `?20`..`?63` subfields) make "MT940 support"
    mean one parser per bank. Strong argument for camt.053's structured remittance data.
  - Example MT942 (intraday, note `:34F:` floor limit, `:13D:` report time, `:90C:` credit count/sum):

    ```text
    :20:INTRA-0001
    :25:BE68539007547034
    :28C:00001/001
    :34F:EUR0,
    :13D:2611241030+0100
    :61:2611241124CR250,00NTRFNONREF
    :86:DUPONT SA +++123/4567/89002+++
    :90C:1EUR250,00
    ```

  - Example MT941 (balances only, no `:61:` lines):

    ```text
    :20:BAL-0001
    :25:BE68539007547034
    :28:00001/001
    :13D:2611241700+0100
    :60F:C261123EUR1000,00
    :62F:C261124EUR1250,00
    :64:C261124EUR1250,00
    ```

  - MT940 example is in 2.8 (German flavour).

- Show a short annotated MT103 snippet vs the equivalent pacs.008 fragment. Best visual in the article.
  Example MT103 (pacs.008 twin in 2.3). Note the 4x35 free-text party fields that CBPR+ addresses replace:

  ```text
  {1:F01BANKBEBBAXXX0000000000}{2:I103BANKUS33XXXXN}{3:{121:eb6305c9-1f7f-49de-aed0-16487c27b42d}}{4:
  :20:PAY-2026-0042
  :23B:CRED
  :32A:261124USD12500,00
  :50K:/BE68539007547034
  ACME NV
  RUE DE LA LOI 1
  1000 BRUXELLES BE
  :59:/123456789
  GLOBEX CORP
  SPRINGFIELD IL US
  :70:INV-2026-0042
  :71A:SHA
  -}
  ```

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
- Example MX envelope on SWIFT: BAH (`head.001`) + Document. `MsgDefIdr` says which message is inside,
  `BizSvc` says which rulebook applies (verify current CBPR+ versions):

  ```xml
  <AppHdr xmlns="urn:iso:std:iso:20022:tech:xsd:head.001.001.02">
    <Fr><FIId><FinInstnId><BICFI>BANKBEBBXXX</BICFI></FinInstnId></FIId></Fr>
    <To><FIId><FinInstnId><BICFI>BANKUS33XXX</BICFI></FinInstnId></FIId></To>
    <BizMsgIdr>PAY-2026-0042</BizMsgIdr>
    <MsgDefIdr>pacs.008.001.08</MsgDefIdr>
    <BizSvc>swift.cbprplus.02</BizSvc>
    <CreDt>2026-11-24T09:05:00Z</CreDt>
  </AppHdr>
  <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf><GrpHdr>…</GrpHdr><CdtTrfTxInf>…see 2.3…</CdtTrfTxInf></FIToFICstmrCdtTrf>
  </Document>
  ```

### 2.7 CBPR+ (Cross-Border Payments and Reporting Plus)

- SWIFT working group and **usage guidelines** defining how ISO 20022 messages are used for cross-border and
  correspondent banking on SWIFT.
- Restricts the base ISO message: which fields are mandatory, cardinalities, character sets, code lists.
  Published on SWIFT MyStandards. Validation against plain ISO XSD is not enough; you need the CBPR+ usage
  guideline (or a library implementing it).
- Coexistence: started March 2023, ended November 2025 for payment instructions (MT103/MT202 family).
- Structured / hybrid postal address requirements: unstructured addresses were due to be rejected from
  14 November 2026. In August 2026 Swift accepted a request to extend that migration; the deferred SR 2026
  release moves to 12 June 2027 and the address date is under consultation (checked 2026-11-27, see
  `docs/spec/PRD-banking-formats-map.md`). Hook for the article: the deadline moved, the work did not.
- Example, the same debtor address three ways (verify current CBPR+ rules):

  ```xml
  <!-- unstructured: being phased out -->
  <PstlAdr><AdrLine>RUE DE LA LOI 1</AdrLine><AdrLine>1000 BRUXELLES BE</AdrLine></PstlAdr>
  <!-- hybrid: town + country structured, max 2 address lines -->
  <PstlAdr><TwnNm>Bruxelles</TwnNm><Ctry>BE</Ctry><AdrLine>Rue de la Loi 1</AdrLine></PstlAdr>
  <!-- fully structured: the target -->
  <PstlAdr><StrtNm>Rue de la Loi</StrtNm><BldgNb>1</BldgNb><PstCd>1000</PstCd>
    <TwnNm>Bruxelles</TwnNm><Ctry>BE</Ctry></PstlAdr>
  ```

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
- Structured communication (Belgian "+++123/4567/89002+++" OGM/VCS reference) parsed for auto-matching.
- Current version 2.7 (2.6 dated February 2018; see the spec file for the source). Febelfin pushes CAMT.053 as the successor; many banks deliver both.
- Why include it: a concrete example of **national legacy formats** (others: MT940 dialects in the
  Netherlands and Germany, Norma 43 (AEB 43) in Spain, CFONB 120 in France, BAI2 in the US). The pattern is the same:
  fixed-width or tag-based local format → camt.053.
- Same role in other big European markets (bank → ERP statement import), all moving to camt.053:

  | Country | Format                                                | Shape                       |
  | ------- | ----------------------------------------------------- | --------------------------- |
  | Belgium | CODA                                                  | Fixed-width, 128 chars/line |
  | France  | CFONB 120                                             | Fixed-width, 120 chars/line |
  | Germany | MT940/MT942 with `?xx` subfields in `:86:`, via EBICS | SWIFT MT tags               |
  | Spain   | Norma 43 (AEB 43)                                     | Fixed-width, 80 chars/line  |
  | Italy   | CBI RH                                                | Fixed-width, 120 chars/line |

  - Verify line lengths and current bank support before publishing.

  - Small examples, same story in each (opening balance 1,000.00 EUR, one 250.00 credit, closing balance 1,250.00).
    These only show the record structure: lines are shortened with `...` and the column positions are not exact.
    Build the article/repo samples from the official specs. Date formats differ: CODA, CFONB and CBI use DDMMYY,
    MT940 and Norma 43 use YYMMDD (all samples: 23 Nov 2026 opening, 24 Nov 2026 booking).

    **Family 1: fixed-width national files** (one record per line, record code at the start, positions
    defined by the spec): CODA, CFONB 120, Norma 43, CBI RH.

    Belgium, CODA (record type = first digit):

    ```text
    0000024112600005        ...ACME NV                   BBRUBEBB   ...  <- header
    1 ...BE68539007547034     EUR0000000001000000231126...               <- old balance
    21...0000000000250000241126...+++123/4567/89002+++...                <- movement + OGM/VCS
    8 ...BE68539007547034     EUR0000000001250000241126...               <- new balance
    9 ...000004000000000000000000000000250000...                         <- trailer
    ```

    France, CFONB 120 (record code `01`/`04`/`07`; the sign is overpunched in the last digit, `{` = +0):

    ```text
    0130004...EUR2...00012345678...231126...ACME SA...0000000010000{              <- old balance
    0430004...EUR2...00012345678...241126...VIR SEPA RECU DUPONT...000000002500{  <- movement
    0730004...EUR2...00012345678...241126...0000000012500{                        <- new balance
    ```

    Spain, Norma 43 (record codes `11`/`22`/`23`/`33`/`88`, 80 chars):

    ```text
    11 2100 0418 4502000513 261123 261124 2 00000000100000 978 3 ACME SL  <- account header
    22 ...261124 261124 02 ...2 00000000025000 ...                        <- movement (2 = credit)
    23 01 TRANSFERENCIA DUPONT SA INV-2026-001                            <- optional concept
    33 2100 0418 4502000513 ... 00000000125000 978                        <- account totals
    88 999999999999999999 000004                                          <- file end
    ```

    Italy, CBI RH (fixed-width, record type in columns 2–3):

    ```text
     RH...ACME SPA...241126...                          <- file header
     61...IT60X0542811101000000123456...EUR...+1000,00  <- opening balance
     62...241126...C...250,00...BONIFICO DUPONT...      <- movement
     63...INV-2026-001...                               <- movement detail
     64...EUR...+1250,00                                <- closing balance
     EF...                                              <- file end
    ```

    **Family 2: SWIFT MT tag-based** (`:tag:` fields, bank-specific `:86:`): MT940/MT942, German flavour shown.

    Germany, MT940 with structured `:86:` (`?00` posting text, `?20`–`?29` purpose, `?32` counterparty):

    ```text
    :20:STARTUMS
    :25:10020030/1234567890
    :28C:00001/001
    :60F:C261123EUR1000,00
    :61:2611241124CR250,00NTRFNONREF
    :86:166?00SEPA-GUTSCHRIFT?20EREF+INV-2026-001?21SVWZ+Rechnung 001?32DUPONT SA
    :62F:C261124EUR1250,00
    ```

    **Family 3: ISO 20022 XML** (the target all of the above migrate to): camt.053, trimmed.

    ```xml
    <Stmt>
      <Acct><Id><IBAN>BE68539007547034</IBAN></Id></Acct>
      <Bal><Tp><CdOrPrtry><Cd>OPBD</Cd></CdOrPrtry></Tp><Amt Ccy="EUR">1000.00</Amt><CdtDbtInd>CRDT</CdtDbtInd>…</Bal>
      <Bal><Tp><CdOrPrtry><Cd>CLBD</Cd></CdOrPrtry></Tp><Amt Ccy="EUR">1250.00</Amt><CdtDbtInd>CRDT</CdtDbtInd>…</Bal>
      <Ntry>
        <Amt Ccy="EUR">250.00</Amt><CdtDbtInd>CRDT</CdtDbtInd><BookgDt><Dt>2026-11-24</Dt></BookgDt>
        <NtryDtls><TxDtls>
          <RltdPties><Dbtr><Pty><Nm>DUPONT SA</Nm></Pty></Dbtr></RltdPties>
          <RmtInf><Strd><CdtrRefInf><Ref>RF18539007547034</Ref></CdtrRefInf></Strd></RmtInf>
        </TxDtls></NtryDtls>
      </Ntry>
    </Stmt>
    ```

- National payment/collection files still in use next to the statement formats (the rest were replaced by
  SEPA pain.001/pain.008). They survive because they cover cases SEPA does not:

  | Country | Still-used format  | Purpose                                                     |
  | ------- | ------------------ | ----------------------------------------------------------- |
  | France  | LCR (CFONB 160)    | Bill-of-exchange style collection with a due date           |
  | Germany | DTAZV              | Non-SEPA / non-EUR foreign payments                         |
  | Spain   | Norma 19.14 (flat) | SEPA direct debit in the Spanish flat-file layout           |
  | Italy   | RiBa (CBI)         | Bank receipt collection (ricevuta bancaria) with a due date |

  Belgium has no live equivalent: payments and debits go out as pain.001/pain.008 (see 2.2).

  Small examples, same style as above (shortened with `...`, positions not exact, verify against specs).
  Story: ACME collects 250.00 EUR from DUPONT, or pays GLOBEX 12,500.00 USD.

  France, LCR (CFONB 160 layout, operation code `60`, DDMMYY dates):

  ```text
  0360...ACME SA...241126...EUR...                                               <- header (03 = remittance, 60 = LCR)
  0660...DUPONT SA...30004...00012345678...000000025000...301226...INV-2026-001  <- one bill: amount, due date
  0860...000000025000...                                                         <- total
  ```

  Germany, DTAZV (record length + type: Q header 256, T payment 768, Z trailer 256):

  ```text
  0256Q...10020030...ACME GMBH...261124...                                     <- header
  0768T...USD...000000001250000...GLOBEX CORP...US...BANKUS33...INV-2026-0042  <- payment
  0256Z...000000001250000...0001...                                            <- trailer: sum, count
  ```

  Spain, Norma 19.14 flat file (record code + `19143` = core SDD, YYYYMMDD dates):

  ```text
  0119143001ES12000B12345678...ACME SL...20261124...                                                 <- presenter header
  0219143002ES12000B12345678...20261128...ACME SL...                                                 <- creditor header + collection date
  0319143003INV-2026-001...MANDATE-0001...RCUR...00000025000...ES9121000418450200051332...DUPONT SA  <- one debit
  04ES12000B12345678...00000025000...0000000001...                                                   <- creditor total
  99...00000025000...0000000001...                                                                   <- file total
  ```

  Italy, RiBa (CBI flat file, record type in columns 2-3, DDMMYY dates):

  ```text
   IB...ACME SPA...241126...                   <- header
   14...000000025000...301226...DUPONT SRL...  <- amount, due date
   20...ACME SPA...                            <- creditor
   30...DUPONT SRL...                          <- debtor
   40...VIA ROMA 1...20100 MILANO MI...        <- debtor address
   50...INV-2026-001...                        <- reference
   51...0000000001...                          <- receipt number
   70...                                       <- closing
   EF...                                       <- file end
  ```

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
- **Not a message format.** It is an EU directive that forced banks to open access to accounts via APIs
  (XS2A, access to account) for licensed third parties:
  - AISP: Account Information Service Provider
  - PISP: Payment Initiation Service Provider
  - CBPII: Card-Based Payment Instrument Issuer
- Strong Customer Authentication (SCA): two of knowledge / possession / inherence, plus dynamic linking.
- eIDAS certificates (QWAC, QSealC) identify TPPs.
- PSD2 itself does not define an API. Market standards fill the gap: **Berlin Group NextGenPSD2**
  (most of the EU), **STET** (France), **UK Open Banking** (UK, separate regime post-Brexit).
  These are REST/JSON, but their payment payloads borrow ISO 20022 data elements.
- Example Berlin Group AIS call (a fintech reads ACME's account; same EndToEndId as the pain.001):

  ```http
  GET /v1/accounts/3dc3d5b3-7023/transactions?bookingStatus=booked&dateFrom=2026-11-24
  X-Request-ID: 99391c7e-ad88-49ec-a2ad-99ddcb1f7721
  Consent-ID: 1234-wertiq-983
  ```

  ```json
  {
    "account": { "iban": "BE68539007547034" },
    "transactions": {
      "booked": [
        {
          "endToEndId": "INV-2026-0042",
          "bookingDate": "2026-11-24",
          "transactionAmount": { "currency": "USD", "amount": "-12500.00" },
          "creditorName": "GLOBEX CORP",
          "remittanceInformationUnstructured": "INV-2026-0042"
        }
      ]
    }
  }
  ```

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
