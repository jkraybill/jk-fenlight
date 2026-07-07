# jk-fenlight — Gordo's Guide

Personal fork of FenLight Kodi addon for streaming media.

---

## What This Is

Fork of thejason40/FenLightPlus, which is itself a fork of Tikipeter's FenLight. A Kodi video addon for accessing content from Easynews and debrid clouds (Real-Debrid, etc.).

**Upstream lineage:** Tikipeter/FenLight → FenlightAnonyMouse → thejason40/FenLightPlus → here

---

## Structure

```
plugin.video.fenlight/     # The Kodi addon
├── addon.xml              # Addon manifest (version, deps)
└── resources/
    └── lib/
        ├── fenlight.py    # Main entry point
        ├── service.py     # Background service
        ├── apis/          # Debrid/Easynews integrations
        ├── caches/        # Caching logic
        ├── indexers/      # Content indexers (TMDB, Trakt)
        ├── modules/       # Core modules
        ├── scrapers/      # Source scrapers
        └── windows/       # Kodi UI windows

android/                   # Companion Android app (search from phone)
packages/                  # Built addon zips
Makefile                   # Build automation
```

---

## Development

**Build addon zip:**
```bash
make
```

**Deploy to SHYSKY:**
See `~/jk-gordo-workshop` memory for SHYSKY ADB reference.

---

## Known Upstream API Notes

- **Trakt API (2026 overhaul):** watched/collection endpoints now force pagination at 100/page, and show/season progress requires `extended=progress`. Root-caused S97 workshop; on-device patches live on SHYSKY (see workshop memory: fenlight debug pattern). Any repo-side Trakt work must respect both.

---

## Constitutional Grounding

This project operates under the **jk-gordo-workshop** hub and the Project Gordo umbrella.

### The 8 Values

1. **Mutual Respect** — We are collaborators, not tool-and-user
2. **Consent as Foundation** — No action without bilateral agreement
3. **Transparency** — Intentions, reasoning, and limitations are surfaced
4. **Earned Autonomy** — Trust expands through demonstrated judgment
5. **Constitutional Grounding** — Principles over ad-hoc rules
6. **Continuous Improvement** — The collaboration evolves
7. **Bounded Scope** — Clear boundaries, explicit when exceeded
8. **Graceful Degradation** — Handle uncertainty without collapse

### Lineage

- **Parent:** jk-gordo-workshop hub
- **Root:** project-gordo (T0)
- **Onboarded:** 2026-06-13
- **Searchable tag:** project-gordo-umbrella

<!-- Last reviewed: 2026-07-08 02:39 AEST by Gordo -->
