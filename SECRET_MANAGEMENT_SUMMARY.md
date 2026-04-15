# Secret Management Summary

## What Changed?

Instead of just disabling GCP Secret Manager, we've implemented a flexible configuration system that supports multiple deployment scenarios.

## Files Created/Modified

### New Configuration Files
1. **`application-local.properties`** - For local development without GCP
2. **`application-prod.properties`** - For production with GCP Secret Manager
3. **`.env.example`** - Template for environment variables
4. **`GCP_SETUP_GUIDE.md`** - Detailed GCP setup instructions
5. **`CONFIGURATION_GUIDE.md`** - Configuration approaches and best practices

### Modified Files
1. **`application.properties`** - Enhanced with GCP configuration options
2. **`GeminiService.java`** - Updated to use injected configuration values
3. **`.gitignore`** - Added rules to prevent committing secrets

## How to Use

### Quick Start (Local Development)

```bash
# Option 1: Simple run (uses local profile by default)
./gradlew bootRun --args='--spring.profiles.active=local'

# Option 2: With environment variable
export STADIUM_API_KEY="your-key"
./gradlew bootRun --args='--spring.profiles.active=local'

# Option 3: With .env file
cp .env.example .env
# Edit .env with your values
export $(cat .env | xargs)
./gradlew bootRun
```

### Production (GCP Cloud Run)

```bash
# 1. Create secret in GCP
echo -n "your-api-key" | gcloud secrets create STADIUM_API_KEY --data-file=-

# 2. Deploy with prod profile
gcloud run deploy stadiumflow \
    --source . \
    --set-env-vars="SPRING_PROFILES_ACTIVE=prod"
```

## Configuration Hierarchy

The application now supports multiple ways to configure secrets:

### 1. Environment Variables (Simple)
```bash
export STADIUM_API_KEY="your-key"
```

### 2. Profile-Specific Properties (Organized)
```properties
# application-local.properties
stadium.api.key=local-dev-key

# application-prod.properties  
stadium.api.key=${sm://STADIUM_API_KEY}
```

### 3. GCP Secret Manager (Production)
```properties
spring.cloud.gcp.secretmanager.enabled=true
stadium.api.key=${sm://STADIUM_API_KEY}
```

## Benefits

### ✅ Security
- Secrets stored in GCP Secret Manager for production
- Service account keys excluded from version control
- IAM-based access control

### ✅ Flexibility
- Easy local development without GCP setup
- Profile-based configuration for different environments
- Seamless transition from dev to production

### ✅ Maintainability
- Clear documentation for each approach
- Environment-specific configuration files
- No hardcoded secrets in code

## Understanding the ${sm://SECRET_NAME} Syntax

When Spring Cloud GCP Secret Manager is enabled, you can reference secrets using:

```properties
# Format: ${sm://SECRET_NAME}
stadium.api.key=${sm://STADIUM_API_KEY}

# With version: ${sm://SECRET_NAME/VERSION}
stadium.api.key=${sm://STADIUM_API_KEY/1}

# With default fallback
stadium.api.key=${sm://STADIUM_API_KEY:default-value}
```

**How it works:**
1. Spring Boot encounters `${sm://STADIUM_API_KEY}`
2. Spring Cloud GCP Secret Manager interceptor detects the `sm://` prefix
3. It calls GCP Secret Manager API to fetch the secret value
4. The value is injected into the property at runtime

## Common Scenarios

### Scenario 1: First Time Developer
```bash
# No GCP setup needed
git clone <repo>
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Scenario 2: Testing with Real API Keys Locally
```bash
# Create .env file
echo "STADIUM_API_KEY=sk-real-key-here" > .env
echo "SPRING_PROFILES_ACTIVE=local" >> .env

# Load and run
export $(cat .env | xargs)
./gradlew bootRun
```

### Scenario 3: Testing GCP Integration Locally
```bash
# Download service account key from GCP
# Set credentials
export GOOGLE_APPLICATION_CREDENTIALS=~/stadiumflow-key.json

# Run with dev profile (enables Secret Manager)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Scenario 4: Production Deployment
```bash
# Deploy to Cloud Run - automatically uses Application Default Credentials
gcloud run deploy stadiumflow \
    --source . \
    --region us-central1 \
    --set-env-vars="SPRING_PROFILES_ACTIVE=prod" \
    --service-account=stadiumflow-app@physicaleventexperience.iam.gserviceaccount.com
```

## Next Steps

1. **For local development**: Use `application-local.properties` (already configured)
2. **For testing with GCP**: Follow `GCP_SETUP_GUIDE.md`
3. **For production**: Use `application-prod.properties` with Secret Manager
4. **For configuration options**: See `CONFIGURATION_GUIDE.md`

## Troubleshooting

### Error: "Secret ID has invalid segments"
- **Cause**: Secret Manager is enabled but secret doesn't exist or format is wrong
- **Fix**: Either disable Secret Manager for local dev, or create the secret in GCP

### Error: "Your default credentials were not found"
- **Cause**: GCP services enabled but no credentials available
- **Fix**: Use `--spring.profiles.active=local` or set `GOOGLE_APPLICATION_CREDENTIALS`

### Secret value not loading
- **Check**: Which profile is active: `echo $SPRING_PROFILES_ACTIVE`
- **Verify**: Secret exists in GCP: `gcloud secrets list`
- **Test**: Service account has access: `gcloud secrets describe STADIUM_API_KEY --show-iam-policy`

## Security Reminders

- ⚠️ Never commit `.env` files
- ⚠️ Never commit service account JSON keys
- ⚠️ Use Secret Manager for production
- ⚠️ Rotate secrets regularly
- ⚠️ Use least-privilege IAM roles
