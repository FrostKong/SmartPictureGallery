<template>
  <div id="video-list">
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: video }">
        <a-list-item style="padding: 0">
          <a-card hoverable @click="doClickVideo(video)">
            <template #cover>
              <div class="video-cover">
                <img
                  v-if="video.coverUrl"
                  style="height: 180px; object-fit: cover"
                  :alt="video.name"
                  :src="video.coverUrl"
                  loading="lazy"
                />
                <div v-else class="video-cover-empty">
                  <PlayCircleOutlined />
                </div>
                <span v-if="video.duration" class="video-duration">
                  {{ formatDuration(video.duration) }}
                </span>
              </div>
            </template>
            <a-card-meta :title="video.name">
              <template #description>
                <a-flex>
                  <a-tag color="blue">{{ video.category ?? '默认' }}</a-tag>
                  <a-tag v-for="tag in video.tags" :key="tag">{{ tag }}</a-tag>
                </a-flex>
              </template>
            </a-card-meta>
            <template v-if="showOp" #actions>
              <a-tooltip v-if="video.spaceId" title="发布到公共图库">
                <upload-outlined @click="(e) => doPublish(video, e)" />
              </a-tooltip>
              <edit-outlined v-if="canEdit" @click="(e) => doEdit(video, e)" />
              <delete-outlined v-if="canDelete" @click="(e) => doDelete(video, e)" />
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  DeleteOutlined,
  EditOutlined,
  PlayCircleOutlined,
  UploadOutlined,
} from '@ant-design/icons-vue'
import { deleteVideoUsingPost, publishVideoUsingPost } from '@/api/videoController'
import { formatDuration } from '@/utils'

interface Props {
  dataList?: API.VideoVO[]
  loading?: boolean
  showOp?: boolean
  canEdit?: boolean
  canDelete?: boolean
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
  canEdit: false,
  canDelete: false,
})

const router = useRouter()

// 视频详情在新标签页打开
const doClickVideo = (video: API.VideoVO) => {
  if (!video.id) {
    return
  }
  window.open(`/video/${video.id}`, '_blank')
}

// 发布：把 spaceId 置空，进入公共图库
const doPublish = async (video: API.VideoVO, e: Event) => {
  e.stopPropagation()
  if (!video.id) {
    return
  }
  const res = await publishVideoUsingPost({ id: video.id })
  if (res.data.code === 0) {
    message.success('已发布到公共图库')
    props.onReload?.()
  } else {
    message.error('发布失败，' + res.data.message)
  }
}

// 编辑元数据
const doEdit = (video: API.VideoVO, e: Event) => {
  e.stopPropagation()
  router.push({
    path: '/add_video',
    query: {
      id: video.id,
      spaceId: video.spaceId,
    },
  })
}

// 删除
const doDelete = async (video: API.VideoVO, e: Event) => {
  e.stopPropagation()
  if (!video.id) {
    return
  }
  const res = await deleteVideoUsingPost({ id: video.id })
  if (res.data.code === 0) {
    message.success('删除成功')
    props.onReload?.()
  } else {
    message.error('删除失败，' + res.data.message)
  }
}
</script>

<style scoped>
.video-cover {
  position: relative;
  height: 180px;
  background: #f0f2f5;
}

.video-cover img {
  display: block;
  width: 100%;
}

.video-cover-empty {
  display: flex;
  height: 180px;
  align-items: center;
  justify-content: center;
  color: #bfbfbf;
  font-size: 40px;
}

.video-duration {
  position: absolute;
  right: 8px;
  bottom: 8px;
  padding: 1px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.65);
  color: #fff;
  font-size: 12px;
}
</style>
