# Banking formats series: Java examples, sample files, articles, slides and diagrams.
PUML := $(shell find docs -name '*.puml')
MMD  := $(shell find docs -name '*.mmd')
DECK := docs/presentations/slides.md
DECK_NAME := demystify-banking-formats

.PHONY: help examples run diagrams lint links slides pptx check clean

help: ## Show targets
	@grep -E '^[a-z-]+:.*## ' $(MAKEFILE_LIST) | awk -F':.*## ' '{printf "  %-10s %s\n", $$1, $$2}'

examples: ## Build and test the Java examples (Java 25, Maven)
	cd examples && mvn -q -B verify

run: ## Run the examples against docs/samples
	cd examples && mvn -q -B compile exec:java

diagrams: ## Render all PlantUML and Mermaid sources to PNG next to their source
	@for f in $(PUML); do plantuml -tpng "$$f"; done
	@for f in $(MMD); do npx -y @mermaid-js/mermaid-cli -p puppeteer-config.json -i "$$f" -o "$${f%.mmd}.png" -b white -s 2; done

lint: ## Lint Markdown
	npx -y markdownlint-cli2 "**/*.md" "#node_modules" "#examples/target"

links: ## Check relative links between Markdown files (offline)
	lychee --offline --no-progress "**/*.md"

slides: ## Export the Marp deck to PDF (Speaker Deck)
	npx -y @marp-team/marp-cli $(DECK) --pdf --allow-local-files -o docs/presentations/$(DECK_NAME).pdf </dev/null

pptx: ## Build the editable PPTX (Google Slides, Keynote, PowerPoint)
	uv run --with python-pptx --with pillow docs/presentations/build_pptx.py

check: examples lint links ## Run all checks

clean: ## Remove build output and HTML exports
	rm -rf examples/target docs/presentations/*.html
