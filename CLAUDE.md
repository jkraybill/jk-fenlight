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

- **Trakt API (2026 overhaul):** watched/collection endpoints force pagination at
  100/page, and the `seasons` array requires `extended=progress` — `extended=full`
  does not bring it back. Root-caused S97 against the live API; **fixed in this
  repo at S155** (`get_watched_pages` in `apis/trakt_api.py`), so the patch is no
  longer device-only. Verified: before the fix the addon collected 0 episodes and
  the first 100 movies of any account, and *wrote* the empty episode list —
  `set_bulk_tvshow_watched` deletes the whole `db_type` before inserting, so it
  emptied the indicator table rather than merely failing to fill it.
  `tests/test_trakt_indicators.py` holds the RED/GREEN control against f22a07e.
- **Version is parked at 2.2.99** so a stray upstream 2.2.x cannot sort above the
  fork and overwrite the fixes. Do not point "Manage Addon Updates" at
  thejason40's repo — an update from it still would.

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


<!-- Last reviewed: 2026-08-11 23:52 AEST by Gordo -->
