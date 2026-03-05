# Parallel Sprints Design

## Overview

5 independent Scrum teams run in parallel, each on its own git branch via worktrees.
Each team follows a sequential role pipeline: Stakeholder → PO → RD → QA.
The Stakeholder agent autonomously identifies the feature to build.

## Sprint Branches

| Sprint | Branch | Feature Domain |
|--------|--------|----------------|
| 1 | sprint-1 | TBD by Stakeholder |
| 2 | sprint-2 | TBD by Stakeholder |
| 3 | sprint-3 | TBD by Stakeholder |
| 4 | sprint-4 | TBD by Stakeholder |
| 5 | sprint-5 | TBD by Stakeholder |

## Role Pipeline (per sprint)

1. **Stakeholder**: Reviews codebase + product state, identifies user need, writes `docs/sprints/sprint-N/stakeholder.md`
2. **PO**: Reads stakeholder doc, writes Gherkin spec to `docs/sprints/sprint-N/spec.md`
3. **RD**: Reads spec + codebase, implements code, commits to branch
4. **QA**: Reads spec + implementation, writes `docs/sprints/sprint-N/qa.md`, validates logic

## Constraints

- Each sprint agent works exclusively on its own worktree (no cross-branch file access)
- Features must be self-contained (new files preferred, minimal shared file edits)
- QA validates against the Gherkin spec written by PO
