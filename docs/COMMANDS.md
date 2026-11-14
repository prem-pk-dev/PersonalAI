# MVP Command Contract

The command router should use explicit, testable intents rather than executing arbitrary AI-generated code.

| User phrase examples | Intent | MVP behavior |
|---|---|---|
| "Hey Assistant, WhatsApp kholo" | OPEN_APP(whatsapp) | Launch installed WhatsApp app |
| "YouTube kholo" | OPEN_APP(youtube) | Launch YouTube if installed |
| "Chrome kholo" | OPEN_APP(chrome) | Launch Chrome if installed |
| "Settings kholo" | OPEN_SETTINGS | Open Android Settings |
| "Rahul ko call karo" | DIAL_CONTACT(Rahul) | Find contact and open dialer for confirmation |
| "9876543210 par call karo" | DIAL_NUMBER(number) | Open dialer for confirmation |
| "SMS 9876543210" | COMPOSE_SMS(number) | Open SMS composer; user sends manually |
| "Alarm kholo" | OPEN_ALARMS | Open alarm screen |

## Action safety levels

- Level 0 — Read-only/local: parse commands, show history, explain help.
- Level 1 — Open app/screen: launch apps and Android settings.
- Level 2 — Prepare communication: resolve contact, open dialer, prepare SMS/share sheet. User confirms final action.
- Level 3 — High impact: call disconnect, changing security-sensitive settings, payments, or sending messages. Must use supported Android roles/APIs and explicit user confirmation. Never fake support with accessibility hacks.

## Parser rules

- Normalize case and extra whitespace.
- Recognize English and Hindi command variants.
- Strip only known command filler words; do not strip tokens from the contact name unnecessarily.
- Prefer exact contact match, then unique partial match.
- If multiple contacts match, ask the user to choose.
- If a required value is missing, ask one focused question.
- Never send an SMS or WhatsApp message silently.
- Always provide a spoken and visible outcome.
