<template>
  <div :class="['ds-card', `ds-card-${variant}`, className]">
    <div v-if="title || $slots.header" class="ds-card-header">
      <slot name="header">
        <div class="ds-card-title-row">
          <div>
            <h3 class="ds-card-title">{{ title }}</h3>
            <p v-if="subtitle" class="ds-card-subtitle">{{ subtitle }}</p>
          </div>
          <div v-if="$slots.actions" class="ds-card-actions">
            <slot name="actions" />
          </div>
        </div>
      </slot>
    </div>
    <div class="ds-card-body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="ds-card-footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: String,
  subtitle: String,
  variant: { type: String, default: 'default', validator: v => ['default', 'bordered', 'flat'].includes(v) },
  className: String,
})
</script>

<style scoped>
.ds-card {
  background: #ffffff;
  border: 1px solid #e8ebe4;
  border-radius: 16px;
  transition: box-shadow 0.2s ease;
}
.ds-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.ds-card-flat { border-color: transparent; background: #f9faf8; }
.ds-card-flat:hover { box-shadow: none; }

.ds-card-header {
  padding: 20px 20px 0;
}
.ds-card-title-row {
  display: flex; justify-content: space-between; align-items: flex-start; gap: 12px;
}
.ds-card-title {
  font-size: 15px; font-weight: 600; color: #1e1e1e; margin: 0;
}
.ds-card-subtitle {
  font-size: 12px; color: #9ca3af; margin: 2px 0 0;
}
.ds-card-actions { display: flex; gap: 8px; flex-shrink: 0; }
.ds-card-body { padding: 20px; }
.ds-card-footer {
  padding: 0 20px 20px; border-top: 1px solid #f0f0eb; margin-top: 12px; padding-top: 16px;
}
</style>
