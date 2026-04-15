# 🚀 Quick Start Guide

## Just Want to Run It? (30 seconds)

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Then open: http://localhost:8080

**That's it!** No GCP setup, no credentials, no secrets needed.

---

## Want to Use Real API Keys Locally?

### Option 1: Environment Variable (5 seconds)
```bash
export STADIUM_API_KEY="your-key-here"
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Option 2: .env File (30 seconds)
```bash
# Create .env file
cat > .env << EOF
STADIUM_API_KEY=your-key-here
SPRING_PROFILES_ACTIVE=local
EOF

# Load and run
export $(cat .env | xargs)
./gradlew bootRun
```

---

## Want to Use GCP Secret Manager?

### Local Testing with GCP (5 minutes)

```bash
# 1. Download service account key from GCP Console
#    IAM & Admin > Service Accounts > Create Key

# 2. Set environment variable
export GOOGLE_APPLICATION_CREDENTIALS=~/stadiumflow-key.json

# 3. Create secret in GCP
echo -n "your-api-key" | gcloud secrets create STADIUM_API_KEY --data-file=-

# 4. Run with dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Production Deployment (2 minutes)

```bash
# 1. Ensure secret exists in GCP
gcloud secrets create STADIUM_API_KEY --data-file=- <<< "your-api-key"

# 2. Deploy to Cloud Run
gcloud run deploy stadiumflow \
    --source . \
    --region us-central1 \
    --set-env-vars="SPRING_PROFILES_ACTIVE=prod"
```

---

## Understanding Profiles

| Profile | When to Use | Secret Source |
|---------|-------------|---------------|
| `local` | Local development, no GCP | Environment variable or hardcoded |
| `dev` | Testing with GCP locally | GCP Secret Manager |
| `prod` | Production on Cloud Run | GCP Secret Manager |

---

## Common Commands

### Run Tests
```bash
./gradlew test
```

### Build JAR
```bash
./gradlew build
```

### Run with Different Profile
```bash
./gradlew bootRun --args='--spring.profiles.active=PROFILE_NAME'
```

### Check Application Health
```bash
curl http://localhost:8080/api/iot/data
```

---

## Configuration Files Reference

- **`application.properties`** - Base configuration (always loaded)
- **`application-local.properties`** - Local dev (no GCP)
- **`application-prod.properties`** - Production (with Secret Manager)
- **`.env`** - Environment variables (create from `.env.example`)

---

## Troubleshooting

### Port 8080 already in use?
```bash
# Kill existing process
lsof -ti:8080 | xargs kill -9
```

### Secret Manager errors?
```bash
# Use local profile instead
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Want to see detailed logs?
```bash
./gradlew bootRun --args='--spring.profiles.active=local --debug'
```

---

## Next Steps

1. ✅ Application is running!
2. 📖 Read `SECRET_MANAGEMENT_SUMMARY.md` for architecture overview
3. 🔧 Read `CONFIGURATION_GUIDE.md` for detailed configuration options  
4. ☁️ Read `GCP_SETUP_GUIDE.md` for production deployment

---

## API Endpoints

- **GET** `/` - Web UI
- **GET** `/api/iot/data` - Get all zones data
- **GET** `/api/iot/stream` - Real-time SSE stream
- **POST** `/api/ai/ask` - Ask AI assistant
- **POST** `/api/orders` - Place order

---

## Need Help?

Check these files:
- `SECRET_MANAGEMENT_SUMMARY.md` - Overview of what changed
- `CONFIGURATION_GUIDE.md` - Detailed configuration guide
- `GCP_SETUP_GUIDE.md` - GCP setup instructions
