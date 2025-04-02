<script setup lang="ts">
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import { computed, toRefs } from 'vue'
import { ElFormItem, ElIcon } from 'element-plus-secondary'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import Icon from '../../components/icon-custom/src/Icon.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
const dvMainStore = dvMainStoreWithOut()
const { t } = useI18n()

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
    return ['jump', 'refreshDataV', 'fullScreen', 'download']
  } else {
    return ['jump', 'showHidden', 'refreshDataV', 'fullScreen', 'download']
  }
})
const onEventChange = () => {
  snapshotStore.recordSnapshotCacheToMobile('events')
}

const onJumpValueChange = () => {
  snapshotStore.recordSnapshotCacheToMobile('events')
}
const getTypeLabel = type => {
  return typeMap[type] || type
}
const typeMap = {
  jump: t('visualization.jump'),
  download: t('visualization.download'),
  share: t('visualization.share'),
  fullScreen: t('visualization.fullscreen'),
  showHidden: t('visualization.pop_area'),
  refreshDataV: t('visualization.refresh'),
  refreshView: t('visualization.refresh_view')
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
          >{{ t('visualization.enable_event_binding') }}</el-checkbox
        >
        <el-tooltip class="item" :effect="themes" placement="top">
          <template #content>
            <div>{{ t('visualization.event_binding_tips') }}</div>
          </template>
          <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
            <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
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
            :label="getTypeLabel(typeInfo.label)"
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
          :placeholder="t('visualization.input_url_tips')"
          @change="onJumpValueChange"
        />
      </el-form-item>
      <el-form-item
        v-if="eventsInfo.type === 'jump' && eventsInfo.jump.type"
        class="form-item"
        :class="'form-item-' + themes"
        style="margin-bottom: 8px"
      >
        <el-radio-group
          size="small"
          v-model="eventsInfo.jump.type"
          :effect="themes"
          :disabled="!eventsInfo.checked"
          @change="onJumpValueChange"
        >
          <el-radio :effect="themes" label="_blank">{{ t('visualization.new_window') }}</el-radio>
          <el-radio :effect="themes" label="_self">{{ t('visualization.now_window') }}</el-radio>
          <el-radio :effect="themes" label="newPop">{{ t('visualization.pop_window') }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
  </el-row>
</template>

<style scoped lang="less">
.form-item-light {
  .ed-radio {
    margin-right: 3px !important;
  }
}
.form-item-dark {
  .ed-radio {
    margin-right: 3px !important;
  }
}
</style>
