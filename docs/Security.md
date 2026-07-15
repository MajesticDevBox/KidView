# Security

## Security Goals

KidView is a private family-use app, but it still needs deliberate security boundaries because it is designed to:

- restrict what a child can watch
- reduce exits from the safe viewing environment
- protect parent-only controls
- avoid collecting unnecessary data

## Threat Model

Primary risks:

- a child exits child mode without parent approval
- a child changes approved content
- local device data exposes parent PIN material
- signing assets or keys are accidentally committed
- future metadata integrations over-collect personal information

Non-goals for the current private family build:

- enterprise-grade remote device management
- public multi-tenant account security
- internet-facing user administration

## Current Platform Security Posture

### Android

Current and planned protections:

- parent PIN gate before app use
- immersive child-mode flow
- screen pinning or stronger dedicated-device mode later
- DataStore-backed local state

Future strengthening:

- store secrets with stronger Android keystore-backed protection where useful
- complete the parent-authenticated child-mode exit flow
- harden dedicated-device mode for special family devices

### iOS

Current and planned protections:

- parent PIN gate
- Guided Access awareness
- `WKWebView`-based playback shell

Future strengthening:

- move PIN storage to Keychain
- replace temporary development exit controls
- improve Guided Access onboarding and session-state handling

## Parent PIN Handling

Rules:

- never store raw PIN values
- never commit test PINs into source
- never log entered PINs

Current state:

- Android hashes the PIN
- iOS scaffold hashes the PIN but still stores material in `UserDefaults` temporarily

Target state:

- Android uses secure local storage with platform protections where possible
- iOS stores PIN material in Keychain

## Device Lock Strategy

### Android

Family-phone mode:

- immersive mode
- screen pinning guidance

Dedicated-device mode:

- evaluate Lock Task Mode or device-owner style setup later

### iOS

Family-phone mode:

- parent manually enables Guided Access

Dedicated-device mode:

- only consider supervised-device paths if a real family need appears

## Content Safety

KidView should only play:

- parent-approved video IDs
- parent-approved playlist IDs

Recommended controls:

- allowlist-only content model
- no child-facing search
- no automatic expansion into arbitrary recommendations
- validate pasted URLs before saving

If metadata import is added later:

- verify embeddability
- respect made-for-kids related metadata where available

## Privacy

Privacy principles:

- local-first by default
- no ads
- no analytics SDKs by default
- no child behavior profiling
- no unnecessary network transmission of viewing history

If optional sync is ever introduced:

- make it opt-in
- document exactly what syncs
- keep the default experience usable offline

## Repository Security

Never commit:

- keystores
- `.jks` files
- provisioning profiles
- API secrets
- local environment files with secrets

Current repository rule:

- signing materials such as `keys/` are intentionally excluded from migration and version control

## Build And Release Guidance

Android:

- use release signing outside the repo
- keep local signing config off version control

iOS:

- keep signing managed through Xcode and Apple developer credentials
- do not store sensitive signing artifacts in the repo

## Logging And Diagnostics

Allowed:

- non-sensitive app state diagnostics
- generic playback and parser failures

Avoid:

- PIN values
- full sensitive identifiers if later account features exist
- detailed child usage logs unless intentionally added for family use

## Recommended Near-Term Improvements

1. Move the iOS parent PIN store to Keychain.
2. Complete parent-authenticated exit flows on both platforms.
3. Review Android local storage for any sensitive values beyond the PIN hash.
4. Keep future YouTube and metadata keys outside the repository.
5. Add a lightweight security checklist to release prep.
