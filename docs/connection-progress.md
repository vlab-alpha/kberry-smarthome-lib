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



FAQ:
Der Grund, warum dein Python-Code ein ACK (E5) erhält, dein Java-Code jedoch nicht, liegt an der Parität (Parity).
In deinem Python-Skript ist parity=serial.PARITY_EVEN gesetzt. Das KNX FT1.2 Protokoll (das du mit der Sequenz 68 0C 0C 68... ansprichst) erfordert zwingend Even Parity. 
In deinem Java-Code nutzt du aktuell diesen stty-Befehl:
stty -F ... cs8 -cstopb -parenb ... 
Das -parenb deaktiviert die Parität komplett (Parity None). Da der KNX-Adapter ein Signal mit Paritätsbit erwartet, ignoriert er deine Java-Nachricht einfach, weil sie für ihn wie „Rauschen“ oder ein fehlerhafter Datenstrom aussieht. 


Ein Paritätsbit ist ein zusätzliches Bit, das an eine Folge von Datenbits (meist ein Byte) angehängt wird, um eine einfache Fehlererkennung bei der Datenübertragung zu ermöglichen. 
Es dient dazu, die Gesamtzahl der „1“-Bits in einem übertragenen Datenwort entweder auf eine gerade oder eine ungerade Zahl zu ergänzen. 