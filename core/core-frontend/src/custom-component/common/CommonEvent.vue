<script setup lang="ts">
import { computed, toRefs } from 'vue'
import { ElFormItem, ElIcon } from 'element-plus-secondary'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import Icon from '../../components/icon-custom/src/Icon.vue'

const snapshotStore = snapshotStoreWithOut()

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    element: any
  }>(),
  {
    themes: 'dark'
  }
)
const { themes, element } = toRefs(props)

const curSupportEvents = ['jump', 'showHidden', 'refreshDataV']

const eventsInfo = computed(() => {
  return element.value.events
})

const onEventChange = () => {
  snapshotStore.recordSnapshotCache('renderChart')
}

const onJumpValueChange = () => {
  snapshotStore.recordSnapshotCache('renderChart')
}
</script>

<template>
  <el-row class="custom-row">
    <el-form label-position="top">
      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          :effect="themes"
          size="small"
          v-model="eventsInfo.checked"
          @change="onEventChange"
          >开启事件绑定</el-checkbox
        >
        <el-tooltip class="item" :effect="themes" placement="top">
          <template #content>
            <div>事件绑定需要退出编辑模式才开生效</div>
          </template>
          <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
            <Icon name="icon_info_outlined" />
          </el-icon>
        </el-tooltip>
      </el-form-item>

      <el-form-item class="form-item" :class="'form-item-' + themes" style="margin-bottom: 8px">
        <el-select
          v-model="eventsInfo.type"
          :disabled="!eventsInfo.checked"
          :effect="themes"
          @change="onEventChange"
          size="small"
        >
          <el-option
            v-for="typeInfo in eventsInfo.typeList"
            v-show="curSupportEvents.includes(typeInfo.key)"
            size="small"
            :effect="themes"
            :key="typeInfo.key"
            :label="typeInfo.label"
            :value="typeInfo.key"
          />
        </el-select>
      </el-form-item>

      <el-form-item
        v-if="eventsInfo.type === 'jump'"
        class="form-item"
        :class="'form-item-' + themes"
        style="margin-bottom: 8px"
      >
        <el-input
          v-model="eventsInfo.jump.value"
          :effect="themes"
          :disabled="!eventsInfo.checked"
          clearable
          :placeholder="'请输入跳转地址'"
          @change="onJumpValueChange"
        />
      </el-form-item>
    </el-form>
  </el-row>
</template>

<style scoped lang="less"></style>
