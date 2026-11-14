# MVP Command Contract

The command router uses explicit, testable intents rather than executing arbitrary AI-generated code.

| User phrase examples | Intent | MVP behavior |
|---|---|---|
| "PK, WhatsApp kholo" | OPEN_APP(whatsapp) | Launch installed WhatsApp app |
| "YouTube kholo" | OPEN_APP(youtube) | Launch YouTube if installed |
| "Chrome kholo" | OPEN_APP(chrome) | Launch Chrome if installed |
| "Settings kholo" | OPEN_SETTINGS | Open Android Settings |
| "Rahul ko call karo" | DIAL_CONTACT(Rahul) | Rank exact, prefix, and partial matches; dial unique result or ask user to choose |
| "9876543210 par call karo" | DIAL_NUMBER(number) | Open dialer for user confirmation |
| "SMS 9876543210" | COMPOSE_SMS(number) | Open SMS composer; user sends manually |
| "Rahul ko SMS bhejo" / "Papa ko message bhejo" / "Amit ko SMS karo" | COMPOSE_SMS(contact) | Resolve contact safely and open composer |
| "Alarm kholo" | OPEN_ALARMS | Open the clock/alarm screen where supported |

## Wake-word behavior

- Say "PK" (Hindi speech transcript forms "पीके" or "पी के") and then speak the command.
- A combined utterance such as "PK YouTube kholo" is also supported.
- Wake-word recognition is active only during a user-started listening session while the app is open.
- ACTION_ASSIST is declared as an app entry point, but default-assistant role integration is deferred until a valid Android RecognitionService provider is integrated.

## Contact resolution and communication safety

1. Require Contacts permission before querying the phone's contact provider.
2. Rank exact display-name matches before prefix and substring matches.
3. Deduplicate equivalent phone-number formatting.
4. If multiple best-ranked matches remain, show a chooser.
5. Open the system dialer; never place a call silently.
6. Open the SMS composer; the user reviews and sends the message manually.

## Action safety levels

- Level 0 — Read-only/local: parse commands, show history, explain help.
- Level 1 — Open app/screen: launch apps and Android settings.
- Level 2 — Prepare communication: resolve contact, open dialer, prepare SMS/share sheet. User confirms final action.
- Level 3 — High impact: call disconnect, security-sensitive settings, payments, or sending messages. Requires supported Android roles/APIs and explicit confirmation; never fake support with accessibility hacks.

## Parser rules

- Normalize case and extra whitespace for intent matching.
- Preserve the original recognized transcript when returning text after the wake phrase.
- Recognize supported English and Hindi command variants.
- Strip only known filler words; do not remove contact-name tokens unnecessarily.
- Ask one focused question when a required value is missing.
- Never send SMS or WhatsApp messages silently.
- Provide visible and spoken outcomes where supported.
