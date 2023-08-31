<script setup lang="ts">
import { computed, ref, toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { styleData, selectKey, optionMap, positionData, horizontalPosition } from '@/utils/attr'
import DeInputNum from '@/custom-component/common/DeInputNum.vue'
import ComponentPosition from '@/components/visualization/common/ComponentPosition.vue'
import BackgroundOverallCommon from '@/components/visualization/component-background/BackgroundOverallCommon.vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

const props = defineProps({
  themes: {
    type: String,
    default: 'dark'
  }
})

const { themes } = toRefs(props)
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

const onBackgroundChange = val => {
  curComponent.value.commonBackground = val
}
</script>

<template>
  <div class="v-common-attr" :class="{ 'attr-dark': themes === 'dark' }">
    <el-collapse v-model="activeName" @change="onChange()">
      <el-collapse-item title="位置" name="position" v-if="!dashboardActive">
        <component-position :themes="themes" style="padding-top: 10px"></component-position>
      </el-collapse-item>

      <el-collapse-item title="背景" name="background">
        <background-overall-common
          v-if="curComponent"
          :themes="themes"
          :common-background-pop="curComponent.commonBackground"
          component-position="component"
          @onBackgroundChange="onBackgroundChange"
        ></background-overall-common>
      </el-collapse-item>

      <el-collapse-item title="样式" name="style" class="common-style-area">
        <div class="common-style-inner">
          <div
            v-for="({ key, label }, index) in styleKeys"
            :key="index"
            :title="label"
            style="display: flex; float: left; margin-top: 10px"
          >
            <div class="attr-custom-icon-main">
              <el-icon :title="label" class="attr-custom-icon">
                <Icon :name="'dv-style-' + key"></Icon>
              </el-icon>
            </div>
            <div style="width: 85px">
              <el-color-picker
                v-if="isIncludesColor(key)"
                v-model="curComponent.style[key]"
                :themes="themes"
                size="small"
                show-alpha
              ></el-color-picker>
              <el-radio-group
                v-else-if="horizontalPosition.includes(key)"
                v-model="curComponent.style[key]"
              >
                <el-radio label="left" :title="t('chart.text_pos_left')"
                  ><Icon class-name="bash-icon" name="filter-h-left"
                /></el-radio>
                <el-radio label="center" :title="t('chart.text_pos_center')"
                  ><Icon class-name="bash-icon" name="filter-h-center"
                /></el-radio>
                <el-radio label="right" :title="t('chart.text_pos_right')"
                  ><Icon class-name="bash-icon" name="filter-h-right"
                /></el-radio>
              </el-radio-group>
              <el-select
                v-else-if="selectKey.includes(key)"
                size="small"
                :themes="themes"
                v-model="curComponent.style[key]"
              >
                <el-option
                  v-for="item in optionMap[key]"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                ></el-option>
              </el-select>
              <de-input-num
                v-else
                :themes="themes"
                v-model="curComponent.style[key]"
              ></de-input-num>
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

:deep(.ed-collapse-item__content) {
  border-top: 0 !important;
}

.attr-dark :deep(.ed-collapse-item__header) {
  background-color: @side-area-background !important;
  color: #ffffff;
  padding-left: 5px;
  border-bottom: 1px solid rgba(85, 85, 85, 1);
  height: 38px !important;
}
.attr-dark :deep(.ed-collapse-item__content) {
  background-color: @side-content-background;
  color: #ffffff;
  padding-left: 5px;
}

.attr-dark :deep(.ed-collapse-item__wrap) {
  background-color: @side-content-background;
  border-bottom: 1px solid rgba(85, 85, 85, 1);
  padding-bottom: 10px;
}
:deep(.ed-collapse) {
  width: 100%;
}

.attr-dark :deep(.ed-input__wrapper) {
  background-color: rgba(37, 45, 54, 1);
}
.attr-dark :deep(.ed-input__inner) {
  color: #ffffff;
}
.attr-dark :deep(.ed-form-item__label) {
  color: #ffffff;
}

.attr-custom-icon-main {
  padding-top: 4px;
  width: 30px;
  overflow: hidden;
  text-align: right;
}

.attr-custom-icon {
  font-size: 16px;
  color: #646a73;
  margin-right: 5px;
}

.common-style-area :deep(.ed-collapse-item__wrap) {
  padding-bottom: 16px;
}

.common-style-inner {
  width: 100%;
  min-width: 230px;
  margin-left: -12px;
}

:deep(.ed-radio) {
  margin-right: 0;
  .ed-radio__label {
    padding: 0 4px 0 0;
  }
}

:deep(.ed-radio.is-checked) {
  .ed-radio__label {
    .bash-icon {
      background: rgba(51, 112, 255, 0.1);
      border-radius: 4px;
    }
  }
}

:deep(.ed-radio__input) {
  display: none;
}

:deep(.ed-radio__input.is-checked) {
  .ed-radio__inner {
    padding: 4px;
    background-color: green;
    background-clip: content-box;
  }
}

.bash-icon {
  width: 24px;
  height: 24px;
}
</style>
