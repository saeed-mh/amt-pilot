# Security policy

## Reporting a vulnerability

Please report security issues privately through GitHub's **Security** tab using a private vulnerability report. Do not open a public issue containing credentials, tokens, personal data, uploaded documents, or exploit details.

Include the affected component, reproduction steps, expected impact, and any suggested mitigation. Use synthetic data in every example.

## Supported version

The latest revision of the `main` branch is the supported portfolio version.

## Secrets and sensitive documents

Never commit a populated `.env` file, JWT secrets, Google API keys, database exports, uploaded PDFs, AI reports derived from real documents, or local development logs. If a secret is exposed, revoke and rotate it immediately; removing it in a later commit is not sufficient because Git history retains it.
