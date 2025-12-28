-- 迁移：创建门户内容表 portal_post（轮播/新闻/公告）
-- 用法：USE book; SOURCE backend/sql/migrations/2025_12_28_create_portal_post.sql;

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

