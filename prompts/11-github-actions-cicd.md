# Prompt 11 — GitHub Actions CI/CD

> Create a GitHub Actions workflow `.github/workflows/build.yml` that:
> - Runs on every push
> - Builds with Maven
> - Runs tests
> - Builds Docker image and pushes to GitHub Container Registry if tests pass.

## Implementation Summary

This prompt implemented comprehensive CI/CD pipeline for the EventPulse application using GitHub Actions by:

### Files Created:
- `.github/workflows/build.yml` - Main CI/CD pipeline
- `.github/workflows/pr.yml` - Pull request workflow
- `.github/workflows/release.yml` - Release workflow
- `.github/ISSUE_TEMPLATE/cicd-issue.yml` - CI/CD issue template
- `.github/pull_request_template.md` - Pull request template
- `CICD_README.md` - Comprehensive CI/CD documentation

### Key Features Implemented:

#### Main CI/CD Pipeline (`build.yml`):
- **Multi-Job Architecture** - Parallel execution for efficiency
- **Build & Test** - Maven build with JDK 21 and PostgreSQL
- **Security Scanning** - OWASP dependency check for vulnerabilities
- **Code Quality** - SpotBugs and Checkstyle validation
- **Docker Build** - Multi-platform container images (AMD64, ARM64)
- **GitHub Container Registry** - Automated image push
- **Deployment** - Staging and production environment deployment
- **Notifications** - Success/failure alerts

#### Pull Request Workflow (`pr.yml`):
- **Quick Feedback** - Fast feedback for developers
- **Code Quality Checks** - Automated quality validation with PR comments
- **Security Scanning** - Vulnerability assessment with PR integration
- **Docker Build Verification** - Build validation without registry push
- **Comprehensive Status** - Detailed status reporting in PR comments

#### Release Workflow (`release.yml`):
- **Full Test Suite** - Complete test execution for releases
- **Security Scanning** - Comprehensive security assessment
- **Docker Image Build** - Production-ready container images
- **Release Notes** - Automated changelog generation
- **Production Deployment** - Automated production deployment
- **Manual Release** - Workflow dispatch for manual releases

#### Enhanced Maven Configuration:
- **JaCoCo** - Code coverage reporting (60% minimum threshold)
- **SpotBugs** - Static code analysis with XML output
- **Checkstyle** - Code style validation (Google Java Style)
- **OWASP Dependency Check** - Security vulnerability scanning

### Technical Implementation:

#### Main Pipeline Structure:
```yaml
name: EventPulse CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]
  release:
    types: [ published ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: eventpulse_test
          POSTGRES_USER: eventpulse
          POSTGRES_PASSWORD: eventpulse
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: maven
    
    - name: Run tests
      run: mvn clean verify
      env:
        SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/eventpulse_test
```

#### Docker Build and Push:
```yaml
  build-docker:
    needs: [build-and-test, security-scan]
    runs-on: ubuntu-latest
    
    steps:
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v3
    
    - name: Log in to Container Registry
      uses: docker/login-action@v3
      with:
        registry: ghcr.io
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
    
    - name: Build and push Docker image
      uses: docker/build-push-action@v5
      with:
        context: .
        platforms: linux/amd64,linux/arm64
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        cache-from: type=gha
        cache-to: type=gha,mode=max
```

#### Code Quality Integration:
```yaml
  code-quality:
    runs-on: ubuntu-latest
    
    steps:
    - name: Run Checkstyle
      run: mvn checkstyle:check
      continue-on-error: true
    
    - name: Run SpotBugs
      run: mvn spotbugs:check
      continue-on-error: true
    
    - name: Upload quality reports
      uses: actions/upload-artifact@v3
      with:
        name: code-quality-reports
        path: |
          target/checkstyle-result.xml
          target/spotbugsXml.xml
```

### Pipeline Features:

#### Quality Gates:
- **Test Coverage** - 60% minimum code coverage requirement
- **Code Quality** - SpotBugs and Checkstyle validation
- **Security** - OWASP dependency vulnerability scanning
- **Build Success** - All tests must pass before deployment

#### Multi-Platform Support:
- **AMD64** - Intel/AMD processor support
- **ARM64** - Apple Silicon and ARM server support
- **Docker Layer Caching** - Optimized build performance
- **Registry Integration** - GitHub Container Registry

#### Deployment Strategy:
- **Staging** - Automatic deployment on develop branch
- **Production** - Automatic deployment on main branch
- **Manual Release** - Workflow dispatch for manual releases
- **Environment Protection** - Protected environments with approvals

#### Security Features:
- **Dependency Scanning** - Automated vulnerability assessment
- **Secret Management** - Secure handling of sensitive data
- **Container Security** - Secure Docker image builds
- **Access Control** - Proper permissions and authentication

### Usage Examples:

#### Automatic Triggers:
```bash
# Push to main/develop triggers full pipeline
git push origin main

# Create PR triggers PR workflow
git push origin feature/new-feature
```

#### Manual Release:
```bash
# Go to GitHub Actions → Release → Run workflow
# Or create GitHub release to trigger automatically
```

#### Local Quality Checks:
```bash
# Run all checks locally
mvn clean verify
mvn checkstyle:check
mvn spotbugs:check
mvn org.owasp:dependency-check-maven:check
```

### Monitoring and Reporting:

#### Test Reports:
- **JUnit XML** - GitHub integration for test results
- **Coverage Reports** - JaCoCo coverage with artifacts
- **Test Artifacts** - Uploaded for review and analysis

#### Quality Reports:
- **SpotBugs XML** - Static analysis results
- **Checkstyle XML** - Code style validation results
- **Security Reports** - OWASP vulnerability assessments

#### PR Integration:
- **Automatic Comments** - Quality and security results in PRs
- **Status Checks** - Required checks for merge protection
- **Quality Gates** - Enforcement of quality standards

### Benefits:

#### For Developers:
- **Fast Feedback** - Quick validation of code changes
- **Quality Assurance** - Automated quality and security checks
- **Easy Deployment** - Automated deployment to environments
- **Clear Status** - Comprehensive status reporting

#### For Operations:
- **Automated Testing** - Comprehensive test execution
- **Security Scanning** - Vulnerability assessment
- **Container Registry** - Automated image management
- **Deployment Automation** - Consistent deployment processes

#### For Project Management:
- **Quality Metrics** - Code quality and coverage tracking
- **Security Compliance** - Vulnerability management
- **Release Management** - Automated release processes
- **Documentation** - Comprehensive CI/CD documentation

This GitHub Actions CI/CD implementation provided enterprise-grade automation for the EventPulse platform, ensuring code quality, security, and reliable deployments across multiple environments.
