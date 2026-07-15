-- ==========================================
-- 朋友圈模块：动态表、点赞表、评论表
-- 版本：v5.0
-- 依赖：无（纯新增表，不修改已有表）
-- ==========================================

-- 动态表
create table `im_feed`
(
    `id`           bigint       not null auto_increment primary key comment 'id',
    `user_id`      bigint       not null comment '发布用户id',
    `content`      text comment '文字内容（可为空，纯图片动态）',
    `images`       varchar(2000) default '' comment '图片URL列表，JSON数组格式',
    `created_time` datetime      default current_timestamp comment '发布时间',
    key            `idx_user_id` (`user_id`),
    key            `idx_created_time` (`created_time`)
) engine = innodb charset = utf8mb4 comment '朋友圈动态';

-- 点赞表
create table `im_feed_like`
(
    `id`           bigint   not null auto_increment primary key comment 'id',
    `feed_id`      bigint   not null comment '动态id',
    `user_id`      bigint   not null comment '点赞用户id',
    `created_time` datetime default current_timestamp comment '点赞时间',
    unique key     `idx_feed_user` (`feed_id`, `user_id`),
    key            `idx_feed_id` (`feed_id`)
) engine = innodb charset = utf8mb4 comment '朋友圈点赞';

-- 评论表
create table `im_feed_comment`
(
    `id`           bigint       not null auto_increment primary key comment 'id',
    `feed_id`      bigint       not null comment '动态id',
    `user_id`      bigint       not null comment '评论用户id',
    `content`      varchar(500) not null comment '评论内容',
    `created_time` datetime     default current_timestamp comment '评论时间',
    key            `idx_feed_id` (`feed_id`)
) engine = innodb charset = utf8mb4 comment '朋友圈评论';
