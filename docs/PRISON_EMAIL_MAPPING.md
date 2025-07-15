# Prison Email Mapping Implementation

This document explains the implementation of the prison email mapping feature for the IA Case Notifications API.

## Overview

The prison email mapping feature allows the system to send detention engagement team email notifications to the correct prison email address based on the prison name where an appellant is detained.

## Implementation Details

### Architecture

The implementation follows a clean architecture approach with:

1. **JSON Configuration Files**: Environment-specific email mappings
2. **Service Layer**: Business logic for email address resolution
3. **Integration Layer**: Updated `DetEmailService` to handle both IRC and prison cases

### Key Components

#### 1. Configuration Files

**Location**: `src/main/resources/prison-emails/`

- `prison-emails-dev.json`: Development/test environment with example email addresses
- `prison-emails-prod.json`: Production environment with real prison email addresses

**Format**:
```json
{
  "prisonEmailMappings": {
    "Addiewell": "adcourts@sodexogov.co.uk",
    "Belmarsh": "omu.belmarsh@justice.gov.uk",
    "Birmingham": "omu.birmingham@justice.gov.uk"
  }
}
```

#### 2. Configuration Loading

**PrisonEmailMappingService** loads configurations based on environment variables:

```java
@Service
public class PrisonEmailMappingService {
    @Value("${prison.email.environment:${PRISON_EMAIL_ENV:dev}}")
    private String environment;
    
    // Loads prison-emails-{environment}.json
}
```

#### 3. Environment Variable Priority

The system checks environment variables in this order:
1. `PRISON_EMAIL_ENV` (primary)
2. `ENVIRONMENT` (fallback)
3. `APP_ENV` (fallback)
4. `dev` (default)

#### 4. Integration with DetEmailService

```java
@Service
public class DetEmailService {
    public Optional<String> getDetEmailAddress(AsylumCase asylumCase) {
        if (detentionFacility == YesOrNo.YES) {
            return getIrcEmailAddress(asylumCase);
        } else {
            return getPrisonEmailAddress(asylumCase);
        }
    }
}
```

## Configuration Management

### Environment Variables

**Primary Configuration**:
```bash
# Set the prison email environment
export PRISON_EMAIL_ENV=prod

# Alternative environment variables (fallback)
export ENVIRONMENT=production
export APP_ENV=prod
```

**Application Properties**:
```yaml
prison:
  email:
    environment: ${PRISON_EMAIL_ENV:${ENVIRONMENT:${APP_ENV:dev}}}
```

### Deployment Instructions

#### Development Environment
```bash
# No configuration needed - defaults to dev
# Uses prison-emails-dev.json with test email addresses
```

#### Production Environment
```bash
# Option 1: Use PRISON_EMAIL_ENV
export PRISON_EMAIL_ENV=prod

# Option 2: Use existing ENVIRONMENT variable
export ENVIRONMENT=prod

# Option 3: Use APP_ENV variable
export APP_ENV=prod
```

#### Docker Deployment
```dockerfile
# In Dockerfile or docker-compose.yml
ENV PRISON_EMAIL_ENV=prod
```

#### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: ia-case-notifications-api
        env:
        - name: PRISON_EMAIL_ENV
          value: "prod"
```

## Why Environment Variables Over Spring Profiles?

### ✅ **Advantages of Environment Variable Approach**

1. **No Spring Profiles Dependency**: Avoids introducing Spring profiles if not already used
2. **Flexible Fallback**: Multiple environment variable options
3. **Simple Configuration**: Single environment variable controls behavior
4. **Production Ready**: Works with existing deployment patterns
5. **Environment Agnostic**: Works with any deployment environment

### 🔄 **Alternative Approaches Considered**

1. **Spring Profiles**: `@Profile("prod")` - Requires profile management
2. **External Configuration**: File system paths - Adds deployment complexity
3. **Database Configuration**: Dynamic loading - Overkill for static data
4. **ConfigMaps**: Kubernetes-specific - Environment dependent

## API Usage

### Get Prison Email Address
```java
@Autowired
private PrisonEmailMappingService prisonEmailService;

public void sendNotification(String prisonName) {
    Optional<String> email = prisonEmailService.getPrisonEmail(prisonName);
    if (email.isPresent()) {
        // Send notification to email.get()
    }
}
```

### Check Prison Support
```java
if (prisonEmailService.isPrisonSupported("Belmarsh")) {
    // Prison is supported
}
```

### Get All Mappings
```java
Map<String, String> allMappings = prisonEmailService.getAllPrisonEmails();
```

## Testing

### Unit Tests
```java
@Test
void shouldLoadProdConfigurationWhenEnvironmentIsProduction() {
    ReflectionTestUtils.setField(service, "environment", "prod");
    service.loadConfiguration();
    
    Optional<String> email = service.getPrisonEmail("Addiewell");
    assertThat(email.get()).isEqualTo("adcourts@sodexogov.co.uk");
}
```

### Integration Tests
```java
@SpringBootTest
@TestPropertySource(properties = "prison.email.environment=dev")
class PrisonEmailIntegrationTest {
    // Test with dev configuration
}
```

## Monitoring and Logging

The service provides comprehensive logging:

```
INFO  - Loading prison email mappings from: prison-emails/prison-emails-prod.json
INFO  - Successfully loaded 120 prison email mappings for environment: prod
DEBUG - Found prison email address for 'Belmarsh': omu.belmarsh@justice.gov.uk
WARN  - No email mapping found for prison: UnknownPrison
```

## Maintenance

### Adding New Prisons
1. Update both `prison-emails-dev.json` and `prison-emails-prod.json`
2. Add test email for development environment
3. Add production email for production environment

### Updating Existing Emails
1. Modify the appropriate JSON file
2. Test in development environment first
3. Deploy to production

### Environment-Specific Updates
- **Development**: Update `prison-emails-dev.json` only
- **Production**: Update `prison-emails-prod.json` only
- **Both**: Update both files with appropriate email addresses

## Security Considerations

1. **Email Validation**: All email addresses are validated during loading
2. **Fallback Handling**: Graceful fallback to dev configuration if production file missing
3. **Logging**: Sensitive information is not logged in production
4. **Configuration Protection**: JSON files are included in application classpath

## Troubleshooting

### Common Issues

1. **Missing Configuration File**:
   - **Error**: File not found
   - **Solution**: Check file exists in `src/main/resources/prison-emails/`

2. **Invalid JSON Format**:
   - **Error**: JSON parsing error
   - **Solution**: Validate JSON syntax

3. **Missing Prison Mapping**:
   - **Error**: No email found for prison
   - **Solution**: Add prison to appropriate JSON file

4. **Environment Variable Not Working**:
   - **Error**: Wrong configuration loaded
   - **Solution**: Check environment variable precedence and spelling

### Debug Commands

```bash
# Check current environment
echo $PRISON_EMAIL_ENV

# Verify configuration loading
curl http://localhost:8093/actuator/info

# Check logs for configuration loading
tail -f application.log | grep "prison email"
``` 