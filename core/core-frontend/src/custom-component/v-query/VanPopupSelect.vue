<script lang="ts" setup>
import { ref, computed } from 'vue'
import FixedSizeList from 'element-plus-secondary/es/components/virtual-list/src/components/fixed-size-list.mjs'
import VanPopup from 'vant/es/popup'
import 'vant/es/popup/style'

const props = defineProps({
  options: {
    type: Array,
    default: () => []
  },
  selectValue: {
    type: Array,
    default: () => []
  },
  multiple: {
    type: Boolean,
    default: false
  }
})

const showSelect = ref(false)
let oldCheckList = []
const checkAll = ref(false)
const isIndeterminate = ref(false)
const checkTableList = ref([])
const checkList = ref([])
const keywords = ref('')
const tableListWithSearch = computed(() => {
  if (!keywords.value) return props.options
  return props.options.filter((ele: any) =>
    ele.label.toLowerCase().includes(keywords.value.toLowerCase())
  )
})
const emits = defineEmits(['onClear', 'onConfirm'])
const reset = () => {
  oldCheckList = []
  showSelect.value = false
  checkAll.value = false
  isIndeterminate.value = false
  keywords.value = ''
}
const showPopup = () => {
  reset()
  if (props.multiple) {
    checkList.value = [...props.selectValue]
  } else {
    checkList.value = Array.isArray(props.selectValue)
      ? [...props.selectValue]
      : [props.selectValue]
    oldCheckList = [...checkList.value]
  }
  showSelect.value = true
}

const onClear = () => {
  reset()
  emits('onClear')
}

const onConfirm = () => {
  showSelect.value = false
  checkAll.value = false
  isIndeterminate.value = false
  keywords.value = ''
  emits('onConfirm', checkList.value)
}

const handleCheckedTablesChange = (value: any[]) => {
  if (!props.multiple) {
    if (!oldCheckList.length) {
      oldCheckList = [...value]
    } else {
      checkList.value = value.filter(ele => !oldCheckList.includes(ele))
      oldCheckList = [...checkList.value]
    }
    return
  }
  const checkedCount = value.length
  checkAll.value = checkedCount === tableListWithSearch.value.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < tableListWithSearch.value.length
  const tableNameArr = tableListWithSearch.value.map((ele: any) => ele.label)
  checkTableList.value = [
    ...new Set([...checkTableList.value.filter(ele => !tableNameArr.includes(ele)), ...value])
  ]
}

const handleCheckAllChange = (val: any) => {
  checkList.value = val
    ? [...new Set([...tableListWithSearch.value.map((ele: any) => ele.label), ...checkList.value])]
    : []
  isIndeterminate.value = false
  const tableNameArr = tableListWithSearch.value.map((ele: any) => ele.label)
  checkTableList.value = val
    ? [...new Set([...tableNameArr, ...checkTableList.value])]
    : checkTableList.value.filter(ele => !tableNameArr.includes(ele))
}
</script>

<template>
  <div class="vant-mobile_select" @click="showPopup" />
  <van-popup teleport="body" position="bottom" v-model:show="showSelect">
    <div class="container-vant_mobile">
      <div class="select-all">
        <el-checkbox
          v-model="checkAll"
          v-if="multiple"
          :indeterminate="isIndeterminate"
          @change="handleCheckAllChange"
        >
          {{ $t('component.allSelect') }}
        </el-checkbox>

        <el-input
          style="position: absolute; top: 3px; right: 150px; width: 150px"
          v-model="keywords"
          clearable
        ></el-input>
        <button
          style="position: absolute; top: 0; right: 60px"
          @click="onClear"
          class="van-picker__confirm van-haptics-feedback"
        >
          {{ $t('commons.clear') }}
        </button>
        <button
          style="position: absolute; top: 0; right: 10px"
          @click="onConfirm"
          class="van-picker__confirm van-haptics-feedback"
        >
          {{ $t('commons.confirm') }}
        </button>
      </div>
      <el-checkbox-group
        v-model="checkList"
        style="position: relative"
        @change="handleCheckedTablesChange"
      >
        <FixedSizeList
          :item-size="32"
          :data="tableListWithSearch"
          :total="tableListWithSearch.length"
          :height="460"
          :scrollbar-always-on="true"
          class-name="ed-select-dropdown__list"
          layout="vertical"
        >
          <template #default="{ index, style }">
            <div class="list-item_primary" :style="style">
              <el-checkbox :label="tableListWithSearch[index].value">
                {{ tableListWithSearch[index].label }}</el-checkbox
              >
            </div>
          </template>
        </FixedSizeList>
      </el-checkbox-group>
    </div>
  </van-popup>
</template>

<style lang="less">
.vant-mobile_select {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.container-vant_mobile {
  overflow: hidden;
  padding: 0 10px;

  .select-all {
    height: 40px;
    padding-left: 12px;
    display: flex;
    align-items: center;
    border-bottom: 1px solid #dee0e3;
    position: relative;

    .ed-input__wrapper {
      background-color: var(--ed-input-bg-color, var(--ed-fill-color-blank)) !important;
      box-shadow: 0 0 0 1px var(--ed-color-primary) inset !important;
    }
  }

  .ed-checkbox__label {
    display: inline-flex;
    align-items: center;
    color: #1f2329 !important;
  }

  .ed-vl__window {
    scrollbar-width: none;
  }
}
</style>
