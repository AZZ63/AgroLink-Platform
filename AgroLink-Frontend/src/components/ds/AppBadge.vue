<template>
  <span :class="['ds-badge', `ds-badge-${variant}`, `ds-badge-${size}`]">
    <span v-if="dot" class="ds-badge-dot"></span>
    <slot />
  </span>
</template>

<script setup>
defineProps({
  variant: { type: String, default: 'default', validator: v => ['default', 'success', 'warning', 'danger', 'info'].includes(v) },
  size: { type: String, default: 'sm', validator: v => ['sm', 'md'].includes(v) },
  dot: Boolean,
})
</script>

<style scoped>
.ds-badge {
  display: inline-flex; align-items: center; gap: 4px;
  border-radius: 6px; font-weight: 600; white-space: nowrap;
}
.ds-badge-sm { padding: 3px 10px; font-size: 11px; }
.ds-badge-md { padding: 4px 12px; font-size: 12px; }

.ds-badge-default { background: #f0f0eb; color: #6b7280; }
.ds-badge-success { background: #e8f5e4; color: #7CCF5F; }
.ds-badge-warning { background: #fef3c7; color: #f59e0b; }
.ds-badge-danger { background: #fee2e2; color: #ef4444; }
.ds-badge-info { background: #dbeafe; color: #3b82f6; }

.ds-badge-dot {
  width: 6px; height: 6px; border-radius: 50%; background: currentColor;
}
</style>
