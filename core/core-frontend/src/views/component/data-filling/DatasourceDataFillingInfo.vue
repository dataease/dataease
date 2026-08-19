<script setup lang="ts">

import BaseInfoItem from '@/views/visualized/data/datasource/BaseInfoItem.vue'
import {useI18n} from "@/hooks/web/useI18n";
import {computed, onMounted, ref, watch} from "vue";
import {getDfPlugin} from "../../menu/data/data-filling/fill/fill_api";

const {t} = useI18n()

const props = defineProps<{
  nodeInfo?: {
    id: number
    name: string
    desc: string
    type: string
    syncSetting?: any
    configuration?: any
    apiConfiguration?: any
    paramsConfiguration?: any
    enableDataFill?: boolean
  }
}>()


const hasPlugin = ref(false)

const show = computed(() => {
  if (props.nodeInfo.type === 'mysql' || props.nodeInfo.type === 'mariadb') {
    return true;
  }
  return hasPlugin.value
})

function checkShow(type) {
  if (type) {
    getDfPlugin(type).then(res => {
      if (res && res.data) {
        hasPlugin.value = true
      } else {
        hasPlugin.value = false
      }
    }).catch(e => {
      hasPlugin.value = false
    })
  } else {
    hasPlugin.value = false
  }
}

onMounted(() => {
  checkShow(props.nodeInfo.type)
})

watch(() => props.nodeInfo.type, (type) => {
  checkShow(type)
})

</script>

<template>
  <el-row :gutter="24" v-if="show">
    <el-col :span="12">
      <BaseInfoItem :label="t('data_fill.data_fill')">
        {{
          nodeInfo.enableDataFill ? t('commons.enable') : t('commons.close')
        }}
      </BaseInfoItem>
    </el-col>
  </el-row>

</template>

<style scoped lang="less">
.hint-icon {
  cursor: pointer;
  font-size: 14px;
  color: #646a73;
}

.hint-inline {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6px
}
</style>
