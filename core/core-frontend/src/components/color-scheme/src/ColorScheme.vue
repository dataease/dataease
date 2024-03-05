<script lang="ts" setup>
import { ref } from 'vue'
import { COLOR_CASES, COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { useI18n } from '@/hooks/web/useI18n'

const colorCases = COLOR_CASES
const { t } = useI18n()
const predefineColors = COLOR_PANEL

const activeColor = ref('')
const colorScheme = ref({
  name: '',
  colors: []
})
const changeColorOption = () => {
  activeColor.value = ''
  const items = colorCases.filter(ele => {
    return ele.value === colorScheme.value.name
  })
  colorScheme.value.colors = JSON.parse(JSON.stringify(items[0].colors))
}

const showCustom = ref(false)

const changeShowCustom = () => {
  showCustom.value = !showCustom.value
  colorPicker.value?.hide()
}

const customColor = ref()
let colorIndex = ref(0)

const colorPicker = ref()

const switchColorCase = () => {
  colorScheme.value.colors[colorIndex.value] = customColor.value
}

const changeActive = (color: string, index: number) => {
  activeColor.value = color
  colorIndex.value = index
  customColor.value = color
  colorPicker.value?.hide()
  setTimeout(() => {
    colorPicker.value.show()
  }, 200)
}

const resetCustomColor = () => {
  changeColorOption()
}
</script>

<template>
  <div class="color-scheme">
    <div class="custom-title">
      {{ t('chart.color_case') }}
    </div>
    <div class="flex-align-center color-select">
      <el-select
        v-model="colorScheme.name"
        size="default"
        :placeholder="t('chart.pls_slc_color_case')"
        @change="changeColorOption()"
      >
        <el-option
          v-for="option in colorCases"
          :key="option.value"
          :label="option.name"
          :value="option.value"
        >
          <div class="flex-align-center">
            <div style="overflow: hidden; border-radius: 2px">
              <span
                v-for="(c, index) in option.colors"
                :key="index"
                :style="{
                  width: '12px',
                  height: '16px',
                  float: 'left',
                  backgroundColor: c
                }"
              />
            </div>

            <span style="margin-left: 4px">{{ option.name }}</span>
          </div>
        </el-option>
      </el-select>
      <div class="color-custom-select flex-align-center">
        <span
          class="item-select"
          :style="{
            backgroundColor: color
          }"
          v-for="color in colorScheme.colors"
          :key="color"
        ></span>
      </div>
      <el-button @click="changeShowCustom" style="margin-left: 8px" size="default" plain>
        <template #icon>
          <icon name="icon_admin_outlined"></icon>
        </template>
      </el-button>
    </div>
    <!--自定义配色方案-->
    <div v-show="showCustom" style="margin-top: 8px">
      <div class="custom-title flex-align-center">
        <span>{{ t('chart.custom_case') }}</span>
        <el-button style="margin-right: -4px" text @click="resetCustomColor"
          >{{ t('chart.reset') }}
        </el-button>
      </div>
      <div class="color-custom flex-align-center">
        <span
          class="color-item"
          :class="activeColor === color && 'active'"
          @click="changeActive(color, index)"
          :style="{
            backgroundColor: color
          }"
          v-for="(color, index) in colorScheme.colors"
          :key="color"
        ></span>
        <span class="color-picker-style" :style="{ left: colorIndex * 19 + 7 + 'px' }">
          <el-color-picker
            ref="colorPicker"
            v-model="customColor"
            :predefine="predefineColors"
            @change="switchColorCase"
          />
        </span>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.color-scheme {
  .color-picker-style {
    position: absolute;
    top: 3px;
    height: 1px;
    width: 1px;
    overflow: hidden;
    z-index: 1;
  }
  .color-select {
    position: relative;
    justify-content: space-between;
  }
  :deep(.ed-select),
  :deep(.ed-input) {
    --ed-input-height: 28px;
    width: 188px;
    height: 28px;
    line-height: 28px;
  }

  :deep(.is-plain) {
    height: 28px;
    line-height: 28px;
    padding: 8px;
    min-width: 28px;
    .ed-icon {
      font-size: 12px;
    }
  }

  :deep(.ed-input__inner) {
    height: 28px !important;
    line-height: 28px;
  }

  .color-custom-select {
    border-radius: 3px;
    overflow: hidden;
    position: absolute;
    top: 6px;
    left: 8px;
    left: 10px;
    .item-select {
      width: 12px;
      height: 16px;
    }
  }
  .custom-title {
    justify-content: space-between;
    color: #646a73;
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    line-height: 20px;
    margin-bottom: 8px;
  }
  .color-custom {
    position: relative;
    .color-item {
      border-radius: 3px;
      width: 14px;
      height: 14px;
      position: relative;

      &::after {
        content: '';
        width: calc(100% + 2px);
        height: calc(100% + 2px);
        border-radius: 3px;
        position: absolute;
        top: -2px;
        border: 1px solid;
        left: -2px;
        display: none;
      }

      &:hover {
        &::after {
          border-color: var(--ed-color-primary-99, #3370ff99);
          display: block;
        }
      }

      &.active {
        &::after {
          border-color: var(--ed-color-primary);
          display: block;
        }
      }

      &.color-item {
        margin-left: 5px;
      }
    }
  }
}
</style>
