# Backend Project Rules — HungPhu CRM

## Tech Stack

| Công nghệ | Phiên bản |
|-----------|-----------|
| Language | Java 21+ |
| Framework | Spring Boot 3.x |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8.x (MySQL Workbench) |
| Build tool | Gradle |
| Authentication | JWT (stateless) |
| Deploy | Docker / Docker Compose |
| Migration | Flyway |

---

## 1. Cấu trúc project

```
src/main/java/com/hungphu/crm/
├── features/
│   ├── auth/           # JWT, login, phân quyền
│   ├── user/           # Quản lý tài khoản nhân viên
│   ├── customer/       # Quản lý khách hàng
│   ├── consultation/   # Theo dõi tư vấn (Kanban)
│   ├── project/        # Dự án, tiến độ, tài liệu, thanh toán
│   ├── task/           # Công việc (khảo sát / lắp đặt)
│   ├── maintenance/    # Hợp đồng & lịch bảo trì
│   └── notification/   # Thông báo, cron job reminder
├── shared/
│   ├── config/         # SecurityConfig, JwtConfig, CorsConfig
│   ├── exception/      # GlobalExceptionHandler, custom exceptions
│   ├── response/       # ApiResponse wrapper
│   ├── security/       # JwtFilter, JwtUtil, UserDetailsServiceImpl
│   ├── enums/          # Các enum dùng chung toàn hệ thống
│   └── utils/          # DateUtils, FileUtils, ...
└── CrmApplication.java

src/main/resources/
├── db/migration/       # Flyway scripts: V1__init.sql, V2__...sql
├── application.yml
├── application-dev.yml
└── application-prod.yml
```

**Mỗi feature folder:**

```
features/[feature-name]/
├── [Feature]Controller.java
├── [Feature]Service.java
├── [Feature]ServiceImpl.java
├── repository/
│   └── [Entity]Repository.java
├── entity/
│   └── [Entity].java
├── dto/
│   ├── [Entity]Request.java
│   └── [Entity]Response.java
├── mapper/
│   └── [Entity]Mapper.java
└── CONTEXT.md
```

---

## 2. Naming Conventions

| Element | Convention | Ví dụ |
|---------|------------|-------|
| Package | lowercase, dot-separated | `com.hungphu.crm.features.task` |
| Class | PascalCase | `TaskService`, `ConsultationController` |
| Interface | PascalCase | `TaskService`, `UserRepository` |
| Implementation | PascalCase + `Impl` | `TaskServiceImpl` |
| Method | camelCase | `findById()`, `createTask()` |
| Variable | camelCase | `userId`, `consultationList` |
| Constant | UPPER_SNAKE_CASE | `MAX_EVIDENCES`, `JWT_EXPIRATION` |
| Entity | PascalCase singular | `Task`, `MaintenanceContract` |
| DTO Request | PascalCase + `Request` | `CreateTaskRequest`, `UpdateProjectRequest` |
| DTO Response | PascalCase + `Response` | `TaskResponse`, `ProjectDetailResponse` |
| Enum | PascalCase | `TaskStatus`, `UserRole` |
| DB table | snake_case plural | `tasks`, `maintenance_contracts` |
| DB column | snake_case | `assigned_to`, `created_at` |

---

## 3. ORM — Spring Data JPA + Hibernate

Dự án dùng **Spring Data JPA** với **Hibernate** làm ORM. Đây là lựa chọn phù hợp vì:
- Schema đã thiết kế sẵn, Flyway quản lý migration → dùng `spring.jpa.hibernate.ddl-auto=validate`
- Quan hệ phức tạp (project → tasks → evidences) được map tự nhiên qua JPA annotations
- Spring Data Repository giảm boilerplate cho CRUD thông thường
- JPQL / Criteria API cho các query phức tạp (kanban, filter, report)

### Entity example

```java

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.CHUA_THUC_HIEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskEvidence> evidences = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### Repository pattern

```java
// ✅ DO: Dùng Spring Data Repository cho CRUD thông thường
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByAssignedToIdAndStatus(UUID userId, TaskStatus status);

    // ✅ DO: JPQL cho query có join
    @Query("""
        SELECT t FROM Task t
        JOIN FETCH t.project p
        JOIN FETCH t.assignedTo u
        WHERE p.id = :projectId
        ORDER BY t.createdAt DESC
        """)
    List<Task> findByProjectIdWithDetails(@Param("projectId") UUID projectId);
}

// ✅ DO: Custom repository cho query phức tạp (filter, pagination)
@Repository
@RequiredArgsConstructor
public class ConsultationRepositoryCustomImpl implements ConsultationRepositoryCustom {

    private final EntityManager em;

    public Page<Consultation> findByFilters(ConsultationFilterRequest filter, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Consultation> query = cb.createQuery(Consultation.class);
        Root<Consultation> root = query.from(Consultation.class);

        List<Predicate> predicates = new ArrayList<>();
        if (filter.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getAssignedTo() != null) {
            predicates.add(cb.equal(root.get("assignedTo").get("id"), filter.getAssignedTo()));
        }
        query.where(predicates.toArray(new Predicate[0]));
        // ... count query + return Page
    }
}

// ❌ DON'T: Viết query trong Service
// entityManager.createNativeQuery("SELECT * FROM tasks WHERE ...") // SAI chỗ
```

### Fetch strategy

```java
// ✅ DO: Luôn dùng FetchType.LAZY cho @ManyToOne và @OneToMany
@ManyToOne(fetch = FetchType.LAZY)

// ✅ DO: JOIN FETCH khi thực sự cần load relation
@Query("SELECT t FROM Task t JOIN FETCH t.assignedTo WHERE t.id = :id")

// ❌ DON'T: FetchType.EAGER — gây N+1 query
@ManyToOne(fetch = FetchType.EAGER) // TRÁNH
```

---

## 4. Feature Boundaries

| Feature | Quản lý entity | Ghi chú |
|---------|---------------|---------|
| auth | JWT, Spring Security config | Login, refresh token, phân quyền |
| user | User | Admin tạo tài khoản, phân role |
| customer | Customer | CRUD khách hàng |
| consultation | Consultation | Kanban tư vấn, giao việc |
| project | Project, PaymentInstallment, ProjectDocument | Hồ sơ dự án sau chốt hợp đồng |
| task | Task, TaskEvidence | Công việc khảo sát / lắp đặt |
| maintenance | MaintenanceContract, MaintenanceSchedule, MaintenanceEvidence | Hợp đồng + lịch 2 tháng/lần |
| notification | Notification | Lưu DB + cron job gửi reminder |

### Giao tiếp giữa các feature

```java
// ✅ DO: Inject Service qua Spring DI (cùng layer)
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final CustomerService customerService; // OK nếu không circular
}

// ✅ DO: Dùng ApplicationEventPublisher cho async / loose coupling
// Khi tư vấn thành công → tự động tạo notification
@Service
@RequiredArgsConstructor
public class ConsultationService {
    private final ApplicationEventPublisher eventPublisher;

    public void markSuccess(UUID id) {
        // ... update status
        eventPublisher.publishEvent(new ConsultationSuccessEvent(consultation));
    }
}

@Component
public class ConsultationEventListener {
    @EventListener
    @Async
    public void onConsultationSuccess(ConsultationSuccessEvent event) {
        // Tạo notification cho admin
    }
}

// ❌ DON'T: Feature gọi trực tiếp Repository của feature khác
// taskRepository từ trong MaintenanceService // SAI
```

---

## 5. API Design

### Cấu trúc URL

```
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh

GET    /api/v1/users
POST   /api/v1/users
PATCH  /api/v1/users/{id}
DELETE /api/v1/users/{id}

GET    /api/v1/consultations?status=cho_tiep_nhan&assignedTo={userId}
POST   /api/v1/consultations
PATCH  /api/v1/consultations/{id}/status
POST   /api/v1/consultations/{id}/convert     # chuyển thành dự án

GET    /api/v1/projects/{id}/tasks
POST   /api/v1/projects/{id}/tasks
PATCH  /api/v1/tasks/{id}/status

POST   /api/v1/tasks/{id}/evidences           # upload minh chứng

GET    /api/v1/maintenance/contracts
POST   /api/v1/maintenance/contracts
GET    /api/v1/maintenance/contracts/{id}/schedules
PATCH  /api/v1/maintenance/schedules/{id}/complete

GET    /api/v1/notifications
PATCH  /api/v1/notifications/{id}/read
```

### Controller example

```java
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task", description = "Quản lý công việc")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ApiResponse<TaskResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(taskService.findById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ApiResponse<TaskResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ApiResponse.success(taskService.updateStatus(id, request, currentUser));
    }

    // ❌ DON'T: Business logic trong controller
    // if (task.getEvidences().isEmpty()) throw new ... // SAI chỗ
}
```

---

## 6. Response Format

```java
// Wrapper chung
@Getter
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Object meta;       // dùng cho pagination

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }
}

// Pagination meta
@Getter
@Builder
public class PageMeta {
    private int page;
    private int limit;
    private long total;
    private int totalPages;
}
```

```json
// Success đơn lẻ
{ "success": true, "message": "Tạo công việc thành công", "data": { ... } }

// Pagination
{ "success": true, "data": [...], "meta": { "page": 1, "limit": 10, "total": 45, "totalPages": 5 } }

// Error
{ "success": false, "message": "Không tìm thấy công việc #uuid", "error": "NOT_FOUND" }
```

---

## 7. Exception Handling

```java
// ✅ DO: Custom exceptions
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " không tồn tại: " + id);
    }
}

public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}

// Ví dụ dùng trong dự án
throw new ResourceNotFoundException("Task", id);
throw new BusinessException("Task đã tiếp nhận, không thể xóa", HttpStatus.BAD_REQUEST);
throw new BusinessException("Tối đa 3 minh chứng mỗi task", HttpStatus.BAD_REQUEST);

// ✅ DO: Global handler
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage(), "NOT_FOUND"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(message, "VALIDATION_ERROR"));
    }
}
```

---

## 8. Validation (DTO)

```java
// ✅ DO: Bean Validation trong Request DTO
@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank(message = "Tên công việc không được trống")
    @Size(max = 255)
    private String title;

    @NotNull(message = "Loại công việc không được trống")
    private TaskType taskType;         

    @NotNull(message = "Người thực hiện không được trống")
    private UUID assignedTo;

    @Future(message = "Hạn phải là ngày trong tương lai")
    private LocalDate deadline;
}

// ✅ DO: @Valid trong Controller
public ApiResponse<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request)
```

---

## 9. Security & JWT

```java
// application.yml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000      # 1 ngày (ms)
  refresh-expiration: 604800000  # 7 ngày (ms)

// JwtFilter — chạy trước mọi request
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractBearerToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            UsernamePasswordAuthenticationToken auth = jwtUtil.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}

// ✅ DO: Phân quyền theo role — 3 cấp trong HungPhu CRM
@PreAuthorize("hasRole('ADMIN')")                          // chỉ Admin
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")            // Admin + Manager
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')") // tất cả

// ✅ DO: Kiểm tra ownership trong Service (không chỉ dựa vào role)
public TaskResponse findById(UUID id, UserDetailsImpl currentUser) {
    Task task = taskRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Task", id));

    // Employee chỉ xem task của mình
    if (currentUser.getRole() == UserRole.EMPLOYEE
            && !task.getAssignedTo().getId().equals(currentUser.getId())) {
        throw new AccessDeniedException("Bạn không có quyền xem task này");
    }
    return taskMapper.toResponse(task);
}
```

---

## 10. Business Logic đặc thù HungPhu CRM

### Tư vấn — giới hạn xóa

```java
// ✅ Khi đã tiếp nhận (da_tiep_nhan trở đi), không được xóa
public void deleteConsultation(UUID id) {
    Consultation consultation = findOrThrow(id);
    if (consultation.getStatus() != ConsultationStatus.CHO_TIEP_NHAN) {
        throw new BusinessException(
            "Không thể xóa tư vấn đã tiếp nhận", HttpStatus.BAD_REQUEST);
    }
    consultationRepository.delete(consultation);
}
```

### Task evidence — giới hạn 3 file

```java
// ✅ Validate trước khi upload (enforce cả ở DB trigger)
public void addEvidence(UUID taskId, MultipartFile file, UserDetailsImpl currentUser) {
    Task task = findOrThrow(taskId);
    if (task.getEvidences().size() >= 3) {
        throw new BusinessException("Tối đa 3 minh chứng mỗi task", HttpStatus.BAD_REQUEST);
    }
    if (task.getStatus() != TaskStatus.DANG_THUC_HIEN) {
        throw new BusinessException("Chỉ upload minh chứng khi task đang thực hiện",
            HttpStatus.BAD_REQUEST);
    }
    // ... lưu file, tạo TaskEvidence
}
```

### Chuyển trạng thái task

```java
// ✅ State machine — kiểm soát luồng chuyển trạng thái
public TaskResponse updateStatus(UUID id, UpdateTaskStatusRequest req,
                                  UserDetailsImpl currentUser) {
    Task task = findOrThrow(id);
    validateTransition(task.getStatus(), req.getStatus(), currentUser, task);

    if (req.getStatus() == TaskStatus.CHO_DANH_GIA) {
        // Bắt buộc có minh chứng
        if (task.getEvidences().isEmpty()) {
            throw new BusinessException(
                "Cần upload ít nhất 1 minh chứng trước khi chờ đánh giá",
                HttpStatus.BAD_REQUEST);
        }
    }

    if (req.getStatus() == TaskStatus.TU_CHOI) {
        if (!StringUtils.hasText(req.getRejectionReason())) {
            throw new BusinessException("Cần ghi rõ lý do từ chối", HttpStatus.BAD_REQUEST);
        }
        task.setRejectionReason(req.getRejectionReason());
    }

    task.setStatus(req.getStatus());
    if (req.getStatus() == TaskStatus.HOAN_THANH) {
        task.setCompletedAt(LocalDateTime.now());
    }
    return taskMapper.toResponse(taskRepository.save(task));
}

private void validateTransition(TaskStatus current, TaskStatus next,
                                  UserDetailsImpl user, Task task) {
    // Employee chỉ được: CHUA_THUC_HIEN → DANG_THUC_HIEN → CHO_DANH_GIA
    // Supervisor được: CHO_DANH_GIA → HOAN_THANH hoặc TU_CHOI
    Map<TaskStatus, Set<TaskStatus>> allowed = Map.of(
        TaskStatus.CHUA_THUC_HIEN, Set.of(TaskStatus.DANG_THUC_HIEN),
        TaskStatus.DANG_THUC_HIEN, Set.of(TaskStatus.CHO_DANH_GIA),
        TaskStatus.CHO_DANH_GIA,   Set.of(TaskStatus.HOAN_THANH, TaskStatus.TU_CHOI)
    );
    if (!allowed.getOrDefault(current, Set.of()).contains(next)) {
        throw new BusinessException(
            "Không thể chuyển từ " + current + " sang " + next, HttpStatus.BAD_REQUEST);
    }
}
```

### Bảo trì — cron job reminder

```java
// ✅ Cron chạy mỗi ngày 8:00 sáng
@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceReminderJob {

    private final MaintenanceScheduleRepository scheduleRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendReminders() {
        log.info("Running maintenance reminder job");
        LocalDate upcoming = LocalDate.now().plusDays(7);

        List<MaintenanceSchedule> schedules = scheduleRepository
            .findPendingBefore(upcoming);

        schedules.forEach(schedule ->
            notificationService.createMaintenanceReminder(schedule));
    }
}

// Repository query tương ứng
@Query("""
    SELECT s FROM MaintenanceSchedule s
    JOIN FETCH s.contract c
    JOIN FETCH s.assignedTo u
    WHERE s.status = 'CHO_THUC_HIEN'
      AND s.scheduledDate <= :upcomingDate
    """)
List<MaintenanceSchedule> findPendingBefore(@Param("upcomingDate") LocalDate upcomingDate);
```

---

## 11. Transaction Management

```java
// ✅ DO: @Transactional trên Service method khi có nhiều thao tác DB
@Transactional
public ProjectResponse convertConsultationToProject(UUID consultationId,
                                                     CreateProjectRequest req) {
    Consultation consultation = consultationRepository.findById(consultationId)
        .orElseThrow(() -> new ResourceNotFoundException("Consultation", consultationId));

    if (consultation.getStatus() != ConsultationStatus.THANH_CONG) {
        throw new BusinessException("Chỉ chuyển tư vấn thành công sang dự án",
            HttpStatus.BAD_REQUEST);
    }

    // Tạo hoặc gắn vào khách hàng cũ
    Customer customer = resolveCustomer(req, consultation);

    // Tạo project
    Project project = projectMapper.toEntity(req);
    project.setCustomer(customer);
    project.setConsultation(consultation);
    projectRepository.save(project);

    // Publish event → notification async
    eventPublisher.publishEvent(new ProjectCreatedEvent(project));

    return projectMapper.toResponse(project);
}

// ❌ DON'T: @Transactional trên Controller
// ❌ DON'T: @Transactional(readOnly = false) khi chỉ đọc — dùng readOnly = true
@Transactional(readOnly = true)
public List<TaskResponse> findByProject(UUID projectId) { ... }
```

---

## 12. File Upload

```java
// ✅ Cấu hình giới hạn file
// application.yml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 30MB

// ✅ Service upload (lưu local hoặc S3-compatible)
@Service
public class FileStorageService {

    public String store(MultipartFile file, String folder) {
        validateFileType(file); // chỉ cho image/* và application/pdf
        String filename = UUID.randomUUID() + getExtension(file.getOriginalFilename());
        // lưu file → trả về relative path: "uploads/{folder}/{filename}"
        return "uploads/" + folder + "/" + filename;
    }

    private void validateFileType(MultipartFile file) {
        List<String> allowed = List.of("image/jpeg", "image/png", "application/pdf");
        if (!allowed.contains(file.getContentType())) {
            throw new BusinessException("Chỉ chấp nhận JPG, PNG, PDF", HttpStatus.BAD_REQUEST);
        }
    }
}
```

---

## 13. Logging

```java
// ✅ DO: Dùng @Slf4j (Lombok) ở Service layer
@Service
@Slf4j
public class TaskService {
    public TaskResponse create(CreateTaskRequest req, UserDetailsImpl currentUser) {
        log.info("Creating task for project {} by user {}", req.getProjectId(), currentUser.getId());
        // ...
        log.debug("Task created: {}", task.getId());
    }
}

// ❌ DON'T: System.out.println() ở bất kỳ đâu
// ❌ DON'T: Log thông tin nhạy cảm (password, token)
```

---

## 14. Database Migration (Flyway)

```
src/main/resources/db/migration/
├── V1__create_enums_and_users.sql
├── V2__create_customers_and_consultations.sql
├── V3__create_projects_and_tasks.sql
├── V4__create_maintenance.sql
├── V5__create_notifications.sql
└── V6__add_indexes.sql
```

```yml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  jpa:
    hibernate:
      ddl-auto: validate   # KHÔNG để create/update trên prod
```

> **Quy tắc:** Mỗi khi thay đổi schema, tạo file migration mới. Không sửa file `V{n}__` đã commit.

---

## 15. Docker

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon
COPY src ./src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/hungphu_crm?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      SPRING_PROFILES_ACTIVE: prod
    depends_on:
      db:
        condition: service_healthy

  db:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: hungphu_crm
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      retries: 5

volumes:
  mysql_data:
```

---

## 16. Anti-Patterns (TRÁNH)

| ❌ TRÁNH | ✅ NÊN LÀM |
|----------|------------|
| Business logic trong Controller | Để trong Service |
| Query trong Service (JPQL/native SQL) | Dùng Repository |
| `FetchType.EAGER` | Dùng `LAZY` + JOIN FETCH khi cần |
| `ddl-auto=create` hay `update` trên prod | `validate` + Flyway migration |
| Hardcode config (secret, DB URL) | Dùng environment variable |
| Lưu mật khẩu plain text | BCrypt hash |
| `System.out.println()` | `@Slf4j` logger |
| Xóa file migration đã commit | Tạo file migration mới |
| `@Transactional` trên Controller | Chỉ dùng ở Service |
| Circular dependency giữa features | Dùng `ApplicationEventPublisher` |
| Không validate input | Bean Validation + `@Valid` |
| Expose entity trực tiếp qua API | Dùng DTO + Mapper |

---

## 17. Git Workflow

### Branch naming

```
feature/task-evidence-upload
feature/maintenance-cron-reminder
fix/consultation-kanban-status
refactor/project-service-convert-flow
```

### Commit messages

```
feat: add task evidence upload with 3-file limit
fix: correct status transition validation in task service
refactor: extract consultation-to-project convert logic
chore: add flyway migration V4 for maintenance tables
```

### PR checklist

- ✅ Gắn với issue/task tương ứng
- ✅ Build Gradle pass (`./gradlew build`)
- ✅ Không có lỗi compilation
- ✅ Unit test service layer pass
- ✅ Ít nhất 1 người review

---

## 18. Testing

| Layer | Coverage mục tiêu | Vị trí |
|-------|-------------------|--------|
| Service | 80%+ | `src/test/java/.../features/[feature]/[Feature]ServiceTest.java` |
| Controller | 70%+ | `[Feature]ControllerTest.java` (MockMvc) |
| Repository | 60%+ | `[Entity]RepositoryTest.java` (@DataJpaTest) |

```java
// ✅ Cấu trúc test
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock TaskMapper taskMapper;
    @InjectMocks TaskServiceImpl taskService;

    @Test
    @DisplayName("Không thể upload quá 3 minh chứng")
    void addEvidence_shouldThrow_whenExceedsLimit() {
        // Arrange
        Task task = mockTaskWithEvidences(3);
        when(taskRepository.findById(any())).thenReturn(Optional.of(task));

        // Act & Assert
        assertThatThrownBy(() ->
            taskService.addEvidence(task.getId(), mockFile(), mockUser()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Tối đa 3 minh chứng");
    }
}
```
