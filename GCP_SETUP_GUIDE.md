# Google Cloud Platform Setup Guide

This guide explains how to configure Google Cloud services for the StadiumFlow application.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Local Development Setup](#local-development-setup)
- [Production Setup with Secret Manager](#production-setup-with-secret-manager)
- [Environment Variables](#environment-variables)
- [Running the Application](#running-the-application)

---

## Prerequisites

1. Google Cloud Platform account
2. Project created: `physicaleventexperience`
3. Billing enabled on the project
4. gcloud CLI installed and authenticated

```bash
gcloud auth login
gcloud config set project physicaleventexperience
```

---

## Local Development Setup

### Option 1: Run Without GCP Services (Simplest)

Use the `local` profile which disables all GCP integrations:

```bash
# Set environment variable
export SPRING_PROFILES_ACTIVE=local

# Or run directly
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Option 2: Run With GCP Services Locally

If you want to test with real GCP services:

#### Step 1: Create Service Account

```bash
# Create service account
gcloud iam service-accounts create stadiumflow-app \
    --display-name="StadiumFlow Application"

# Grant necessary permissions
gcloud projects add-iam-policy-binding physicaleventexperience \
    --member="serviceAccount:stadiumflow-app@physicaleventexperience.iam.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"

gcloud projects add-iam-policy-binding physicaleventexperience \
    --member="serviceAccount:stadiumflow-app@physicaleventexperience.iam.gserviceaccount.com" \
    --role="roles/aiplatform.user"
```

#### Step 2: Download Service Account Key

```bash
gcloud iam service-accounts keys create ~/stadiumflow-key.json \
    --iam-account=stadiumflow-app@physicaleventexperience.iam.gserviceaccount.com
```

⚠️ **Security Note**: Keep this file secure and never commit it to version control!

#### Step 3: Set Environment Variable

```bash
export GOOGLE_APPLICATION_CREDENTIALS=~/stadiumflow-key.json
```

Or add to your `.env` file:
```
GOOGLE_APPLICATION_CREDENTIALS=/path/to/stadiumflow-key.json
```

---

## Production Setup with Secret Manager

### Step 1: Enable Secret Manager API

```bash
gcloud services enable secretmanager.googleapis.com
```

### Step 2: Create Secrets

```bash
# Create the STADIUM_API_KEY secret
echo -n "your-actual-api-key-value" | \
    gcloud secrets create STADIUM_API_KEY \
    --data-file=- \
    --replication-policy="automatic"

# Verify the secret was created
gcloud secrets list
```

### Step 3: Grant Service Account Access

```bash
gcloud secrets add-iam-policy-binding STADIUM_API_KEY \
    --member="serviceAccount:stadiumflow-app@physicaleventexperience.iam.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
```

### Step 4: Deploy to Cloud Run

```bash
# Build and deploy
gcloud run deploy stadiumflow \
    --source . \
    --platform managed \
    --region us-central1 \
    --allow-unauthenticated \
    --service-account stadiumflow-app@physicaleventexperience.iam.gserviceaccount.com \
    --set-env-vars="SPRING_PROFILES_ACTIVE=prod"
```

---

## Environment Variables

### Required for All Environments
- `SPRING_PROFILES_ACTIVE`: Set to `local`, `dev`, or `prod`

### Required for GCP Integration
- `GOOGLE_APPLICATION_CREDENTIALS`: Path to service account key (local only)
- `GCP_PROJECT_ID`: Your GCP project ID (default: physicaleventexperience)

### Optional
- `STADIUM_API_KEY`: API key for local development
- `VERTEX_AI_LOCATION`: Vertex AI region (default: us-central1)
- `VERTEX_AI_MODEL`: Model name (default: gemini-1.5-pro)

---

## Running the Application

### Local Development (No GCP)
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Local Development (With GCP)
```bash
export GOOGLE_APPLICATION_CREDENTIALS=~/stadiumflow-key.json
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Production (Cloud Run)
Application automatically uses Application Default Credentials on GCP.

---

## Accessing Secrets in Code

### From application.properties
```properties
# Reference secrets from Secret Manager
stadium.api.key=${sm://STADIUM_API_KEY}
```

### From Java code
```java
@Value("${stadium.api.key}")
private String stadiumApiKey;
```

---

## Troubleshooting

### Error: "Your default credentials were not found"
- Make sure `GOOGLE_APPLICATION_CREDENTIALS` is set correctly
- Or use `spring.profiles.active=local` to disable GCP services

### Error: "Secret ID has invalid segments"
- Check that secret name format is correct: `${sm://SECRET_NAME}`
- Verify the secret exists: `gcloud secrets list`

### Error: "Permission denied on secret"
- Grant access: `gcloud secrets add-iam-policy-binding SECRET_NAME --member=... --role=roles/secretmanager.secretAccessor`

---

## Security Best Practices

1. ✅ Never commit service account keys to version control
2. ✅ Use Secret Manager for all sensitive data in production
3. ✅ Use least-privilege IAM roles
4. ✅ Rotate secrets regularly
5. ✅ Use different service accounts for dev/staging/prod
6. ✅ Enable audit logging for secret access
