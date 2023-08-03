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
    type: String as PropType<'plain' | 'dark' | 'light'>,
    default: 'dark'
  }
})
const emit = defineEmits(['update:modelValue', 'modelChange'])
const { changeModel, title, themes } = toRefs(props)
const onSwitchChange = e => {
  emit('modelChange', changeModel.value)
  if (switchValue.value !== collapseOpen.value) {
    e.stopPropagation()
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
const collapseOpen = ref(false)
</script>
<template>
  <el-collapse-item :effect="themes" @click="collapseOpen = !collapseOpen" v-bind="$attrs">
    <template #title>
      <div class="collapse-header">
        <span>
          {{ title }}
        </span>
        <el-switch
          :effect="themes"
          size="small"
          v-model="switchValue"
          @click.stop="e => onSwitchChange(e)"
        />
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
  padding-right: 20px;
  flex-grow: 1;
}
</style>
