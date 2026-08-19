<template>
  <el-popover
      :popper-options="popperOptions"
      :visible="popoverVisible"
      placement="bottom"
      popper-class="condition-drawer-fullscreen"
      show-arrow :title="title"
      :width="460">
    <template #reference>
      <el-button text @click="customTrigger" @click.stop>
        <template #icon>
          <Icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></Icon>
        </template>
      </el-button>
    </template>
    <ConditionDefaultConfiguration :cur-component="curComponent" :show-position="'report'"></ConditionDefaultConfiguration>
  </el-popover>
</template>

<script lang="ts" setup>
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import ConditionDefaultConfiguration from '@/custom-component/v-query/ConditionDefaultConfiguration.vue'
import {Icon} from "@/components/icon-custom";
import {computed, ref} from "vue";
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const props = defineProps({
  curComponent: {
    type: Object,
    required: true
  }
})
const popoverVisible = ref(false)

const popperOptions = {
  modifiers: [
    {
      name: 'eventListeners',
      options: {
        scroll: false,
        resize: false,
      }
    }
  ]
}

const filterType = {
  0: t('v_query.text_drop_down'),
  1: t('v_query.time_type'),
  2: t('v_query.number_drop_down'),
  5: t('v_query.number_drop_down'),
  7: t('common.component.dateRange'),
  8: t('v_query.text_search'),
  9: t('v_query.drop_down_tree'),
  22: t('v_query.number_range'),
}

const customTrigger = e =>{
  popoverVisible.value = !popoverVisible.value;
}

const closePopover = e =>{
  popoverVisible.value = false
}

const title = computed(()=>`${props.curComponent.name}（${filterType[props.curComponent.displayType]}）`)

defineExpose({
  closePopover
})
</script>

<style lang="less">
.condition-drawer-fullscreen{
  .ed-popover__title {
    font-weight: bold;
    font-size: 14px;
  }
}
</style>
