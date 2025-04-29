
INSERT INTO EVENT (event_id, name, description, event_type, start_date, end_date, category, status, created_at, updated_at)
VALUES
(1, '아이유의 10주년 전국·아시아 투어 콘서트 : 이 지금 dlwlrma', '아이유의 데뷔 10주년 기념 투어 콘서트', 'CONCERT', '2025-04-22 19:30:00', '2025-04-24 19:30:00', '콘서트', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, '2025 IU HEREH WORLD TOUR CONCERT : THE WINNING', '2024 아이유 HEREH 월드투어 콘서트', 'CONCERT', '2025-04-15 19:30:00', '2025-05-15 19:30:00', '콘서트', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, '팬텀', '뮤지컬 〈팬텀〉 10주년 기념 공연', 'MUSICAL', '2025-05-01 19:30:00', '2025-06-15 19:30:00', '뮤지컬', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());



-- 구역 정보 추가
INSERT INTO Section (section_id, name, capacity, price_category, status, created_at, updated_at)
VALUES
(1, 'A구역', 100, 'VIP', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'B구역', 150, 'R', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'C구역', 200, 'S', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 각 구역별 좌석 몇 개만 샘플 데이터로 추가
-- A구역 좌석
INSERT INTO Seat (seat_id, row_name, seat_number, status, created_at, updated_at, section_id)
VALUES
(1, 'A', '1', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1),
(2, 'A', '2', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1),
(3, 'B', '1', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1),
(4, 'B', '2', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1);

-- B구역 좌석
INSERT INTO Seat (seat_id, row_name, seat_number, status, created_at, updated_at, section_id)
VALUES
(5, 'A', '1', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2),
(6, 'A', '2', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2),
(7, 'B', '1', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2),
(8, 'B', '2', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2);

-- C구역 좌석
INSERT INTO Seat (seat_id, row_name, seat_number, status, created_at, updated_at, section_id)
VALUES
(9, 'A', '1', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3),
(10, 'A', '2', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3),
(11, 'B', '1', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3),
(12, 'B', '2', 'AVAILABLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3);

-- 공연 좌석 정보 연결 (EVENT ID가 1인 공연에 대해)
INSERT INTO PerformanceSeat (performance_seat_id, price, status, version, created_at, updated_at, event_id, seat_id)
VALUES
-- A구역 좌석 (VIP 가격)
(1, 150000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 1),
(2, 150000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 2),
(3, 150000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 3),
(4, 150000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 4),

-- B구역 좌석 (R 가격)
(5, 120000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 5),
(6, 120000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 6),
(7, 120000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 7),
(8, 120000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 8),

-- C구역 좌석 (S 가격)
(9, 90000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 9),
(10, 90000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 10),
(11, 90000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 11),
(12, 90000, 'AVAILABLE', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 12);


-- 사용자 데이터 삽입
INSERT INTO Users (username, email, password, name, phone, status, created_at, updated_at)
VALUES ('donghyun', 'user01@example.com', '1234', 'Alice', '010-1111-1111', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 좌석 잠금 상태 설정
UPDATE PERFORMANCESEAT
SET STATUS = 'LOCKED',
    LOCK_UNTIL = DATEADD('MINUTE', 5, CURRENT_TIMESTAMP())
WHERE PERFORMANCE_SEAT_ID = 1;

