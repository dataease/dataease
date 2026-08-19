<template>
  <el-collapse-item :name="name" class="switch-collapse-item">
    <template #title>
      <div class="switch-collapse-item__title">
        <span>{{ title }}</span>
        <el-switch
          class="switch-collapse-item__switch"
          :model-value="modelValue"
          :disabled="disabled"
          @click.stop
          @change="handleChange"
        />
      </div>
    </template>

    <slot />
  </el-collapse-item>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    name: string
    modelValue: boolean
    disabled?: boolean
  }>(),
  {
    disabled: false
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  change: [value: boolean]
}>()

const handleChange = (value: boolean) => {
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style scoped lang="less">
.switch-collapse-item {
  :deep(.ed-collapse-item__header) {
    height: 36px;
    padding: 0 8px;
    background: #f5f6f7;
    color: #1f2329;
    font-size: 12px;
    font-weight: 500;
    line-height: 20px;
  }
}

.switch-collapse-item__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}

.switch-collapse-item__switch {
  height: 12px;
  line-height: 12px;

  :deep(.ed-switch__core) {
    width: 24px;
    min-width: 24px;
    height: 6px;
    border: 0;
    border-radius: 3px;
  }

  :deep(.ed-switch__core .ed-switch__action) {
    top: -3px;
    left: 0;
    width: 12px;
    height: 12px;
  }

  :deep(.ed-switch__action) {
    box-shadow: 0px 2px 4px rgba(31, 35, 41, 0.12);
  }

  &.is-checked :deep(.ed-switch__core .ed-switch__action) {
    left: calc(100% - 12px);
  }
}
</style>
