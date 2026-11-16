# MVP Command Contract

The command router uses explicit, testable intents rather than executing arbitrary AI-generated code.

| User phrase examples | Intent | MVP behavior |
|---|---|---|
| "PK, WhatsApp kholo" | OPEN_APP(whatsapp) | Launch installed WhatsApp app |
| "WhatsApp par message bhejo hello" / "व्हाट्सएप पर मैसेज भेजो नमस्ते" | COMPOSE_WHATSAPP | Prepare text in WhatsApp share flow; user selects chat and sends manually |
| "YouTube kholo" | OPEN_APP(youtube) | Launch YouTube if installed |
| "Chrome kholo" | OPEN_APP(chrome) | Launch Chrome if installed |
| "Settings kholo" | OPEN_SETTINGS | Open Android Settings |\n| "Wi-Fi settings kholo" / "wifi setting kholo" / "वाईफाई सेटिंग खोलो" | OPEN_WIFI_SETTINGS | Open the Android Wi-Fi settings screen; the user changes the setting |\n| "Bluetooth settings kholo" / "bluetooth setting kholo" / "ब्लूटूथ सेटिंग खोलो" | OPEN_BLUETOOTH_SETTINGS | Open the Android Bluetooth settings screen; the user changes the setting |
| "Rahul ko call karo" | DIAL_CONTACT(Rahul) | Rank exact, prefix, and partial matches; dial unique result or ask user to choose |
| "9876543210 par call karo" | DIAL_NUMBER(number) | Open dialer for user confirmation |
| "SMS 9876543210" | COMPOSE_SMS(number) | Open SMS composer; user sends manually |
| "Rahul ko SMS bhejo" / "Papa ko message bhejo" / "Amit ko SMS karo" | COMPOSE_SMS(contact) | Resolve contact safely and open composer |
| "Alarm kholo" / "अलार्म खोलो" | OPEN_ALARMS | Try Android Clock alarm intents, then supported Clock app launchers (including MIUI Clock); the user selects/edits alarms in Clock |
| "Time kya hua" | INFO_TIME | Speak current device time offline |
| "Aaj ki date batao" | INFO_DATE | Speak current device date offline |
| "Battery kitni hai" | INFO_BATTERY | Read battery percentage offline |
| "Maps kholo" | OPEN_MAPS | Open a Maps handler |
| "gaana chalao" / "गाना चलाओ" | MEDIA_PLAY | Resume the active media session where supported |
| "music roko" / "गाना रोक दो" | MEDIA_PAUSE | Pause the active media session where supported |
| "agla gaana" / "अगला गाना" | MEDIA_NEXT | Skip to the next active-session track |
| "pichla gana" / "पिछला गाना" | MEDIA_PREVIOUS | Return to the previous active-session track |
| "volume badhao" / "आवाज बढ़ा दो" | VOLUME_UP | Increase system music-stream volume |
| "awaz kam karo" / "आवाज कम करो" | VOLUME_DOWN | Decrease system music-stream volume |
| "awaz band" / "आवाज बंद" | VOLUME_MUTE | Mute system music-stream volume where supported |
| "Help" / "मदद" | HELP | Explain supported app, communication, media, volume, accessibility, alarm/timer, reminder and device-info actions, including permission/app-support limits |

## Wake-word behavior\n\n- Canonical and only configured wake phrase: "PK". Do not substitute "Hey Assistant" or "Hey PersonalAI".

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
- Recognize supported English, Devanagari Hindi, and explicitly tested Romanized-Hinglish aliases (including media, volume, navigation, time, and date intents). Romanized-Hinglish coverage is intentionally finite, not general speech understanding.
- Strip only known filler words; do not remove contact-name tokens unnecessarily.
- Ask one focused question when a required value is missing.
- Never send SMS or WhatsApp messages silently.
- Provide visible and spoken outcomes where supported.


## Cross-app navigation (opt-in)

After enabling **PK Accessibility Service** in Android Settings, these commands can act on the currently active app:

| English | Hindi | Action |
|---|---|---|
| Scroll up / Page up | ऊपर स्क्रॉल | Scroll the first available scrollable node backward |
| Scroll down / Page down | नीचे स्क्रॉल | Scroll the first available scrollable node forward |
| Go back | पीछे जाओ / वापस जाओ | Android Back global action |
| Go home / Home screen | होम स्क्रीन / होम जाओ | Android Home global action |

The service is disabled until the user explicitly enables it in Android Accessibility Settings. It does not read node text or message content and does not send messages. Scrolling depends on the active app exposing a scrollable accessibility node; some apps or screens may not support it. Accessibility access is powerful, so enable it only if you understand and accept this scope.
