# EventPulse - Complete Project Index

## 📖 Quick Navigation

### 🚀 Getting Started
- [README.md](README.md) - **Start here!** Quick start guide and feature overview
- [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - One-page cheat sheet for daily tasks

### 📚 Detailed Documentation
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - Complete API reference with code examples
- [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) - Docker deployment and production guide
- [CICD_DOCUMENTATION.md](CICD_DOCUMENTATION.md) - CI/CD workflows and automation
- [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - Project overview and architecture

### 🔧 Technical Specifications
- [design/openapi.yaml](design/openapi.yaml) - OpenAPI 3.0 API specification
- [pom.xml](pom.xml) - Maven dependencies and build configuration
- [Dockerfile](Dockerfile) - Multi-stage Docker build instructions
- [docker-compose.yml](docker-compose.yml) - Container orchestration

### 🔄 CI/CD Workflows
- [.github/workflows/build.yml](.github/workflows/build.yml) - Main build pipeline
- [.github/workflows/pr-check.yml](.github/workflows/pr-check.yml) - Pull request validation
- [.github/workflows/release.yml](.github/workflows/release.yml) - Release automation
- [.github/workflows/dependency-update.yml](.github/workflows/dependency-update.yml) - Dependency checks
- [.github/workflows/README.md](.github/workflows/README.md) - Workflows documentation

### 🛠️ Scripts
- [docker-manage.ps1](docker-manage.ps1) - PowerShell Docker management script

## 📂 Source Code Structure

### Application Code (`src/main/java/com/eventpulse/`)

#### Configuration (`config/`)
- [OpenApiConfig.java](src/main/java/com/eventpulse/config/OpenApiConfig.java) - Swagger/OpenAPI configuration
- [SecurityConfig.java](src/main/java/com/eventpulse/config/SecurityConfig.java) - Spring Security & JWT configuration
- [WebSocketConfig.java](src/main/java/com/eventpulse/config/WebSocketConfig.java) - STOMP WebSocket configuration

#### Controllers (`controller/`)
- [AuthController.java](src/main/java/com/eventpulse/controller/AuthController.java) - `/api/auth/login` endpoint
- [EventController.java](src/main/java/com/eventpulse/controller/EventController.java) - `/api/events` CRUD endpoints
- [HealthController.java](src/main/java/com/eventpulse/controller/HealthController.java) - `/api/health` endpoint
- [MetricsController.java](src/main/java/com/eventpulse/controller/MetricsController.java) - `/api/metrics` endpoint

#### Data Transfer Objects (`dto/`)
- [EventRequest.java](src/main/java/com/eventpulse/dto/EventRequest.java) - Event creation request
- [EventResponse.java](src/main/java/com/eventpulse/dto/EventResponse.java) - Event response
- [LoginRequest.java](src/main/java/com/eventpulse/dto/LoginRequest.java) - Login credentials
- [LoginResponse.java](src/main/java/com/eventpulse/dto/LoginResponse.java) - JWT token response
- [MetricsResponse.java](src/main/java/com/eventpulse/dto/MetricsResponse.java) - Metrics aggregation

#### Domain Models (`entity/`)
- [Event.java](src/main/java/com/eventpulse/entity/Event.java) - Event JPA entity with UUID, validation

#### Exception Handling (`exception/`)
- [GlobalExceptionHandler.java](src/main/java/com/eventpulse/exception/GlobalExceptionHandler.java) - @RestControllerAdvice
- [InvalidRequestException.java](src/main/java/com/eventpulse/exception/InvalidRequestException.java) - Custom exception

#### Data Access (`repository/`)
- [EventRepository.java](src/main/java/com/eventpulse/repository/EventRepository.java) - JPA repository with JPQL queries

#### Security (`security/`)
- [JwtAuthenticationFilter.java](src/main/java/com/eventpulse/security/JwtAuthenticationFilter.java) - JWT token filter
- [JwtUtils.java](src/main/java/com/eventpulse/security/JwtUtils.java) - JWT generation and validation

#### Business Logic (`service/`)
- [EventService.java](src/main/java/com/eventpulse/service/EventService.java) - Event operations with filtering
- [MetricsService.java](src/main/java/com/eventpulse/service/MetricsService.java) - Metrics aggregation logic

### Test Code (`src/test/java/com/eventpulse/`)

#### Controller Tests
- [EventControllerTest.java](src/test/java/com/eventpulse/controller/EventControllerTest.java) - MockMvc unit tests (10 tests)
- [EventControllerIntegrationTest.java](src/test/java/com/eventpulse/controller/EventControllerIntegrationTest.java) - Full integration tests (9 tests)
- [TestSecurityConfig.java](src/test/java/com/eventpulse/controller/TestSecurityConfig.java) - Test security configuration

#### Service Tests
- [EventServiceTest.java](src/test/java/com/eventpulse/service/EventServiceTest.java) - Event service unit tests (10 tests)
- [MetricsServiceTest.java](src/test/java/com/eventpulse/service/MetricsServiceTest.java) - Metrics service unit tests (6 tests)

### Configuration Files

#### Application Configuration (`src/main/resources/`)
- [application.yaml](src/main/resources/application.yaml) - Default configuration (H2 database)
- [application-docker.yaml](src/main/resources/application-docker.yaml) - Docker/PostgreSQL configuration
- [application-h2.yaml](src/main/resources/application-h2.yaml) - H2 development configuration

#### Static Resources
- [ws-client.html](src/main/resources/static/ws-client.html) - WebSocket test client

## 🎯 Quick Access by Task

### I want to...

#### ...start developing
→ [README.md#Quick Start](README.md#-quick-start)

#### ...understand the API
→ [API_DOCUMENTATION.md](API_DOCUMENTATION.md)  
→ http://localhost:8080/swagger-ui.html

#### ...deploy with Docker
→ [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)  
→ `docker compose up -d --build`

#### ...set up CI/CD
→ [CICD_DOCUMENTATION.md](CICD_DOCUMENTATION.md)  
→ [.github/workflows/](.github/workflows/)

#### ...run tests
→ `mvn test`  
→ [EventServiceTest.java](src/test/java/com/eventpulse/service/EventServiceTest.java)

#### ...find a specific command
→ [QUICK_REFERENCE.md](QUICK_REFERENCE.md)

#### ...understand the architecture
→ [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)

#### ...integrate with the API
→ [API_DOCUMENTATION.md#Integration Examples](API_DOCUMENTATION.md#integration-examples)  
→ [design/openapi.yaml](design/openapi.yaml)

#### ...monitor the application
→ http://localhost:8080/actuator/health  
→ [API_DOCUMENTATION.md#Actuator Endpoints](API_DOCUMENTATION.md#actuator-endpoints)

#### ...test WebSocket
→ http://localhost:8080/ws-client.html  
→ [API_DOCUMENTATION.md#WebSocket Support](API_DOCUMENTATION.md#websocket-support)

## 📊 Project Metrics

### Documentation
- **Total Documentation Files**: 8
- **Total Lines of Documentation**: ~5,000+
- **Code Examples**: 50+
- **PowerShell Scripts**: 200+ lines

### Source Code
- **Java Classes**: 21
- **Test Classes**: 5
- **Total Lines of Code**: ~2,500
- **Test Coverage**: High (35 tests)

### Features
- **API Endpoints**: 7
- **WebSocket Topics**: 1
- **Actuator Endpoints**: 6+
- **Database Tables**: 1
- **CI/CD Workflows**: 4

## 🔗 External Resources

### Running Application
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/api/health
- **Actuator**: http://localhost:8080/actuator
- **H2 Console**: http://localhost:8080/h2-console
- **WebSocket Client**: http://localhost:8080/ws-client.html

### Container Registry
- **GHCR**: ghcr.io/\<owner\>/eventpulse-ai-augmented

### Development Tools
- **Maven Central**: https://mvnrepository.com/
- **Spring Initializr**: https://start.spring.io/
- **Swagger Editor**: https://editor.swagger.io/

## 🎓 Learning Path

### For Beginners
1. Start with [README.md](README.md)
2. Follow Quick Start guide
3. Test API using [Swagger UI](http://localhost:8080/swagger-ui.html)
4. Read [QUICK_REFERENCE.md](QUICK_REFERENCE.md)

### For Developers
1. Review [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) for architecture
2. Study [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
3. Explore source code in `src/main/java/`
4. Review test examples in `src/test/java/`

### For DevOps
1. Read [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)
2. Study [CICD_DOCUMENTATION.md](CICD_DOCUMENTATION.md)
3. Review [docker-compose.yml](docker-compose.yml)
4. Examine workflows in `.github/workflows/`

### For API Consumers
1. View [Swagger UI](http://localhost:8080/swagger-ui.html)
2. Download [openapi.yaml](design/openapi.yaml)
3. Read [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
4. Try integration examples

## 🔍 Find by Technology

### Spring Boot
- Controllers: `src/main/java/com/eventpulse/controller/`
- Configuration: `src/main/java/com/eventpulse/config/`
- Main class: [EventPulseApplication.java](src/main/java/com/eventpulse/EventPulseApplication.java)

### Spring Security & JWT
- Security Config: [SecurityConfig.java](src/main/java/com/eventpulse/config/SecurityConfig.java)
- JWT Filter: [JwtAuthenticationFilter.java](src/main/java/com/eventpulse/security/JwtAuthenticationFilter.java)
- JWT Utils: [JwtUtils.java](src/main/java/com/eventpulse/security/JwtUtils.java)

### Spring Data JPA
- Entity: [Event.java](src/main/java/com/eventpulse/entity/Event.java)
- Repository: [EventRepository.java](src/main/java/com/eventpulse/repository/EventRepository.java)
- Service: [EventService.java](src/main/java/com/eventpulse/service/EventService.java)

### Spring WebSocket
- WebSocket Config: [WebSocketConfig.java](src/main/java/com/eventpulse/config/WebSocketConfig.java)
- Broadcasting: [EventController.java#L72](src/main/java/com/eventpulse/controller/EventController.java)
- Client Example: [ws-client.html](src/main/resources/static/ws-client.html)

### OpenAPI/Swagger
- Configuration: [OpenApiConfig.java](src/main/java/com/eventpulse/config/OpenApiConfig.java)
- Specification: [openapi.yaml](design/openapi.yaml)
- Annotations: All controllers

### Testing
- Service Tests: `src/test/java/com/eventpulse/service/`
- Controller Tests: `src/test/java/com/eventpulse/controller/`
- Test Config: [TestSecurityConfig.java](src/test/java/com/eventpulse/controller/TestSecurityConfig.java)

### Docker
- Build: [Dockerfile](Dockerfile)
- Orchestration: [docker-compose.yml](docker-compose.yml)
- Management: [docker-manage.ps1](docker-manage.ps1)
- Exclusions: [.dockerignore](.dockerignore)

### CI/CD
- Main Build: [.github/workflows/build.yml](.github/workflows/build.yml)
- PR Checks: [.github/workflows/pr-check.yml](.github/workflows/pr-check.yml)
- Releases: [.github/workflows/release.yml](.github/workflows/release.yml)
- Dependencies: [.github/workflows/dependency-update.yml](.github/workflows/dependency-update.yml)

## 📞 Support & Help

### Common Tasks

| Task | Command | Documentation |
|------|---------|---------------|
| Start app (dev) | `mvn spring-boot:run` | [README.md](README.md) |
| Start app (Docker) | `docker compose up -d --build` | [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) |
| Run tests | `mvn test` | [README.md](README.md) |
| View API docs | Open http://localhost:8080/swagger-ui.html | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) |
| Get JWT token | Login via Swagger or PowerShell | [QUICK_REFERENCE.md](QUICK_REFERENCE.md) |
| Check health | `curl http://localhost:8080/actuator/health` | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) |
| Deploy | `docker compose up -d` | [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) |
| Release | `git tag v1.0.0 && git push origin v1.0.0` | [CICD_DOCUMENTATION.md](CICD_DOCUMENTATION.md) |

### Documentation by Audience

#### Developers
1. [README.md](README.md) - Setup and quick start
2. [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - API integration
3. [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Daily commands
4. Source code in `src/main/java/`

#### DevOps Engineers
1. [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) - Deployment guide
2. [CICD_DOCUMENTATION.md](CICD_DOCUMENTATION.md) - CI/CD setup
3. [docker-compose.yml](docker-compose.yml) - Container config
4. [Dockerfile](Dockerfile) - Build instructions

#### API Consumers
1. [Swagger UI](http://localhost:8080/swagger-ui.html) - Interactive docs
2. [design/openapi.yaml](design/openapi.yaml) - API contract
3. [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - Integration examples

#### Project Managers
1. [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - Overview and metrics
2. [README.md](README.md) - Feature list
3. [CICD_DOCUMENTATION.md](CICD_DOCUMENTATION.md) - Automation

## 🎯 Feature Matrix

| Feature | Status | Documentation | Code Location |
|---------|--------|---------------|---------------|
| Event CRUD | ✅ Complete | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | [EventController.java](src/main/java/com/eventpulse/controller/EventController.java) |
| JWT Auth | ✅ Complete | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | [SecurityConfig.java](src/main/java/com/eventpulse/config/SecurityConfig.java) |
| WebSocket | ✅ Complete | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | [WebSocketConfig.java](src/main/java/com/eventpulse/config/WebSocketConfig.java) |
| Metrics | ✅ Complete | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | [MetricsService.java](src/main/java/com/eventpulse/service/MetricsService.java) |
| Filtering | ✅ Complete | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | [EventService.java](src/main/java/com/eventpulse/service/EventService.java) |
| Validation | ✅ Complete | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | [Event.java](src/main/java/com/eventpulse/entity/Event.java) |
| OpenAPI | ✅ Complete | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | [OpenApiConfig.java](src/main/java/com/eventpulse/config/OpenApiConfig.java) |
| Actuator | ✅ Complete | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | [application.yaml](src/main/resources/application.yaml) |
| Docker | ✅ Complete | [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) | [Dockerfile](Dockerfile) |
| CI/CD | ✅ Complete | [CICD_DOCUMENTATION.md](CICD_DOCUMENTATION.md) | [.github/workflows/](.github/workflows/) |
| Tests | ✅ Complete | Test files | `src/test/java/` |

## 📦 Deliverables Checklist

### Code
- [x] Spring Boot application
- [x] RESTful API endpoints
- [x] WebSocket support
- [x] JWT authentication
- [x] Input validation
- [x] Exception handling
- [x] JPA entities and repositories
- [x] Service layer
- [x] 35 comprehensive tests

### Configuration
- [x] application.yaml (H2)
- [x] application-docker.yaml (PostgreSQL)
- [x] SecurityConfig (JWT)
- [x] WebSocketConfig (STOMP)
- [x] OpenApiConfig (Swagger)

### Docker
- [x] Multi-stage Dockerfile
- [x] docker-compose.yml (PostgreSQL + Backend)
- [x] .dockerignore
- [x] Health checks configured
- [x] Resource limits set

### CI/CD
- [x] Build workflow
- [x] PR check workflow
- [x] Release workflow
- [x] Dependency update workflow
- [x] GHCR integration

### Documentation
- [x] README.md (main)
- [x] API_DOCUMENTATION.md (API reference)
- [x] DOCKER_DEPLOYMENT.md (Docker guide)
- [x] CICD_DOCUMENTATION.md (CI/CD guide)
- [x] QUICK_REFERENCE.md (cheat sheet)
- [x] PROJECT_SUMMARY.md (overview)
- [x] INDEX.md (this file)
- [x] design/openapi.yaml (API spec)

### Additional Files
- [x] docker-manage.ps1 (management script)
- [x] ws-client.html (WebSocket client)
- [x] .github/workflows/README.md (workflow docs)

## 🌟 Project Highlights

### Production-Ready Features
- ✅ Comprehensive error handling
- ✅ Input validation on all endpoints
- ✅ Security best practices
- ✅ Health checks and monitoring
- ✅ Graceful shutdown
- ✅ Resource limits
- ✅ Multi-platform Docker images
- ✅ Automated CI/CD

### Developer Experience
- ✅ Interactive Swagger UI (no auth popup!)
- ✅ Hot reload in development
- ✅ Clear error messages
- ✅ Comprehensive examples
- ✅ PowerShell scripts for Windows
- ✅ One-command Docker deployment

### Documentation Quality
- ✅ 8 comprehensive documentation files
- ✅ 5,000+ lines of documentation
- ✅ 50+ code examples
- ✅ Multiple formats (MD, YAML)
- ✅ Step-by-step guides
- ✅ Troubleshooting sections

## 🎓 Skills Demonstrated

### Backend Development
- Spring Boot application architecture
- RESTful API design
- WebSocket real-time communication
- JPA/Hibernate ORM
- JPQL queries and aggregations

### Security
- JWT authentication implementation
- Spring Security configuration
- Stateless session management
- Input validation and sanitization

### Testing
- Unit testing with Mockito
- Integration testing with Spring
- MockMvc for controller testing
- Test-driven development

### DevOps
- Docker containerization
- Multi-stage builds
- Docker Compose orchestration
- GitHub Actions CI/CD
- Container registry management

### Documentation
- Technical writing
- API documentation (OpenAPI)
- User guides
- Deployment documentation

## 📈 Next Steps

### For Development
1. Review source code structure
2. Run application locally
3. Test API via Swagger UI
4. Explore WebSocket client
5. Run test suite

### For Deployment
1. Review Docker deployment guide
2. Configure environment variables
3. Set up PostgreSQL
4. Deploy with Docker Compose
5. Configure monitoring

### For CI/CD
1. Fork repository
2. Configure GitHub secrets (if needed)
3. Push changes to trigger workflow
4. Review Actions tab for results
5. Create release tag

## 🏁 Conclusion

EventPulse is a **complete, production-ready application** demonstrating modern Java development practices with Spring Boot, including:

- ✅ **Full-stack capabilities** (REST + WebSocket)
- ✅ **Enterprise security** (JWT)
- ✅ **Comprehensive testing** (35 tests)
- ✅ **Complete documentation** (8 files)
- ✅ **Automated CI/CD** (4 workflows)
- ✅ **Container-ready** (Docker + Compose)
- ✅ **Production monitoring** (Actuator)
- ✅ **API documentation** (Swagger + OpenAPI)

**Ready to run, ready to deploy, ready for production!** 🚀

---

*For questions or issues, check the relevant documentation file or open a GitHub issue.*

