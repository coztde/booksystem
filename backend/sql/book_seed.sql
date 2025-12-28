-- 图书管理系统（book）示例数据
-- 密码统一：111111aA
-- password_hash = SHA-256('111111aA') = 41c667051788249604fde272ce140ebc36f4df9285612a0a7f664de5534d9b22

USE `book`;

SET NAMES utf8mb4;

-- 兼容修复：如果你的 `book` 表是旧版本（缺少 category 字段），先自动补齐字段与索引
SET @has_category := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'book'
    AND COLUMN_NAME = 'category'
);
SET @sql := IF(
  @has_category = 0,
  'ALTER TABLE `book` ADD COLUMN `category` varchar(50) DEFAULT NULL COMMENT ''分类（用于馆藏资源页筛选）'' AFTER `isbn`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx_category := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'book'
    AND INDEX_NAME = 'idx_book_category'
);
SET @sql := IF(
  @has_idx_category = 0,
  'CREATE INDEX `idx_book_category` ON `book` (`category`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- portal_post 表可能尚未创建（例如老库只执行了旧版建表脚本），这里兜底创建
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

-- 读者类型（含借阅规则）
INSERT INTO `reader_type` (`id`, `name`, `max_borrow`, `borrow_days`, `max_renew`, `description`, `status`)
VALUES
  (1, '本科生', 10, 30, 1, '本科生借阅规则', 1),
  (2, '研究生', 15, 60, 2, '研究生借阅规则', 1),
  (3, '教职工', 20, 90, 3, '教职工借阅规则', 1)
ON DUPLICATE KEY UPDATE
  `max_borrow` = VALUES(`max_borrow`),
  `borrow_days` = VALUES(`borrow_days`),
  `max_renew` = VALUES(`max_renew`),
  `description` = VALUES(`description`),
  `status` = VALUES(`status`);

-- 用户（管理员/读者）
-- 角色：1管理员（username登录），2读者（学号code+密码登录，且 phone 必填）
INSERT INTO `user` (`id`, `role`, `username`, `phone`, `password_hash`, `name`, `code`, `reader_type_id`, `status`)
VALUES
  (9001, 1, 'admin', NULL, '41c667051788249604fde272ce140ebc36f4df9285612a0a7f664de5534d9b22', '系统管理员', NULL, NULL, 1),

  (10001, 2, NULL, '13800000001', '41c667051788249604fde272ce140ebc36f4df9285612a0a7f664de5534d9b22', '张同学', '20230001', 1, 1),
  (10002, 2, NULL, '13800000002', '41c667051788249604fde272ce140ebc36f4df9285612a0a7f664de5534d9b22', '李同学', '20230002', 1, 1),
  (10003, 2, NULL, '13800000003', '41c667051788249604fde272ce140ebc36f4df9285612a0a7f664de5534d9b22', '王同学', '20230003', 1, 1),

  (20001, 2, NULL, '13900000001', '41c667051788249604fde272ce140ebc36f4df9285612a0a7f664de5534d9b22', '陈研一', '20240001', 2, 1),
  (20002, 2, NULL, '13900000002', '41c667051788249604fde272ce140ebc36f4df9285612a0a7f664de5534d9b22', '赵研二', '20240002', 2, 1),

  (30001, 2, NULL, '13700000001', '41c667051788249604fde272ce140ebc36f4df9285612a0a7f664de5534d9b22', '周老师', 'T2025001', 3, 1)
ON DUPLICATE KEY UPDATE
  `username` = VALUES(`username`),
  `phone` = VALUES(`phone`),
  `password_hash` = VALUES(`password_hash`),
  `name` = VALUES(`name`),
  `reader_type_id` = VALUES(`reader_type_id`),
  `status` = VALUES(`status`);

-- 图书（分类用于馆藏资源页筛选）
INSERT INTO `book` (`id`, `title`, `author`, `publisher`, `isbn`, `category`, `location`, `description`, `total_qty`, `available_qty`, `status`)
VALUES
  (1, '计算机网络（第8版）', '谢希仁', '电子工业出版社', '9787121400000', '计算机', '主馆A区-TP3', '经典教材，适合课程学习与复习。', 8, 6, 1),
  (2, '算法导论（原书第3版）', 'Thomas H. Cormen', '机械工业出版社', '9787111407010', '计算机', '主馆A区-TP3', '算法与数据结构权威参考。', 5, 3, 1),
  (3, 'Java核心技术（卷I）', 'Cay S. Horstmann', '机械工业出版社', '9787110000001', '计算机', '主馆A区-TP3', 'Java语言与工程实践入门。', 6, 5, 1),
  (4, '平凡的世界', '路遥', '人民文学出版社', '9787020049295', '文学', '主馆B区-I2', '现实主义长篇小说。', 10, 10, 1),
  (5, '活着', '余华', '作家出版社', '9787506365437', '文学', '主馆B区-I2', '当代文学经典。', 9, 9, 1),
  (6, '史记', '司马迁', '中华书局', '9787101000000', '历史', '主馆C区-K2', '中国史学经典。', 6, 6, 1),
  (7, '万历十五年', '黄仁宇', '中华书局', '9787101000007', '历史', '主馆C区-K2', '以一年的切面观察制度与社会。', 7, 6, 1),
  (8, '经济学原理', 'N. Gregory Mankiw', '北京大学出版社', '9787301000000', '经济', '主馆D区-F0', '经济学入门教材。', 7, 7, 1),
  (9, '国富论', '亚当·斯密', '商务印书馆', '9787100000009', '经济', '主馆D区-F0', '古典经济学奠基之作。', 4, 4, 1),
  (10, '西方哲学史', '罗素', '商务印书馆', '9787100000000', '哲学', '主馆E区-B5', '哲学史导读。', 4, 4, 1),
  (11, '理想国', '柏拉图', '商务印书馆', '9787100000011', '哲学', '主馆E区-B5', '政治哲学经典。', 5, 5, 1)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `author` = VALUES(`author`),
  `publisher` = VALUES(`publisher`),
  `category` = VALUES(`category`),
  `location` = VALUES(`location`),
  `description` = VALUES(`description`),
  `total_qty` = VALUES(`total_qty`),
  `available_qty` = VALUES(`available_qty`),
  `status` = VALUES(`status`);

-- 门户内容（轮播/新闻/公告）
INSERT INTO `portal_post` (`id`, `type`, `tag`, `title`, `subtitle`, `content`, `accent`, `sort`, `publish_time`, `status`)
VALUES
  (11001, 1, '新学期 · 借阅指南', '借阅规则与续借说明一页读懂', '以“读者类型/借阅期限/可续借次数”为核心维度，适合课程设计演示与扩展。', '欢迎使用高校图书馆中文门户。本系统支持图书检索、借阅、归还与续借。\n\n- 新用户可先注册\n- 登录后在馆藏资源页可直接借阅\n- 在用户中心可查看当前借阅并进行续借/归还', 'rgba(215, 177, 100, 1)', 1, '2025-12-20 09:00:00', 1),
  (11002, 1, '数据库 · 文献资源', '馆藏检索：从关键词到分类筛选', '支持按分类浏览与关键词检索，数据来自 MySQL 数据库。', '馆藏资源页提供分类筛选与关键词检索，所有数据均来自数据库表 `book`。你可以自行扩展更多字段（如封面、主题词、索书号）。', 'rgba(184, 138, 44, 1)', 2, '2025-12-18 09:00:00', 1),
  (11003, 1, '读者服务 · 学术支持', '用户中心：我的借阅与到期提醒', '登录态采用 JWT，前端保存在 localStorage。', '用户中心展示读者信息与当前借阅列表，并提供续借/归还操作。后端通过 JWT 鉴权保护用户接口。', 'rgba(140, 196, 255, 1)', 3, '2025-12-16 09:00:00', 1),

  (12001, 2, NULL, '关于开放寒假自习区的通知', '寒假期间部分自习区开放，具体安排如下。', '寒假期间，图书馆开放安排如下：\n\n1. 主馆一层自习区：每日 08:30-21:30\n2. 二层阅览区：每日 09:00-17:00\n\n具体以现场公告为准。', NULL, 1, '2025-12-25 10:00:00', 1),
  (12002, 2, NULL, '图书馆信息素养讲座（第3期）报名', '本期主题：学术检索与引用管理。', '讲座时间：2025-12-28 14:00\n地点：主馆报告厅\n内容：数据库检索、文献管理工具、引用规范。', NULL, 2, '2025-12-21 10:00:00', 1),
  (12003, 2, NULL, '中文数据库试用开通：欢迎体验', '新增试用资源，覆盖多学科领域。', '即日起开通中文数据库试用，欢迎师生体验与反馈。', NULL, 3, '2025-12-18 10:00:00', 1),
  (12004, 2, NULL, '馆藏整理：部分楼层短期闭馆说明', '因馆藏整理，部分区域暂停开放。', '为提升馆藏管理质量，图书馆将对部分楼层进行整理维护，期间暂停开放。给您带来不便敬请谅解。', NULL, 4, '2025-12-12 10:00:00', 1),

  (13001, 3, NULL, '期末周座位预约规则（演示）', '期末周座位预约规则说明。', '本条为课程设计演示内容：可扩展座位预约模块与预约记录表。', NULL, 1, '2025-12-26 09:00:00', 1),
  (13002, 3, NULL, '新增图书推荐：计算机类精选书单', '推荐算法、网络、工程实践等经典书目。', '推荐书目包括：算法导论、计算机网络、Java核心技术等。', NULL, 2, '2025-12-22 09:00:00', 1),
  (13003, 3, NULL, '网络维护：今晚23:00起短暂停机', '系统维护公告。', '今晚 23:00-23:30 将进行网络维护，期间服务可能短暂不可用。', NULL, 3, '2025-12-20 09:00:00', 1),
  (13004, 3, NULL, '读者证补办流程与费用说明', '读者证补办说明。', '读者证遗失可在服务台补办，按规定收取工本费。', NULL, 4, '2025-12-16 09:00:00', 1),
  (13005, 3, NULL, '关于规范借阅与逾期处理的提示', '请按时归还图书，避免逾期。', '逾期将影响后续借阅权限；如需续借请在到期前办理。', NULL, 5, '2025-12-10 09:00:00', 1)
ON DUPLICATE KEY UPDATE
  `type` = VALUES(`type`),
  `tag` = VALUES(`tag`),
  `title` = VALUES(`title`),
  `subtitle` = VALUES(`subtitle`),
  `content` = VALUES(`content`),
  `accent` = VALUES(`accent`),
  `sort` = VALUES(`sort`),
  `publish_time` = VALUES(`publish_time`),
  `status` = VALUES(`status`);

-- 借阅记录（演示：当前借阅/逾期）
-- handled_by 关联管理员 9001
INSERT INTO `borrow_record` (`id`, `user_id`, `book_id`, `borrow_at`, `due_at`, `return_at`, `renew_count`, `status`, `fine_amount`, `handled_by`)
VALUES
  (50001, 10001, 2, '2025-12-10 09:30:00', '2026-01-09 23:59:59', NULL, 0, 0, NULL, 9001),
  (50002, 10001, 1, '2025-12-06 14:10:00', '2026-01-05 23:59:59', NULL, 1, 0, NULL, 9001),
  (50003, 20001, 10, '2025-10-01 10:00:00', '2025-11-30 23:59:59', NULL, 2, 2, 5.00, 9001),
  (50004, 30001, 8, '2025-12-01 16:20:00', '2026-02-28 23:59:59', NULL, 0, 0, NULL, 9001),
  (50005, 10002, 4, '2025-11-01 09:00:00', '2025-12-01 23:59:59', '2025-11-20 10:10:00', 0, 1, NULL, 9001),
  (50006, 10003, 6, '2025-10-10 15:00:00', '2025-11-09 23:59:59', '2025-11-12 09:20:00', 0, 1, 2.00, 9001)
ON DUPLICATE KEY UPDATE
  `borrow_at` = VALUES(`borrow_at`),
  `due_at` = VALUES(`due_at`),
  `return_at` = VALUES(`return_at`),
  `renew_count` = VALUES(`renew_count`),
  `status` = VALUES(`status`),
  `fine_amount` = VALUES(`fine_amount`),
  `handled_by` = VALUES(`handled_by`);

-- 按借阅记录同步可借库存（避免示例数据可借数与借出记录不一致）
UPDATE `book` b
LEFT JOIN (
  SELECT `book_id`, COUNT(*) AS `borrowed_cnt`
  FROM `borrow_record`
  WHERE `is_deleted` = 0 AND `return_at` IS NULL
  GROUP BY `book_id`
) br ON br.`book_id` = b.`id`
SET b.`available_qty` = GREATEST(b.`total_qty` - IFNULL(br.`borrowed_cnt`, 0), 0)
WHERE b.`is_deleted` = 0;
