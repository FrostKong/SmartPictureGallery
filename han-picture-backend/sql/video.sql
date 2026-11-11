-- 视频 MVP 建表脚本（可单独执行，重复执行安全）
-- 说明：视频暂不纳入空间配额统计，spaceId 仅作预留字段；
--       AI 任务本阶段只记录用户指令与状态，不接模型、不跑队列。

use han_picture;

-- 视频表
create table if not exists video
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '视频 url（COS 公有读直链）',
    coverUrl     varchar(512)                       null comment '封面图 url（用户拖动选帧后由 CI 同步截帧生成）',
    name         varchar(128)                       not null comment '视频名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                       null comment '标签（JSON 数组）',
    videoSize    bigint                             null comment '文件体积（字节）',
    duration     int                                null comment '时长（秒），上传时由 CI avinfo 同步回填',
    videoFormat  varchar(32)                        null comment '视频格式：mp4/webm/mov/m4v',
    width        int                                null comment '宽度，由 CI avinfo 同步回填',
    height       int                                null comment '高度，由 CI avinfo 同步回填',
    userId       bigint                             not null comment '创建用户 id',
    spaceId      bigint                             null comment '发布状态位：非空=我的空间草稿，NULL=已发布；不参与配额统计',
    reviewStatus int      default 0                 not null comment '审核状态：0-待审核; 1-通过; 2-拒绝',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_userId (userId),
    INDEX idx_spaceId (spaceId)
) comment '视频' collate = utf8mb4_unicode_ci;

-- 视频 AI 任务表（MVP 占位：只记录指令与状态）
create table if not exists video_task
(
    id             bigint auto_increment comment 'id' primary key,
    videoId        bigint                             not null comment '原视频 id',
    userId         bigint                             not null comment '提交用户 id',
    prompt         text                               not null comment '用户自然语言指令',
    state          int      default 0                 not null comment '任务状态：0-待处理 1-处理中 2-成功 3-失败',
    resultVideoUrl varchar(512)                       null comment '结果视频 url（AI 跑完回填）',
    resultJson     text                               null comment 'AI 结构化输出（预留，暂替素材/片段/脚本表）',
    errorMessage   varchar(512)                       null comment '错误信息',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                 not null comment '是否删除',
    INDEX idx_videoId (videoId),
    INDEX idx_userId (userId),
    INDEX idx_state (state)
) comment '视频 AI 任务' collate = utf8mb4_unicode_ci;
