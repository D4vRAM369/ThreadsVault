# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [Unreleased]

### Added
- Placeholder section for upcoming public release notes and updates.

## [1.0.0] - 2026-02-23

### Added
- Core vault flow to save and organize Threads links.
- Categories, favorites, and quick notes.
- OCR extraction from post images.
- Export to CSV and PDF.
- Backup and restore (JSON/CSV), including SAF auto-backup.
- Theme modes: light, dark, and system.
- In-app About Dev screen with localized content (ES/EN).
- Clickable #hashtags in post content and notes with instant filter chips.
- Category-colored filter chips with dynamic luminance-based text contrast.
- Accent-colored empty state matching the selected category color.
- "How to use" BottomSheet tutorial accessible from the empty state and Settings.
- 4-page onboarding flow shown once on first launch.

### Changed
- Favorites filter moved to top bar heart action for faster access.
- Empty state CTA cleaned to avoid duplicate manual add actions.
- Hashtag filter resets automatically on category or favorites context change.

### Fixed
- URLs inside saved post content are now clickable hyperlinks in post detail and vault previews.
- Hashtag filter now matches all source fields: content, notes, and labels.
- Accent bar on post cards respects card clip boundary (no more square corners).
- Extracted post text uses explicit onSurface color for legibility in detail view.
