# CLAUDE_BACKEND.md — HungPhu CRM Backend

> Đặt tại: root của repo `hungphu-crm-backend/`
> Đọc CLAUDE_GLOBAL.md trước để hiểu business context.

---

## Repo này làm gì?

REST API cho hệ thống CRM Hưng Phú — xử lý toàn bộ business logic:
auth, tư vấn, dự án, công việc, bảo trì, thông báo.

---

## Stack

| Công nghệ | Version |
|-----------|---------|
| Java | 21 |
| Spring Boot | 3.x |
| Spring Data JPA + Hibernate | — |
| MySQL | 8.x |
| Flyway | — |
| Gradle | — |
| JWT | stateless |
| Docker | — |

---

## Chạy local

```bash
# 1. Khởi động MySQL bằng Docker
docker compose up db -d

# 2. Chạy ứng dụng
./gradlew bootRun --args='--spring.profiles.active=dev'

# 3. API sẵn sàng tại
http://localhost:8080/api/v1
```

**Biến môi trường cần có** (copy từ `.env.example`):
```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=hungphu_crm
DB_USER=hungphu
DB_PASSWORD=...
JWT_SECRET=...
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

---

## Folder structure

```
src/main/java/com/hungphu/crm/
├── CrmApplication.java          # @SpringBootApplication @EnableScheduling @EnableAsync
├── shared/
│   ├── config/                  # SecurityConfig, CorsConfig, AsyncConfig
│   ├── security/                # JwtAuthFilter, JwtUtil, UserDetailsServiceImpl
│   ├── exception/               # GlobalExceptionHandler, ResourceNotFoundException, BusinessException
│   ├── response/                # ApiResponse<T>, PageMeta
│   ├── enums/                   # UserRole, TaskStatus, ConsultationStatus, ...
│   └── utils/                   # DateUtils, FileUtils
└── features/
    ├── auth/
    ├── user/
    ├── customer/
    ├── consultation/
    ├── project/
    ├── task/
    ├── maintenance/
    └── notification/

src/main/resources/
├── db/migration/                # V1__*.sql, V2__*.sql ... (Flyway)
├── application.yml
├── application-dev.yml
└── application-prod.yml
```

---

## Cấu trúc mỗi feature

```
features/[name]/
├── [Name]Controller.java
├── [Name]Service.java               # interface
├── [Name]ServiceImpl.java           # implementation + @Transactional
├── repository/
│   ├── [Entity]Repository.java      # extends JpaRepository
│   ├── [Entity]RepositoryCustom.java       # interface (nếu có filter phức tạp)
│   └── [Entity]RepositoryCustomImpl.java   # CriteriaBuilder queries
├── entity/
│   └── [Entity].java                # @Entity @Table @PrePersist @PreUpdate
├── dto/
│   ├── [Entity]Request.java         # @NotBlank @NotNull @Valid
│   └── [Entity]Response.java
├── mapper/
│   └── [Entity]Mapper.java          # Entity ↔ DTO
├── event/                           # ApplicationEvent (nếu có)
├── job/                             # @Scheduled cron (nếu có)
└── CONTEXT.md
```

---

## Patterns bắt buộc

### Entity — luôn có @PrePersist / @PreUpdate
```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

### Repository — LAZY fetch, JOIN FETCH khi cần
```java
// ✅ Luôn LAZY
@ManyToOne(fetch = FetchType.LAZY)

// ✅ JOIN FETCH khi load relation
@Query("SELECT t FROM Task t JOIN FETCH t.assignedTo WHERE t.id = :id")
```

### Service — @Transactional đúng chỗ
```java
@Transactional           // write operations
@Transactional(readOnly = true)  // read operations
// ❌ KHÔNG đặt @Transactional trên Controller
```

### Exception
```java
throw new ResourceNotFoundException("Task", id);
throw new BusinessException("Tối đa 3 minh chứng mỗi task", HttpStatus.BAD_REQUEST);
```

### Phân quyền
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
// Ownership check → xử lý trong Service, không chỉ dựa role
```

### Event async
```java
// Publish
eventPublisher.publishEvent(new ConsultationSuccessEvent(consultation));

// Listen
@EventListener @Async
public void onSuccess(ConsultationSuccessEvent event) { ... }
```

### Cron job
```java
@Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
```

---

## Business rules quan trọng — KHÔNG được bỏ qua

| Rule | Nơi enforce |
|------|-------------|
| Consultation đã `da_tiep_nhan` → không xóa được | `ConsultationServiceImpl.delete()` |
| Task `cho_danh_gia` → bắt buộc có ≥1 minh chứng | `TaskServiceImpl.updateStatus()` |
| Task evidence tối đa 3 file | `TaskServiceImpl.addEvidence()` + DB trigger |
| `tu_choi` → bắt buộc có `rejectionReason` | `TaskServiceImpl.updateStatus()` |
| `that_bai` → bắt buộc có `failureReason` | `ConsultationServiceImpl.updateStatus()` |
| Maintenance schedule sinh tự động 2 tháng/lần | `MaintenanceContractServiceImpl.create()` |
| 1 consultation_id chỉ có 1 project (UNIQUE) | DB constraint |

---

## State machine task — chỉ được chuyển theo chiều này

```
chua_thuc_hien → dang_thuc_hien
dang_thuc_hien → cho_danh_gia   (cần ≥1 evidence)
cho_danh_gia   → hoan_thanh     (chỉ supervisor)
cho_danh_gia   → tu_choi        (chỉ supervisor, cần rejectionReason)
```

## State machine consultation

```
cho_tiep_nhan → da_tiep_nhan
da_tiep_nhan  → da_lien_lac | chua_lien_lac_duoc
da_lien_lac | chua_lien_lac_duoc → dang_bao_gia
dang_bao_gia  → thanh_cong | that_bai (cần failureReason)
```

---

## API conventions

- Base URL: `/api/v1`
- UUID trong path: `/tasks/{id}` — `id` là `UUID` Java
- Pagination: `?page=1&limit=10&sort=createdAt&order=desc`
- Response wrapper: `ApiResponse<T>` cho mọi endpoint
- Error codes: `AUTH_*`, `USER_*`, `CUST_*`, `CONS_*`, `PROJ_*`, `TASK_*`, `MAINT_*`, `SYS_*`

---

## Database

- **MySQL 8.x** — xem `src/main/resources/db/migration/` cho schema
- `ddl-auto=validate` — Hibernate KHÔNG tự tạo/sửa bảng
- Schema thay đổi → tạo file `V{n}__description.sql` mới, KHÔNG sửa file cũ
- UUID sinh tại Java: `UUID.randomUUID().toString()`
- Lưu dạng `CHAR(36)` trong MySQL

---

## Lưu ý khi AI sinh code

- Mọi entity phải có `id` kiểu `UUID` (không dùng `Long` auto-increment)
- File `.java` — không dùng TypeScript hay JavaScript syntax
- Không dùng `FetchType.EAGER`
- Không viết JPQL/native query trong Service — chỉ trong Repository
- Không để business logic trong Controller
- Import đúng package Spring Boot 3.x (`jakarta.*` không phải `javax.*`)
- Mọi API response phải wrap trong `ApiResponse<T>`
- Khi tạo file migration mới: tên format `V{n}__{snake_case_description}.sql`
