# Series outline

One idea, seven angles: banking formats are layers of one stack, not competing standards. Every article links
<https://github.com/wallaceespindola/demystify-banking-formats>, uses the story and facts in
[../spec/PRD-banking-formats-map.md](../spec/PRD-banking-formats-map.md), and copies snippets verbatim from
`docs/samples/` and `examples/`. Titles and angles differ; no paragraph is shared between platforms.

| Platform | Working title | Angle | Snippets | Diagrams |
| --- | --- | --- | --- | --- |
| Dev.to | pain, pacs, camt, MT, MX: a field guide to banking formats | Hands-on reading guide: open each sample file, what to look at, how to tell them apart, run the examples | pain.001 levels, CODA record lines, MT940 `:86:`, camt.053 entry, `make run` output | format-layers (shared), own "which format am I looking at" decision flow |
| DZone | A canonical model for bank statement migration: CODA and MT940 to camt.053 | Enterprise pattern: anti-corruption layer, one canonical `Statement`, parallel run and reconciliation gates, ownership of format adapters | `Statement` record, the three parser classes as adapters, test that checks each against one expected statement | own migration architecture (PlantUML), own parallel-run flow |
| Medium | Why are there so many banking formats? | Opinion essay: the acronyms are layers, history explains the mess, what I think developers should stop doing | at most one short sample (the MT103 truncation) | format-layers (shared), own timeline (MT era to ISO 20022) |
| LinkedIn | The MT deadline passed. Your reporting stack probably didn't move. | Leadership: what a lead decides now (inventory of formats, canonical model, address data quality, PSD3/PSR watch), what to measure, how to pilot; no tables | none or one line | own decision roadmap |
| Substack | Keep one statement model, whatever the bank sends | Code & Coffee habit note: one canonical model, adapters per format; coffee-length | `Statement` record, `make run` line | own small adapter sketch |
| InfoQ | From Fixed-Width Files to ISO 20022: How Banking Message Standards Converged | Trend and pattern analysis for architects: history with dated sources, the layered model, the translation-loss risk, where it goes (structured addresses, PSD3/PSR, APIs) | one short pacs.008 vs MT103 comparison | own original: convergence timeline, own original layer model |
| JavaPro | Three statement formats, one Java model: parsing CODA, MT940 and camt.053 in Java 25 | Code walkthrough: records, switch with patterns, positions vs tags vs XML, XXE, tests; then pacs.008 to MT103 loss report | full listings from `examples/` | own original class diagram, own original parse-flow diagram |

The deck (`docs/presentations/slides.md`) follows the Dev.to and InfoQ structure: the stack, one payment, the
samples, the code, the pitfalls.
