<script setup lang="ts">
import { computed, toRefs } from 'vue'
import { ElFormItem, ElIcon } from 'element-plus-secondary'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import Icon from '../../components/icon-custom/src/Icon.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
const dvMainStore = dvMainStoreWithOut()

const snapshotStore = snapshotStoreWithOut()

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    eventsInfo: any
  }>(),
  {
    themes: 'dark'
  }
)
const { themes, eventsInfo } = toRefs(props)
const isDashboard = dvMainStore.dvInfo.type === 'dashboard'

const curSupportEvents = computed(() => {
  if (isDashboard) {
    return ['jump', 'refreshDataV']
  } else {
    return ['jump', 'showHidden', 'refreshDataV']
  }
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
            <div>事件绑定需退出编辑模式后生效,富文本开启绑定事件则内部点击事件失效</div>
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
