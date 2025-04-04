CREATE TABLE `PerformanceSeat` (
`performance_seat_id`	INT	NOT NULL,
`price`	INT	NULL,
`status`	VARCHAR(255)	NULL,
`version`	INT	NULL,
`lock_until`	DATETIME	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`event_id`	INT	NOT NULL,
`seat_id`	INT	NOT NULL
);

CREATE TABLE `EVENT` (
`event_id`	INT	NOT NULL,
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
`ticket_id`	INT	NOT NULL,
`access_code`	INT	NULL,
`status`	VARCHAR(255)	NULL,
`issued_at`	DATETIME	NULL,
`used_at`	DATETIME	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`order_item_id`	INT	NOT NULL
);

CREATE TABLE `UserSession` (
`session_id`	INT	NOT NULL,
`token`	VARCHAR(255)	NULL,
`ip_address`	VARCHAR(255)	NULL,
`expires_at`	DATETIME	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`user_id`	INT	NOT NULL
);

CREATE TABLE `SeatReservationLog` (
`log_id`	INT	NOT NULL,
`action`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`timestamp`	DATETIME	NULL,
`ip_address`	VARCHAR(255)	NULL,
`error_message`	VARCHAR(255)	NULL,
`user_id`	INT	NOT NULL,
`performance_seat_id`	INT	NOT NULL
);

CREATE TABLE `WaitingQueue` (
`queue_id`	INT	NOT NULL,
`entry_time`	DATETIME	NULL,
`position`	INT	NULL,
`status`	VARCHAR(255)	NULL,
`processing_started_at`	DATETIME	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`user_id`	INT	NOT NULL,
`event_id`	INT	NOT NULL
);

CREATE TABLE `Seat` (
`seat_id`	INT	NOT NULL,
`row_name`	VARCHAR(255)	NULL,
`seat_number`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`section_id`	INT	NOT NULL
);

CREATE TABLE `Order` (
`order_id`	INT	NOT NULL,
`total_amount`	INT	NULL,
`payment_status`	VARCHAR(255)	NULL,
`order_status`	VARCHAR(255)	NULL,
`order_date`	DATETIME	NULL,
`payment_method`	VARCHAR(255)	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`user_id`	INT	NOT NULL
);

CREATE TABLE `Section` (
`section_id`	INT	NOT NULL,
`name`	VARCHAR(255)	NULL,
`capacity`	INT	NULL,
`price_category`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL
);

CREATE TABLE `OrderItem` (
`order_item_id`	INT	NOT NULL,
`price`	INT	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL,
`order_id`	INT	NOT NULL,
`performance_seat_id`	INT	NOT NULL
);

CREATE TABLE `Users` (
`user_id`	INT	NOT NULL,
`email`	VARCHAR(255)	NULL,
`password`	VARCHAR(255)	NULL,
`name`	VARCHAR(255)	NULL,
`phone`	VARCHAR(255)	NULL,
`status`	VARCHAR(255)	NULL,
`created_at`	DATETIME	NULL,
`updated_at`	DATETIME	NULL
);

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