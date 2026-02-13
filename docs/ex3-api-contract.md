# Enigma Exercise 3 API Contract Lock

Last updated: 2026-02-13

## Source of truth used in this repository

This contract is locked to:
- The Exercise 3 requirements in `docs/Enigma - 3.0 V3.pdf` (controller groups and base URL).
- The implemented server in this repository.

Note:
- The assignment mentions Postman/OpenAPI files, but those files are not present in this repository.
- If you add the official Postman/OpenAPI files later, update only controller signatures/DTO shapes (service internals can stay as-is).

## Base URL

- `http://localhost:8080/enigma`

## Route groups

- `/session`
- `/load`
- `/config`
- `/process`
- `/history`

## Endpoints

### `POST /load`
Load/register a machine by XML file path.

Request:
```json
{
  "xmlPath": "/absolute/or/relative/path/to/machine.xml"
}
```

Response 200:
```json
{
  "machineName": "sanity",
  "xmlPath": "/absolute/or/relative/path/to/machine.xml",
  "loadedAt": "2026-02-13T11:12:13.456Z"
}
```

Errors:
- `400` invalid XML path/schema/validation
- `409` duplicate machine name

### `GET /load`
List loaded machines.

Response 200:
```json
[
  {
    "machineName": "sanity",
    "xmlPath": "/path/to/sanity.xml",
    "loadedAt": "2026-02-13T11:12:13.456Z"
  }
]
```

### `POST /session/open`
Open a session for a machine name.

Request:
```json
{
  "machineName": "sanity"
}
```

Response 200:
```json
{
  "sessionId": "0f6dd133-5c2e-4ffe-83de-3f84cb68ecef",
  "machineName": "sanity",
  "status": "OPEN",
  "openedAt": "2026-02-13T11:13:14.456Z",
  "closedAt": null
}
```

Errors:
- `404` machine not found

### `DELETE /session/{sessionId}`
Close session.

Response 200:
```json
{
  "sessionId": "0f6dd133-5c2e-4ffe-83de-3f84cb68ecef",
  "machineName": "sanity",
  "status": "CLOSED",
  "openedAt": "2026-02-13T11:13:14.456Z",
  "closedAt": "2026-02-13T11:30:00.000Z"
}
```

Errors:
- `404` session not found
- `409` already closed

### `GET /session/{sessionId}`
Get session details.

### `GET /session`
List all sessions.

### `POST /config/manual`
Configure machine manually for a session.

Request:
```json
{
  "sessionId": "0f6dd133-5c2e-4ffe-83de-3f84cb68ecef",
  "rotorIds": [1, 2, 3],
  "positions": "ABC",
  "reflectorId": "I",
  "plugboard": ""
}
```

Response 200:
```json
{
  "rotorsDefined": 5,
  "reflectorsDefined": 2,
  "stringsProcessed": 0,
  "originalCode": "<1,2,3><A(1),B(2),C(3)><I>",
  "currentCode": "<1,2,3><A(1),B(2),C(3)><I>"
}
```

Errors:
- `400` invalid config
- `404` session not found
- `409` closed session

### `POST /config/random`
Apply random config for a session.

Request:
```json
{
  "sessionId": "0f6dd133-5c2e-4ffe-83de-3f84cb68ecef"
}
```

### `POST /config/reset`
Reset session machine to original configured code.

Request:
```json
{
  "sessionId": "0f6dd133-5c2e-4ffe-83de-3f84cb68ecef"
}
```

### `POST /process`
Process input text for a session.

Request:
```json
{
  "sessionId": "0f6dd133-5c2e-4ffe-83de-3f84cb68ecef",
  "input": "HELLO"
}
```

Response 200:
```json
{
  "sessionId": "0f6dd133-5c2e-4ffe-83de-3f84cb68ecef",
  "machineName": "sanity",
  "input": "HELLO",
  "output": "XMCKL",
  "durationNanos": 123456,
  "machineState": {
    "rotorsDefined": 5,
    "reflectorsDefined": 2,
    "stringsProcessed": 1,
    "originalCode": "<1,2,3><A(1),B(2),C(3)><I>",
    "currentCode": "<1,2,3><A(2),B(2),C(3)><I>"
  }
}
```

Errors:
- `400` invalid input or machine not configured
- `404` session not found
- `409` closed session

### `GET /history/session/{sessionId}`
Get configuration and process history for one session.

### `GET /history/machine/{machineName}`
Get configuration and process history for all sessions of one machine name.

Response 200 shape for history endpoints:
```json
{
  "scope": "SESSION",
  "sessionId": "0f6dd133-5c2e-4ffe-83de-3f84cb68ecef",
  "machineName": "sanity",
  "configurationEvents": [
    {
      "id": 1,
      "sessionId": "0f6dd133-5c2e-4ffe-83de-3f84cb68ecef",
      "machineName": "sanity",
      "action": "MANUAL_CONFIG",
      "payload": "<1,2,3><ABC><I>",
      "createdAt": "2026-02-13T11:15:00.000Z"
    }
  ],
  "processRecords": [
    {
      "id": 1,
      "sessionId": "0f6dd133-5c2e-4ffe-83de-3f84cb68ecef",
      "machineName": "sanity",
      "inputText": "HELLO",
      "outputText": "XMCKL",
      "durationNanos": 123456,
      "processedAt": "2026-02-13T11:16:00.000Z"
    }
  ]
}
```

## Error model

All errors are returned as:

```json
{
  "timestamp": "2026-02-13T11:20:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation details",
  "path": "/enigma/config/manual"
}
```

Status mapping:
- `400` validation/config/input errors
- `404` missing machine/session
- `409` duplicates and illegal session state transitions
- `500` unexpected server errors
