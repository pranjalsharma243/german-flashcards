# German B1.1 Flashcards

Modern React + Spring Boot flashcard app for chapter-wise German vocabulary. The app stores users, vocabulary, and progress in Postgres.

## Structure

- `backend/` Spring Boot API
- `backend/src/main/resources/data/chapters.json` seed vocabulary
- `docker-compose.yml` local Postgres with persistent volume
- `frontend/` React + Vite UI

## Run

Start Postgres:

```bash
docker compose up -d postgres
```

Start backend:

```bash
cd backend
mvn spring-boot:run
```

Start frontend in another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## API

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/chapters`
- `GET /api/chapters/{chapterId}`
- `GET /api/progress/{chapterId}`
- `PUT /api/progress/{chapterId}`

To add more chapters, append another chapter object to `backend/src/main/resources/data/chapters.json` and start with an empty Postgres volume, or insert the rows directly into Postgres.
