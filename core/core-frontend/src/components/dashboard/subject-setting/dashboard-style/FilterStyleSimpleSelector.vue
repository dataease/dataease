<template>
  <el-row class="custom-row">
    <el-row><span class="custom-item-text">标题水平位置</span> </el-row>
    <el-row style="margin-top: 8px">
      <div>
        <el-radio-group v-model="filterForm.horizontal" @change="themeChange('horizontal')">
          <el-radio label="left" :title="t('chart.text_pos_left')"
            ><Icon class-name="bash-icon" name="filter-h-left"
          /></el-radio>
          <el-radio
            :disabled="filterForm.vertical === 'center'"
            label="center"
            :title="t('chart.text_pos_center')"
            ><Icon class-name="bash-icon" name="filter-h-center"
          /></el-radio>
          <el-radio label="right" :title="t('chart.text_pos_right')"
            ><Icon class-name="bash-icon" name="filter-h-right"
          /></el-radio>
        </el-radio-group>
      </div>
    </el-row>
    <el-row style="margin-top: 16px"><span class="custom-item-text">标题显示位置</span> </el-row>
    <el-row style="margin-top: 8px">
      <div>
        <el-radio-group v-model="filterForm.vertical" @change="themeChange('vertical')">
          <el-radio label="top" :title="t('chart.text_pos_top')"
            ><Icon class-name="bash-icon" name="filter-top"
          /></el-radio>
          <el-radio
            :disabled="filterForm.horizontal === 'center'"
            label="center"
            :title="t('chart.text_pos_center')"
            ><Icon class-name="bash-icon" name="filter-center"
          /></el-radio>
        </el-radio-group>
      </div>
    </el-row>
    <el-row style="margin-top: 16px"><span class="custom-item-text">标题颜色</span> </el-row>
    <el-row style="margin-top: 8px">
      <div>
        <el-color-picker
          v-model="filterForm.color"
          class="color-picker-style"
          size="small"
          :predefine="state.predefineColors"
          @change="themeChange('color')"
        />
      </div>
    </el-row>
    <el-divider></el-divider>
    <el-row class="inner-type-text">输入框样式</el-row>
    <el-row style="margin-top: 16px"
      ><span class="custom-item-text">{{ t('visualization.board') }}</span>
    </el-row>
    <el-row style="margin-top: 8px">
      <div>
        <el-color-picker
          v-model="filterForm.brColor"
          class="color-picker-style"
          size="small"
          :predefine="state.predefineColors"
          @change="themeChange('brColor')"
        />
      </div>
    </el-row>
    <el-row style="margin-top: 16px"
      ><span class="custom-item-text">{{ t('visualization.text') }}</span>
    </el-row>
    <el-row style="margin-top: 8px">
      <div>
        <el-color-picker
          v-model="filterForm.wordColor"
          class="color-picker-style"
          size="small"
          :predefine="state.predefineColors"
          @change="themeChange('wordColor')"
        />
      </div>
    </el-row>
    <el-row style="margin-top: 16px"
      ><span class="custom-item-text">{{ t('visualization.board_background') }}</span>
    </el-row>
    <el-row style="margin-top: 8px">
      <div>
        <el-color-picker
          v-model="filterForm.innerBgColor"
          class="color-picker-style"
          size="small"
          :predefine="state.predefineColors"
          @change="themeChange('innerBgColor')"
        />
      </div>
    </el-row>
  </el-row>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import { adaptCurThemeFilterStyleAll } from '@/utils/canvasStyle'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()

const emits = defineEmits(['onTextChange'])
const filterForm = computed(() => dvMainStore.canvasStyleData.component.filterStyle)
const state = reactive({
  fontSize: [],
  isSetting: false,
  predefineColors: COLOR_PANEL
})

const initForm = () => {
  // do
}
const themeChange = styleKey => {
  adaptCurThemeFilterStyleAll(styleKey)
  snapshotStore.recordSnapshot('filterStyleSimpleSelector-themeChange')
}

onMounted(() => {
  eventBus.on('onThemeColorChange', initForm)
})
</script>

<style scoped lang="less">
.custom-item-text {
  font-size: 12px !important;
  font-weight: 400 !important;
  line-height: 20px;
  color: #646a73 !important;
}

:deep(.ed-radio) {
  margin-right: 0;
  .ed-radio__label {
    padding: 0 8px 0 0;
  }
}
:deep(.ed-radio-group) {
  padding-top: 2px;
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

.custom-divider {
  margin: 5px 0 0 8px;
  height: 20px;
  width: 1px;
  background-color: rgba(31, 35, 41, 0.15);
}

.inner-type-text {
  font-weight: 500;
  font-size: 12px;
  margin-bottom: 8px;
}
</style>
