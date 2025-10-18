# GitHub Actions Workflows

This directory contains CI/CD workflows for the EventPulse project.

## Workflows Overview

### 🔨 build.yml - Main Build Pipeline

**Triggers**: Push to any branch, Pull requests

**Purpose**: Build, test, and publish Docker images

**Jobs**:
1. **Build and Test**
   - Compile with Maven
   - Run all tests
   - Generate test reports
   - Upload JAR artifacts

2. **Docker Build and Push** (only on main/tags)
   - Build multi-platform Docker image
   - Push to GitHub Container Registry
   - Tag based on branch/version

**Badge**:
```markdown
![Build](https://github.com/<owner>/<repo>/workflows/Build%20and%20Push/badge.svg)
```

### ✅ pr-check.yml - Pull Request Validation

**Triggers**: Pull requests to main or develop

**Purpose**: Validate code quality before merging

**Jobs**:
1. **Validate PR**
   - Compile code
   - Run tests
   - Check code style
   - Comment test results on PR

2. **Security Scan**
   - Run Trivy vulnerability scanner
   - Upload to GitHub Security tab
   - Check for known CVEs

**Badge**:
```markdown
![PR Check](https://github.com/<owner>/<repo>/workflows/Pull%20Request%20Check/badge.svg)
```

### 🚀 release.yml - Release Automation

**Triggers**: Version tags (v*.*.*)

**Purpose**: Automate release creation and publishing

**Jobs**:
1. **Create Release**
   - Build and test
   - Generate changelog
   - Create GitHub Release
   - Upload JAR and OpenAPI spec
   - Build and push versioned Docker image
   - Create artifact attestation

**Usage**:
```powershell
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

**Badge**:
```markdown
![Release](https://github.com/<owner>/<repo>/workflows/Release/badge.svg)
```

### 🔄 dependency-update.yml - Dependency Checks

**Triggers**: 
- Scheduled: Every Monday at 9 AM UTC
- Manual: workflow_dispatch

**Purpose**: Keep dependencies up to date

**Jobs**:
1. **Check Updates**
   - Check Maven dependencies
   - Check Maven plugins
   - Create issue if updates available

**Manual Trigger**:
```powershell
gh workflow run dependency-update.yml
```

## Workflow Files

| File | Lines | Purpose |
|------|-------|---------|
| build.yml | ~120 | Main CI/CD pipeline |
| pr-check.yml | ~70 | PR validation |
| release.yml | ~100 | Release automation |
| dependency-update.yml | ~55 | Dependency monitoring |

## Required Permissions

All workflows use `GITHUB_TOKEN` with these permissions:

```yaml
permissions:
  contents: write        # For creating releases
  packages: write        # For pushing to GHCR
  security-events: write # For security scans
```

## GitHub Container Registry

### Image Names

```
ghcr.io/<owner>/eventpulse-ai-augmented:latest
ghcr.io/<owner>/eventpulse-ai-augmented:1.0.0
ghcr.io/<owner>/eventpulse-ai-augmented:main-abc123
```

### Pull Images

```powershell
docker pull ghcr.io/<owner>/eventpulse-ai-augmented:latest
```

### Use in docker-compose.yml

```yaml
services:
  eventpulse-backend:
    image: ghcr.io/<owner>/eventpulse-ai-augmented:latest
```

## Customization

### Add Environment Secrets

**Repository Settings → Secrets and variables → Actions**

Example secrets:
- `DOCKER_USERNAME` - For Docker Hub
- `SONAR_TOKEN` - For SonarQube
- `SLACK_WEBHOOK` - For notifications

### Modify Triggers

```yaml
# Run on specific branches only
on:
  push:
    branches:
      - main
      - staging

# Run on schedule
on:
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM UTC
```

### Add New Jobs

```yaml
jobs:
  deploy:
    needs: build-and-test
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to server
        run: |
          # Deployment script
```

## Monitoring Workflows

### View Status

**GitHub UI**:
```
Repository → Actions tab
```

**GitHub CLI**:
```powershell
# List recent runs
gh run list

# Watch specific run
gh run watch <run-id>

# Download artifacts
gh run download <run-id>
```

### Email Notifications

GitHub sends emails on:
- ❌ Workflow failures
- ✅ First success after failure
- 🔄 Always (configurable)

## Troubleshooting

### Workflow Not Triggering

1. Check workflow syntax: `.github/workflows/*.yml`
2. Verify branch name matches trigger pattern
3. Check if workflow is enabled (Actions settings)

### Build Fails

1. Check logs in Actions tab
2. Test locally: `mvn clean package`
3. Check Java version: Must be 17

### Docker Push Fails

1. Verify `packages: write` permission
2. Check GITHUB_TOKEN is passed correctly
3. Ensure package visibility matches repo

### Tests Fail in CI

1. Run locally: `mvn test`
2. Check for environment differences
3. Ensure H2 database is used in tests

## Best Practices

### Workflow Design
- ✅ Use caching for dependencies
- ✅ Run tests before Docker build
- ✅ Use conditions to skip unnecessary jobs
- ✅ Set appropriate timeouts
- ✅ Use latest action versions

### Security
- ✅ Pin action versions (@v4)
- ✅ Limit permissions (least privilege)
- ✅ Use GITHUB_TOKEN (not PAT)
- ✅ Scan for vulnerabilities
- ✅ Never commit secrets

### Performance
- ✅ Cache Maven dependencies
- ✅ Cache Docker layers
- ✅ Run jobs in parallel
- ✅ Skip tests in Docker build
- ✅ Use build artifacts

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Container Registry](https://docs.github.com/en/packages)
- [Workflow Syntax](https://docs.github.com/en/actions/reference/workflow-syntax-for-github-actions)
- [GHCR Authentication](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)

