-- =============================================================================
-- HPCRM - Dữ liệu test v2.0
-- Flyway migration: V100__seed_test_data.sql
--
-- Mật khẩu tất cả user: Test@123
--
-- Đặt tại: src/main/resources/db/migration/local/V100__seed_test_data.sql
-- Chỉ chạy khi profile = local
-- =============================================================================

-- =============================================================================
-- USERS (6 users)
--
-- admin@hungphu.vn        | ADMIN
-- duc.tran@hungphu.vn     | MANAGER  (khu vực Q1-Q3)
-- nhung.le@hungphu.vn     | MANAGER  (khu vực Bình Thạnh - Thủ Đức)
-- khai.pham@hungphu.vn    | EMPLOYEE (thuộc Manager Đức)
-- tuan.hoang@hungphu.vn   | EMPLOYEE (thuộc Manager Đức)
-- huy.do@hungphu.vn       | EMPLOYEE (thuộc Manager Nhung)
-- =============================================================================
INSERT INTO users (id, full_name, email, phone, address, dob, password_hash, role, is_active, manager_id, created_by) VALUES

(UUID_TO_BIN('11111111-1111-1111-1111-111111111111'),
 N'Nguyễn Hùng Phú', 'admin@hungphu.vn', '0900000001',
 N'Tòa nhà Hùng Phú, 15 Nguyễn Trãi, Q1, TP.HCM', '1985-03-15',
 '$2a$10$cqldyGtwDnAR5doDpDJuwOv9KlKfyhE/5NCLlXc65yznWkxS/uI5O',
 'ADMIN', 1, NULL, NULL),

(UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 N'Trần Minh Đức', 'duc.tran@hungphu.vn', '0900000002',
 N'45 Lê Duẩn, Q1, TP.HCM', '1990-07-20',
 '$2a$10$cqldyGtwDnAR5doDpDJuwOv9KlKfyhE/5NCLlXc65yznWkxS/uI5O',
 'MANAGER', 1, NULL,
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

(UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
 N'Lê Thị Hồng Nhung', 'nhung.le@hungphu.vn', '0900000003',
 N'120 Điện Biên Phủ, Bình Thạnh, TP.HCM', '1992-11-05',
 '$2a$10$cqldyGtwDnAR5doDpDJuwOv9KlKfyhE/5NCLlXc65yznWkxS/uI5O',
 'MANAGER', 1, NULL,
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

(UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 N'Phạm Văn Khải', 'khai.pham@hungphu.vn', '0900000004',
 N'78 Nguyễn Thị Minh Khai, Q3, TP.HCM', '1995-01-10',
 '$2a$10$cqldyGtwDnAR5doDpDJuwOv9KlKfyhE/5NCLlXc65yznWkxS/uI5O',
 'EMPLOYEE', 1,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222')),

(UUID_TO_BIN('55555555-5555-5555-5555-555555555555'),
 N'Hoàng Anh Tuấn', 'tuan.hoang@hungphu.vn', '0900000005',
 N'200 Cách Mạng Tháng 8, Q3, TP.HCM', '1996-08-22',
 '$2a$10$cqldyGtwDnAR5doDpDJuwOv9KlKfyhE/5NCLlXc65yznWkxS/uI5O',
 'EMPLOYEE', 1,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222')),

(UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 N'Đỗ Quang Huy', 'huy.do@hungphu.vn', '0900000006',
 N'55 Xô Viết Nghệ Tĩnh, Bình Thạnh, TP.HCM', '1997-04-18',
 '$2a$10$cqldyGtwDnAR5doDpDJuwOv9KlKfyhE/5NCLlXc65yznWkxS/uI5O',
 'EMPLOYEE', 1,
 UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
 UUID_TO_BIN('33333333-3333-3333-3333-333333333333'));

-- =============================================================================
-- CUSTOMERS (6 khách hàng)
-- =============================================================================
INSERT INTO customers (id, full_name, phone, address, elevator_type, project_type, assigned_user_id, created_by) VALUES

(UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
 N'Nguyễn Thanh Sơn', '0912345001',
 N'123 Lê Lợi, Phường Bến Nghé, Q1, TP.HCM',
 'GIA_DINH', 'XAY_MOI',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444')),

(UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
 N'Trần Thị Mai Lan', '0912345002',
 N'456 Nguyễn Huệ, Phường Bến Nghé, Q1, TP.HCM',
 'KINH', 'CAI_TAO',
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555')),

(UUID_TO_BIN('cccccccc-cccc-cccc-cccc-cccccccccccc'),
 N'Lê Hoàng Nam', '0912345003',
 N'789 Đinh Tiên Hoàng, Phường 1, Bình Thạnh, TP.HCM',
 'HOMELIFT', 'XAY_MOI',
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666')),

(UUID_TO_BIN('dddddddd-dddd-dddd-dddd-dddddddddddd'),
 N'Phạm Thị Ngọc Ánh', '0912345004',
 N'321 Cách Mạng Tháng 8, Phường 12, Q3, TP.HCM',
 'GIA_DINH', 'CAI_TAO',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444')),

(UUID_TO_BIN('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'),
 N'Hoàng Văn Thắng', '0912345005',
 N'654 Võ Văn Tần, Phường 5, Q3, TP.HCM',
 'KINH', 'XAY_MOI',
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555')),

(UUID_TO_BIN('ffffffff-ffff-ffff-ffff-ffffffffffff'),
 N'Đỗ Thị Phương Thảo', '0912345006',
 N'987 Trần Hưng Đạo, Phường 1, Q5, TP.HCM',
 'HOMELIFT', 'CAI_TAO',
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'));

-- =============================================================================
-- CONSULTATIONS (8 tư vấn - bao phủ tất cả status)
-- =============================================================================
INSERT INTO consultations (id, customer_id, customer_name, customer_phone, site_address, priority, price, notes, status, failure_reason, assigned_by, assigned_to, accepted_at) VALUES

-- CHO_TIEP_NHAN
(UUID_TO_BIN('c0000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('dddddddd-dddd-dddd-dddd-dddddddddddd'),
 N'Phạm Thị Ngọc Ánh', '0912345004',
 N'321 Cách Mạng Tháng 8, Phường 12, Q3, TP.HCM',
 'CAO', NULL,
 N'Khách hỏi về thang máy gia đình 3 tầng, nhà phố mặt tiền 4m',
 'CHO_TIEP_NHAN', NULL,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'), NULL),

-- DA_TIEP_NHAN
(UUID_TO_BIN('c0000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'),
 N'Hoàng Văn Thắng', '0912345005',
 N'654 Võ Văn Tần, Phường 5, Q3, TP.HCM',
 'TRUNG_BINH', NULL,
 N'Tư vấn thang máy kinh doanh cho tòa văn phòng 7 tầng',
 'DA_TIEP_NHAN', NULL,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'),
 '2026-05-20 08:30:00'),

-- DA_LIEN_LAC
(UUID_TO_BIN('c0000001-0000-0000-0000-000000000003'),
 UUID_TO_BIN('ffffffff-ffff-ffff-ffff-ffffffffffff'),
 N'Đỗ Thị Phương Thảo', '0912345006',
 N'987 Trần Hưng Đạo, Phường 1, Q5, TP.HCM',
 'THAP', NULL,
 N'Khách muốn lắp homelift cho biệt thự 3 tầng có sân vườn',
 'DA_LIEN_LAC', NULL,
 UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 '2026-05-18 09:00:00'),

-- CHUA_LIEN_LAC_DUOC
(UUID_TO_BIN('c0000001-0000-0000-0000-000000000004'),
 NULL,
 N'Vũ Đình Công', '0912345007',
 N'12 Nguyễn Thị Minh Khai, Phường Đa Kao, Q1, TP.HCM',
 'TRUNG_BINH', NULL,
 N'Lead từ website, gọi 3 lần không nghe máy',
 'CHUA_LIEN_LAC_DUOC', NULL,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 '2026-05-15 10:00:00'),

-- DANG_BAO_GIA
(UUID_TO_BIN('c0000001-0000-0000-0000-000000000005'),
 UUID_TO_BIN('cccccccc-cccc-cccc-cccc-cccccccccccc'),
 N'Lê Hoàng Nam', '0912345003',
 N'789 Đinh Tiên Hoàng, Phường 1, Bình Thạnh, TP.HCM',
 'CAO', NULL,
 N'Khách đã xem catalogue, đang chờ báo giá chi tiết homelift Cibes A5000',
 'DANG_BAO_GIA', NULL,
 UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 '2026-05-10 14:00:00'),

-- DA_CHUYEN_DU_AN (từ consultation thành công → project 1)
(UUID_TO_BIN('c0000001-0000-0000-0000-000000000006'),
 UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
 N'Nguyễn Thanh Sơn', '0912345001',
 N'123 Lê Lợi, Phường Bến Nghé, Q1, TP.HCM',
 'CAO', 280000000,
 N'Khách đồng ý lắp thang máy gia đình 4 tầng, tải trọng 450kg',
 'DA_CHUYEN_DU_AN', NULL,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 '2026-04-01 09:00:00'),

-- DA_CHUYEN_DU_AN (từ consultation thành công → project 2)
(UUID_TO_BIN('c0000001-0000-0000-0000-000000000007'),
 UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
 N'Trần Thị Mai Lan', '0912345002',
 N'456 Nguyễn Huệ, Phường Bến Nghé, Q1, TP.HCM',
 'TRUNG_BINH', 350000000,
 N'Cải tạo thang máy kinh doanh cũ 5 tầng, thay cabin và hệ thống điều khiển',
 'DA_CHUYEN_DU_AN', NULL,
 UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'),
 '2026-03-15 10:30:00'),

-- THAT_BAI
(UUID_TO_BIN('c0000001-0000-0000-0000-000000000008'),
 NULL,
 N'Bùi Minh Tú', '0912345008',
 N'55 Lý Tự Trọng, Phường Bến Nghé, Q1, TP.HCM',
 'THAP', NULL,
 N'Khách hỏi giá thăm dò, so sánh nhiều nhà cung cấp',
 'THAT_BAI',
 N'Khách chọn nhà cung cấp khác vì giá thấp hơn 15%',
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'),
 '2026-02-20 15:00:00');

-- =============================================================================
-- PROJECTS (3 dự án)
-- =============================================================================
INSERT INTO projects (id, name, customer_id, consultation_id, elevator_type, project_type, project_status, supervisor_id, created_by) VALUES

-- Project 1: đang giám sát xây dựng
(UUID_TO_BIN('b0000001-0000-0000-0000-000000000001'),
 N'TM-GD-001 Thang máy gia đình Nguyễn Thanh Sơn',
 UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
 UUID_TO_BIN('c0000001-0000-0000-0000-000000000006'),
 'GIA_DINH', 'XAY_MOI', 'GIAM_SAT_XAY_DUNG',
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

-- Project 2: đang thi công
(UUID_TO_BIN('b0000001-0000-0000-0000-000000000002'),
 N'TM-KD-002 Cải tạo thang kinh doanh Trần Thị Mai Lan',
 UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
 UUID_TO_BIN('c0000001-0000-0000-0000-000000000007'),
 'KINH', 'CAI_TAO', 'THI_CONG',
 UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

-- Project 3: đang bảo trì
(UUID_TO_BIN('b0000001-0000-0000-0000-000000000003'),
 N'TM-HL-003 Homelift biệt thự Lê Hoàng Nam',
 UUID_TO_BIN('cccccccc-cccc-cccc-cccc-cccccccccccc'),
 NULL,
 'HOMELIFT', 'XAY_MOI', 'BAO_TRI',
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111'));

-- =============================================================================
-- PAYMENT INSTALLMENTS (7 đợt thanh toán)
-- =============================================================================
INSERT INTO payment_installments (id, project_id, installment_no, amount, payment_date, notes, created_by) VALUES

-- Project 1: 3 đợt (2 đã thanh toán, 1 chưa)
(UUID_TO_BIN('d0000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000001'), 1,
 84000000, '2026-04-05', N'Đặt cọc 30% hợp đồng',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

(UUID_TO_BIN('d0000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000001'), 2,
 112000000, '2026-04-20', N'Thanh toán đợt 2 sau giám sát xây dựng',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

(UUID_TO_BIN('d0000001-0000-0000-0000-000000000003'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000001'), 3,
 84000000, NULL, N'Thanh toán cuối khi bàn giao',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

-- Project 2: 2 đợt (1 đã thanh toán, 1 chưa)
(UUID_TO_BIN('d0000001-0000-0000-0000-000000000004'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000002'), 1,
 140000000, '2026-03-20', N'Đặt cọc 40% hợp đồng cải tạo',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

(UUID_TO_BIN('d0000001-0000-0000-0000-000000000005'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000002'), 2,
 210000000, NULL, N'Thanh toán sau khi thi công hoàn tất',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

-- Project 3: 2 đợt (đã thanh toán hết)
(UUID_TO_BIN('d0000001-0000-0000-0000-000000000006'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000003'), 1,
 200000000, '2025-10-01', N'Đặt cọc 50% homelift',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

(UUID_TO_BIN('d0000001-0000-0000-0000-000000000007'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000003'), 2,
 200000000, '2025-12-15', N'Thanh toán bàn giao homelift',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111'));

-- =============================================================================
-- PROJECT DOCUMENTS (4 tài liệu)
-- =============================================================================
INSERT INTO project_documents (id, project_id, label, file_url, file_type, uploaded_by) VALUES

(UUID_TO_BIN('e0000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000001'),
 N'Bản vẽ thiết kế hố thang tầng 1',
 '/uploads/projects/b0000001/docs/design_floor1.pdf', 'PDF',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444')),

(UUID_TO_BIN('e0000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000002'),
 N'Ảnh hiện trạng thang máy cũ',
 '/uploads/projects/b0000002/docs/old_elevator.jpg', 'IMAGE',
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555')),

(UUID_TO_BIN('e0000001-0000-0000-0000-000000000003'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000003'),
 N'Hợp đồng thi công homelift',
 '/uploads/projects/b0000003/docs/contract.pdf', 'PDF',
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

(UUID_TO_BIN('e0000001-0000-0000-0000-000000000004'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000003'),
 N'Ảnh bàn giao hoàn công',
 '/uploads/projects/b0000003/docs/handover.jpg', 'IMAGE',
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'));

-- =============================================================================
-- TASKS (8 task - bao phủ 4 task_type × 5 status)
-- =============================================================================
INSERT INTO tasks (id, project_id, title, site_address, deadline, task_type, status, rejection_reason, assigned_by, assigned_to, supervisor_id, completed_at) VALUES

-- ── Project 1: GIAM_SAT_XAY_DUNG ──

-- Task 1: Giám sát XD → HOAN_THANH
(UUID_TO_BIN('f0000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000001'),
 N'Giám sát xây dựng hố thang nhà anh Sơn',
 N'123 Lê Lợi, Phường Bến Nghé, Q1, TP.HCM',
 '2026-05-30', 'GIAM_SAT_XAY_DUNG', 'HOAN_THANH', NULL,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 '2026-05-15 16:00:00'),

-- Task 2: Thi công → CHO_DANH_GIA
(UUID_TO_BIN('f0000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000001'),
 N'Thi công lắp đặt hố thang tầng 1-2',
 N'123 Lê Lợi, Phường Bến Nghé, Q1, TP.HCM',
 '2026-06-15', 'THI_CONG', 'CHO_DANH_GIA', NULL,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 NULL),

-- Task 8: Thi công cabin → CHUA_THUC_HIEN
(UUID_TO_BIN('f0000001-0000-0000-0000-000000000008'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000001'),
 N'Thi công lắp đặt cabin và cửa tầng 3-4',
 N'123 Lê Lợi, Phường Bến Nghé, Q1, TP.HCM',
 '2026-07-15', 'THI_CONG', 'CHUA_THUC_HIEN', NULL,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 NULL),

-- ── Project 2: THI_CONG ──

-- Task 3: Giám sát tháo dỡ → DANG_THUC_HIEN
(UUID_TO_BIN('f0000001-0000-0000-0000-000000000003'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000002'),
 N'Giám sát tháo dỡ thang máy cũ tòa nhà',
 N'456 Nguyễn Huệ, Phường Bến Nghé, Q1, TP.HCM',
 '2026-05-28', 'GIAM_SAT_XAY_DUNG', 'DANG_THUC_HIEN', NULL,
 UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'),
 UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
 NULL),

-- Task 4: Thi công cabin → TU_CHOI
(UUID_TO_BIN('f0000001-0000-0000-0000-000000000004'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000002'),
 N'Thi công lắp ray dẫn hướng và cabin mới',
 N'456 Nguyễn Huệ, Phường Bến Nghé, Q1, TP.HCM',
 '2026-05-20', 'THI_CONG', 'TU_CHOI',
 N'Cabin giao sai kích thước (rộng 1.2m thay vì 1.4m), cần đặt lại nhà cung cấp',
 UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'),
 UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
 NULL),

-- ── Project 3: BAO_TRI ──

-- Task 5: Bàn giao → HOAN_THANH
(UUID_TO_BIN('f0000001-0000-0000-0000-000000000005'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000003'),
 N'Bàn giao homelift cho anh Lê Hoàng Nam',
 N'789 Đinh Tiên Hoàng, Phường 1, Bình Thạnh, TP.HCM',
 '2025-11-30', 'BAN_GIAO', 'HOAN_THANH', NULL,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 '2025-11-28 10:00:00'),

-- Task 6: Bảo trì Q1 → HOAN_THANH
(UUID_TO_BIN('f0000001-0000-0000-0000-000000000006'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000003'),
 N'Bảo trì định kỳ Q1/2026 homelift biệt thự',
 N'789 Đinh Tiên Hoàng, Phường 1, Bình Thạnh, TP.HCM',
 '2026-03-31', 'BAO_TRI', 'HOAN_THANH', NULL,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 '2026-03-15 17:00:00'),

-- Task 7: Bảo trì Q3 → CHUA_THUC_HIEN (sắp tới)
(UUID_TO_BIN('f0000001-0000-0000-0000-000000000007'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000003'),
 N'Bảo trì định kỳ Q3/2026 homelift biệt thự',
 N'789 Đinh Tiên Hoàng, Phường 1, Bình Thạnh, TP.HCM',
 '2026-09-30', 'BAO_TRI', 'CHUA_THUC_HIEN', NULL,
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 NULL);

-- =============================================================================
-- TASK MEMBERS (LEAD + MEMBER)
-- =============================================================================
INSERT INTO task_members (id, task_id, user_id, member_role) VALUES

(UUID_TO_BIN('ab000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'), 'LEAD'),

(UUID_TO_BIN('ab000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'), 'LEAD'),

(UUID_TO_BIN('ab000001-0000-0000-0000-000000000003'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000003'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'), 'LEAD'),

(UUID_TO_BIN('ab000001-0000-0000-0000-000000000004'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000004'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'), 'LEAD'),

(UUID_TO_BIN('ab000001-0000-0000-0000-000000000005'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000005'),
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'), 'LEAD'),

(UUID_TO_BIN('ab000001-0000-0000-0000-000000000006'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000006'),
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'), 'LEAD'),

(UUID_TO_BIN('ab000001-0000-0000-0000-000000000007'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000007'),
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'), 'LEAD'),

(UUID_TO_BIN('ab000001-0000-0000-0000-000000000008'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000008'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'), 'LEAD'),

-- MEMBER phụ cho task 2 (test team nhiều người)
(UUID_TO_BIN('ab000001-0000-0000-0000-000000000009'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'), 'MEMBER');

-- =============================================================================
-- TASK EVIDENCES
-- =============================================================================
INSERT INTO task_evidences (id, task_id, file_url, file_type, uploaded_by) VALUES

(UUID_TO_BIN('a0000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000001'),
 '/uploads/tasks/f0000001/evidence/survey_photo1.jpg', 'IMAGE',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444')),

(UUID_TO_BIN('a0000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000001'),
 '/uploads/tasks/f0000001/evidence/survey_report.pdf', 'PDF',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444')),

(UUID_TO_BIN('a0000001-0000-0000-0000-000000000003'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000005'),
 '/uploads/tasks/f0000005/evidence/handover_photo.jpg', 'IMAGE',
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666')),

(UUID_TO_BIN('a0000001-0000-0000-0000-000000000004'),
 UUID_TO_BIN('f0000001-0000-0000-0000-000000000006'),
 '/uploads/tasks/f0000006/evidence/maintenance_check.jpg', 'IMAGE',
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'));

-- =============================================================================
-- MAINTENANCE CONTRACTS (2 hợp đồng)
-- =============================================================================
INSERT INTO maintenance_contracts (id, project_id, customer_id, start_date, end_date, status, assigned_to, created_by) VALUES

-- Contract 1: còn hạn dài (project 3 - homelift)
(UUID_TO_BIN('90000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000003'),
 UUID_TO_BIN('cccccccc-cccc-cccc-cccc-cccccccccccc'),
 '2026-01-01', '2027-12-31', 'MOI',
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111')),

-- Contract 2: sắp hết hạn (project 1)
(UUID_TO_BIN('90000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('b0000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
 '2024-01-01', '2026-06-30', 'SAP_HET_HAN',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 UUID_TO_BIN('11111111-1111-1111-1111-111111111111'));

-- =============================================================================
-- MAINTENANCE SCHEDULES (4 lịch bảo trì)
-- =============================================================================
INSERT INTO maintenance_schedules (id, contract_id, scheduled_date, status, assigned_to, completed_at) VALUES

-- Contract 1: 1 đã xong, 1 sắp tới
(UUID_TO_BIN('80000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('90000001-0000-0000-0000-000000000001'),
 '2026-03-15', 'HOAN_THANH',
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 '2026-03-15 10:30:00'),

(UUID_TO_BIN('80000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('90000001-0000-0000-0000-000000000001'),
 '2026-09-15', 'CHO_THUC_HIEN',
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 NULL),

-- Contract 2: 1 đã xong, 1 sắp tới
(UUID_TO_BIN('80000001-0000-0000-0000-000000000003'),
 UUID_TO_BIN('90000001-0000-0000-0000-000000000002'),
 '2025-07-20', 'HOAN_THANH',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 '2025-07-20 14:00:00'),

(UUID_TO_BIN('80000001-0000-0000-0000-000000000004'),
 UUID_TO_BIN('90000001-0000-0000-0000-000000000002'),
 '2026-05-28', 'CHO_THUC_HIEN',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 NULL);

-- =============================================================================
-- MAINTENANCE EVIDENCES
-- =============================================================================
INSERT INTO maintenance_evidences (id, schedule_id, file_url, file_type, uploaded_by) VALUES

(UUID_TO_BIN('70000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('80000001-0000-0000-0000-000000000001'),
 '/uploads/maintenance/80000001/check_photo.jpg', 'IMAGE',
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666')),

(UUID_TO_BIN('70000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('80000001-0000-0000-0000-000000000003'),
 '/uploads/maintenance/80000003/check_photo.jpg', 'IMAGE',
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'));

-- =============================================================================
-- NOTIFICATIONS (6 thông báo)
-- =============================================================================
INSERT INTO notifications (id, user_id, type, title, body, ref_type, ref_id, is_read, scheduled_at, sent_at) VALUES

-- TASK_ASSIGNED → NV Khải (chưa đọc)
(UUID_TO_BIN('60000001-0000-0000-0000-000000000001'),
 UUID_TO_BIN('44444444-4444-4444-4444-444444444444'),
 'TASK_ASSIGNED',
 N'Bạn được giao công việc mới',
 N'Công việc "Thi công lắp đặt hố thang tầng 1-2" đã được giao cho bạn. Hạn: 15/06/2026.',
 'TASK', UUID_TO_BIN('f0000001-0000-0000-0000-000000000002'),
 0, '2026-05-01 08:00:00', '2026-05-01 08:00:00'),

-- TASK_COMPLETED → Manager Đức (đã đọc)
(UUID_TO_BIN('60000001-0000-0000-0000-000000000002'),
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 'TASK_COMPLETED',
 N'Công việc hoàn thành',
 N'NV Phạm Văn Khải đã hoàn thành "Giám sát xây dựng hố thang nhà anh Sơn". Vui lòng đánh giá.',
 'TASK', UUID_TO_BIN('f0000001-0000-0000-0000-000000000001'),
 1, '2026-05-15 16:05:00', '2026-05-15 16:05:00'),

-- CONSULTATION_ASSIGNED → NV Tuấn (chưa đọc)
(UUID_TO_BIN('60000001-0000-0000-0000-000000000003'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'),
 'CONSULTATION_ASSIGNED',
 N'Bạn được giao tư vấn mới',
 N'Tư vấn KH Hoàng Văn Thắng (0912345005) đã được giao cho bạn. Ưu tiên: Trung bình.',
 'CONSULTATION', UUID_TO_BIN('c0000001-0000-0000-0000-000000000002'),
 0, '2026-05-20 08:35:00', '2026-05-20 08:35:00'),

-- TASK_REJECTED → NV Tuấn (chưa đọc)
(UUID_TO_BIN('60000001-0000-0000-0000-000000000004'),
 UUID_TO_BIN('55555555-5555-5555-5555-555555555555'),
 'TASK_REJECTED',
 N'Công việc bị từ chối',
 N'CV "Thi công lắp ray dẫn hướng và cabin mới" bị từ chối. Lý do: Cabin sai kích thước.',
 'TASK', UUID_TO_BIN('f0000001-0000-0000-0000-000000000004'),
 0, '2026-05-21 09:00:00', '2026-05-21 09:00:00'),

-- MAINTENANCE_REMINDER → NV Huy (chưa đọc, chưa gửi)
(UUID_TO_BIN('60000001-0000-0000-0000-000000000005'),
 UUID_TO_BIN('66666666-6666-6666-6666-666666666666'),
 'MAINTENANCE_REMINDER',
 N'Nhắc nhở bảo trì sắp đến hạn',
 N'Lịch bảo trì 15/09/2026 cho dự án "TM-HL-003 Homelift biệt thự Lê Hoàng Nam" sắp đến hạn.',
 'MAINTENANCE_SCHEDULE', UUID_TO_BIN('80000001-0000-0000-0000-000000000002'),
 0, '2026-08-16 07:00:00', NULL),

-- MAINTENANCE_REMINDER → Manager Đức (đã đọc)
(UUID_TO_BIN('60000001-0000-0000-0000-000000000006'),
 UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
 'MAINTENANCE_REMINDER',
 N'Hợp đồng bảo trì sắp hết hạn',
 N'HĐ bảo trì KH Nguyễn Thanh Sơn hết hạn 30/06/2026. Vui lòng liên hệ gia hạn.',
 'MAINTENANCE_CONTRACT', UUID_TO_BIN('90000001-0000-0000-0000-000000000002'),
 1, '2026-05-24 07:00:00', '2026-05-24 07:00:00');