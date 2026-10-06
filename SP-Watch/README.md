# SP Watch

Super Productivity tasks on an Amazfit Cheetah 2 Ultra.

## Current state — v0.1

The first working path is intentionally local-first:

`Super Productivity desktop REST API -> SP Watch Bridge -> Zepp Side Service -> Bluetooth -> Cheetah 2 Ultra`

Supported on the watch:

- list pending **Today** tasks (or all pending tasks);
- local task cache on the watch;
- refresh from the watch;
- complete a task with a tap on `✓`;
- start a task with a long press on `✓`;
- pagination in groups of four tasks.

No Super Productivity API token is stored on the watch or in Zepp. The Zepp app only stores a separate bridge token.

## Requirements

- Amazfit Cheetah 2 Ultra / Zepp OS 5.0 (API level 4.3 device; app targets API 4.0 for compatibility);
- Zepp app paired to the watch;
- Node.js 18+ on the computer running Super Productivity;
- Super Productivity desktop app with **Settings -> Misc -> Enable local REST API** enabled.

## 1. Start the bridge

Open `bridge/` and set environment variables from `.env.example`.

PowerShell example:

```powershell
$env:SP_API_TOKEN="YOUR_SP_TOKEN"
$env:WATCH_TOKEN="A_LONG_RANDOM_SECRET"
$env:HOST="0.0.0.0"
$env:PORT="8787"
node .\server.mjs
```

Check locally:

```powershell
curl http://127.0.0.1:8787/health
```

Then find the LAN IP of the computer, for example `192.168.1.50`.

## 2. Configure the Zepp companion settings

In Zepp -> SP Watch settings:

- **Bridge URL**: `http://192.168.1.50:8787`
- **Token del bridge**: the same `WATCH_TOKEN`
- **Lista**: `today` or `all`

The phone must be able to reach that address. If Android/Zepp blocks clear-text HTTP on your setup, expose the bridge through an HTTPS endpoint you control; the bridge works unchanged behind a reverse proxy.

## 3. Build / preview the Zepp app

From `watch/`:

```bash
npm install
npm install -g @zeppos/zeus-cli
zeus preview
```

Scan the QR from Zepp to install/preview on the Cheetah 2 Ultra. For the simulator use `zeus dev`; for a package use `zeus build`.

## Bridge API

- `GET /health`
- `GET /api/tasks?scope=today|all&limit=40`
- `POST /api/tasks/:id/complete`
- `POST /api/tasks/:id/start`

Every `/api/*` route accepts `Authorization: Bearer <WATCH_TOKEN>` when `WATCH_TOKEN` is configured.

## Security notes

The Super Productivity REST token never leaves the computer running the bridge. The bridge forwards only a reduced task model to the watch (`id`, title, project, schedule and time fields). Do not expose port 8787 directly to the public internet. Use LAN/VPN or an authenticated HTTPS reverse proxy.

## Roadmap

1. v0.1 — Today/all list, refresh, complete, start, cache.
2. v0.2 — Projects, Inbox, subtasks and task details.
3. v0.3 — Create task from watch and quick postpone.
4. v0.4 — Mobile-first bridge so the computer is not required.
5. v0.5 — Voice task creation.
