<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>

<script setup lang="ts">
import BasicLayout from "@/layouts/BasicLayout.vue";
//import { healthUsingGet } from '@/api/mainController'
//import { useLoginUserStore } from '@/stores/useLoginUserStore'

//这个在access里有了，这里注释掉
// const loginUserStore = useLoginUserStore()
// loginUserStore.fetchLoginUser()

// healthUsingGet().then((response)=>
//   {
//     console.log(response);
//   }
// )
</script>
