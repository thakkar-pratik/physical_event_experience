# Configuration Guide for StadiumFlow

This document explains the different ways to configure secrets and API keys for the application.

## Configuration Approaches

### 1. Environment Variables (Simple, Local Development)

**Best for**: Local development, quick testing

**Setup**:
```bash
# Set environment variables before running
export STADIUM_API_KEY="your-key-here"
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

**Pros**:
- ✅ Simple and quick
- ✅ No GCP setup required
- ✅ Good for local development

**Cons**:
- ❌ Not secure for production
- ❌ Keys visible in process list
- ❌ Manual management

---

### 2. Application Properties (Local Configuration)

**Best for**: Team development with shared defaults

**Setup**:
1. Edit `src/main/resources/application-local.properties`:
```properties
stadium.api.key=local-dev-key
```

2. Run with profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

**Pros**:
- ✅ Easy to manage
- ✅ Profile-based configuration
- ✅ Good for local development

**Cons**:
- ❌ Risk of committing secrets
- ❌ Not secure for production

---

### 3. .env File (Hidden Configuration)

**Best for**: Local development with real API keys

**Setup**:
1. Create `.env` file (already in .gitignore):
```bash
STADIUM_API_KEY=your-actual-key
SPRING_PROFILES_ACTIVE=local
```

2. Load and run:
```bash
# Load .env file
export $(cat .env | xargs)
./gradlew bootRun
```

**Pros**:
- ✅ Keeps secrets out of version control
- ✅ Easy to switch between environments
- ✅ Good for local development

**Cons**:
- ❌ Manual file management
- ❌ Not suitable for production

---

### 4. GCP Secret Manager (Production-Ready) ⭐ RECOMMENDED

**Best for**: Production deployments, team environments

**Setup**:
1. Create secrets in GCP:
```bash
echo -n "your-key" | gcloud secrets create STADIUM_API_KEY --data-file=-
```

2. Update `application-prod.properties`:
```properties
spring.cloud.gcp.secretmanager.enabled=true
stadium.api.key=${sm://STADIUM_API_KEY}
```

3. Deploy to Cloud Run:
```bash
gcloud run deploy stadiumflow --source . --set-env-vars="SPRING_PROFILES_ACTIVE=prod"
```

**Pros**:
- ✅ Highly secure
- ✅ Automatic rotation support
- ✅ Audit logging
- ✅ IAM-based access control
- ✅ No secrets in code or environment

**Cons**:
- ❌ Requires GCP setup
- ❌ Additional cost (minimal)
- ❌ More complex for local dev

---

## Quick Start Matrix

| Scenario | Approach | Command |
|----------|----------|---------|
| First time running | Environment Variable | `export STADIUM_API_KEY=test && ./gradlew bootRun --args='--spring.profiles.active=local'` |
| Local development | .env file | `export $(cat .env \| xargs) && ./gradlew bootRun` |
| Testing with GCP | Service Account | `export GOOGLE_APPLICATION_CREDENTIALS=~/key.json && ./gradlew bootRun` |
| Production | Secret Manager | Deploy to Cloud Run with prod profile |

---

## Configuration Hierarchy

Spring Boot loads configuration in this order (later ones override earlier):

1. `application.properties` (default values)
2. `application-{profile}.properties` (profile-specific)
3. Environment variables
4. Command-line arguments
5. GCP Secret Manager (when enabled)

**Example**:
```properties
# application.properties
stadium.api.key=${STADIUM_API_KEY:default-placeholder}

# application-local.properties  
stadium.api.key=local-dev-key

# application-prod.properties
stadium.api.key=${sm://STADIUM_API_KEY}
```

---

## Testing Your Configuration

### Verify API Key is Loaded

Add this to any controller:
```java
@Value("${stadium.api.key}")
private String apiKey;

@GetMapping("/api/config/test")
public Map<String, String> testConfig() {
    return Map.of(
        "apiKeyLength", String.valueOf(apiKey.length()),
        "apiKeyPrefix", apiKey.substring(0, Math.min(4, apiKey.length())) + "..."
    );
}
```

### Check which profile is active

```bash
curl http://localhost:8080/actuator/env | jq '.activeProfiles'
```

---

## Migration Path

### Phase 1: Local Development (Current)
- Use environment variables or .env file
- Profile: `local`

### Phase 2: Staging Environment
- Set up GCP service account
- Use Secret Manager
- Profile: `dev`

### Phase 3: Production
- Full GCP Secret Manager integration
- Automated secret rotation
- Profile: `prod`

---

## Example Configurations

### Local Development
```bash
# .env file
SPRING_PROFILES_ACTIVE=local
STADIUM_API_KEY=local-test-key
```

### Staging/Development on GCP
```bash
# application-dev.properties
spring.cloud.gcp.secretmanager.enabled=true
spring.cloud.gcp.credentials.location=file:/path/to/dev-key.json
stadium.api.key=${sm://STADIUM_API_KEY_DEV}
```

### Production on Cloud Run
```bash
# application-prod.properties
spring.cloud.gcp.secretmanager.enabled=true
stadium.api.key=${sm://STADIUM_API_KEY}
# No credentials file needed - uses Application Default Credentials
```

---

## Security Checklist

- [ ] `.env` file is in `.gitignore`
- [ ] Service account keys are NOT in version control
- [ ] Production uses Secret Manager, not environment variables
- [ ] Secrets are rotated regularly
- [ ] Least-privilege IAM roles are used
- [ ] Audit logging is enabled for secret access
