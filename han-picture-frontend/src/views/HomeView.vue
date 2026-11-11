<template>
  <div id="homeView">
    <!-- 资源类型 + 搜索框：两类各自独立分页，不做混排 -->
    <div class="search-row">
      <a-radio-group v-model:value="mediaType" button-style="solid" @change="onMediaTypeChange">
        <a-radio-button value="picture">图片</a-radio-button>
        <a-radio-button value="video">视频</a-radio-button>
      </a-radio-group>
      <a-input-search
        :placeholder="mediaType === 'picture' ? '从海量图片中搜索' : '搜索视频名称或简介'"
        v-model:value="searchParams.searchText"
        enter-button="搜索"
        size="large"
        @search="doSearch"
      />
    </div>

    <!-- 分类与标签只有图片侧有候选接口，视频侧暂不展示 -->
    <template v-if="mediaType === 'picture'">
      <a-tabs v-model:activeKey="selectedCategory" @change="doSearch">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane v-for="category in categoryList" :key="category" :tab="category" />
      </a-tabs>
      <div class="tag-bar">
        <span style="margin-right: 8px">标签:</span>
        <a-space :size="[0, 8]" wrap>
          <a-checkable-tag
            v-for="(tag, index) in tagList"
            :key="tag"
            v-model:checked="selectedTagList[index]"
            @change="doSearch"
          >
            {{ tag }}
          </a-checkable-tag>
        </a-space>
      </div>
    </template>

    <PictureList v-if="mediaType === 'picture'" :dataList="pictureList" :loading="loading" />
    <VideoList v-else :dataList="videoList" :loading="loading" />
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    />
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController'
import { listVideoVoByPageUsingPost } from '@/api/videoController'
import { message } from 'ant-design-vue'
import PictureList from '@/components/PictureList.vue'
import VideoList from '@/components/VideoList.vue'

type MediaType = 'picture' | 'video'

const mediaType = ref<MediaType>('picture')
const pictureList = ref<API.PictureVO[]>([])
const videoList = ref<API.VideoVO[]>([])
const total = ref(0)
const loading = ref(true)
const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

/**
 * 获取图片的分类和标签候选值（视频侧暂无对应接口）
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagList.value = res.data.data.tagList ?? []
    categoryList.value = res.data.data.categoryList ?? []
  } else {
    message.error('获取标签分类列表失败' + res.data.message)
  }
}

const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

/**
 * 按当前资源类型请求各自的分页接口，total 取该类型自己的总数
 */
const fetchData = async () => {
  loading.value = true
  try {
    if (mediaType.value === 'picture') {
      const params = {
        ...searchParams,
        tags: [] as string[],
      }
      if (selectedCategory.value != 'all') {
        params.category = selectedCategory.value
      }
      selectedTagList.value.forEach((useTag, index) => {
        if (useTag) {
          params.tags.push(tagList.value[index])
        }
      })
      const res = await listPictureVoByPageUsingPost(params)
      if (res.data.data) {
        pictureList.value = res.data.data.records ?? []
        total.value = res.data.data.total ?? 0
      } else {
        message.error('获取数据失败，' + res.data.message)
      }
    } else {
      const res = await listVideoVoByPageUsingPost({
        current: searchParams.current,
        pageSize: searchParams.pageSize,
        sortField: 'createTime',
        sortOrder: 'descend',
        searchText: searchParams.searchText,
      })
      if (res.data.data) {
        videoList.value = res.data.data.records ?? []
        total.value = res.data.data.total ?? 0
      } else {
        message.error('获取数据失败，' + res.data.message)
      }
    }
  } finally {
    loading.value = false
  }
}

// 切换资源类型：重置分页与图片侧筛选条件，避免把上一类的状态带过来
const onMediaTypeChange = () => {
  searchParams.current = 1
  total.value = 0
  selectedCategory.value = 'all'
  selectedTagList.value = []
  fetchData()
}

const onPageChange = (current: number, pageSize: number) => {
  searchParams.current = current
  searchParams.pageSize = pageSize
  fetchData()
}

const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

onMounted(() => {
  getTagCategoryOptions()
  fetchData()
})
</script>

<style scoped>
#homeView .search-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

#homeView .search-row .ant-input-search {
  max-width: 480px;
}

#homeView .tag-bar {
  margin-bottom: 16px;
}
</style>
