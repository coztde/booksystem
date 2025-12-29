-- 为图书增加封面图字段（OSS上传后的URL）

ALTER TABLE `book`
  ADD COLUMN `cover_url` varchar(255) DEFAULT NULL COMMENT '封面图URL（可选）' AFTER `id`;

