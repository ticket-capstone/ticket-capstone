CREATE TABLE `PerformanceSeat` (
`performance_seat_id`	LONG	NOT NULL,
`price`	LONG	NULL,
`status`	VARCHAR(255)	NULL,
`version`	LONG	NULL,
`lock_until`	DATETIME	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`event_id`	LONG	NOT NULL,
`seat_id`	LONG	NOT NULL
);

CREATE TABLE `EVENT` (
`event_id`	LONG	NOT NULL,
`name`	VARCHAR(255)	NULL,
`description`	VARCHAR(255)	NULL,
`event_type`	VARCHAR(255)	NULL,
`start_date`	DATETIME	NULL,
`end_date`	DATETIME	NULL,
`category`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL
);

CREATE TABLE `Ticket` (
`ticket_id`	LONG	NOT NULL,
`access_code`	LONG	NULL,
`status`	VARCHAR(255)	NULL,
`issued_at`	DATETIME	NULL,
`used_at`	DATETIME	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`order_item_id`	LONG	NOT NULL
);

CREATE TABLE `UserSession` (
`session_id`	LONG	NOT NULL,
`token`	VARCHAR(255)	NULL,
`ip_address`	VARCHAR(255)	NULL,
`expires_at`	DATETIME	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`user_id`	LONG	NOT NULL
);

CREATE TABLE `SeatReservationLog` (
`log_id`	LONG	NOT NULL,
`action`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`timestamp`	DATETIME	NULL,
`ip_address`	VARCHAR(255)	NULL,
`error_message`	VARCHAR(255)	NULL,
`user_id`	LONG	NOT NULL,
`performance_seat_id`	LONG	NOT NULL
);

CREATE TABLE `WaitingQueue` (
`queue_id`	LONG	NOT NULL,
`entry_time`	DATETIME	NULL,
`position`	LONG	NULL,
`status`	VARCHAR(255)	NULL,
`processing_started_at`	DATETIME	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`user_id`	LONG	NOT NULL,
`event_id`	LONG	NOT NULL
);

CREATE TABLE `Seat` (
`seat_id`	LONG	NOT NULL,
`row_name`	VARCHAR(255)	NULL,
`seat_number`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`section_id`	LONG	NOT NULL
);

CREATE TABLE `Order` (
`order_id`	LONG	NOT NULL,
`total_amount`	LONG	NULL,
`payment_status`	VARCHAR(255)	NULL,
`order_status`	VARCHAR(255)	NULL,
`order_date`	DATETIME	NULL,
`payment_method`	VARCHAR(255)	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`user_id`	LONG	NOT NULL
);

CREATE TABLE `Section` (
`section_id`	LONG	NOT NULL,
`name`	VARCHAR(255)	NULL,
`capacity`	LONG	NULL,
`price_category`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL
);

CREATE TABLE `OrderItem` (
`order_item_id`	LONG	NOT NULL,
`price`	LONG	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`order_id`	LONG	NOT NULL,
`performance_seat_id`	LONG	NOT NULL
);

CREATE TABLE `Users` (
`user_id`   LONG    NOT NULL AUTO_INCREMENT,
`username`  VARCHAR(255)   NULL,  -- 아이디 필드 추가
`email`     VARCHAR(255)   NULL,
`password`  VARCHAR(255)   NULL,
`name`      VARCHAR(255)   NULL,
`phone`     VARCHAR(255)   NULL,
`status`    VARCHAR(255)   NULL,
`created_at`    DATETIME   NULL,
`updated_at`    DATETIME   NULL
);

ALTER TABLE `Users` ADD CONSTRAINT `UK_USERS_USERNAME` UNIQUE (`username`);

ALTER TABLE `PerformanceSeat` ADD CONSTRAINT `PK_PERFORMANCESEAT` PRIMARY KEY (
`performance_seat_id`
);

ALTER TABLE `EVENT` ADD CONSTRAINT `PK_EVENT` PRIMARY KEY (
`event_id`
);

ALTER TABLE `Ticket` ADD CONSTRAINT `PK_TICKET` PRIMARY KEY (
`ticket_id`
);

ALTER TABLE `UserSession` ADD CONSTRAINT `PK_USERSESSION` PRIMARY KEY (
`session_id`
);

ALTER TABLE `SeatReservationLog` ADD CONSTRAINT `PK_SEATRESERVATIONLOG` PRIMARY KEY (
`log_id`
);

ALTER TABLE `WaitingQueue` ADD CONSTRAINT `PK_WAITINGQUEUE` PRIMARY KEY (
`queue_id`
);

ALTER TABLE `Seat` ADD CONSTRAINT `PK_SEAT` PRIMARY KEY (
`seat_id`
);

ALTER TABLE `Order` ADD CONSTRAINT `PK_ORDER` PRIMARY KEY (
`order_id`
);

ALTER TABLE `Section` ADD CONSTRAINT `PK_SECTION` PRIMARY KEY (
`section_id`
);

ALTER TABLE `OrderItem` ADD CONSTRAINT `PK_ORDERITEM` PRIMARY KEY (
`order_item_id`
);

ALTER TABLE `Users` ADD CONSTRAINT `PK_USERS` PRIMARY KEY (
`user_id`
);

ALTER TABLE `PerformanceSeat` ADD CONSTRAINT `FK_EVENT_TO_PERFORMANCESEAT` FOREIGN KEY (
`event_id`
) REFERENCES `EVENT` (
`event_id`
);

ALTER TABLE `PerformanceSeat` ADD CONSTRAINT `FK_SEAT_TO_PERFORMANCESEAT` FOREIGN KEY (
`seat_id`
) REFERENCES `Seat` (
`seat_id`
);

ALTER TABLE `Ticket` ADD CONSTRAINT `FK_ORDERITEM_TO_TICKET` FOREIGN KEY (
`order_item_id`
) REFERENCES `OrderItem` (
`order_item_id`
);

ALTER TABLE `UserSession` ADD CONSTRAINT `FK_USERS_TO_USERSESSION` FOREIGN KEY (
`user_id`
) REFERENCES `Users` (
`user_id`
);

ALTER TABLE `SeatReservationLog` ADD CONSTRAINT `FK_USERS_TO_SEATRESERVATIONLOG` FOREIGN KEY (
`user_id`
) REFERENCES `Users` (
`user_id`
);

ALTER TABLE `SeatReservationLog` ADD CONSTRAINT `FK_PERFORMANCESEAT_TO_SEATRESERVATIONLOG` FOREIGN KEY (
`performance_seat_id`
) REFERENCES `PerformanceSeat` (
`performance_seat_id`
);

ALTER TABLE `WaitingQueue` ADD CONSTRAINT `FK_USERS_TO_WAITINGQUEUE` FOREIGN KEY (
`user_id`
) REFERENCES `Users` (
`user_id`
);

ALTER TABLE `WaitingQueue` ADD CONSTRAINT `FK_EVENT_TO_WAITINGQUEUE` FOREIGN KEY (
`event_id`
) REFERENCES `EVENT` (
`event_id`
);

ALTER TABLE `Seat` ADD CONSTRAINT `FK_SECTION_TO_SEAT` FOREIGN KEY (
`section_id`
) REFERENCES `Section` (
`section_id`
);

ALTER TABLE `Order` ADD CONSTRAINT `FK_USERS_TO_ORDER` FOREIGN KEY (
`user_id`
) REFERENCES `Users` (
`user_id`
);

ALTER TABLE `OrderItem` ADD CONSTRAINT `FK_ORDER_TO_ORDERITEM` FOREIGN KEY (
`order_id`
) REFERENCES `Order` (
`order_id`
);

ALTER TABLE `OrderItem` ADD CONSTRAINT `FK_PERFORMANCESEAT_TO_ORDERITEM` FOREIGN KEY (
`performance_seat_id`
) REFERENCES `PerformanceSeat` (
`performance_seat_id`
);

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