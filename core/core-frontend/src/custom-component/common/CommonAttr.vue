<script setup lang="ts">
import { computed, ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { styleData, selectKey, optionMap, positionData } from '@/utils/attr'
import DeInputNum from '@/custom-component/common/DeInputNum.vue'

const dvMainStore = dvMainStoreWithOut()
const { curComponent, dvInfo } = storeToRefs(dvMainStore)
const activeName = ref(curComponent.value.collapseName)

const styleKeys = computed(() => {
  if (curComponent.value) {
    const curComponentStyleKeys = Object.keys(curComponent.value.style)
    return styleData.filter(item => curComponentStyleKeys.includes(item.key))
  } else {
    return null
  }
})

const positionKeys = computed(() => {
  if (curComponent.value) {
    const curComponentStyleKeys = Object.keys(curComponent.value.style)
    return positionData.filter(item => curComponentStyleKeys.includes(item.key))
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
const dashboardActive = computed(() => {
  return dvInfo.value.type === 'dashboard'
})
</script>

<template>
  <div class="v-common-attr">
    <el-collapse v-model="activeName" @change="onChange()">
      <el-collapse-item title="位置" name="position" v-if="!dashboardActive">
        <div style="width: 100%">
          <div
            v-for="({ key, label }, index) in positionKeys"
            :key="index"
            :title="label"
            style="display: flex; float: left; margin-top: 10px"
          >
            <div style="width: 25px; overflow: hidden; text-align: right">
              <span>{{ label }}</span>
            </div>
            <div style="width: 85px">
              <de-input-num v-model="curComponent.style[key]"></de-input-num>
            </div>
          </div>
        </div>
      </el-collapse-item>

      <el-collapse-item title="背景" name="background">
        <background-overall-component
          v-if="curComponent"
          themes="dark"
          position="component"
        ></background-overall-component>
      </el-collapse-item>

      <el-collapse-item title="样式" name="style">
        <div style="width: 100%">
          <div
            v-for="({ key, label }, index) in styleKeys"
            :key="index"
            :title="label"
            style="display: flex; float: left; margin-top: 10px"
          >
            <div style="width: 30px; overflow: hidden; text-align: right">
              <el-icon :title="label" class="attr-custom-icon">
                <Icon :name="'dv-style-' + key"></Icon>
              </el-icon>
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
      <slot></slot>
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
  background-color: @side-area-background !important;
  color: #ffffff;
  padding-left: 5px;
  border-bottom: 1px solid rgba(85, 85, 85, 1);
  height: 38px !important;
}
:deep(.ed-collapse-item__content) {
  background-color: @side-content-background;
  color: #ffffff;
  padding-left: 5px;
}

:deep(.ed-collapse-item__wrap) {
  background-color: @side-content-background;
  border-bottom: 1px solid rgba(85, 85, 85, 1);
  padding-bottom: 10px;
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

.attr-custom-icon {
  font-size: 16px;
  color: #646a73;
  margin-right: 5px;
}
</style>
