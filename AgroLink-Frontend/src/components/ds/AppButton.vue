<template>
  <button
    :class="[
      'ds-btn',
      `ds-btn-${variant}`,
      `ds-btn-${size}`,
      { 'ds-btn-loading': loading, 'ds-btn-block': block }
    ]"
    :disabled="disabled || loading"
    v-bind="$attrs"
  >
    <span v-if="loading" class="ds-btn-spinner"></span>
    <slot />
  </button>
</template>

<script setup>
defineProps({
  variant: { type: String, default: 'primary', validator: v => ['primary', 'secondary', 'outline', 'ghost', 'danger'].includes(v) },
  size: { type: String, default: 'md', validator: v => ['sm', 'md', 'lg'].includes(v) },
  loading: Boolean,
  disabled: Boolean,
  block: Boolean,
})
</script>

<style scoped>
.ds-btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 6px;
  font-family: 'Inter', sans-serif; font-weight: 500; cursor: pointer;
  border: 1px solid transparent; border-radius: 10px;
  transition: all 0.15s ease; white-space: nowrap; line-height: 1;
}
.ds-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.ds-btn-sm { padding: 7px 14px; font-size: 12px; border-radius: 8px; }
.ds-btn-md { padding: 10px 20px; font-size: 14px; }
.ds-btn-lg { padding: 14px 28px; font-size: 15px; border-radius: 12px; }

.ds-btn-primary { background: #7CCF5F; color: white; border-color: #7CCF5F; }
.ds-btn-primary:hover:not(:disabled) { background: #6bc04e; }

.ds-btn-secondary { background: #f5f7f2; color: #374151; border-color: #e8ebe4; }
.ds-btn-secondary:hover:not(:disabled) { background: #eef0eb; }

.ds-btn-outline { background: transparent; color: #374151; border-color: #e8ebe4; }
.ds-btn-outline:hover:not(:disabled) { border-color: #7CCF5F; color: #7CCF5F; }

.ds-btn-ghost { background: transparent; color: #6b7280; border-color: transparent; }
.ds-btn-ghost:hover:not(:disabled) { background: #f5f7f2; color: #374151; }

.ds-btn-danger { background: #ef4444; color: white; border-color: #ef4444; }
.ds-btn-danger:hover:not(:disabled) { background: #dc2626; }

.ds-btn-block { width: 100%; }

.ds-btn-spinner {
  width: 14px; height: 14px; border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
