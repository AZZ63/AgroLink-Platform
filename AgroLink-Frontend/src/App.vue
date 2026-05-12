<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from './stores/user'

onMounted(() => {
  const userStore = useUserStore()
  userStore.restoreFromStorage()
})
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  font-family: 'Outfit', sans-serif;
  background: #0a0c10;
  color: #e8edf5;
  -webkit-font-smoothing: antialiased;
}
body::after {
  content: '';
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: repeating-linear-gradient(
    0deg, transparent, transparent 2px,
    rgba(255,255,255,0.012) 2px, rgba(255,255,255,0.012) 4px
  );
  pointer-events: none;
  z-index: 9999;
}
</style>
