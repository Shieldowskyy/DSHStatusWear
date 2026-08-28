# DSH Status — aplikacja na Wear OS

Natywna aplikacja Wear OS (Kotlin + Jetpack Compose for Wear) pokazująca stan usług
z **https://status.dsh.yt/status/dsh** (instancja Uptime Kuma).

## Co robi

- Ekran główny: duży wskaźnik ogólnego stanu (zielony = wszystko OK, czerwony = awaria)
  + tytuł strony statusu (dotknięcie wymusza natychmiastowe odświeżenie).
- **Aktywne incydenty**: wyróżnione karty z powiadomieniami o awariach/pracach (tytuł, data, opis, możliwość rozwinięcia/zwinięcia, ikona przypięcia).
- **Podział na kategorie**: usługi są pogrupowane w sekcje dokładnie tak jak na stronie statusowej (np. *Grupa usług*, *Serwery Gier*, *Serwery Usług Publicznych*, *Strony statyczne* itp.).
- **Monitory**: nazwa, kolorowy wskaźnik stanu, uptime 24h lub treść błędu w przypadku awarii.
- **Stopka z akcjami**: na dole listy znajduje się wskaźnik czasu ostatniej aktualizacji (`Zaktualizowano: HH:mm:ss`), przycisk `Odśwież` oraz przycisk informacji `ℹ` (z oknem informacyjnym o twórcy i narzędziach AI).
- **Auto-odświeżanie**: cykliczne odświeżanie danych w tle co 60 sekund z automatycznym resetem timera przy manualnym odświeżeniu.
- Dane pobierane bezpośrednio z publicznego API Uptime Kuma:
  - `GET /api/status-page/dsh` — konfiguracja, incydenty i grupy monitorów
  - `GET /api/status-page/heartbeat/dsh` — ostatni status + uptime 24h

## Wymagania

- **Android Studio** (Koala lub nowszy) — https://developer.android.com/studio
- Zegarek z **Wear OS 3+** (min. API 30) lub emulator Wear OS w Android Studio
- Zegarek i telefon w tej samej sieci Wi-Fi (do debugowania bezprzewodowego) —
  albo po prostu użyj emulatora, jeśli chcesz to najpierw przetestować

## Jak zbudować i wgrać na zegarek

1. Otwórz Android Studio → **Open** → wskaż ten folder (`DshStatusWear`).
2. Poczekaj aż Gradle zsynchronizuje projekt (pobierze zależności — potrzebny internet).
3. Podłącz zegarek:
   - Na zegarku: **Ustawienia → Deweloper** (jeśli nie widać, wejdź w
     Ustawienia → System → O zegarku → dotknij 7× "Numer kompilacji", żeby odblokować opcje deweloperskie)
     → włącz **Debugowanie ADB** i **Debugowanie przez Wi-Fi**.
   - Na zegarku pojawi się adres IP i port (np. `192.168.1.50:5555`).
   - Na komputerze w terminalu: `adb connect 192.168.1.50:5555`
4. W Android Studio wybierz swój zegarek jako urządzenie docelowe (górny pasek) i kliknij **Run ▶**.
5. Aplikacja "DSH Status" zainstaluje się i uruchomi na zegarku.

Alternatywnie: **Build → Build Bundle(s) / APK(s) → Build APK(s)**, znajdziesz gotowy
plik `.apk` w `app/build/outputs/apk/debug/` i zainstalujesz go ręcznie przez `adb install`.

## Struktura projektu

```
app/src/main/java/yt/dsh/statuswear/
├── MainActivity.kt              # punkt wejścia
├── data/
│   ├── Models.kt                 # modele JSON + model widoku
│   └── StatusRepository.kt       # pobieranie danych z API (OkHttp + kotlinx.serialization)
└── ui/
    ├── StatusViewModel.kt        # stan + auto-refresh co 60s
    ├── StatusScreen.kt           # ekran Compose (nagłówek + lista)
    └── theme/                    # kolory i motyw Material dla Wear
```

## Dostosowanie

- **Interwał odświeżania**: zmień `AUTO_REFRESH_INTERVAL_MS` w `StatusViewModel.kt`.
- **Inna strona statusu / slug**: zmień `SLUG` w `StatusRepository.kt`
  (np. jeśli kiedyś zmienisz adres na `status.dsh.yt/status/inny-slug`).
- **Kolory**: `ui/theme/Color.kt`.

## Uwaga o self-hostowanej instancji

Jeśli `status.dsh.yt` kiedyś zacznie wymagać logowania (obecnie strona jest publiczna),
API przestanie zwracać dane bez tokenu — trzeba by dodać obsługę autoryzacji w
`StatusRepository.kt`.
