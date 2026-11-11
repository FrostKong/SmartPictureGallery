<template>
  <div id="addVideoView">
    <h2 style="margin-bottom: 16px">{{ route.query?.id ? '修改视频' : '创建视频' }}</h2>

    <a-typography-paragraph v-if="spaceId" type="secondary">
      保存至空间：<a :href="`/space/${spaceId}`" target="_blank">{{ spaceId }}</a>
      <span> · 先当草稿存着，弄好了在空间里点「发布」</span>
    </a-typography-paragraph>

    <a-form layout="vertical">
      <a-form-item label="视频文件（mp4 / webm / mov / m4v，不超过 100MB）">
        <a-upload
          :file-list="fileList"
          :before-upload="beforeUpload"
          :max-count="1"
          accept=".mp4,.webm,.mov,.m4v"
          @remove="onRemove"
        >
          <a-button><UploadOutlined /> 选择视频</a-button>
        </a-upload>
        <a-typography-paragraph v-if="route.query?.id" type="secondary" style="margin-top: 8px">
          修改时可不选文件：不选则只改下面的元数据，选了则重新上传覆盖原视频。
        </a-typography-paragraph>
      </a-form-item>

      <a-form-item v-if="!spaceId && !route.query?.id" label="保存到">
        <a-radio-group v-model:value="saveTarget">
          <a-radio value="draft">我的空间（草稿，之后手动发布）</a-radio>
          <a-radio value="public">公共图库（直接发布）</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="名称">
        <a-input v-model:value="form.name" placeholder="请输入名称" allow-clear />
      </a-form-item>
      <a-form-item label="简介">
        <a-textarea v-model:value="form.introduction" placeholder="请输入简介" :rows="2" allow-clear />
      </a-form-item>
      <a-form-item label="分类 / 活动">
        <a-auto-complete
          v-model:value="form.category"
          :options="activityOptions"
          placeholder="例如：2026 秋游（同一活动名会自动归为一组）"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="标签">
        <a-select v-model:value="form.tags" mode="tags" placeholder="请输入标签" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="submitting" @click="handleSubmit" style="width: 100%">
          {{ route.query?.id ? '保存修改' : '上传并继续' }}
        </a-button>
      </a-form-item>
    </a-form>

    <!-- 上传完成后停留在本页：CI 只对 COS 上已存在的对象截帧，所以选封面必须在上传之后 -->
    <a-card v-if="uploadedVideo?.url" title="挑选封面（可跳过）" style="margin-top: 24px">
      <a-typography-paragraph type="secondary">
        视频已上传。拖动播放器到想要的画面，再点「上传为封面」；封面由数据万象同步截帧生成，图片存入对象存储后由后端返回地址。
      </a-typography-paragraph>
      <video
        ref="playerRef"
        class="cover-player"
        controls
        preload="metadata"
        :src="uploadedVideo.url"
        @timeupdate="onTimeUpdate"
      />
      <a-space wrap style="margin-top: 12px">
        <a-typography-text type="secondary">当前位置：{{ currentTime.toFixed(1) }} 秒</a-typography-text>
        <a-button type="primary" :loading="snapshotting" @click="doSnapshotCover">上传为封面</a-button>
        <a-typography-text v-if="coverUrl" type="success">封面已生成，可继续拖动重选</a-typography-text>
      </a-space>
      <div v-if="coverUrl" class="cover-preview">
        <a-typography-text type="secondary">当前封面</a-typography-text>
        <img :src="coverUrl" alt="视频封面" />
      </div>
      <a-divider />
      <a-button :loading="finishing" @click="finish" style="width: 100%">完成</a-button>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import {
  editVideoUsingPost,
  getVideoVoByIdUsingGet,
  listVideoVoByPageUsingPost,
  snapshotVideoCoverUsingPost,
  uploadVideoUsingPost,
} from '@/api/videoController'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const spaceId = computed(() => route.query?.spaceId as string | undefined)
const saveTarget = ref<'draft' | 'public'>('draft')
// 后端 Long 统一序列化为字符串（JsonConfig），雪花 ID 有 19 位，
// 全程原样传递，绝不能 Number() 转换：超过 2^53 后末位会被抹成 0
const personalSpaceId = ref<string | number>()
const submitting = ref(false)
const snapshotting = ref(false)
const finishing = ref(false)
const selectedFile = ref<File>()
const fileList = ref<any[]>([])
const activityOptions = ref<{ value: string }[]>([])
const uploadedVideo = ref<API.VideoVO>()
const coverUrl = ref<string>()
const playerRef = ref<HTMLVideoElement>()
const currentTime = ref(0)
const form = reactive<API.VideoEditRequest>({
  name: undefined,
  introduction: undefined,
  category: undefined,
  tags: [],
})

const beforeUpload = (file: File) => {
  const suffix = file.name.split('.').pop()?.toLowerCase()
  if (!['mp4', 'webm', 'mov', 'm4v'].includes(suffix ?? '')) {
    message.error('仅支持 mp4 / webm / mov / m4v 格式')
    return false
  }
  if (file.size > 100 * 1024 * 1024) {
    message.error('文件大小不能超过 100MB')
    return false
  }
  selectedFile.value = file
  fileList.value = [{ uid: '-1', name: file.name, status: 'done' }]
  if (!form.name) {
    form.name = file.name.replace(/\.[^.]+$/, '')
  }
  // 阻止组件自动上传，由下面的提交逻辑统一处理
  return false
}

const onRemove = () => {
  selectedFile.value = undefined
  fileList.value = []
}

const onTimeUpdate = () => {
  currentTime.value = playerRef.value?.currentTime ?? 0
}

// 虚拟分组：活动名直接存在视频的 category 里，候选值从已有视频聚合
const fetchActivityOptions = async () => {
  const res = await listVideoVoByPageUsingPost({
    current: 1,
    pageSize: 20,
    ...(spaceId.value ? { spaceId: spaceId.value } : {}),
  })
  const names = new Set<string>()
  res.data.data?.records?.forEach((item) => {
    if (item.category) {
      names.add(item.category)
    }
  })
  activityOptions.value = Array.from(names).map((value) => ({ value }))
}

const fetchPersonalSpace = async () => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser.id) {
    return
  }
  const res = await listSpaceVoByPageUsingPost({
    userId: loginUser.id,
    current: 1,
    pageSize: 1,
    spaceType: 0,
  })
  if (res.data.code === 0) {
    personalSpaceId.value = res.data.data?.records?.[0]?.id
  }
}

const loadOldVideo = async () => {
  // id 是雪花 ID 字符串，原样传给后端；加 Number() 会丢精度
  const id = route.query?.id as string | undefined
  if (!id) {
    return
  }
  const res = await getVideoVoByIdUsingGet({ id })
  if (res.data.code === 0 && res.data.data) {
    const data = res.data.data
    form.name = data.name
    form.introduction = data.introduction
    form.category = data.category
    form.tags = data.tags ?? []
    // 编辑模式也直接展示播放器，方便重选封面
    uploadedVideo.value = data
    coverUrl.value = data.coverUrl
  }
}

const saveForm = async (videoId: string | number) => {
  const res = await editVideoUsingPost({
    id: videoId,
    name: form.name,
    introduction: form.introduction,
    category: form.category,
    tags: form.tags,
  })
  if (res.data.code !== 0) {
    message.error('保存失败，' + res.data.message)
    return false
  }
  return true
}

const goNext = (videoId: string | number) => {
  if (spaceId.value) {
    router.push(`/space/${spaceId.value}`)
  } else {
    router.push(`/video/${videoId}`)
  }
}

const handleSubmit = async () => {
  // 与 VideoDetailView 的 props.id 一样保持字符串原样，不做任何数值转换
  const id = route.query?.id as string | undefined
  if (!id && !selectedFile.value) {
    message.error('请先选择视频文件')
    return
  }

  // 编辑模式不传 spaceId，后端会复用旧归属；新建时按页面选择决定草稿还是直接发布
  let targetSpaceId: string | number | undefined
  if (!id) {
    if (spaceId.value) {
      targetSpaceId = spaceId.value
    } else if (saveTarget.value === 'draft') {
      if (!personalSpaceId.value) {
        message.error('未找到个人空间，请先创建空间')
        return
      }
      targetSpaceId = personalSpaceId.value
    }
  }

  submitting.value = true
  try {
    let videoId: string | number | undefined = id
    let justUploaded = false
    if (selectedFile.value) {
      const params: API.uploadVideoUsingPOSTParams = {
        ...(id ? { id } : {}),
        ...(targetSpaceId ? { spaceId: targetSpaceId } : {}),
        videoName: form.name || undefined,
      }
      const res = await uploadVideoUsingPost(params, {}, selectedFile.value)
      if (res.data.code !== 0 || !res.data.data?.id) {
        message.error('上传失败，' + res.data.message)
        return
      }
      videoId = res.data.data.id
      uploadedVideo.value = res.data.data
      coverUrl.value = res.data.data.coverUrl
      justUploaded = true
    }
    if (!videoId) {
      return
    }
    if (!(await saveForm(videoId))) {
      return
    }
    if (justUploaded) {
      // 不跳转：等用户拖进度条挑完封面，由「完成」按钮收尾
      message.success('上传成功，可以在下方挑选封面')
    } else {
      message.success(id ? '修改成功' : '创建成功')
      goNext(videoId)
    }
  } finally {
    submitting.value = false
  }
}

const doSnapshotCover = async () => {
  const videoId = uploadedVideo.value?.id
  if (!videoId) {
    return
  }
  const time = playerRef.value?.currentTime ?? 0
  snapshotting.value = true
  try {
    const res = await snapshotVideoCoverUsingPost({ id: videoId, time })
    if (res.data.code === 0 && res.data.data) {
      coverUrl.value = res.data.data
      message.success('封面已更新')
    } else {
      message.error('封面生成失败，' + res.data.message)
    }
  } finally {
    snapshotting.value = false
  }
}

const finish = async () => {
  const videoId = uploadedVideo.value?.id
  if (!videoId) {
    return
  }
  finishing.value = true
  try {
    if (!(await saveForm(videoId))) {
      return
    }
    message.success('完成')
    goNext(videoId)
  } finally {
    finishing.value = false
  }
}

onMounted(() => {
  fetchActivityOptions()
  fetchPersonalSpace()
  loadOldVideo()
})
</script>

<style scoped>
#addVideoView {
  max-width: 720px;
  margin: 0 auto;
}

.cover-player {
  display: block;
  width: 100%;
  max-height: 420px;
  background: #000;
  border-radius: 8px;
}

.cover-preview {
  margin-top: 16px;
}

.cover-preview img {
  display: block;
  margin-top: 8px;
  max-width: 260px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}
</style>
