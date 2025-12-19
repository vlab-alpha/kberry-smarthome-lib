### Einführung

Die Kommunikation zwischen Client und Server (Java-Programm ↔ kBerry BAOS Modul) basiert auf dem ObjectServer-Protokoll.
- Client: das Programm, das du schreibst.
- Server: das BAOS-Modul auf dem kBerry.
Das Protokoll besteht aus Requests (Anfragen), die vom Client geschickt werden, und Responses (Antworten), die vom Server kommen.

Indications: Das sind asynchrone Meldungen vom Server, die dir sagen, dass sich ein Wert eines Datenpunkts (Datapoint) geändert hat – z. B. ein Licht wurde per Taster eingeschaltet.
Einfach gesagt: Client fragt an, Server antwortet; Server kann auch von selbst Änderungen melden (Indications).

### Transport

BAOS Core Protocol selbst definiert nicht, wie die Bits über die Leitung laufen.
- Je nach Interface (Serial, USB, IP) gibt es verschiedene Frame-Formate (FT1.2 bei Serial, USB-Protokoll bei USB, cEMI bei IP).
- Der Core Protocol Layer ist immer gleich – nur die Verpackung über den Transport unterscheidet sich.

### Services

- GetServerItem.Req/Res: Fordert Infos über ein Item auf dem Server an (z. B. Geräte, Kanäle)
- GetServerItem.Req/Res: Setzt einen Wert eines Server-Items
- SetServerItem.Req/Res:Der Server teilt dem Client mit, dass ein Server-Item sich geändert hat
- ServerItem.Ind:Liefert Metadaten zu einem Datenpunkt (z. B. Typ, Länge)
- GetDatapointDescription.Req/Res:Liefert Metadaten zu einem Datenpunkt (z. B. Typ, Länge)
- GetDescriptionString.Req/Res:Liefert einen Textstring mit Beschreibung eines Items oder DPs
- GetDatapointValue.Req/Res:Fragt den aktuellen Wert eines Datenpunkts ab
- DatapointValue.Ind:Der Server meldet, dass sich der Wert eines Datenpunkts geändert hat
- SetDatapointValue.Req/Res:Setzt den Wert eines Datenpunkts
- GetParameterByte.Req/Res:Liest Parameterbytes vom Server (z. B. Konfiguration)
- SetParameterByte.Req/Res:Schreibt Parameterbytes auf den Server

# BAOS Core Protocol: GetServerItem.Req

**Beschreibung:**  
`GetServerItem.Req` ist eine Anfrage des Clients an den BAOS-Server, um Informationen über ein oder mehrere Server-Items (z. B. Datenpunkte oder Eigenschaften) abzufragen. Der Server antwortet mit `GetServerItem.Res` und liefert die Werte der angeforderten Items.

---

## Aufbau des Request-Pakets

| Offset | Feld           | Größe | Wert  | Beschreibung |
|--------|----------------|-------|-------|--------------|
| +0     | MainService    | 1 Byte| 0xF0  | Hauptservice-Code für BAOS Datapoint-Service |
| +1     | SubService     | 1 Byte| 0x01  | Subservice-Code = `GetServerItem.Request` |
| +2     | StartItem      | 2 Bytes | ID des ersten Items (LSB/MSB) | Start-ID des Items, das abgefragt wird |
| +4     | NumberOfItems  | 2 Bytes | Anzahl Items | Maximale Anzahl der zurückzugebenden Items |

---

## Beispiel

- **Ziel:** ObjectID 4 abfragen
- **StartItem:** 4
- **NumberOfItems:** 1

### Request-Paket:

- `F0` → MainService
- `01` → SubService = GetServerItem.Req
- `04 00` → StartItem = 4
- `01 00` → NumberOfItems = 1

### Antwort (Beispiel):
- `81` → Response für GetServerItem
- Enthält die Werte der angefragten Items in der Range `[StartItem … StartItem+NumberOfItems-1]`.

# BAOS Host ↔ ObjectServer Kommunikation (FT1.2)

Die Kommunikation zwischen einem Host (z. B. Raspberry Pi) und dem Weinzierl ObjectServer (BAOS) erfolgt in mehreren Schritten über das FT1.2-Protokoll. Jeder Schritt wird durch **Request**, **Acknowledgement** oder **Data** gekennzeichnet.

---

## 1. Reset / Initialisierung

| Sender | Nachricht | Beschreibung |
|--------|-----------|-------------|
| Host → ObjectServer | `Reset Request` | Der Host signalisiert, dass er die Verbindung initialisieren möchte. |
| ObjectServer → Host | `Acknowledgement` | Bestätigung des Reset durch den Server. Der Server ist nun bereit für Daten. |

---

## 2. Daten senden / empfangen

| Schritt | Sender | Nachricht | Beschreibung |
|---------|--------|-----------|-------------|
| 1 | Host → ObjectServer | `Data` | Der Host sendet die eigentlichen Requests (z. B. `GetServerItem.Req`, `SetDatapointValue.Req`). |
| 2 | ObjectServer → Host | `Acknowledgement` | Der ObjectServer bestätigt den Empfang der Daten. |
| 3 | ObjectServer → Host | `Data` | Der Server liefert die eigentliche Antwort oder Indication (z. B. `GetServerItem.Res`, `DatapointValue.Ind`). |
| 4 | Host → ObjectServer | `Acknowledgement` | Der Host bestätigt den Empfang der Antwort. |

---

## 3. Asynchrone Indications

- Der ObjectServer kann jederzeit **DatapointValue.Ind** oder andere Indications senden.
- Jede Indication muss vom Host bestätigt werden.
- Damit werden Zustände auf beiden Seiten synchron gehalten.

---

## 4. Wichtig für Implementierungen

- **Acknowledgements sind Pflicht**: Ohne sie werden Nachrichten vom Server verworfen oder erneut gesendet.
- **FT1.2 Frames**:
    - Startbyte `0x68`
    - Länge (`L`) des Payload + Control + Checksum
    - Kontrollbyte (`C`) z. B. `0x53` für Client → Server
    - Payload: BAOS ObjectServer Nachricht (`0xF0 ...`)
    - Endbyte `0x16`
- **Retries**: Host muss bei Timeouts ggf. die Anfrage erneut senden.
- **GetServerItem / SetDatapointValue**:
    - Request → Server
    - Acknowledge → Host
    - Response / Indication → Server
    - Acknowledge → Host

---

## 5. Beispielablauf

```
Host                  ObjectServer
----                  ------------
Reset Request   ---->
                <----   Acknowledgement
GetServerItem   ---->
                <----   Acknowledgement
                <----   GetServerItem.Res
Acknowledgement ---->
```


# FT1.2 Frame-Typen

Das FT1.2-Protokoll definiert drei Arten von Frames, die für die Kommunikation zwischen Host und BAOS ObjectServer verwendet werden.

---

## 1. Positive Acknowledgement Frame

- **Länge:** 1 Byte
- **Inhalt:** `0xE5`
- **Bedeutung:**  
  Wird gesendet, um den Empfang einer Nachricht zu bestätigen.  
  Wird z. B. nach einem Reset Request oder Datenframe verwendet.

---

## 2. Reset Request / Reset Indication Frame

- **Länge:** 4 Bytes
- **Verwendung:** Initialisierung der Verbindung oder Neustart-Signalisierung

| Typ | Bytes | Beschreibung |
|-----|-------|-------------|
| Reset Request (Reset.Req) | `0x10 0x40 0x40 0x16` | Wird vom Host an den ObjectServer gesendet, um die Verbindung zu initialisieren. |
| Reset Indication (Reset.Ind) | `0x10 0xC0 0xC0 0x16` | Wird vom ObjectServer an den Host gesendet, z. B. nach einem Reset oder Reboot des Servers. |

- **Byte 1 (`0x10`)**: Startbyte für Reset-Frames  
- **Byte 2/3 (`0x40 0x40` bzw. `0xC0 0xC0`)**: Steuer- bzw. Kontrollbytes, die den Frame-Typ definieren  
- **Byte 4 (`0x16`)**: Endbyte für FT1.2-Frame  

---

## 3. Bedeutung

- Reset-Frames sind **immer 4 Bytes lang**.  
- Sie dienen ausschließlich dazu, **die Verbindung zu initialisieren** und keine Nutzdaten zu übertragen.  
- Positive Acknowledgements (`0xE5`) sind nur 1 Byte und bestätigen den Empfang.