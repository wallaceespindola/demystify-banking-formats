# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

A content series plus runnable examples that explain how banking message formats fit together: ISO 20022, pain,
pacs, camt, MT, MX, CBPR+, CGI-MP, CODA (and other national statement files) and PSD2. The source of truth for
the idea, the story and every dated fact is `docs/spec/PRD-banking-formats-map.md`; `docs/context.md` holds the
research notes. Keep `README.md`, the articles and the slides consistent with the spec.

## Layout

- `docs/spec/`: the series definition and the verified facts table (with sources and the date checked).
- `docs/context.md`: research brief, per-format examples, national formats per country.
- `docs/samples/`: the sample files (pain.001, pacs.008, camt.053, CODA, MT940) that the code and articles use.
- `docs/diagrams/`: shared diagram sources (`.mmd`, `.puml`) with rendered PNGs; article copies derive from these.
- `examples/`: Java 25 Maven module, JDK only. `CodaParser`, `Mt940Parser`, `Camt053Parser` map to one
  `Statement` record; `Pacs008ToMt103` shows what MT loses. Tests read `../docs/samples`.
- `docs/articles/<platform>/`: `article.md`, banner, `diagrams/`. Each platform has its own angle and title.
- `docs/presentations/`: Marp `slides.md`, PDF, editable PPTX built by `build_pptx.py`, `GOOGLE_SLIDES.md`.
- `docs/glossary.md`: every acronym used in any Markdown file. Add a row when you introduce a new one.

## Commands

```bash
make examples   # mvn verify in examples/ (Java 25)
make run        # run Main against docs/samples
make diagrams   # render .puml (plantuml) and .mmd (mermaid-cli, local Chrome via puppeteer-config.json)
make lint       # markdownlint-cli2, config in .markdownlint.json
make links      # offline relative-link check (lychee)
make slides     # Marp export to PDF
make pptx       # editable PPTX via python-pptx
```

CI (`.github/workflows/ci.yml`) builds the Java examples and runs markdownlint and the offline link check.

## Conventions

- Code snippets in articles are copied from `examples/` or `docs/samples/` and must stay identical. Change the repo
  first, then the articles.
- Sample files are trimmed teaching samples, not production messages. CODA positions follow the Febelfin layout for
  the fields the parser reads.
- Dates and versions (Swift deadlines, CBPR+ address rules, PSD3/PSR status, CODA version) drift. Only state what
  the spec's facts table lists, and re-check it before publishing.
- Prose: sentence-case headings, no em dashes, no emojis, no fabricated stats or anecdotes, no AI-assistance
  statements. Never name the author's employer or internal tools.
- Fixed-width samples in Markdown use ASCII `...` and `<-` so columns stay aligned on GitHub.
- Author: Wallace Espindola, Solution Architect, <wallace.espindola@gmail.com>,
  <https://www.linkedin.com/in/wallaceespindola/>, <https://github.com/wallaceespindola/>
