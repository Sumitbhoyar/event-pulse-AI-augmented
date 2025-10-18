# CI/CD Documentation

This document explains the Continuous Integration and Continuous Deployment (CI/CD) setup for EventPulse.

## Overview

EventPulse uses **GitHub Actions** for CI/CD with three main workflows:

1. **build.yml** - Build, test, and publish Docker images
2. **pr-check.yml** - Validate pull requests
3. **release.yml** - Create releases with artifacts
4. **dependency-update.yml** - Check for dependency updates

## Workflows

### 1. Build and Push (`build.yml`)

**Triggers**:
- Every push to `main`, `develop`, or `feature/*` branches
- Pull requests to `main` or `develop`
- Tags matching `v*` pattern

**Jobs**:

#### Job 1: Build and Test
- Checks out code
- Sets up JDK 17 with Maven cache
- Compiles the application
- Runs all tests (unit + integration)
- Generates test reports
- Packages the JAR file
- Uploads JAR as artifact (retained for 7 days)

#### Job 2: Docker Build and Push
- **Only runs on**: Push to `main` branch or version tags
- **Requires**: Build and Test job to pass
- Builds Docker image using multi-stage Dockerfile
- Pushes to GitHub Container Registry (GHCR)
- Creates multi-platform images (linux/amd64, linux/arm64)
- Uses build cache for faster builds
- Tags images based on branch/tag

**Image Tags**:
- `main` → `latest`
- `v1.2.3` → `1.2.3`, `1.2`, `1`, `latest`
- `feature/xyz` → `feature-xyz-<sha>`

### 2. Pull Request Check (`pr-check.yml`)

**Triggers**:
- Pull requests to `main` or `develop`

**Jobs**:

#### Job 1: Validate PR
- Compiles code
- Runs all tests
- Checks code style (optional)
- Builds package
- Comments on PR with test results

#### Job 2: Security Scan
- Runs Trivy vulnerability scanner
- Scans filesystem for vulnerabilities
- Uploads results to GitHub Security tab
- Checks dependencies, code, and Docker base images

### 3. Release (`release.yml`)

**Triggers**:
- Tags matching `v*.*.*` (e.g., `v1.0.0`)

**Jobs**:

#### Create GitHub Release
- Builds and tests the application
- Generates changelog from commit messages
- Creates GitHub Release with:
  - JAR artifact
  - OpenAPI specification (design/openapi.yaml)
  - Changelog
- Builds and pushes Docker image with version tags
- Creates artifact attestation for security
- Updates docker-compose.yml with release image

**Version Tags**:
- Tag `v1.2.3` creates:
  - `1.2.3`
  - `1.2`
  - `1`
  - `latest`

### 4. Dependency Update Check (`dependency-update.yml`)

**Triggers**:
- Scheduled: Every Monday at 9 AM UTC
- Manual trigger via workflow_dispatch

**Jobs**:
- Checks for Maven dependency updates
- Checks for plugin updates
- Creates GitHub issue if updates are available
- Labels issue with `dependencies` and `maintenance`

## GitHub Container Registry (GHCR)

### Image Location

```
ghcr.io/<owner>/eventpulse-ai-augmented:latest
```

### Pull Image

```powershell
docker pull ghcr.io/<owner>/eventpulse-ai-augmented:latest
```

### Run Image

```powershell
docker run -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=docker `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/eventpulse `
  ghcr.io/<owner>/eventpulse-ai-augmented:latest
```

### Image Tags Strategy

| Source | Tag Pattern | Example |
|--------|-------------|---------|
| Main branch | `latest` | `latest` |
| Version tag | `x.y.z`, `x.y`, `x` | `1.2.3`, `1.2`, `1` |
| Feature branch | `feature-name-sha` | `feature-auth-a1b2c3d` |
| Commit SHA | `branch-sha` | `main-a1b2c3d` |

## Secrets Required

### GitHub Secrets

The workflows use the following secrets (automatically provided by GitHub Actions):

| Secret | Description | Auto-Provided |
|--------|-------------|---------------|
| `GITHUB_TOKEN` | GitHub API token | ✅ Yes |

**No manual secrets configuration needed!**

### Optional Secrets (for advanced features)

| Secret | Purpose | When Needed |
|--------|---------|-------------|
| `DOCKER_USERNAME` | Docker Hub username | If pushing to Docker Hub |
| `DOCKER_PASSWORD` | Docker Hub token | If pushing to Docker Hub |
| `SONAR_TOKEN` | SonarQube token | If using SonarQube |

## Permissions

The workflows require the following permissions:

```yaml
permissions:
  contents: write      # For creating releases
  packages: write      # For pushing to GHCR
  security-events: write  # For security scans
```

These are automatically configured in the workflow files.

## Workflow Status Badges

Add these badges to your README.md:

```markdown
![Build](https://github.com/<owner>/<repo>/workflows/Build%20and%20Push/badge.svg)
![PR Check](https://github.com/<owner>/<repo>/workflows/Pull%20Request%20Check/badge.svg)
![Release](https://github.com/<owner>/<repo>/workflows/Release/badge.svg)
```

## Local Testing

### Test Build Workflow Locally

Using [act](https://github.com/nektos/act):

```powershell
# Install act (if not already installed)
choco install act-cli

# Test build workflow
act push

# Test PR workflow
act pull_request

# Test specific job
act -j build-and-test
```

### Validate Workflow Syntax

```powershell
# Using GitHub CLI
gh workflow view build.yml

# Or use online validator
# https://rhysd.github.io/actionlint/
```

## Triggering Workflows

### Manual Trigger

For workflows with `workflow_dispatch`:

```powershell
# Using GitHub CLI
gh workflow run dependency-update.yml

# Or via GitHub UI
# Actions → Select workflow → Run workflow
```

### Push Trigger

```powershell
git add .
git commit -m "feat: add new feature"
git push origin main
```

### Tag Trigger (Release)

```powershell
# Create and push version tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

## CI/CD Pipeline Flow

### Feature Development

```
1. Create feature branch
   ↓
2. Push commits → build.yml runs (build + test)
   ↓
3. Create PR → pr-check.yml runs (validate + security scan)
   ↓
4. Merge to main → build.yml runs (build + test + push Docker image)
   ↓
5. Tag release → release.yml runs (create GitHub release + artifacts)
```

### Deployment Pipeline

```
Code Push
   ↓
Build & Test (build.yml)
   ↓
Docker Build
   ↓
Push to GHCR
   ↓
Ready for Deployment
```

## Docker Image Usage

### Development

```powershell
# Use latest image
docker pull ghcr.io/<owner>/eventpulse-ai-augmented:latest
docker compose up -d
```

### Production

```powershell
# Use specific version
docker pull ghcr.io/<owner>/eventpulse-ai-augmented:1.2.3
docker run -p 8080:8080 ghcr.io/<owner>/eventpulse-ai-augmented:1.2.3
```

### Update docker-compose.yml

```yaml
eventpulse-backend:
  image: ghcr.io/<owner>/eventpulse-ai-augmented:1.2.3
  # Remove build section when using pre-built images
```

## Build Optimization

### Caching Strategy

The workflows use multiple caching strategies:

1. **Maven Dependencies**: Cached using `actions/setup-java@v4`
2. **Docker Layers**: Cached using GitHub Actions cache
3. **Build Cache**: Shared between workflow runs

### Estimated Build Times

| Job | Duration | Cache Hit | Cache Miss |
|-----|----------|-----------|------------|
| Compile | 30s | 20s | 60s |
| Tests | 45s | 35s | 90s |
| Package | 15s | 10s | 30s |
| Docker Build | 2m | 1m | 5m |

### Reducing Build Time

1. **Use cache effectively**: Dependencies are cached by default
2. **Skip tests for Docker**: Tests run in separate job
3. **Multi-stage build**: Only runtime artifacts in final image
4. **Parallel jobs**: Build and test run in parallel where possible

## Monitoring CI/CD

### View Workflow Runs

**GitHub UI**:
```
Repository → Actions tab
```

**GitHub CLI**:
```powershell
# List recent runs
gh run list

# View specific run
gh run view <run-id>

# Watch live
gh run watch
```

### Workflow Artifacts

Artifacts are stored for 7 days:
- JAR files
- Test reports
- Build logs

**Download artifacts**:
```powershell
# Using GitHub CLI
gh run download <run-id>

# Or via GitHub UI
# Actions → Select run → Artifacts section
```

## Troubleshooting

### Build Fails

**Check logs**:
1. Go to Actions tab
2. Click on failed workflow run
3. Click on failed job
4. Expand failed step

**Common issues**:
- Compilation errors → Check Java version
- Test failures → Review test logs
- Docker build fails → Check Dockerfile syntax

### Tests Fail in CI but Pass Locally

**Possible causes**:
1. Different Java version
2. Missing environment variables
3. Timezone differences
4. Database state issues

**Fix**:
```yaml
# Use same Java version as CI
<java.version>17</java.version>
```

### Docker Push Fails

**Check**:
1. GITHUB_TOKEN has `packages: write` permission
2. Repository visibility (public vs private)
3. Package settings allow push

**Enable package write**:
```yaml
permissions:
  packages: write
```

### Slow Builds

**Optimization steps**:
1. Enable Maven cache (already enabled)
2. Use Docker build cache (already enabled)
3. Skip tests for Docker build (already configured)
4. Use self-hosted runners for faster builds

## Security

### Dependency Scanning

**Trivy** scans for:
- Known vulnerabilities (CVEs)
- Misconfigurations
- Secrets in code
- License compliance

Results appear in:
- Security tab → Vulnerability alerts
- Pull request checks

### SBOM Generation

Generate Software Bill of Materials:

```powershell
mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
```

Output: `target/bom.json`

### Secret Scanning

GitHub automatically scans for:
- API keys
- Passwords
- Tokens
- Private keys

**Never commit**:
- `application-local.yaml` with real credentials
- `.env` files with secrets
- Private keys or certificates

## Release Process

### Creating a Release

**1. Prepare release**:
```powershell
# Update version in pom.xml
mvn versions:set -DnewVersion=1.2.3

# Commit version change
git add pom.xml
git commit -m "chore: bump version to 1.2.3"
git push
```

**2. Create and push tag**:
```powershell
git tag -a v1.2.3 -m "Release version 1.2.3"
git push origin v1.2.3
```

**3. Workflow automatically**:
- Runs tests
- Builds JAR
- Creates GitHub Release
- Generates changelog
- Builds Docker image
- Pushes to GHCR with version tags

**4. Release artifacts**:
- JAR file: `eventpulse-1.2.3.jar`
- OpenAPI spec: `openapi.yaml`
- Docker image: `ghcr.io/<owner>/eventpulse:1.2.3`

### Versioning Strategy

Follow [Semantic Versioning](https://semver.org/):

- `MAJOR.MINOR.PATCH`
- `1.0.0` - Initial release
- `1.1.0` - New features (backward compatible)
- `1.1.1` - Bug fixes
- `2.0.0` - Breaking changes

## Advanced Configuration

### Custom Test Configuration

```yaml
- name: Run tests with coverage
  run: mvn test jacoco:report -B

- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
  with:
    files: target/site/jacoco/jacoco.xml
```

### SonarQube Integration

```yaml
- name: SonarQube Scan
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  run: mvn sonar:sonar -B
```

### Slack Notifications

```yaml
- name: Notify Slack
  if: failure()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

## Environment-Specific Deployments

### Dev Environment

```yaml
- name: Deploy to Dev
  if: github.ref == 'refs/heads/develop'
  run: |
    # Deploy to dev server
    ssh user@dev-server 'docker pull ghcr.io/<owner>/eventpulse:develop && docker-compose up -d'
```

### Staging Environment

```yaml
- name: Deploy to Staging
  if: github.ref == 'refs/heads/main'
  run: |
    # Deploy to staging server
    ssh user@staging-server 'docker pull ghcr.io/<owner>/eventpulse:latest && docker-compose up -d'
```

### Production Environment

```yaml
- name: Deploy to Production
  if: startsWith(github.ref, 'refs/tags/v')
  run: |
    # Deploy to production server
    ssh user@prod-server 'docker pull ghcr.io/<owner>/eventpulse:${{ steps.version.outputs.VERSION }} && docker-compose up -d'
```

## Best Practices

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add new WebSocket endpoint
fix: resolve JWT token expiration issue
docs: update API documentation
chore: bump dependencies
test: add integration tests for metrics
refactor: simplify event service logic
```

**Benefits**:
- Automatic changelog generation
- Semantic version bumping
- Better release notes

### Branch Strategy

```
main (production)
  ↓
develop (staging)
  ↓
feature/* (development)
```

**Workflow**:
1. Create feature branch from `develop`
2. Make changes and push (triggers build)
3. Create PR to `develop` (triggers PR check)
4. Merge to `develop` (triggers build + push to GHCR)
5. Merge `develop` to `main` when ready for release
6. Tag `main` for production release

### Testing in CI

**Required**:
- All tests must pass before merge
- No skipping tests in CI
- Fix failing tests immediately

**Test categories**:
- Unit tests (fast, isolated)
- Integration tests (database, full context)
- Security tests (authentication, authorization)

## Deployment

### Using CI-Built Images

**1. Update docker-compose.yml**:
```yaml
services:
  eventpulse-backend:
    image: ghcr.io/<owner>/eventpulse-ai-augmented:latest
    # Comment out build section
    # build:
    #   context: .
    #   dockerfile: Dockerfile
```

**2. Pull and run**:
```powershell
docker compose pull
docker compose up -d
```

### Rollback

**To previous version**:
```powershell
# Pull specific version
docker pull ghcr.io/<owner>/eventpulse-ai-augmented:1.1.0

# Update docker-compose.yml with version
# Restart services
docker compose up -d
```

**Quick rollback**:
```powershell
# Tag previous good version as latest
docker tag ghcr.io/<owner>/eventpulse:1.1.0 ghcr.io/<owner>/eventpulse:latest
docker compose up -d
```

## Monitoring CI/CD

### GitHub Actions Dashboard

View all workflow runs:
```
https://github.com/<owner>/<repo>/actions
```

### Workflow Status

**Check status via CLI**:
```powershell
# List recent runs
gh run list --workflow=build.yml

# Watch specific run
gh run watch <run-id>

# View logs
gh run view <run-id> --log
```

### Email Notifications

GitHub automatically sends emails on:
- Workflow failures
- First failure after success
- Success after failure

**Configure in GitHub settings**:
```
Settings → Notifications → Actions
```

## Cost Optimization

### GitHub Actions Minutes

**Free tier**:
- Public repos: Unlimited
- Private repos: 2,000 minutes/month

**Optimization**:
1. Use caching (already enabled)
2. Skip redundant jobs with conditions
3. Cancel in-progress runs on new push
4. Use self-hosted runners (free)

### Storage

**Artifact retention**:
- Default: 7 days
- Configurable per artifact
- Cleanup old artifacts regularly

## Security Best Practices

### Workflow Security

1. **Pin action versions**: Use `@v4` instead of `@main`
2. **Limit permissions**: Only grant required permissions
3. **Review third-party actions**: Check before using
4. **Use GITHUB_TOKEN**: Avoid personal access tokens
5. **Scan for secrets**: Enable secret scanning

### Image Security

1. **Multi-stage builds**: Smaller attack surface
2. **Non-root user**: Run as `spring:spring` user
3. **Minimal base image**: Alpine JRE
4. **Regular updates**: Weekly dependency checks
5. **Vulnerability scanning**: Trivy in PR checks

## Troubleshooting Guide

### Workflow Won't Trigger

**Check**:
1. Workflow file syntax (YAML validation)
2. Branch name matches trigger pattern
3. Workflow is enabled in Actions settings

### Docker Push Permission Denied

**Fix**:
1. Check `packages: write` permission in workflow
2. Verify GITHUB_TOKEN is passed correctly
3. Check repository package settings

### Tests Pass Locally but Fail in CI

**Common causes**:
1. **Java version mismatch**: Use Java 17
2. **Timezone issues**: Set UTC in tests
3. **Port conflicts**: Use random ports or testcontainers
4. **File paths**: Use OS-agnostic paths

**Fix**:
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
```

### Cache Not Working

**Check**:
1. Cache key is consistent
2. Dependencies changed (cache invalidated)
3. Cache size limits (10GB max)

**Debug**:
```yaml
- name: Debug cache
  run: |
    echo "Cache key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}"
```

## Advanced Features

### Matrix Builds

Test on multiple Java versions:

```yaml
strategy:
  matrix:
    java: [17, 21]
steps:
  - uses: actions/setup-java@v4
    with:
      java-version: ${{ matrix.java }}
```

### Conditional Execution

```yaml
- name: Deploy to production
  if: github.ref == 'refs/heads/main' && github.event_name == 'push'
  run: ./deploy.sh
```

### Reusable Workflows

Create `.github/workflows/reusable-build.yml`:

```yaml
on:
  workflow_call:
    inputs:
      java-version:
        required: true
        type: string
```

## Metrics and Reporting

### Build Metrics

Track:
- Build duration
- Test success rate
- Docker image size
- Deployment frequency

### Test Reports

- Automatically commented on PRs
- Available in Actions tab
- Downloadable as artifacts

### Code Coverage

Add Jacoco:

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.11</version>
</plugin>
```

Then in workflow:
```yaml
- name: Generate coverage report
  run: mvn jacoco:report
```

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
- [Maven GitHub Actions](https://github.com/actions/setup-java)

