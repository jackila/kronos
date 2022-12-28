CREATE TABLE `student`
(
    `id`          INT NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(45) NULL,
    `sex`         TINYINT(1) NULL,
    `age`         INT NULL,
    `grade_id`       INT NULL,
    `create_time` TIMESTAMP NULL,
    `update_time` TIMESTAMP NULL,
    PRIMARY KEY (`id`)
);


CREATE TABLE `optional_course`
(
    `id`          int(10) unsigned NOT NULL AUTO_INCREMENT,
    `no`          varchar(45) DEFAULT NULL COMMENT '课程编号',
    `name`        varchar(45) DEFAULT NULL COMMENT '课程名',
    `redit`       varchar(45) DEFAULT NULL COMMENT '学分',
    `teacher_id`  int(11) DEFAULT NULL COMMENT '授课老师',
    `student_id` int(11) DEFAULT NULL COMMENT '学生id',
    `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `grade`
(
    `id`          INT NOT NULL COMMENT 'id',
    `grade_name`  VARCHAR(45) NULL COMMENT '班级名',
    `create_time` TIMESTAMP NULL COMMENT '创建时间',
    `update_time` TIMESTAMP NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
);

CREATE TABLE `teacher_info`
(
    `id`          INT NOT NULL,
    `name`        VARCHAR(45) NULL COMMENT '名字',
    `age`         VARCHAR(45) NULL COMMENT '年龄',
    `sex`         TINYINT NULL COMMENT '性别',
    `create_time` TIMESTAMP NULL,
    `update_time` TIMESTAMP NULL,
    PRIMARY KEY (`id`)
);
