<template>
  <div id="searchPictureView">
    <h2 style="margin-bottom: 16px">以图搜图</h2>
    <h3 style="margin: 16px 0">原图</h3>
    <a-card hoverable style="width: 240px">
      <template #cover>
        <img
          style="height: 180px; object-fit: cover"
          :alt="picture.name"
          :src="picture.thumbnailUrl ?? picture.url"
        />
      </template>
    </a-card>
    <h3 style="margin: 16px 0">识图结果</h3>
    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <a :href="item.fromUrl" target="_blank">
            <a-card>
              <template #cover>
                <img
                  style="height: 180px; object-fit: cover"
                  :src="item.thumbUrl"
                />
              </template>
            </a-card>
          </a>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  getPictureVoByIdUsingGet,
  searchPictureByPictureUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'

const picture = ref<API.PictureVO>({})
const route = useRoute()
const loading = ref<boolean>(false)

// 图片id
const pictureId = computed(() => route.query?.pictureId)

// 获取图片详情
const fetchPicture = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: pictureId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('获取图片失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取图片失败：' + e.message)
  }
}

onMounted(() => {
  fetchPicture()
})

const dataList = ref<API.ImageSearchResult[]>([])
// 获取搜图结果
const fetchData = async () => {
  loading.value = true
  try {
    const res = await searchPictureByPictureUsingPost({
      pictureId: pictureId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取数据失败：' + e.message)
  }
  loading.value = false
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#searchPictureView {
  margin-bottom: 16px;
}
</style>
