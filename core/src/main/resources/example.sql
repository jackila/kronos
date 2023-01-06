-- Copyright 2022 Ververica Inc.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--   http://www.apache.org/licenses/LICENSE-2.0
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.

-- create database
CREATE
DATABASE school_user CHARSET=UTF8;
CREATE
DATABASE course_db CHARSET=UTF8;
CREATE
DATABASE score_db CHARSET=UTF8;

-- create students and insert data
USE
school_user;

CREATE TABLE students
(
    id            INT(20) UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '学号',
    sname         VARCHAR(20) COMMENT '姓名',
    sclass        VARCHAR(10) COMMENT '班级',
    sgender       VARCHAR(10) COMMENT '性别',
    smajor        VARCHAR(20) COMMENT '专业',
    sbirthday     DATE COMMENT '出生日期',
    credit_points INT(5) COMMENT '学生已修学分'
) AUTO_INCREMENT=1;
INSERT INTO students
VALUES (default, "Sally", "3-2", "male", "CS", "2002-10-11", 4),
       (default, "George", "3-2", "female", "CS", "2002-11-1", 0),
       (default, "Edward", "3-2", "male", "CS", "2002-8-10", 5),
       (default, "Anne", "3-2", "female", "CS", "2002-1-21", 2);

-- create teachers and insert data
USE
school_user;

CREATE TABLE teachers
(
    id      INT(10) UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
    tname   VARCHAR(20) COMMENT '姓名',
    tschool VARCHAR(20) COMMENT '学院'
) AUTO_INCREMENT = 1;

INSERT INTO teachers
VALUES (default, "Thomas", "IT"),
       (default, "Bailey", "IT"),
       (default, "Walker", "IT"),
       (default, "Kretchmar", "IT");

-- create courses and insert data
USE
course_db;

CREATE TABLE courses
(
    id           INT(10) UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '课程编号',
    tid          INT(10) NOT NULL COMMENT '教师编号',
    cname        VARCHAR(20) COMMENT '课程名称',
    credit_point INT(5) COMMENT '课程学分'
) AUTO_INCREMENT = 1;

INSERT INTO courses
VALUES (default, 1, "高等数学", 10),
       (default, 2, "线性代数", 14),
       (default, 3, "概率统计", 12),
       (default, 3, "电路分析基础", 16);

-- create scores and insert data
USE
score_db;
CREATE TABLE scores
(
    id    INT(10) UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '唯一id',
    sid   INT(10) UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '学号',
    cid   INT(10) NOT NULL COMMENT '课程编号',
    score DECIMAL(5, 2) COMMENT '分数'
);

INSERT INTO scores
VALUES (default, 1, 1, 5),
       (default, 1, 2, 6),
       (default, 1, 3, 7),
       (default, 3, 1, 7),
       (default, 3, 2, 4),
       (default, 3, 3, 8);