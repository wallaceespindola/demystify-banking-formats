# Getting this deck into Google Slides

The deck is `demystify-banking-formats.pptx` (24 slides, 16:9, with speaker notes).

## Step 0: Get the file

- From a clone: `docs/presentations/demystify-banking-formats.pptx`.
- From GitHub without cloning: open [the file on GitHub](https://github.com/wallaceespindola/demystify-banking-formats/blob/main/docs/presentations/demystify-banking-formats.pptx) and click **Download raw file**.

## Option 1: Open it as a new Google Slides presentation

1. Go to [Google Slides](https://slides.google.com).
2. Click **Blank presentation**, then **File > Open** (or press Ctrl+O / Cmd+O).
3. Choose the **Upload** tab and drop `demystify-banking-formats.pptx` in.
4. When it opens, use **File > Save as Google Slides** so you edit a native copy, not the PPTX.

## Option 2: Add the slides to a deck you already have

1. Open your existing presentation in Google Slides.
2. Go to **File > Import slides**.
3. Choose the **Upload** tab and select `demystify-banking-formats.pptx`.
4. Pick **All** (or only the slides you want). Leave **Keep original theme** checked so the dark title slides and colors stay as designed.
5. Click **Import slides**. They're added after the slide you have selected.

## Option 3: Through Google Drive

1. Upload `demystify-banking-formats.pptx` to [Google Drive](https://drive.google.com) (**New > File upload**).
2. Right-click the file and choose **Open with > Google Slides**.
3. Use **File > Save as Google Slides** to convert it.

To have every PPTX convert automatically on upload, turn on **Drive settings > Convert uploads to Google Docs editor format**.

## After importing

- Speaker notes come along: **View > Show speaker notes**.
- The deck uses only Arial and Courier New, so fonts don't get substituted.
- Diagrams are PNG images, so they look the same as in PowerPoint.
- Check the table slides (3, 10, 11, 15 and 22) once; row heights can shift slightly.
- The CODA and `make run` slides (12 and 20) use a smaller code font so the fixed-width lines don't wrap.
- To present: **Slideshow** (top right), or **Presenter view** from its dropdown to see your notes.

## Keynote

Open `demystify-banking-formats.pptx` in Keynote (**File > Open**). The slides and speaker notes come through; Keynote may substitute fonts it doesn't have, so check the table slides (3, 10, 11, 15 and 22).

## Speaker Deck

Speaker Deck takes PDF only. Sign in at [speakerdeck.com](https://speakerdeck.com), click **New Deck**, upload `demystify-banking-formats.pdf`, add a title and description, then publish. The PDF is exported from the Marp deck (`slides.md`) and has the same 24 slides.

## Marp alternative (HTML / PDF)

`docs/presentations/slides.md` is a [Marp](https://marp.app) deck with speaker notes as HTML comments. Export it without PowerPoint:

```bash
# PDF (the Speaker Deck file), same as `make slides`
npx @marp-team/marp-cli docs/presentations/slides.md --pdf --allow-local-files -o docs/presentations/demystify-banking-formats.pdf </dev/null

# Editable PPTX (Arial and Courier New, speaker notes), same as `make pptx`
uv run --with python-pptx --with pillow docs/presentations/build_pptx.py

# HTML
npx @marp-team/marp-cli docs/presentations/slides.md --allow-local-files -o docs/presentations/slides.html </dev/null
```

`--allow-local-files` lets Marp embed the diagram PNGs. `</dev/null` stops marp-cli from waiting on stdin when it runs outside a terminal. If Marp cannot find a browser, point it at Chrome with `CHROME_PATH`.

## Diagrams

Sources live in `docs/presentations/diagrams/`; run the commands from the repo root:

| Diagram | Source | Render |
| --- | --- | --- |
| The layers (shared with `docs/diagrams/`) | `format-layers.mmd` | `npx -y @mermaid-js/mermaid-cli -p puppeteer-config.json -i docs/presentations/diagrams/format-layers.mmd -o docs/presentations/diagrams/format-layers.png -b white -s 2` |
| One payment, every layer (shared with `docs/diagrams/`) | `payment-journey.puml` | `plantuml -tpng docs/presentations/diagrams/payment-journey.puml` |
| Three parsers, one record | `statement-adapters.mmd` | `npx -y @mermaid-js/mermaid-cli -p puppeteer-config.json -i docs/presentations/diagrams/statement-adapters.mmd -o docs/presentations/diagrams/statement-adapters.png -b white -s 2` |

`make diagrams` renders every `.mmd` and `.puml` in the repo next to its source.

---

Author: Wallace Espindola, Solution Architect, <wallace.espindola@gmail.com>
LinkedIn: <https://www.linkedin.com/in/wallaceespindola/> | GitHub: <https://github.com/wallaceespindola/>
Repo: <https://github.com/wallaceespindola/demystify-banking-formats>
