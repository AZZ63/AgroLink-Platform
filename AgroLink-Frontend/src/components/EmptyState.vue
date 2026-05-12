<template>
  <div class="empty-state">
    <div class="empty-icon-wrapper">
      <div class="empty-icon">{{ icon }}</div>
    </div>
    <h3 v-if="title" class="empty-title">{{ title }}</h3>
    <p v-if="subtitle" class="empty-subtitle">{{ subtitle }}</p>
    <p v-if="description" class="empty-desc">{{ description }}</p>
    <button v-if="actionText" class="empty-action-btn" @click="handleAction">{{ actionText }}</button>
  </div>
</template>

<script setup>
const props = defineProps({
  icon: { type: String, default: '📭' },
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  description: { type: String, default: '' },
  actionText: { type: String, default: '' },
  actionLink: { type: String, default: '' }
})

const emit = defineEmits(['action'])

import { useRouter } from 'vue-router'
const router = useRouter()

function handleAction() {
  emit('action')
  if (props.actionLink) router.push(props.actionLink)
}
</script>

<style scoped>
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #555d6e;
}
.empty-icon-wrapper {
  margin-bottom: 16px;
}
.empty-icon {
  font-size: 56px;
  line-height: 1;
  display: inline-block;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.3));
}
.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #8892a4;
  margin-bottom: 8px;
  font-family: 'Outfit', sans-serif;
}
.empty-subtitle {
  font-size: 13px;
  color: #555d6e;
  margin-bottom: 8px;
  font-family: 'Outfit', sans-serif;
  max-width: 320px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.5;
}
.empty-desc {
  font-size: 14px;
  color: #555d6e;
  margin-bottom: 20px;
  font-family: 'Outfit', sans-serif;
}
.empty-action-btn {
  padding: 10px 28px;
  background: #00ff88;
  border: none;
  border-radius: 6px;
  color: #000;
  font-family: 'Outfit', sans-serif;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.empty-action-btn:hover {
  background: #00cc6a;
  box-shadow: 0 0 24px rgba(0,255,136,0.35);
  transform: translateY(-1px);
}
</style>
