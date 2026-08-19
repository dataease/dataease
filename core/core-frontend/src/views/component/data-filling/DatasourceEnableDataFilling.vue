<script setup lang="ts">
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'

import {computed, nextTick, onMounted, ref, watch} from "vue";
import {ElMessageBox} from "element-plus-secondary";
import {useI18n} from "@/hooks/web/useI18n";
import {getDfPlugin} from "../../menu/data/data-filling/fill/fill_api";

const {t} = useI18n()

const props = defineProps<{
  form: {
    id: string
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

function onClickCheckBox() {
  if (!props.form.enableDataFill) {
    nextTick(() => {
      props.form.enableDataFill = true
    })
  } else {
    if (props.form.id != undefined && props.form.id !== '') {
      ElMessageBox.confirm(t('data_fill.disable_data_fill_hint'), {
        type: 'warning',
        confirmButtonType: 'danger',
        confirmButtonText: t('commons.close'),
        cancelButtonText: t('commons.cancel'),
        autofocus: false,
        showClose: false
      }).then(() => {
        nextTick(() => {
          props.form.enableDataFill = false
        })
      })
    } else {
      nextTick(() => {
        props.form.enableDataFill = false
      })
    }
  }

}

const hasPlugin = ref(false)

const show = computed(() => {
  if (props.form.type === 'mysql' || props.form.type === 'mariadb') {
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
  checkShow(props.form.type)
})

watch(() => props.form.type, (type) => {
  checkShow(type)
})

</script>

<template>
  <el-form-item
      style="margin-top: 16px;"
      v-if="show"
  >
    <el-checkbox
        v-model="form.enableDataFill"
        @click.prevent.stop="onClickCheckBox"
    >
      <span class="hint-inline">
        {{ t('commons.enable') }} {{ t('data_fill.data_fill') }}
        <el-tooltip class="item" placement="bottom" popper-class="df-datasource-setting-tooltip">
          <template #content>
            <div>
              {{ t('data_fill.enable_data_fill_hint') }}
            </div>
          </template>
          <el-icon class="hint-icon">
            <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon"/></Icon>
          </el-icon>
        </el-tooltip>
      </span>
    </el-checkbox>
  </el-form-item>
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

<style lang="less">
.df-datasource-setting-tooltip {
  z-index: 3000 !important;
}
</style>
