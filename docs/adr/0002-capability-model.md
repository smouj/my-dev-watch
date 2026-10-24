# ADR 0002 — Capabilities instead of model checks

Status: accepted (2026-10-24)

## Context

Cheap watches reuse retail names across different firmwares. A boolean
"compatible" flag is wrong for the user and dangerous for the code.

## Decision

- Every feature is a Capability with a category and a label.
- Every device profile maps capabilities to SupportStatus: STABLE, EXPERIMENTAL,
  RESEARCH, UNAVAILABLE, UNKNOWN.
- The UI renders the map; it never asks for a protocol or a model name.

## Consequences

- Adding a device is data work, not a UI branch.
- The interface can honestly show "unverified" instead of hiding a row.
- A capability must be declared before it can be implemented, which forces the
  evidence conversation early.
