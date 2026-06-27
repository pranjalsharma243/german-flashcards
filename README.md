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

Start backend (copy `.env.example` to `.env` and fill in secrets first):

```bash
cd backend
cp .env.example .env   # edit with your secrets
set -a && source .env && set +a
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

## Google Sign-In Setup

To enable "Continue with Google" on the login screen:

### 1. Create OAuth 2.0 credentials in Google Cloud Console

1. Go to [console.cloud.google.com](https://console.cloud.google.com/) → **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth client ID**
3. Application type: **Web application**
4. Add **Authorized redirect URIs**:
   - Development: `http://localhost:8080/login/oauth2/code/google`
   - Production: `https://your-backend-domain.com/login/oauth2/code/google`
5. Copy the **Client ID** and **Client Secret**

### 2. Set environment variables in `backend/.env`

```env
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret
# Backend callback URI (must match what you registered in Google Cloud Console)
GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
# Frontend URL to redirect to after successful sign-in
FRONTEND_URL=http://localhost:5173
```

### 3. OAuth2 flow

1. User clicks **Continue with Google** on the login screen
2. Browser redirects to `/oauth2/authorization/google` (proxied to backend in dev)
3. Backend redirects to Google's consent screen
4. After consent, Google redirects to `GOOGLE_REDIRECT_URI` on the backend
5. Backend finds or creates the user by Google email, issues a JWT
6. Backend redirects browser to `FRONTEND_URL?token=...&username=...&role=...`
7. Frontend captures the URL params, stores the token, and enters the app

### Account linking

If a user with the same email already registered via email/password, their account is linked automatically on first Google sign-in (no duplicate accounts created).
