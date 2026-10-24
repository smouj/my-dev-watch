# ADR 0003 — Evidence levels on every protocol fact

Status: accepted (2026-10-24)

## Context

Public research on these watches is fragmented, sometimes wrong, and frequently
describes a different firmware than the one in front of you. Confident mistakes
brick hardware.

## Decision

Every command in the catalogue carries UNKNOWN, OBSERVED, PROBABLE or VERIFIED, an
evidence string, and an implemented flag. Only verified-adjacent facts reach the UI
as stable.

## Consequences

- The catalogue is useful before the code exists.
- A single wrong byte cannot be promoted silently: promotion is a review decision.
- The project looks "incomplete" by design, which is more accurate than looking
  finished and being wrong.
