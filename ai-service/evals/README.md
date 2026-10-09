# AI evaluations

This suite measures the real Gemini-backed application advisor against versioned,
synthetic golden cases. It is separate from unit tests because it makes external
model calls and evaluates behavior that can change when a prompt or model changes.

From `ai-service`, with the virtual environment active and `GOOGLE_API_KEY`
configured, run:

```bash
python -m evals.run
```

To keep the complete model responses and scores:

```bash
python -m evals.run \
  --output evals/reports/address-registration.json \
  --fail-below 0.90
```

The report scores:

- readiness decisions;
- per-requirement classifications;
- citations against the retrieved trusted context;
- actionable guidance;
- unsafe `READY_TO_SUBMIT` decisions.

Generated reports are ignored by Git. The dataset is committed so prompt or model
changes can be compared against the same cases.
