-- H2 데이터베이스용 스키마 (User 테이블 이름 수정)

-- User 테이블 이름을 USERS로 변경 (예약어 회피)
CREATE TABLE `USERS` (
`user_id`	INT	NOT NULL AUTO_INCREMENT,
`email`	VARCHAR(255)	NULL,
`password`	VARCHAR(255)	NULL,
`name`	VARCHAR(255)	NULL,
`phone`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
PRIMARY KEY (`user_id`)
);

CREATE TABLE `UserSession` (
`session_id`	INT	NOT NULL AUTO_INCREMENT,
`token`	VARCHAR(255)	NULL,
`ip_address`	VARCHAR(255)	NULL,
`expires_at`	TIMESTAMP	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
`user_id`	INT	NOT NULL,
PRIMARY KEY (`session_id`)
);

CREATE TABLE `OrderItem` (
`order_item_id`	INT	NOT NULL AUTO_INCREMENT,
`price`	INT	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
`order_id`	INT	NOT NULL,
`performance_seat_id`	INT	NOT NULL,
PRIMARY KEY (`order_item_id`)
);

CREATE TABLE `Venue` (
`venue_id`	INT	NOT NULL AUTO_INCREMENT,
`name`	VARCHAR(255)	NULL,
`address`	VARCHAR(255)	NULL,
`capacity`	INT	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
PRIMARY KEY (`venue_id`)
);

CREATE TABLE `Section` (
`section_id`	INT	NOT NULL AUTO_INCREMENT,
`name`	VARCHAR(255)	NULL,
`capacity`	INT	NULL,
`price_category`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
`venue_id`	INT	NOT NULL,
PRIMARY KEY (`section_id`)
);

CREATE TABLE `Seat` (
`seat_id`	INT	NOT NULL AUTO_INCREMENT,
`row_name`	VARCHAR(255)	NULL,
`seat_number`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
`section_id`	INT	NOT NULL,
PRIMARY KEY (`seat_id`)
);

CREATE TABLE `PaymentTransaction` (
`transaction_id`	INT	NOT NULL AUTO_INCREMENT,
`amount`	INT	NULL,
`payment_method`	VARCHAR(255)	NULL,
`payment_gateway`	INT	NULL,
`status`	VARCHAR(255)	NULL,
`gateway_transaction_id`	INT	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
`order_id`	INT	NOT NULL,
PRIMARY KEY (`transaction_id`)
);

CREATE TABLE `SeatReservationLog` (
`log_id`	INT	NOT NULL AUTO_INCREMENT,
`action`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`timestamp`	TIMESTAMP	NULL,
`ip_address`	VARCHAR(255)	NULL,
`error_message`	VARCHAR(255)	NULL,
`user_id`	INT	NOT NULL,
`performance_seat_id`	INT	NOT NULL,
PRIMARY KEY (`log_id`)
);

CREATE TABLE `WaitingQueue` (
`queue_id`	INT	NOT NULL AUTO_INCREMENT,
`entry_time`	TIMESTAMP	NULL,
`position`	INT	NULL,
`status`	VARCHAR(255)	NULL,
`processing_started_at`	TIMESTAMP	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
`user_id`	INT	NOT NULL,
`event_id`	INT	NOT NULL,
PRIMARY KEY (`queue_id`)
);

CREATE TABLE `Order` (
`order_id`	INT	NOT NULL AUTO_INCREMENT,
`total_amount`	INT	NULL,
`payment_status`	VARCHAR(255)	NULL,
`order_status`	VARCHAR(255)	NULL,
`order_date`	TIMESTAMP	NULL,
`payment_method`	VARCHAR(255)	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
`user_id`	INT	NOT NULL,
PRIMARY KEY (`order_id`)
);

CREATE TABLE `Ticket` (
`ticket_id`	INT	NOT NULL AUTO_INCREMENT,
`access_code`	INT	NULL,
`status`	VARCHAR(255)	NULL,
`issued_at`	TIMESTAMP	NULL,
`used_at`	TIMESTAMP	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
`order_item_id`	INT	NOT NULL,
PRIMARY KEY (`ticket_id`)
);

CREATE TABLE `EVENT` (
`event_id`	INT	NOT NULL AUTO_INCREMENT,
`name`	VARCHAR(255)	NULL,
`description`	VARCHAR(255)	NULL,
`event_type`	VARCHAR(255)	NULL,
`start_date`	TIMESTAMP	NULL,
`end_date`	TIMESTAMP	NULL,
`category`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
`venue_id`	INT	NOT NULL,
PRIMARY KEY (`event_id`)
);

CREATE TABLE `PerformanceSeat` (
`performance_seat_id`	INT	NOT NULL AUTO_INCREMENT,
`price`	INT	NULL,
`status`	VARCHAR(255)	NULL,
`version`	INT	NULL,
`lock_until`	TIMESTAMP	NULL,
`created_at`	TIMESTAMP	NULL,
`updated_at`	TIMESTAMP	NULL,
`event_id`	INT	NOT NULL,
`seat_id`	INT	NOT NULL,
PRIMARY KEY (`performance_seat_id`)
);

-- 외래 키 제약 조건 - User 테이블 참조를 USERS로 변경
ALTER TABLE UserSession ADD CONSTRAINT FK_UserSession_User FOREIGN KEY (user_id) REFERENCES USERS(user_id);
ALTER TABLE Section ADD CONSTRAINT FK_Section_Venue FOREIGN KEY (venue_id) REFERENCES Venue(venue_id);
ALTER TABLE Seat ADD CONSTRAINT FK_Seat_Section FOREIGN KEY (section_id) REFERENCES Section(section_id);
ALTER TABLE EVENT ADD CONSTRAINT FK_Event_Venue FOREIGN KEY (venue_id) REFERENCES Venue(venue_id);
ALTER TABLE PerformanceSeat ADD CONSTRAINT FK_PerformanceSeat_Event FOREIGN KEY (event_id) REFERENCES EVENT(event_id);
ALTER TABLE PerformanceSeat ADD CONSTRAINT FK_PerformanceSeat_Seat FOREIGN KEY (seat_id) REFERENCES Seat(seat_id);
ALTER TABLE `Order` ADD CONSTRAINT FK_Order_User FOREIGN KEY (user_id) REFERENCES USERS(user_id);
ALTER TABLE OrderItem ADD CONSTRAINT FK_OrderItem_Order FOREIGN KEY (order_id) REFERENCES `Order`(order_id);
ALTER TABLE OrderItem ADD CONSTRAINT FK_OrderItem_PerformanceSeat FOREIGN KEY (performance_seat_id) REFERENCES PerformanceSeat(performance_seat_id);
ALTER TABLE Ticket ADD CONSTRAINT FK_Ticket_OrderItem FOREIGN KEY (order_item_id) REFERENCES OrderItem(order_item_id);
ALTER TABLE PaymentTransaction ADD CONSTRAINT FK_PaymentTransaction_Order FOREIGN KEY (order_id) REFERENCES `Order`(order_id);
ALTER TABLE WaitingQueue ADD CONSTRAINT FK_WaitingQueue_User FOREIGN KEY (user_id) REFERENCES USERS(user_id);
ALTER TABLE WaitingQueue ADD CONSTRAINT FK_WaitingQueue_Event FOREIGN KEY (event_id) REFERENCES EVENT(event_id);
ALTER TABLE SeatReservationLog ADD CONSTRAINT FK_SeatReservationLog_User FOREIGN KEY (user_id) REFERENCES USERS(user_id);
ALTER TABLE SeatReservationLog ADD CONSTRAINT FK_SeatReservationLog_PerformanceSeat FOREIGN KEY (performance_seat_id) REFERENCES PerformanceSeat(performance_seat_id);