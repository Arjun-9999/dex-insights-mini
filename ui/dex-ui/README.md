# Dex Insights UI

Angular frontend for the Dex Insights Mini project.

## Features

- Store list with pagination, filters (`brand`, `status`), and sort by offline pumps.
- Store detail view.
- Insights overview view.
- Chat view that calls `/v1/chat` and displays citations.

## Prerequisites

- Node.js 20+
- npm 10+
- Backend API running at `http://localhost:8080`

## Run locally

From `ui/dex-ui`:

```bash
npm install
npx ng serve --proxy-config proxy.conf.json
```

Then open `http://localhost:4200`.

## Build

```bash
npm run build
```

## Unit tests

```bash
npm run test -- --watch=false --browsers=ChromeHeadless
```

## Project structure

- `src/app/stores`: store listing + filters/sort/pagination
- `src/app/store-detail`: single store details
- `src/app/insights`: insights overview page
- `src/app/chat`: chat UI and citations rendering
