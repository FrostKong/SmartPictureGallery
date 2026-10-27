<template>
  <div id="basicLayout">
    <a-layout>
      <a-layout-header class="header"><GlobalHeader /></a-layout-header>
      <a-layout>
        <GlobalSider class="sider" />
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>
      <a-layout-footer class="footer">
        <a href="https://www.bilibili.com/" target="_blank"> bilibili 观看 </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<style scoped>
/* 使用 scoped 限制样式作用域 */
#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
  margin-top: 100px;
}

#basicLayout .header {
  margin-bottom: 1px;
  color: unset;
  background: white;
}

#basicLayout .content {
  background: linear-gradient(to right, #fefefe, #e69191);
  padding: 28px;
  padding-bottom: 80px;
}

#basicLayout .sider {
  background: #bfcfe5;
  border-right: 0.5px solid #eee;
  height: 100%;
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
}

</style>

<script setup lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
import GlobalSider from '@/components/GlobalSider.vue';
</script>
