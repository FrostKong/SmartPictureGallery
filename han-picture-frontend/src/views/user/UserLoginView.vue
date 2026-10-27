<template>
  <div id="userLoginView">
    <h2 class="title">Cloud Gallery —— User login</h2>
    <div class="desc">Enterprise-level intelligent collaborative cloud gallery</div>
    <a-form
      :model="formState"
      name="basic"
      autocomplete="off"
      @finish="handleSubmit"
      @finishFailed="handleSubmitFailed"
    >
      <a-form-item
        label="账号"
        name="userAccount"
        :rules="[
          { required: true, message: 'Please input your userAccount!' },
          { min: 4, message: '账号不能小于 4 位' },
        ]"
      >
        <a-input v-model:value="formState.userAccount" />
      </a-form-item>

      <a-form-item
        label="密码"
        name="userPassword"
        :rules="[
          { required: true, message: 'Please input your userPassword!' },
          { min: 8, message: '密码不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" />
      </a-form-item>

      <a-form-item>
        <div class="tips">
          no account?
          <RouterLink to="/user/register">sign up</RouterLink>
        </div>
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">login</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { useRouter } from 'vue-router'
import { userLoginUsingPost } from '@/api/userController'
import { message } from 'ant-design-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLoginUsingPost(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    await router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}

const handleSubmitFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo)
}
</script>

<style scoped>
#userLoginView {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}
</style>
