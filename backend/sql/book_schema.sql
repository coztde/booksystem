-- 图书管理系统（book）数据库建表脚本
-- 目标：满足需求文档 xq.md 的最小可用模型（用户/读者类型&借阅规则/图书/借阅记录）

CREATE DATABASE IF NOT EXISTS `book`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `book`;

SET NAMES utf8mb4;

-- 读者类型（同时承载借阅规则）
CREATE TABLE IF NOT EXISTS `reader_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '类型名称（如：本科生/研究生/教师）',
  `max_borrow` int NOT NULL DEFAULT 5 COMMENT '最大可借数量',
  `borrow_days` int NOT NULL DEFAULT 30 COMMENT '借阅期限（天）',
  `max_renew` int NOT NULL DEFAULT 1 COMMENT '最大续借次数',
  `description` varchar(255) DEFAULT NULL COMMENT '说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reader_type_name` (`name`),
  CONSTRAINT `ck_reader_type_rule` CHECK (`max_borrow` >= 0 AND `borrow_days` > 0 AND `max_renew` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='读者类型与借阅规则';

-- 用户（管理员/读者）
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role` tinyint NOT NULL COMMENT '角色 1管理员 2读者',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名（管理员登录）',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号（读者注册登录）',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `code` varchar(30) DEFAULT NULL COMMENT '学号/工号',
  `reader_type_id` bigint DEFAULT NULL COMMENT '读者类型ID（管理员可为空）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0冻结/禁用 1正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  UNIQUE KEY `uk_user_code` (`code`),
  KEY `idx_user_role_status` (`role`, `status`),
  KEY `idx_user_reader_type` (`reader_type_id`),
  CONSTRAINT `fk_user_reader_type` FOREIGN KEY (`reader_type_id`) REFERENCES `reader_type` (`id`),
  CONSTRAINT `ck_user_role` CHECK (`role` IN (1, 2)),
  CONSTRAINT `ck_user_login` CHECK ((`role` = 1 AND `username` IS NOT NULL) OR (`role` = 2 AND `phone` IS NOT NULL)),
  CONSTRAINT `ck_user_reader_type` CHECK (`role` = 1 OR `reader_type_id` IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户（管理员/读者）';

-- 图书
CREATE TABLE IF NOT EXISTS `book` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `cover_url` varchar(255) DEFAULT NULL COMMENT '封面图URL（可选）',
  `title` varchar(200) NOT NULL COMMENT '书名',
  `author` varchar(100) DEFAULT NULL COMMENT '作者',
  `publisher` varchar(100) DEFAULT NULL COMMENT '出版社',
  `isbn` varchar(20) DEFAULT NULL COMMENT 'ISBN',
  `category` varchar(50) DEFAULT NULL COMMENT '分类（用于馆藏资源页筛选）',
  `location` varchar(100) DEFAULT NULL COMMENT '馆藏位置',
  `description` varchar(1000) DEFAULT NULL COMMENT '简介',
  `total_qty` int NOT NULL DEFAULT 0 COMMENT '总数量',
  `available_qty` int NOT NULL DEFAULT 0 COMMENT '可借数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0下架/不可借 1上架/可借',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_book_isbn` (`isbn`),
  KEY `idx_book_category` (`category`),
  KEY `idx_book_title` (`title`),
  KEY `idx_book_author` (`author`),
  KEY `idx_book_publisher` (`publisher`),
  CONSTRAINT `ck_book_qty` CHECK (`total_qty` >= 0 AND `available_qty` >= 0 AND `total_qty` >= `available_qty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书';

-- 借阅记录
CREATE TABLE IF NOT EXISTS `borrow_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '读者ID',
  `book_id` bigint NOT NULL COMMENT '图书ID',
  `borrow_at` datetime NOT NULL COMMENT '借出时间',
  `due_at` datetime NOT NULL COMMENT '应还时间',
  `return_at` datetime DEFAULT NULL COMMENT '归还时间',
  `renew_count` int NOT NULL DEFAULT 0 COMMENT '续借次数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0借出 1已还 2逾期',
  `fine_amount` decimal(10,2) DEFAULT NULL COMMENT '罚款金额（可空）',
  `handled_by` bigint DEFAULT NULL COMMENT '经办管理员ID（可空）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_borrow_user_status` (`user_id`, `status`),
  KEY `idx_borrow_book_status` (`book_id`, `status`),
  KEY `idx_borrow_due_at` (`due_at`),
  CONSTRAINT `fk_borrow_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_borrow_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
  CONSTRAINT `fk_borrow_handled_by` FOREIGN KEY (`handled_by`) REFERENCES `user` (`id`),
  CONSTRAINT `ck_borrow_status` CHECK (`status` IN (0, 1, 2)),
  CONSTRAINT `ck_borrow_time` CHECK (`due_at` >= `borrow_at` AND (`return_at` IS NULL OR `return_at` >= `borrow_at`)),
  CONSTRAINT `ck_borrow_renew` CHECK (`renew_count` >= 0),
  CONSTRAINT `ck_borrow_fine` CHECK (`fine_amount` IS NULL OR `fine_amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录';

-- 门户内容（轮播/新闻/公告）
CREATE TABLE IF NOT EXISTS `portal_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` tinyint NOT NULL COMMENT '类型 1轮播 2新闻 3公告',
  `tag` varchar(30) DEFAULT NULL COMMENT '标签（轮播用）',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `subtitle` varchar(400) DEFAULT NULL COMMENT '副标题/摘要（轮播或列表用）',
  `content` text DEFAULT NULL COMMENT '正文',
  `cover_url` varchar(255) DEFAULT NULL COMMENT '封面图片（可选）',
  `accent` varchar(30) DEFAULT NULL COMMENT '强调色（可选，如#B88A2C或rgba(...)）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `publish_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0下线 1上线',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_portal_post_type_status_sort` (`type`, `status`, `sort`),
  KEY `idx_portal_post_publish_time` (`publish_time`),
  CONSTRAINT `ck_portal_post_type` CHECK (`type` IN (1, 2, 3)),
  CONSTRAINT `ck_portal_post_status` CHECK (`status` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门户内容';

-- 可选：初始化基础读者类型（如需可取消注释）
-- INSERT INTO `reader_type` (`name`, `max_borrow`, `borrow_days`, `max_renew`, `description`)
-- VALUES
--   ('默认', 5, 30, 1, '默认借阅规则')
-- ON DUPLICATE KEY UPDATE
--   `max_borrow` = VALUES(`max_borrow`),
--   `borrow_days` = VALUES(`borrow_days`),
--   `max_renew` = VALUES(`max_renew`),
--   `description` = VALUES(`description`);

-- 示例数据请执行：backend/sql/book_seed.sql
