# Banner design: aurora ribbon

The series banners share one layout: a twisting ribbon of light trails over soft nebula glows, with the strands
meeting in one bright knot on the right third. The ribbon stands for the formats themselves: many strands (MT,
MX, CODA, national files, APIs) that run in parallel and then converge on one point, the way the stack converges
on ISO 20022.

## How the banners vary

Every banner uses the topic `banking formats map`, so the ribbon shape is identical across the series. Each
platform gets its own palette and at most three words on the calm left side:

| Platform | File | Size | Palette | Words |
| --- | --- | --- | --- | --- |
| Dev.to | `devto/banner.png` | 1000 x 420 | borealis | Read the formats |
| Medium | `medium/banner.png` | 1200 x 627 | solar | Layers, not rivals |
| LinkedIn | `linkedin/banner.png` | 1920 x 1080 | oceanic | One stack |
| Substack | `substack/banner.png` | 1200 x 630 | candy | Keep one model |
| DZone | `dzone/banner.jpg` | 1200 x 628, JPEG about 110 KB | electric | Side by side |
| InfoQ | `infoq/banner.jpg` | 1024 x 768, JPEG | nebula | Formats converge |
| JavaPro | `javapro/banner.png` | 1200 x 630 | lagoon | Parse once |

The README banner (`docs/images/banner.png`, 1280 x 400) uses the same topic without words.

Regenerate with the `article-image-generator` skill, for example:

```bash
uv run --with pillow python generate_banner.py --platform devto --topic "banking formats map" \
  --text "Read the formats" --out docs/articles/devto/banner.png
```
