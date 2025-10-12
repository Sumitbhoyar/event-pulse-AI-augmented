# EventPulse CI/CD Pipeline

This document describes the GitHub Actions CI/CD pipeline for EventPulse.

## 🔄 Pipeline Overview

The CI/CD pipeline consists of multiple workflows that handle different aspects of the development lifecycle:

1. **Build & Test** - Runs on every push to main/develop branches
2. **Pull Request** - Runs on PR creation and updates
3. **Release** - Runs on release creation
4. **Manual Release** - Allows manual release creation

## 📋 Workflow Files

### 1. `.github/workflows/build.yml` - Main CI/CD Pipeline

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches
- Release creation

**Jobs:**

#### 🔨 Build and Test
- **JDK 21** setup with Maven caching
- **PostgreSQL service** for integration tests
- **Unit tests** execution
- **Integration tests** execution
- **Test reporting** with JUnit format
- **Code coverage** with JaCoCo (60% minimum)

#### 🔒 Security Scan
- **OWASP dependency check** for vulnerabilities
- **Vulnerability reporting** with HTML output
- **Security artifact** upload for review

#### 📊 Code Quality
- **SpotBugs** static analysis
- **Checkstyle** code style validation
- **Quality reports** upload

#### 🐳 Docker Build
- **Multi-platform build** (AMD64, ARM64)
- **GitHub Container Registry** push
- **Docker layer caching** for performance
- **Image tagging** with semantic versioning

#### 🚀 Deployment
- **Staging deployment** (develop branch)
- **Production deployment** (main branch)
- **Environment-specific** configurations

### 2. `.github/workflows/pr.yml` - Pull Request CI

**Triggers:**
- Pull request creation
- Pull request updates
- Pull request reopening

**Jobs:**

#### ⚡ Quick Test
- **Fast feedback** for developers
- **PostgreSQL service** for testing
- **Test reporting** with PR comments

#### 📊 Code Quality Check
- **Checkstyle** validation
- **SpotBugs** analysis
- **PR comments** with quality results

#### 🔒 Security Check
- **OWASP dependency check**
- **Security report** in PR comments

#### 🐳 Docker Build
- **Local Docker build** (no push)
- **Build verification** without registry

#### 📝 PR Status
- **Comprehensive status** report
- **PR comments** with check results

### 3. `.github/workflows/release.yml` - Release Pipeline

**Triggers:**
- Release publication
- Manual workflow dispatch

**Jobs:**

#### 🧪 Full Test Suite
- **Complete test execution**
- **Integration tests** with database
- **Test reporting** and coverage

#### 🔒 Security Scan
- **Comprehensive security check**
- **Vulnerability report** upload

#### 🐳 Build and Push
- **Production Docker image**
- **Multi-platform support**
- **GitHub Container Registry**

#### 📝 Release Notes
- **Changelog generation**
- **Docker instructions**
- **Installation guides**

#### 🚀 Production Deployment
- **Automated deployment**
- **Environment validation**

## 🛠️ Configuration

### Maven Plugins

The pipeline uses several Maven plugins for code quality and security:

#### JaCoCo (Code Coverage)
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>INSTRUCTION</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.60</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

#### SpotBugs (Static Analysis)
```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.7.3.5</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
        <xmlOutput>true</xmlOutput>
    </configuration>
</plugin>
```

#### Checkstyle (Code Style)
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <configLocation>google_checks.xml</configLocation>
        <encoding>UTF-8</encoding>
        <consoleOutput>true</consoleOutput>
        <failsOnError>true</failsOnError>
    </configuration>
</plugin>
```

#### OWASP Dependency Check
```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.4.0</version>
    <configuration>
        <format>HTML</format>
        <failBuildOnCVSS>7</failBuildOnCVSS>
    </configuration>
</plugin>
```

### Environment Variables

The pipeline uses several environment variables:

```yaml
env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}
```

### Secrets

Required GitHub secrets:
- `GITHUB_TOKEN` - Automatically provided by GitHub
- Repository secrets for deployment (if needed)

## 🐳 Docker Integration

### Multi-Platform Build
The pipeline builds Docker images for multiple architectures:
- `linux/amd64` - Intel/AMD processors
- `linux/arm64` - ARM processors (Apple Silicon, ARM servers)

### Image Tagging Strategy
Images are tagged with multiple strategies:
- **Semantic versioning**: `1.0.0`, `1.0`, `1`
- **Branch-based**: `main`, `develop`
- **SHA-based**: `main-abc1234`
- **Latest**: `latest` (main branch only)

### Container Registry
Images are pushed to GitHub Container Registry:
- **Registry**: `ghcr.io`
- **Repository**: `{owner}/{repository}`
- **Authentication**: GitHub token

## 📊 Monitoring and Reporting

### Test Reports
- **JUnit XML** format for GitHub integration
- **Coverage reports** with JaCoCo
- **Test artifacts** uploaded for review

### Code Quality Reports
- **SpotBugs XML** output
- **Checkstyle XML** output
- **Quality artifacts** uploaded

### Security Reports
- **OWASP HTML** reports
- **Vulnerability artifacts** uploaded
- **Security comments** in PRs

### PR Integration
- **Automatic comments** with results
- **Status checks** for merge requirements
- **Quality gate** enforcement

## 🚀 Deployment Strategy

### Branch Strategy
- **`main`** - Production releases
- **`develop`** - Staging environment
- **Feature branches** - Development

### Environment Promotion
1. **Development** - Feature branches
2. **Staging** - Develop branch (automatic)
3. **Production** - Main branch (automatic)

### Deployment Triggers
- **Staging**: Push to `develop`
- **Production**: Push to `main` or release creation
- **Manual**: Workflow dispatch

## 🔧 Local Development

### Running Tests Locally
```bash
# Run all tests
mvn clean verify

# Run unit tests only
mvn test

# Run integration tests only
mvn verify -DskipUnitTests

# Generate coverage report
mvn jacoco:report
```

### Code Quality Checks
```bash
# Run Checkstyle
mvn checkstyle:check

# Run SpotBugs
mvn spotbugs:check

# Run OWASP dependency check
mvn org.owasp:dependency-check-maven:check
```

### Docker Build
```bash
# Build Docker image
docker build -t eventpulse:local .

# Run with Docker Compose
docker-compose up -d
```

## 📈 Metrics and KPIs

### Build Metrics
- **Build success rate**
- **Build duration**
- **Test execution time**
- **Coverage percentage**

### Quality Metrics
- **Code quality score**
- **Security vulnerabilities**
- **Technical debt**
- **Code complexity**

### Deployment Metrics
- **Deployment frequency**
- **Lead time**
- **Mean time to recovery**
- **Change failure rate**

## 🛡️ Security Considerations

### Secrets Management
- **GitHub secrets** for sensitive data
- **Environment variables** for configuration
- **Container registry** authentication

### Vulnerability Scanning
- **OWASP dependency check** for known vulnerabilities
- **Container scanning** for base image issues
- **Security reporting** in PRs and releases

### Access Control
- **Branch protection** rules
- **Required status checks**
- **Review requirements**
- **Environment protection**

## 🔍 Troubleshooting

### Common Issues

#### Build Failures
```bash
# Check Maven dependencies
mvn dependency:resolve

# Clear Maven cache
mvn dependency:purge-local-repository

# Check Java version
java -version
```

#### Test Failures
```bash
# Run tests with debug output
mvn test -X

# Check test database connection
docker run --rm postgres:15-alpine pg_isready -h localhost -p 5432
```

#### Docker Build Issues
```bash
# Check Docker build context
docker build --no-cache -t eventpulse:debug .

# Verify multi-platform support
docker buildx ls
```

#### Registry Push Issues
```bash
# Check authentication
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Verify permissions
gh auth status
```

### Debug Mode
Enable debug logging in workflows:
```yaml
- name: Debug
  run: |
    echo "Debug information:"
    echo "Branch: ${{ github.ref }}"
    echo "Commit: ${{ github.sha }}"
    echo "Actor: ${{ github.actor }}"
```

## 📚 Best Practices

### Code Quality
1. **Maintain test coverage** above 60%
2. **Fix security vulnerabilities** before merging
3. **Follow coding standards** (Checkstyle)
4. **Address SpotBugs warnings**

### Pipeline Optimization
1. **Use caching** for dependencies
2. **Parallel job execution**
3. **Conditional deployments**
4. **Efficient Docker builds**

### Security
1. **Regular dependency updates**
2. **Vulnerability scanning**
3. **Secret rotation**
4. **Access control**

### Monitoring
1. **Monitor build metrics**
2. **Track deployment success**
3. **Alert on failures**
4. **Review quality trends**

## 🤝 Contributing

### PR Guidelines
1. **Run tests locally** before pushing
2. **Ensure code quality** checks pass
3. **Address security** findings
4. **Update documentation** if needed

### Release Process
1. **Create release branch** from main
2. **Update version numbers**
3. **Generate changelog**
4. **Create GitHub release**
5. **Monitor deployment**

## 📞 Support

For CI/CD issues:
1. **Check workflow logs** in GitHub Actions
2. **Review error messages** and artifacts
3. **Consult this documentation**
4. **Create GitHub issue** if needed

For more information, see:
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Maven Documentation](https://maven.apache.org/guides/)
- [Docker Documentation](https://docs.docker.com/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
