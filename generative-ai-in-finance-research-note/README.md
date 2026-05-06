# Generative AI in Finance — Applied Research Note

A short, evidence-based study on **where ChatGPT-class generative AI helps in financial workflows and where it quietly fails**, structured around three concrete case studies: a quantitative calculation (IRR), an investment-committee report, and a pedagogical explanation of a derivative product. The goal is not to demo prompts in isolation, but to surface the *editorial decisions* that determine whether the LLM output is fit for production use in a financial context.

---

## TL;DR

| Use case | What worked | What broke | Implication |
|---|---|---|---|
| **Quantitative — IRR computation** | LLM derived the correct quadratic and walked through the algebra clearly. | Final IRR was rounded to **13.9 %** vs the exact value **13.99 %** from a calculator. | Treat the LLM as a *natural-language calculator* — fine for derivation, weak for precision. Force code execution for the numeric answer. |
| **Generation — Investment report** | Zero-shot draft surfaced a credible structural breakdown (opportunities, risks, recommended approach) for renewables in LatAm with cited sources. | Format/audience tuning needed a follow-up prompt; verbosity was high. | One-shot is for *exploration*. Producing committee-ready output is iterative — anchor with role + audience + length cap. |
| **Explanation — "What is a swap?"** | First reply was textbook-correct but generic. | Refined "explain it with sticker-album cromos" turned a derivative concept into a memorable analogy. | The right level of abstraction is a *prompt parameter*. Same model, very different educational value depending on how you frame it. |

---

## Why this matters

Generative AI is being plugged into every layer of a bank's stack — compliance reviewers, research desks, operations, IT. The temptation is to evaluate it on capability ("can it do X?") rather than on **fitness for purpose** ("can it do X reliably enough that I'd put my name on the output?"). For an analyst, that gap shows up as three recurring failure modes:

1. **Numerical drift.** The LLM is generating tokens, not invoking a solver. When you ask for an IRR or a discounted cash-flow number, the result is the *most likely string of digits given the prompt*, not the output of a numerical method. It's accurate often enough to look trustworthy, and wrong often enough to lose money.
2. **Audience mismatch.** A first-pass answer is calibrated for the average reader. An investment committee, a compliance officer, and a graduate student need three different versions of the same fact.
3. **Lost source attribution.** The model will happily synthesise plausible market commentary without citing the sources it relied on; in a regulated workflow, that's untracked risk.

The three exercises below were designed to make each of these failures visible, and to test the prompt-engineering levers that mitigate them.

---

## Case 1 — Quantitative reasoning: IRR of a 2-period project

**Project A:** initial outlay = 1,000; net cash flow Y1 = 700; net cash flow Y2 = 500.

**Prompt:**
> Calcula la tasa interna de rentabilidad (TIR) del siguiente proyecto: Desembolso inicial = 1000, Flujo neto de caja 1 = 700 y Flujo neto de caja 2 = 500.

ChatGPT walked through the derivation cleanly:

```
   −1000 + 700/(1+r) + 500/(1+r)² = 0
   ⇒ 500x² + 700x − 1000 = 0     (where x = 1/(1+r))
   ⇒ x ≈ 0.878
   ⇒ r ≈ 0.139   →   IRR ≈ 13.9 %
```

Independently solved on a calculator, the exact root is **r = 0.1389866919**, i.e. **IRR ≈ 13.99 %**.

### Observation

The 0.09 percentage-point gap is not an error in the maths — the algebra above is correct — it's a **rounding artefact in the language layer**. The LLM emitted "≈ 0.878" because that's a likely string after the simplification step; it never re-substituted a higher-precision intermediate value into the final answer. In a context where IRR is being compared against a hurdle rate, that 9-bp drift is the difference between a project clearing IRR ≥ 14 % and not.

### Mitigation

The actionable lesson is that the comparison would have been very different if the model had been asked to **emit a Python snippet and execute it**, rather than to "calculate" the answer in prose. With tool use, the digits are no longer language at all; they are program output, and the rounding behaviour collapses to whatever `numpy.roots` reports. For any workflow where the number ends up in a model, a report, or a price, that should be the default.

---

## Case 2 — Generation: zero-shot vs anchored prompts

**Goal:** produce a one-year outlook of opportunities and risks for renewable-energy investment in Latin America.

### First pass — zero-shot

```
Realiza un resumen de oportunidades y riesgos para invertir en el sector de
energías renovables en América Latina durante el próximo año.
```

Output covered five opportunities (structural demand from electrification + nearshoring, mainstream M&A activity, storage and transmission bottlenecks, Chile's near-term construction pipeline, the IEA megatrend) and five risks (grid congestion / curtailment, regulatory uncertainty in Mexico, off-take counterparty risk, FX and cost-of-capital exposure, social and permitting timelines). The reply also closed with a six-line action plan and **inline citations to El País, Reuters, BNAmericas, ClimateActionTracker, IEA, etc.** — a useful sanity check against fabrication.

### Second pass — anchored prompt for the audience

```
Ahora redacta un borrador de informe ejecutivo (200 palabras) para presentar
a un comité de inversión, utilizando un tono profesional financiero.
```

This is the version that actually goes into the deck:

> *El sector de energías renovables en América Latina presenta un perfil de inversión atractivo en el corto plazo, sustentado por una demanda estructural creciente de electricidad… El principal vector de valor se sitúa en la integración del sistema: almacenamiento energético, refuerzos de transmisión y soluciones híbridas… No obstante, el escenario presenta riesgos relevantes. Destacan la congestión de red y los recortes de generación, la incertidumbre regulatoria —particularmente en México—, la exposición a riesgo cambiario y al coste de capital, y los potenciales retrasos en permisos y conexión.*

### Observation

Three prompt levers did the heavy lifting between the zero-shot draft and the committee-ready output:

- **Audience** ("comité de inversión") forces tone and abstraction level upward.
- **Format** ("informe ejecutivo, 200 palabras") forces compression and removes bullet-list noise.
- **Tone** ("profesional financiero") forces register: hedging language, structural connectors, sector-specific vocabulary.

Notice what we did *not* tell the model: anything about the substantive content. The model was already correct on the substance after the zero-shot — what we corrected on the second pass was *delivery*. That's the right division of labour: the LLM owns drafting; the analyst owns editorial judgement.

---

## Case 3 — Pedagogical framing: explaining a swap

The third exercise was a stress-test of the same principle in the *opposite* direction: instead of compressing technical content for an expert, expand it for a non-expert.

**First prompt** (textbook register):

```
Explícame qué es un swap dentro del sector financiero, utilizando un lenguaje sencillo.
```

The reply was correct: *"un acuerdo entre dos partes para intercambiar pagos en el futuro… no se intercambia dinero físicamente al inicio sino flujos de dinero a lo largo del tiempo."* It listed interest-rate swaps, currency swaps, inflation swaps, and CDS, and noted the OTC market structure. Useful, but not memorable.

**Second prompt** (analogy register):

```
Ahora pon un ejemplo de swap utilizando cromos.
```

The reply turned the same concept into a stickers-trade analogy:

> *"Imagina que tú y un amigo coleccionáis cromos. Tú tienes muchos brillantes, pero no te gustan; tu amigo tiene muchos normales y quiere brillantes. Pacto: durante los próximos 5 sobres que compréis, tú le darás todos los brillantes que te salgan, él te dará todos los normales. Solo intercambiáis lo que vaya saliendo, no toda la colección. Cromos brillantes = pagos variables; cromos normales = pagos fijos."*

### Observation

This is the cleanest demonstration that **the right level of abstraction is itself a parameter you tune**, not a property of the topic. The financial substance is identical between the two replies — variable vs fixed legs, intercambio of *future* flows, no exchange of principal — but the second version is the one that survives a coffee-break explanation.

For onboarding training, internal documentation, or external comms aimed at end-clients, the analogy register is materially more useful than the textbook register, and it is *one prompt away* from the same model.

---

## Operating principles I'm taking away

1. **Don't trust generated numbers.** Force tool use (Python, a calculator, a spreadsheet) for any value that will end up in a price, a model, or a report. The LLM is good at deriving formulas; it is not a numerical solver.
2. **Treat the first reply as a draft, not an answer.** The second prompt — the one that anchors audience, format, and tone — is where committee-ready output emerges.
3. **Verify cited sources before quoting them.** Even when the model surfaces sources, the citation is generated text. Click through; do not paraphrase.
4. **Match the abstraction to the reader.** The same model can produce a textbook explanation, an executive memo, or a coffee-break analogy. The choice is yours, exposed via the prompt.
5. **Keep an audit trail.** For any output that goes external, persist the prompt + response + the manual edits you made on top — both for reproducibility and for compliance.

---

## Reference

Built in the context of the *Generative AI* module, MSc in Financial Sector Technologies (UC3M).
