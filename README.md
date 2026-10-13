# Flip Scanner

AI-powered Android app for thrift resellers: scan any item, get its resale
value, auto-generate marketplace listings, and track profit.

> Built part by part. This repo holds the app source **and** the private
> `pc-inbox/` command channel used by the PC runner — do not edit `pc-inbox/`
> by hand while the runner is active.

## Parts

- **Part 1 (this commit):** project skeleton — Android app shell (Kotlin +
  Jetpack Compose, Hilt, Navigation, dark-first emerald theme, 5 screens),
  PHP backend skeleton (router, config, DB layer, health check, full schema).
- **Part 2:** camera scanner UI, result screen (value range, comparables,
  "Worth flipping?" calculator), listing generator UI.
- **Part 3:** backend API — auth, scan upload + AI identification, eBay
  Browse API comparables, listing generation, billing verify + RTDN.
- **Part 4:** inventory tracker, profit dashboard, scan history, paywall,
  Google Sign-In, settings/account.

## Layout

- `android/` — Kotlin + Jetpack Compose app (`com.flipscanner.app`)
- `backend/` — PHP 8 REST API + MySQL schema
- `pc-inbox/` — private runner channel (do not touch)

## Working with Antigravity

The app folder is synced to `Desktop\Website\flip-scanner-app` on the PC.
Open that folder in Antigravity to browse/edit. After each new part is
pushed, the folder updates with one `git pull` (or ask Muse to sync it).

## Prerequisites (for building, needed from Part 2 on)

- Android Studio (bundles JDK + Android SDK) — for the `android/` app
- PHP 8.1+ and MySQL 8 — for the `backend/` API
