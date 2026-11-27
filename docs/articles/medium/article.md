<!--
Medium metadata
Topics: Software Engineering, Fintech, Payments, ISO 20022, Software Architecture
Meta description: ISO 20022, pacs, camt, MT, MX, CBPR+, CODA and PSD2 are layers of one stack, not rival formats. Why the mess exists and what developers should stop doing.
-->

# Why are there so many banking formats?

*ISO 20022, MT, MX, CBPR+, CODA and PSD2 are not rival standards. They are layers, and history explains why they all still exist*

![Banner for the article: abstract layered shapes suggesting a stack of banking message formats](banner.png)

*By [Wallace Espindola](https://www.linkedin.com/in/wallaceespindola/), Solution Architect | [GitHub](https://github.com/wallaceespindola/)*

---

A developer joins a payments or treasury team and gets a ticket that says something like "support camt.053 next to CODA, and check the MT103 fallback for CBPR+". Every word in that sentence is a format, a rulebook or a network, and none of the onboarding material explains how they relate. So they open a search engine and find vendor pages that sell a converter, usage guidelines that run for hundreds of pages, and forum threads where people argue about "MT vs MX" as if it were a choice between two frameworks.

I think the confusion comes from one wrong assumption. People read the acronyms as a list of competing formats and try to pick a winner. Each one answers a different question, and once you see which question, most of the noise goes away.

This article is the opinion piece in a series on banking formats. It's about why I think the acronyms are layers, how history produced the mess, and the habits I'd like developers to drop. The hands-on parts (the sample files, the parsers, the tests) live in the companion repository, [demystify-banking-formats](https://github.com/wallaceespindola/demystify-banking-formats), which has one payment and one statement written out in every format discussed here, plus a small Java 25 module that parses them.

## The question each acronym answers

Here is how I sort them. I'll use one story throughout, the same one the repository uses: ACME NV in Brussels pays GLOBEX in the US USD 12,500.00, and ACME's account later receives 250.00 EUR from DUPONT SA with a Belgian structured reference. (The bank identifiers in the samples, `BANKBEBB` and `BANKUS33`, are fictional.)

- **ISO 20022:** the standard. It answers "which rulebook defines the data?" It's a data dictionary and a catalogue of messages, and everything else in the modern part of the stack borrows from it.
- **pain, pacs and camt:** message families inside ISO 20022. They answer "who talks to whom about what?" pain is customer to bank (ACME's ERP sends a pain.001 to pay GLOBEX), pacs is bank to bank (ACME's bank sends a pacs.008 onward), camt is reporting (the bank tells ACME what happened on the account with a camt.053).
- **MT and MX:** syntaxes on the Swift network. They answer "what does it look like on the wire?" MT is the old FIN text format with tags like `:59:`. MX is simply what Swift calls ISO 20022 XML when it travels on Swift.
- **CBPR+, CGI-MP and HVPS+:** usage rules. They answer "which fields, lengths and codes are allowed on this path?" CBPR+ restricts ISO 20022 messages for cross-border bank to bank traffic, CGI-MP does the same for corporate to bank. They don't add new messages, they narrow existing ones.
- **CODA, CFONB 120, Norma 43, CBI RH and the MT940 dialects:** national and legacy statement files. They answer "what does my ERP still import?" CODA is the Belgian one. camt.053 is the message meant to replace all of them.
- **PSD2:** an EU directive, [Directive (EU) 2015/2366](https://eur-lex.europa.eu/eli/dir/2015/2366/oj). It answers "who may access the account, and how?" It opened account information and payment initiation APIs (Berlin Group NextGenPSD2 and STET are two common API standards in Europe). It isn't a message format at all.

Drawn as a picture, it looks like this:

![Layer diagram of banking formats: ISO 20022 is the standard at the core; the pain, pacs and camt message families are defined by it and sent as MX XML or legacy MT text on Swift; usage rules such as CBPR+, CGI-MP and HVPS+ restrict the families; national and legacy files such as CODA, CFONB 120 and MT940 dialects are being replaced by camt.053; regulation and APIs such as PSD2 borrow ISO 20022 data](diagrams/format-layers.png)

Once you sort them this way, the questions people ask start to look odd. "Should we use pacs or MX?" is like asking whether to use JSON or REST. A pacs.008 sent on Swift is an MX message. "Is PSD2 replacing camt?" mixes an access channel with a statement message. "Is CBPR+ a new format?" No, it's a profile of formats you already have. I think the most useful thing you can do on a new payments project is put each acronym in the ticket on the right layer before writing any code.

## History explains the mess

If the layers are this clean, why does nobody explain them that way? Because they didn't arrive as a design. They piled up over decades, each one solving the problem of its time, and the old ones were never switched off on a single day.

MT came first. It is a compact text format built for a network where every byte and every character mattered: tagged fields, a restricted character set, party fields limited to a few lines of 35 characters. For what it was designed to carry, it did the job, which is exactly why it lasted so long. Banks built cores, screening rules and back-office habits around those 35-character lines.

Statements went their own way in each country. Belgium had CODA, France CFONB 120, Spain Norma 43, Italy CBI RH, and German banks used MT940 with their own conventions for the free-text `:86:` field. None of these is wrong. Each was a reasonable local answer, written before anyone expected a corporate in Brussels to reconcile accounts in five countries with one ERP. CODA is still maintained: [Febelfin publishes version 2.7](https://febelfin.be/media/pages/publicaties/2023/febelfin-standaarden-voor-online-bankieren/5d601609cd-1754302976/standard-coda-2.7-en.pdf), and version 2.6 is dated February 2018. A format that still gets new versions is not a dead format, whatever the migration slides say.

ISO 20022 arrived as the attempt to describe all of this with one data model. The important word is model. It doesn't just define an XML file, it defines the business concepts (a debtor, a creditor, a postal address with a street, a building number, a town and a country) and then lets messages and syntaxes be built on top. That is why it can sit under pain, pacs, camt and, indirectly, under PSD2 APIs at the same time.

Then the big switch happened in slow motion. In Europe and the UK, T2 (the Eurosystem's payment system) [replaced TARGET2 on ISO 20022 in March 2023](https://www.ecb.europa.eu/press/pr/date/2023/html/ecb.pr230321~f5c7bddf6d.en.html) and [CHAPS followed on 19 June 2023](https://www.bankofengland.co.uk/news/2023/june/chaps-transitions-to-latest-global-financial-messaging-standard). In the US, [Fedwire Funds moved to ISO 20022 in a single-day cutover on 14 July 2025](https://frbservices.org/news/press-releases/071525-iso20022-migration-announcement). On Swift, [MT/MX coexistence for cross-border payment instructions ended on 22 November 2025](https://www.swift.com/insights/news/iso-20022-bytes-payments-journey-continues).

![Timeline from the MT era to ISO 20022: MT text on Swift FIN and national files such as CODA, CFONB 120 and Norma 43; CODA 2.6 in February 2018 with 2.7 current; T2 migrating in March 2023 and CHAPS in June 2023; Fedwire Funds cutover on 14 July 2025; Swift MT/MX coexistence ending for payment instructions on 22 November 2025; Swift agreeing in August 2026 to extend the structured address migration; the original 14 November 2026 unstructured address deadline deferred; the deferred SR 2026 release on 12 June 2027](diagrams/mt-to-iso20022-timeline.png)

The end of coexistence on that timeline is about payment instructions on Swift, which is narrower than "MT is gone". Reporting files, national statement formats and the translation layers inside banks are a different story, and a lot of day-to-day integration work happens there. So I don't blame the standards bodies for the mess. A long migration looks like this from the inside: old and new running side by side, for years, in the same codebase.

## Where the migration really is now

The phase we're in is less about syntax and more about data quality. The clearest example is the postal address.

Under CBPR+, [unstructured postal addresses were due to be rejected from 14 November 2026](https://www.swift.com/standards/iso-20022/removal-unstructured-address). At minimum, town and country need their own fields, and a hybrid form allows up to 2 address lines of 70 characters next to them. Then, in August 2026, [Swift accepted a community request to extend the structured address migration](https://www.swift.com/news-events/news/swift-accepts-community-request-extend-structured-address-migration-iso-20022-payment-messages): the deferred SR 2026 release moves to 12 June 2027, and the address timing is under consultation, with an update expected by December 2026 ([Finextra covered it too](https://www.finextra.com/newsarticle/48318/swift-accepts-request-to-extend-structured-address-migration-for-iso-20022-messages)). Statement and notification messages (camt.052, camt.053 and camt.054) are [excluded from that rule, according to Swift's FAQ](https://www.swift.com/standards/iso-20022/iso-20022-faqs/iso-20022-removal-unstructured-address).

I read the delay as good news for anyone who hasn't started, and as a trap for anyone who thinks the work went away with the date. Unstructured addresses in a customer master still have to be split into street, building number, town and country by someone.

Regulation keeps adding its own layer on top. [Verification of Payee became mandatory for euro credit transfers in the eurozone on 9 October 2025](https://eur-lex.europa.eu/eli/reg/2024/886/oj), under the Instant Payments Regulation. PSD3 and the Payment Services Regulation reached a [provisional political agreement on 27 November 2025](https://www.nortonrosefulbright.com/en/knowledge/publications/cedd39c6/psd3-and-psr-from-provisional-agreement-to-2026-readiness). They are agreed, not yet in force. Verification of Payee already changes what a bank has to check before a payment leaves; PSD3 and the PSR will add their own checks and disclosures once in force.

I don't have adoption numbers for any of this, and I won't invent any. What I can say is that the dates above are the ones the operators and regulators published, and that they all point the same way: richer, structured data, checked earlier.

## What I think developers should stop doing

None of these habits is stupid, and each made sense at some point. I just think they now cost more than they save.

### Stop treating "MT vs MX" as the question

"MT vs MX" sounds like a technology choice, but for new cross-border payment instructions on Swift the choice is already made: coexistence has ended. The question I'd ask instead: how much of the ISO 20022 data model does your system keep?

A system can speak perfect MX on the wire and still be MT on the inside, if the first thing it does is flatten the message into an internal record with a few 35-character name and address lines. That system has migrated its syntax and none of its data. When I look at a payments design, I care less about which syntax the adapter emits and more about whether the internal model can hold a building number, a town and an end-to-end identifier as separate fields.

### Stop translating ISO 20022 back to MT and calling it equivalent

Translation from MT to ISO 20022 is mostly safe, because you are moving into a richer model. The other direction loses data, and nothing in the output warns you. The repository has a small translator that turns the sample pacs.008 into an MT103 so you can see it. Here's a trimmed part of what `make run` prints:

```text
...
:59:/123456789
Globex Corporation International Ho
North Industrial Park Boulevard 120
62701 Springfield IL US
...
Lost in translation:
...
  - :59: name truncated, lost "ldings LLC"
  - :59: street truncated, lost "0"
  - :59: structured address (StrtNm, BldgNb, PstCd, TwnNm, Ctry) flattened to free text
  - EndToEndId INV-2026-0042 has no MT103 field (kept only if the bank maps it to :70:)
```

Look at the second and third lines of the `:59:` field. The creditor, Globex Corporation International Holdings LLC, became "Globex Corporation International Ho", and building number 1200 became "120". That last one bothers me more than the name. It's a different, perfectly plausible address, and nothing in the MT103 tells a reader that it's wrong. The structured address is gone, and so is the end-to-end identifier the customer will use to reconcile the payment.

The translator in the repo is a teaching example, not a production CBPR+ translator, and it says so. Real translation rules are more careful than mine. But the constraint is the same for everyone: a 35-character line can't hold a 45-character name. If your architecture still has an ISO 20022 to MT hop somewhere inside, I'd want to know where it is, who reads its output and what they do with a truncated name. Sanctions screening and reconciliation both depend on those fields.

### Stop validating only against the plain XSD

The ISO 20022 schema tells you whether a message is well-formed for the standard. It doesn't tell you whether a bank or a network will accept it. CBPR+, CGI-MP and each bank's own implementation guide narrow the message further: fewer optional fields, shorter lengths, restricted character sets, mandatory codes. A pacs.008 can pass the XSD and still be rejected.

So I'd treat schema validation as the first check, not the last one. The usage guideline for the path the message travels on is part of the contract. If your test suite only proves that you produce valid XML, it proves less than it seems to.

### Stop letting free-text fields carry meaning

This is the habit I'd most like to see go. MT940's `:86:` field is a block of free text, and over the years every bank invented its own sub-structure inside it. Here is the line from the sample statement in the repo:

`:86:DUPONT SA +++123/4567/89002+++`

To get the counterparty and the Belgian structured reference out of that, you parse a string with rules that belong to one bank. Another bank formats it differently, so "MT940 support" ends up meaning one parser per bank. camt.053 has separate elements for the counterparty and for structured remittance information. CODA has fixed positions for them. When a field exists for a piece of data, I'd put the data there and read it from there, not from a narrative line.

The same goes for the payment side. Stuffing an invoice number into an address line, or an address into a remittance field, works until a translator, a screening tool or a regulator reads the field as what it claims to be. Structured addresses are the industry saying exactly this, with a deadline attached.

### Stop letting every format leak into the domain

When a team supports CODA, MT940 and camt.053, the temptation is three code paths all the way down: three statement classes, three reconciliation rules, three sets of bugs. I prefer one canonical statement model and one small adapter per format. The repository does this with a single `Statement` record and three parsers, and its `make run` ends with the line `Same statement in all three formats: true` for the CODA, MT940 and camt.053 samples. The file format is an input detail there, and the domain code never knows which one arrived.

### Stop treating PSD2 as "the new format"

PSD2 opened account access through APIs. It didn't replace statements, and it didn't replace pacs or pain. A fintech app that reads ACME's account through an account information API is using a different channel to look at the same account, often with data that borrows ISO 20022 names. If your roadmap has a line that says "move from camt.053 to PSD2", I'd ask which problem that line is meant to solve. The two answer different questions.

## Where this view doesn't fit

I should say where the layered picture is a simplification. The layers aren't perfectly clean. MT940 is both a Swift MT message and, in practice, a family of national dialects, which is why the diagram lists MT940 dialects with the national files while MT itself sits in the syntax layer. PSD2 APIs aren't ISO 20022 messages, even when they borrow its data names. And CBPR+ is only one usage rulebook among several, and the SEPA rulebooks and each bank's guide add their own.

The opinions above also have limits. If you maintain a system that only ever receives MT940 from one bank and will be retired next year, writing a canonical model and three adapters is probably not worth it. One straightforward parser is fine. And if your bank still only offers a legacy statement file, you take what it offers. The point is to keep that choice at the edge of the system, not to refuse the file.

Finally, the dates. Several of them are still moving. The structured address timing is under consultation, and PSD3 and the PSR are agreed but not in force. I've only stated what the sources say as of this writing, and I'd check them again before planning anything around them.

## Wrapping up

Here is what I think, in short:

- The acronyms are layers of one stack, not competitors. ISO 20022 is the standard, pain, pacs and camt are message families, MT and MX are syntaxes on Swift, CBPR+ and CGI-MP are usage rules, CODA and its cousins are national files, and PSD2 is an EU directive.
- History explains the mess. Each format solved the problem of its time, and a long migration means old and new run side by side for years.
- "MT vs MX" is the wrong question. The right one is how much of the ISO 20022 data model your system keeps internally.
- Translating ISO 20022 back to MT loses data without warning, and a truncated building number is worse than a truncated name because it still looks valid.
- XSD validation is the first check, not the contract. The usage guideline for the path is part of it.
- Free text shouldn't carry meaning when a structured field exists for it.
- One canonical model with an adapter per format keeps the file format at the edge, where it belongs.

The samples, the parsers and the MT103 translator are all in the [repository](https://github.com/wallaceespindola/demystify-banking-formats) if you want to see the layers in code.

I'm curious how this looks from where you sit. Which acronym confused you most when you started? Does your system still have an ISO 20022 to MT hop somewhere inside, and do you know what it drops? And how many `:86:` parsers do you maintain today? Let me know in the comments.

---

**Examples repository:** every example in this article (the pain.001, pacs.008, CODA, MT940 and camt.053 samples, the Java 25 parsers, the pacs.008 to MT103 translator and the diagrams) is in <https://github.com/wallaceespindola/demystify-banking-formats>.

Need more tech insights?
Check out my GitHub, LinkedIn, and Speaker Deck.
Happy coding!

Wallace Espindola, Solution Architect

- GitHub: <https://github.com/wallaceespindola/>
- LinkedIn: <https://www.linkedin.com/in/wallaceespindola/>
- Speaker Deck: <https://speakerdeck.com/wallacese>
