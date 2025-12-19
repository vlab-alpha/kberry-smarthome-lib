# BAOS Connection Ablauf & Struktur

## 1. Grundaufbau
- **Writer**: sendet Reset, DataFrame, ACK
- **Reader**: empfängt DataFrame, ACK, Indications
- **Connection**: orchestriert alles (synchronized), verarbeitet Listener

---

## 2. Ablauf für `write(DataPoint)`

**Key Points:**
- `writeLock` blockiert parallel laufende Reads/Writes
- ACK wird immer vom BAOS erwartet, sonst Timeout
- Response prüft Statuscode

---

## 3. Ablauf für `read(DataPoint)`
**Key Points:**
- Ein Request liefert **immer nur einen DataPoint**
- `anyProgress()` → prüft State-Flag, ob noch weitere Frames kommen
- Retry-Limit schützt vor Endlosschleifen

---

## 4. Observer für Indications
- Läuft im eigenen Thread (`indicatorLoop`)
- Listener werden nur aufgerufen, wenn registriert (`Optional.ofNullable(...)`)
- Frames kommen **asynchron** vom ObjectServer

---

## 5. Reset-Mechanismus
- Wird verwendet:
    - zu Beginn (`connect()`)
    - bei Error / inProgress Retry zu viele Male

---

## 6. Übersicht der DataPoint States

| Bit(s) | Bedeutung                                | Methoden / Verarbeitung |
|---------|-----------------------------------------|------------------------|
| 0-1     | Transmission Status (OK, ERROR, ...)    | `anyProgress()`, `isOk()`, `isError()` |
| 2       | Read/Write Request                       | `isReadRequestRequired()`, `isWriteRequestRequired()` |
| 3       | Value Updated                            | `isValueUpdated()`, `isValueNotUpdated()` |
| 4       | Already Received / Unknown Object        | `isAlreadyReceived()`, `isUnknown()` |

- mehrere Flags können **gleichzeitig gesetzt** sein → Liste von `State`
- `anyProgress()` prüft Transmission Status (0b10 oder 0b11)

---

## 7. DataFlow Zusammenfassung
- Alles synchronisiert mit `writeLock`
- Alle DataPoints → `DataPoint` Objekt mit Payload + State
- Retry & Reset bei InProgress oder Fehler