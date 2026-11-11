<template>
  <div id="spaceDetailView">
    <!-- 空间信息 -->
    <a-flex justify="space-between">
      <h2>{{ space.spaceName }}（{{ SPACE_TYPE_MAP[space.spaceType ?? 0] }}）</h2>
      <a-space size="middle">
        <a-button v-if="mediaType === 'picture'" :icon="h(EditOutlined)" @click="doBatchEdit">
          批量编辑
        </a-button>
        <a-button
          v-if="canUploadPicture && mediaType === 'picture'"
          type="primary"
          :href="`/add_picture?spaceId=${id}`"
          target="_blank"
        >
          + 创建图片
        </a-button>
        <a-button
          v-if="canUploadPicture && mediaType === 'video'"
          type="primary"
          :href="`/add_video?spaceId=${id}`"
          target="_blank"
        >
          + 创建视频
        </a-button>
        <a-button
          v-if="canManageSpaceUser && SPACE_TYPE_ENUM.TEAM === space.spaceType"
          type="primary"
          ghost
          :icon="h(TeamOutlined)"
          :href="`/spaceUserManage/${id}`"
          target="_blank"
        >
          成员管理
        </a-button>
        <a-tooltip
          :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
        >
          <a-progress
            type="circle"
            :percent="Number((((space.totalSize ?? 0) * 100) / (space.maxSize ?? 1)).toFixed(1))"
            :size="42"
          />
        </a-tooltip>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />

    <!-- 资源类型：两类各自独立分页 -->
    <a-radio-group
      v-model:value="mediaType"
      button-style="solid"
      style="margin-bottom: 16px"
      @change="onMediaTypeChange"
    >
      <a-radio-button value="picture">图片</a-radio-button>
      <a-radio-button value="video">视频</a-radio-button>
    </a-radio-group>

    <template v-if="mediaType === 'picture'">
      <!-- 搜索表单 -->
      <PictureSearchForm :onSearch="onSearch" />
      <div style="margin-bottom: 16px" />
      <!-- 按颜色搜索 -->
      <a-form-item label="按颜色搜索" style="margin-top: 16px">
        <color-picker format="hex" @pureColorChange="onColorChange" />
      </a-form-item>
    </template>
    <template v-else>
      <!-- 虚拟分组：活动名存在视频的 category 字段里，不新建文件夹表 -->
      <a-form-item label="按活动分组">
        <a-space>
          <a-auto-complete
            v-model:value="activity"
            :options="activityOptions"
            placeholder="输入或选择活动名"
            style="width: 260px"
            allow-clear
          />
          <a-button @click="doFilterActivity">筛选</a-button>
          <a-button @click="clearActivity">全部</a-button>
        </a-space>
        <a-typography-text type="secondary" style="margin-left: 12px">
          草稿先放这里，弄好了点卡片上的「发布」进入公共图库
        </a-typography-text>
      </a-form-item>
    </template>

    <PictureList
      v-if="mediaType === 'picture'"
      :dataList="dataList"
      :loading="loading"
      :onReload="fetchData"
      showOp
      :canEdit="canEditPicture"
      :canDelete="canDeletePicture"
    />
    <VideoList
      v-else
      :dataList="videoList"
      :loading="loading"
      :onReload="fetchData"
      showOp
      :canEdit="canEditPicture"
      :canDelete="canDeletePicture"
    />
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      :show-total="
        () =>
          mediaType === 'picture'
            ? `图片总数 ${total} / ${space.maxCount}`
            : `视频总数 ${total}`
      "
      @change="onPageChange"
    />
    <BatchEditPictureModal
      ref="batchEditPictureModalRef"
      :spaceId="spaceIdNumber"
      :pictureList="dataList"
      :onSuccess="onBatchEditPictureSuccess"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, h, onMounted, ref, watch } from 'vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController'
import { message } from 'ant-design-vue'
import { listPictureVoByPageUsingPost, searchPictureByColorUsingPost } from '@/api/pictureController'
import { listVideoVoByPageUsingPost } from '@/api/videoController'
import { formatSize } from '@/utils'
import PictureList from '@/components/PictureList.vue'
import VideoList from '@/components/VideoList.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import { ColorPicker } from 'vue3-colorpicker'
import 'vue3-colorpicker/style.css'
import BatchEditPictureModal from '@/components/BatchEditPictureModal.vue'
import { EditOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { SPACE_PERMISSION_ENUM, SPACE_TYPE_ENUM, SPACE_TYPE_MAP } from '../constant/space'

const space = ref<API.SpaceVO>({})
const props = defineProps<{
  id: string | number
}>()
const spaceIdNumber = computed(() => props.id)

type MediaType = 'picture' | 'video'
const mediaType = ref<MediaType>('picture')
const dataList = ref<API.PictureVO[]>([])
const videoList = ref<API.VideoVO[]>([])
const total = ref(0)
const loading = ref(true)
const activity = ref<string>()
const selectedActivity = ref<string>()
const activityOptions = ref<{ value: string }[]>([])

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

// ----- 获取空间详情 -----
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({ id: props.id })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取空间详情失败：' + e.message)
  }
}

// ----- 搜索条件 -----
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  fetchData()
}

// 按当前资源类型请求各自的分页接口
const fetchData = async () => {
  loading.value = true
  try {
    if (mediaType.value === 'video') {
      const res = await listVideoVoByPageUsingPost({
        spaceId: props.id,
        current: searchParams.value.current,
        pageSize: searchParams.value.pageSize,
        sortField: 'createTime',
        sortOrder: 'descend',
        searchText: searchParams.value.searchText,
        category: selectedActivity.value,
      })
      if (res.data.data) {
        videoList.value = res.data.data.records ?? []
        total.value = res.data.data.total ?? 0
      } else {
        message.error('获取数据失败，' + res.data.message)
      }
    } else {
      const params = {
        spaceId: props.id,
        ...searchParams.value,
      }
      const res = await listPictureVoByPageUsingPost(params)
      if (res.data.data) {
        dataList.value = res.data.data.records ?? []
        total.value = res.data.data.total ?? 0
      } else {
        message.error('获取数据失败，' + res.data.message)
      }
    }
  } finally {
    loading.value = false
  }
}

// 虚拟分组：从本空间视频的 category 聚合出活动候选值
const fetchActivityOptions = async () => {
  const res = await listVideoVoByPageUsingPost({
    spaceId: props.id,
    current: 1,
    pageSize: 20,
  })
  const names = new Set<string>()
  res.data.data?.records?.forEach((item) => {
    if (item.category) {
      names.add(item.category)
    }
  })
  activityOptions.value = Array.from(names).map((value) => ({ value }))
}

const onMediaTypeChange = () => {
  searchParams.value.current = 1
  total.value = 0
  fetchData()
}

const doFilterActivity = () => {
  selectedActivity.value = activity.value || undefined
  searchParams.value.current = 1
  fetchData()
}

const clearActivity = () => {
  activity.value = undefined
  selectedActivity.value = undefined
  searchParams.value.current = 1
  fetchData()
}

// 图片颜色改变
const onColorChange = async (color: string) => {
  const res = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: props.id,
  })
  if (res.data.data && res.data.code === 0) {
    const data = res.data.data ?? []
    dataList.value = data
    total.value = data.length
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 批量编辑
const batchEditPictureModalRef = ref()
const onBatchEditPictureSuccess = () => {
  fetchData()
  message.success('批量编辑成功，已刷新列表')
}
const doBatchEdit = () => {
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.openModal()
  }
}

onMounted(() => {
  fetchSpaceDetail()
  fetchData()
  fetchActivityOptions()
})

watch(
  () => props.id,
  () => {
    fetchSpaceDetail()
    fetchData()
    fetchActivityOptions()
  },
)
</script>

<style scoped>
#spaceDetailView {
  margin-bottom: 16px;
}
</style>
