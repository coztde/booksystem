-- 迁移：为 book 表补充 category 字段（馆藏资源页筛选）
-- 用法：USE book; SOURCE backend/sql/migrations/2025_12_28_add_book_category.sql;

ALTER TABLE `book`
  ADD COLUMN `category` varchar(50) DEFAULT NULL COMMENT '分类（用于馆藏资源页筛选）' AFTER `isbn`;

CREATE INDEX `idx_book_category` ON `book` (`category`);

