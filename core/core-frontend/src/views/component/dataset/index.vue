<template>
  <div v-if="isError" class="permission-error-mask" />
</template>
<script lang="ts" setup>
import { ref, toRefs } from 'vue'
import { ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const props = defineProps({
  dsList: Array,
  isEdit: {
    type: Boolean,
    default: false
  },
  dsId: {
    type: String,
    default: null
  }
})
const isError = ref(true)
const { dsList, isEdit, dsId } = toRefs(props)

const execute = () => {
  isError.value = false
  if (!dsId.value) {
    return
  }
  const dsItem = findDs()
  if (!dsItem) {
    hideOperate()
    showErrorMsg('数据源不存在或无数据源权限，即将返回')
    isError.value = true
    return
  }
  const weight = dsItem['weight']
  if (isEdit && weight < 2) {
    hideOperate()
    showErrorMsg('缺失数据源[使用权限]，禁止编辑数据源！')
    isError.value = true
    return
  }
}
const emits = defineEmits(['back'])
const showErrorMsg = (msg: string) => {
  const boxOption = {
    confirmButtonType: "danger",
    type: "warning",
    confirmButtonText: t('dataset.back'),
    cancelButtonText: t("dataset.cancel"),
    autofocus: false,
    showClose: false,
    tip: msg,
    callback: (action) => {
      if (action === 'confirm') {
        emits('back')
      }
    },
  }
  ElMessageBox.confirm('权限错误！', boxOption)
}

const findDs = () => {
  let stack = [...dsList.value]
  while(stack.length) {
    const item = stack.pop()
    if (item['id'] === dsId.value) {
      return item
    }
    if (item['children']?.length) {
      stack = stack.concat([...item['children']])
    }
  }
  return null
}

const hideOperate = () => {
  const classList = ['oprate', 'field-data']
  classList.forEach(c => {
    const domList = document.getElementsByClassName(c)
    if (domList?.length) {
      for (let i = 0; i < domList.length; i++) {
        const dom = domList[i]
        dom.setAttribute('style', 'display: none;')
      }
    }
  })
}

defineExpose({
  execute
})

</script>

<style lang="less" scoped>
.permission-error-mask {
  position: absolute;
  left: 0;
  top: 56px;
  width: 100%;
  height: calc(100% - 56px);
  background-color: #dee0e3;
  z-index: 20;
  opacity: 0.7;
}
</style>