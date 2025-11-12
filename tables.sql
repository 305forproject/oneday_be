CREATE TABLE `CLASSES` (
                           `CLASS_ID`	INT	NOT NULL,
                           `TEACHER_ID`	INT	NOT NULL,
                           `CATEGORY_ID`	INT	NOT NULL,
                           `CLASS_NAME`	VARCHAR(50)	NULL,
                           `CLASS_DETAIL`	VARCHAR(255)	NULL,
                           `CURRICULUM`	VARCHAR(255)	NULL,
                           `INCLUDED`	VARVAHR(255)	NULL,
                           `REQUIRED`	VARVHAR(255)	NULL,
                           `LONGITUDE`	VARCHAR(20)	NULL,
                           `LATITUDE`	VARCHAR(20)	NULL,
                           `LOCATION`	VARCHAR(255)	NULL,
                           `MAX_CAPACITY`	INT	NULL,
                           `PRICE`	INT	NULL,
                           `Field`	VARCHAR(255)	NULL
);

CREATE TABLE `RESERVATIONS` (
                                `RESERVATION_ID`	INT	NOT NULL,
                                `TIME_ID`	INT	NOT NULL,
                                `STATUS_CODE`	INT	NOT NULL,
                                `STUDENT_ID`	INT	NOT NULL
);

CREATE TABLE `TIMES` (
                         `TIME_ID`	INT	NOT NULL,
                         `CLASS_ID`	INT	NOT NULL,
                         `START_AT`	DATETIME	NULL,
                         `END_AT`	DATETIME	NULL
);

CREATE TABLE `CATEGORIES` (
                              `CATEGORY_ID`	INT	NOT NULL,
                              `CATEGORY`	VARCHAR(100)	NULL
);

CREATE TABLE `PAYMENTS` (
                            `PAYMENT_ID`	INT	NOT NULL,
                            `RESERVATION_ID`	INT	NOT NULL,
                            `TOSS_ORDER_ID`	VARCHAR(255)	NULL,
                            `TOSS_PAYMENT_KEY`	VARCHAR(255)	NULL,
                            `TOSS_PAYMENT_METHOD`	ENUM	NULL,
                            `TOSS_PAYMENT_STATUS`	ENUM	NULL,
                            `REQUESTED_AT`	DATETIME	NULL,
                            `APPROVED_AT`	DATETIME	NULL,
                            `TOTAL_AMOUNT`	INT	NULL
);

CREATE TABLE `USERS` (
                         `USER_ID`	INT	NOT NULL,
                         `PASSWORD`	VARCHAR(20)	NULL,
                         `NAME`	VARCHAR(20)	NULL,
                         `ACCOUNT`	VARCHAR(20)	NOT NULL	COMMENT '선생만 존재',
                         `LOGIN_ID`	VARCHAR(20)	NOT NULL
);

CREATE TABLE `RESERVE_STATUSES` (
                                    `STATUS_CODE`	INT	NOT NULL,
                                    `STATUS_NAME`	VARCHAR(40)	NULL	COMMENT 'RESERVED
CANCELED
COMPLETED'
);

CREATE TABLE `IMAGES` (
                          `IMAGE_ID`	INT	NOT NULL,
                          `CLASS_ID`	INT	NOT NULL,
                          `IMAGE_URL`	VARCHAR(100)	NULL,
                          `IS_REPRESENTATIVE`	TINYINT(1)	NULL
);

ALTER TABLE `CLASSES` ADD CONSTRAINT `PK_CLASSES` PRIMARY KEY (
                                                               `CLASS_ID`
    );

ALTER TABLE `RESERVATIONS` ADD CONSTRAINT `PK_RESERVATIONS` PRIMARY KEY (
                                                                         `RESERVATION_ID`
    );

ALTER TABLE `TIMES` ADD CONSTRAINT `PK_TIMES` PRIMARY KEY (
                                                           `TIME_ID`
    );

ALTER TABLE `CATEGORIES` ADD CONSTRAINT `PK_CATEGORIES` PRIMARY KEY (
                                                                     `CATEGORY_ID`
    );

ALTER TABLE `PAYMENTS` ADD CONSTRAINT `PK_PAYMENTS` PRIMARY KEY (
                                                                 `PAYMENT_ID`
    );

ALTER TABLE `USERS` ADD CONSTRAINT `PK_USERS` PRIMARY KEY (
                                                           `USER_ID`
    );

ALTER TABLE `RESERVE_STATUSES` ADD CONSTRAINT `PK_RESERVE_STATUSES` PRIMARY KEY (
                                                                                 `STATUS_CODE`
    );

ALTER TABLE `IMAGES` ADD CONSTRAINT `PK_IMAGES` PRIMARY KEY (
                                                             `IMAGE_ID`
    );

