<script setup lang="ts">
import { ElCollapseItem, ElSwitch } from 'element-plus-secondary'
import { computed, PropType, ref, toRefs } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean
  },
  changeModel: {
    type: Object
  },
  title: String,
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  showSwitch: {
    type: Boolean,
    required: false,
    default: true
  }
})
const emit = defineEmits(['update:modelValue', 'modelChange'])
const { changeModel, title, themes } = toRefs(props)

const collapseItem = ref()
const onSwitchChange = () => {
  emit('modelChange', changeModel.value)
  if (!props.modelValue && !collapseItem.value.isActive) {
    collapseItem.value.handleHeaderClick()
  }

  if (props.modelValue && collapseItem.value.isActive) {
    collapseItem.value.handleHeaderClick()
  }
}
const switchValue = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emit('update:modelValue', value)
  }
})
</script>
<template>
  <el-collapse-item ref="collapseItem" :effect="themes" v-bind="$attrs">
    <template #title>
      <div class="collapse-header">
        <span>
          {{ title }}
        </span>
        <div>
          <el-switch
            v-show="showSwitch"
            v-model="switchValue"
            :effect="themes"
            size="small"
            @click.stop="onSwitchChange"
          />
        </div>
      </div>
    </template>
    <slot />
  </el-collapse-item>
</template>
<style scoped lang="less">
.collapse-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 10px;
  flex-grow: 1;
  :deep(.ed-switch.is-checked .ed-switch__core > .ed-switch__action) {
    left: calc(100% - 12px);
  }
  :deep(span.ed-switch__core) {
    min-width: 24px;
    border: none;
    height: 6px;
    border-radius: 3px;
    .ed-switch__action {
      left: 0;
      box-shadow: 0 2px 4px rgba(31, 35, 41, 0.12);
    }
  }
}
</style>
