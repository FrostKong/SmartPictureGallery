<template>
  <div id="picture-list">
    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <img
                style="height: 180px; object-fit: cover"
                :alt="picture.name"
                :src="picture.thumbnailUrl ?? picture.url"
                loading="lazy"
              />
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="yellow">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
            <template v-if="showOp" #actions>
              <search-outlined @click="(e) => doSearch(picture, e)" />
              <share-alt-outlined @click="(e) => doShare(picture, e)" />
              <edit-outlined v-if="canEdit" @click="(e) => doEdit(picture, e)" />
              <delete-outlined v-if="canDelete" @click="(e) => doDelete(picture, e)" />
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <ShareModal ref="shareModalRef" :link="shareLink"/>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import { deletePictureUsingPost } from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { DeleteOutlined,EditOutlined,SearchOutlined,ShareAltOutlined } from '@ant-design/icons-vue'
import ShareModal from '@/components/ShareModal.vue'
import { ref } from 'vue'

// 定义数据
interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showOp?: boolean
  onReload?: () => void
  canEdit?: boolean
  canDelete?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
  canEdit: false,
  canDelete: false,
})

// 搜索
const doSearch = (picture, e) => {
  e.stopPropagation()
  window.open(`/search_picture?pictureId=${picture.id}`)
}


// 编辑
const doEdit = (picture, e) => {
  // 防止冒泡
  e.stopPropagation()
  router.push({
    path: '/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

// 删除
const doDelete = async (picture:API.PictureVO, e:Event) => {
  // 防止冒泡
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 让外层刷新
    props?.onReload()
  } else {
    message.error('删除失败')
  }
}

const router = useRouter()
// 跳转至图片详情
const doClickPicture = (picture: any) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

// 分享
const shareModalRef = ref()
const shareLink = ref<string>()
const doShare = (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  if(shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}

</script>

<style scoped>
</style>
