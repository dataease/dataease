<script setup lang="ts">
import { computed, ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { styleData, selectKey, optionMap } from '@/utils/attr'
import DeInputNum from '@/custom-component/common/DeInputNum.vue'
import { ElRow } from 'element-plus-secondary'

const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)
const activeName = ref(curComponent.value.collapseName)

const styleKeys = computed(() => {
  if (curComponent.value) {
    const curComponentStyleKeys = Object.keys(curComponent.value.style)
    return styleData.filter(item => curComponentStyleKeys.includes(item.key))
  } else {
    return null
  }
})

const onChange = () => {
  curComponent.value.collapseName = activeName
}

const isIncludesColor = str => {
  return str.toLowerCase().includes('color')
}
</script>

<template>
  <div class="v-common-attr">
    <el-collapse v-model="activeName" accordion @change="onChange()">
      <el-collapse-item title="通用样式" name="style">
        <div style="width: 100%">
          <div
            v-for="({ key, label }, index) in styleKeys"
            :key="index"
            :title="label"
            style="display: flex; float: left; margin-top: 10px"
          >
            <div style="width: 60px; overflow: hidden; text-align: right">
              <span>{{ label }}</span>
            </div>
            <div style="width: 85px">
              <el-color-picker
                v-if="isIncludesColor(key)"
                v-model="curComponent.style[key]"
                size="small"
                show-alpha
              ></el-color-picker>
              <el-select
                v-else-if="selectKey.includes(key)"
                size="small"
                v-model="curComponent.style[key]"
              >
                <el-option
                  v-for="item in optionMap[key]"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                ></el-option>
              </el-select>
              <de-input-num v-else v-model="curComponent.style[key]"></de-input-num>
            </div>
          </div>
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<style lang="less" scoped>
.v-common-attr {
  .ed-input-group__prepend {
    padding: 0 10px;
  }
}

:deep(.ed-collapse-item__header) {
  background-color: rgba(29, 36, 42, 1) !important;
  color: #ffffff;
  border-bottom: 1px solid rgba(85, 85, 85, 1);
}
:deep(.ed-collapse-item__content) {
  background-color: rgba(37, 45, 54, 1);
  color: #ffffff;
  padding-left: 5px;
}

:deep(.ed-collapse-item__wrap) {
  border-bottom: 1px solid rgba(85, 85, 85, 1);
  background-color: rgba(37, 45, 54, 1) !important;
  padding: 10px 0px 10px 0px;
}
:deep(.ed-collapse) {
  width: 100%;
}
:deep(.ed-input__wrapper) {
  background-color: rgba(37, 45, 54, 1);
}
:deep(.ed-input__inner) {
  color: #ffffff;
}
:deep(.ed-form-item__label) {
  color: #ffffff;
}
</style>
