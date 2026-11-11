<template>
  <div id="videoDetailView">
    <a-row :gutter="[16, 16]">
      <!-- 视频播放区 -->
      <a-col :sm="24" :md="16" :xl="18">
        <a-card title="视频预览">
          <video
            v-if="video.url"
            class="video-player"
            controls
            preload="metadata"
            :src="video.url"
            :poster="video.coverUrl || undefined"
          />
          <a-empty v-else description="暂无视频" />
          <a-typography-paragraph type="secondary" style="margin-top: 8px; margin-bottom: 0">
            播放使用对象存储直链（支持拖动进度条）；另存为文件请用「下载」按钮。
          </a-typography-paragraph>
        </a-card>
      </a-col>

      <!-- 视频信息区 -->
      <a-col :sm="24" :md="8" :xl="6">
        <a-card title="视频信息">
          <a-descriptions :column="1">
            <a-descriptions-item label="作者">
              <a-space>
                <a-avatar :size="24" :src="video.user?.userAvatar" />
                <div>{{ video.user?.userName }}</div>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="名称">{{ video.name ?? '未命名' }}</a-descriptions-item>
            <a-descriptions-item label="简介">{{ video.introduction ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="分类 / 活动">
              {{ video.category ?? '默认' }}
            </a-descriptions-item>
            <a-descriptions-item label="标签">
              <a-tag v-for="tag in video.tags" :key="tag">{{ tag }}</a-tag>
              <span v-if="!video.tags?.length">-</span>
            </a-descriptions-item>
            <a-descriptions-item label="时长">
              {{ formatDuration(video.duration) }}
            </a-descriptions-item>
            <a-descriptions-item label="宽度">{{ video.width ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="高度">{{ video.height ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="大小">
              {{ formatSize(video.videoSize) }}
            </a-descriptions-item>
            <a-descriptions-item label="格式">
              {{ video.videoFormat ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="video.spaceId ? 'orange' : 'green'">
                {{ video.spaceId ? '我的空间草稿' : '已发布' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="上传时间">
              {{ video.createTime ?? '-' }}
            </a-descriptions-item>
          </a-descriptions>
          <a-space wrap>
            <a-button type="primary" @click="doDownload">
              下载
              <template #icon><DownloadOutlined /></template>
            </a-button>
            <a-button type="primary" ghost @click="doShare">
              分享
              <template #icon><ShareAltOutlined /></template>
            </a-button>
            <a-button v-if="canManage && video.spaceId" @click="doPublish">发布到公共图库</a-button>
            <a-button v-if="canManage" @click="doEdit">
              编辑
              <template #icon><EditOutlined /></template>
            </a-button>
            <a-button v-if="canManage" danger @click="doDelete">
              删除
              <template #icon><DeleteOutlined /></template>
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
    <ShareModal ref="shareModalRef" title="分享视频" :link="shareLink" />
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { DeleteOutlined, DownloadOutlined, EditOutlined, ShareAltOutlined } from '@ant-design/icons-vue'
import {
  deleteVideoUsingPost,
  getVideoVoByIdUsingGet,
  publishVideoUsingPost,
} from '@/api/videoController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { formatDuration, formatSize } from '@/utils'
import ShareModal from '@/components/ShareModal.vue'

const video = ref<API.VideoVO>({})
const props = defineProps<{
  id: string | number
}>()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 视频没有空间权限常量，前端沿用「本人 / 管理员」判断，后端仍会做归属校验
const canManage = computed(() => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser.id) {
    return false
  }
  return loginUser.id === video.value.userId || loginUser.userRole === 'admin'
})

const fetchVideoDetail = async () => {
  try {
    const res = await getVideoVoByIdUsingGet({ id: props.id })
    if (res.data.code === 0 && res.data.data) {
      video.value = res.data.data
    } else {
      message.error('获取视频详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取视频详情失败：' + e.message)
  }
}

onMounted(() => {
  fetchVideoDetail()
})

// 下载走后端流式接口（强制 attachment + 中文文件名），不能用直链 <a download>
const doDownload = () => {
  if (!video.value.id) {
    return
  }
  const link = document.createElement('a')
  link.href = `/api/video/download?id=${video.value.id}`
  link.click()
}

const doShare = () => {
  shareLink.value = `${window.location.protocol}//${window.location.host}/video/${video.value.id}`
  shareModalRef.value?.openModal()
}

const doPublish = async () => {
  if (!video.value.id) {
    return
  }
  const res = await publishVideoUsingPost({ id: video.value.id })
  if (res.data.code === 0) {
    message.success('已发布到公共图库')
    await fetchVideoDetail()
  } else {
    message.error('发布失败，' + res.data.message)
  }
}

const doEdit = () => {
  router.push({
    path: '/add_video',
    query: {
      id: video.value.id,
      spaceId: video.value.spaceId,
    },
  })
}

const doDelete = async () => {
  if (!video.value.id) {
    return
  }
  const res = await deleteVideoUsingPost({ id: video.value.id })
  if (res.data.code === 0) {
    message.success('删除成功')
    router.push('/')
  } else {
    message.error('删除失败，' + res.data.message)
  }
}

const shareModalRef = ref()
const shareLink = ref<string>('')
</script>

<style scoped>
#videoDetailView {
  margin-bottom: 16px;
}

.video-player {
  display: block;
  width: 100%;
  max-height: 600px;
  background: #000;
}
</style>
