import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import UserLoginView from '@/views/user/UserLoginView.vue'
import UserRegisterView from '@/views/user/UserRegisterView.vue'
import UserManageView from '@/views/admin/UserManageView.vue'
import AddPictureView from '@/views/AddPictureView.vue'
import PictureManageView from '@/views/admin/PictureManageView.vue'
import PictureDetailView from '@/views/PictureDetailView.vue'
import AddPictureBatchView from '@/views/AddPictureBatchView.vue'
import SpaceManageView from '@/views/admin/SpaceManageView.vue'
import AddSpaceView from '@/views/AddSpaceView.vue'
import MySpaceView from '@/views/MySpaceView.vue'
import SpaceDetailView from '@/views/SpaceDetailView.vue'
import SearchPictureView from '@/views/SearchPictureView.vue'
import SpaceUserManageView from '@/views/admin/SpaceUserManageView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomeView,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginView,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterView,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManageView,
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/add_picture',
      name: '创建图片',
      component: AddPictureView,
    },
    {
      path: '/add_picture/batch',
      name: '批量创建图片',
      component: AddPictureBatchView,
    },
    {
      path: '/admin/pictureManage',
      name: '图片管理',
      component: PictureManageView,
    },
    {
      path: '/admin/spaceManage',
      name: '空间管理',
      component: SpaceManageView,
    },
    {
      path: '/picture/:id',
      name: '图片详情',
      component: PictureDetailView,
      props: true
    },
    {
      path: '/add_space',
      name: '创建空间',
      component: AddSpaceView,
    },
    {
      path: '/my_space',
      name: '我的空间',
      component: MySpaceView,
    },
    {
      path: '/space/:id',
      name: '空间详情',
      component: SpaceDetailView,
      props: true,
    },
    {
      path: '/search_picture',
      name: '图片搜索',
      component: SearchPictureView,
    },
    {
      path: '/spaceUserManage/:id',
      name: '空间成员管理',
      component: SpaceUserManageView,
      props: true,
    },

  ],
})

export default router
