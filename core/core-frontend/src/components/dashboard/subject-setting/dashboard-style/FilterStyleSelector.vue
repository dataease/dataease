<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="filterFormRef" :model="filterForm" label-width="80px" size="mini">
        <div>
          <el-form-item :label="t('chart.text_h_position')" class="form-item">
            <el-radio-group
              v-model="filterForm.horizontal"
              size="mini"
              @change="themeChange('horizontal')"
            >
              <el-radio-button label="left">{{ t('chart.text_pos_left') }}</el-radio-button>
              <el-radio-button :disabled="filterForm.vertical === 'center'" label="center"
                >{{ t('chart.text_pos_center') }}
              </el-radio-button>
              <el-radio-button label="right">{{ t('chart.text_pos_right') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="t('chart.text_v_position')" class="form-item">
            <el-radio-group
              v-model="filterForm.vertical"
              size="mini"
              @change="themeChange('vertical')"
            >
              <el-radio-button label="top">{{ t('chart.text_pos_top') }}</el-radio-button>
              <el-radio-button :disabled="filterForm.horizontal === 'center'" label="center"
                >{{ t('chart.text_pos_center') }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="t('visualization.title_color')" class="form-item">
            <el-color-picker
              v-model="filterForm.color"
              class="color-picker-style"
              :predefine="state.predefineColors"
              @change="themeChange('color')"
            />
          </el-form-item>
          <el-divider>{{ t('visualization.input_style') }}</el-divider>
          <el-row style="height: 40px; overflow: hidden">
            <el-col :span="4" style="padding-top: 8px; padding-left: 10px">
              {{ t('visualization.board') }}
            </el-col>
            <el-col :span="4" style="padding-top: 5px">
              <el-color-picker
                v-model="filterForm.brColor"
                size="mini"
                class="color-picker-style"
                :predefine="state.predefineColors"
                @change="themeChange('brColor')"
              />
            </el-col>
            <el-col :span="4" style="padding-top: 8px; padding-left: 10px">
              {{ t('visualization.text') }}
            </el-col>
            <el-col :span="4" style="padding-top: 5px">
              <el-color-picker
                v-model="filterForm.wordColor"
                size="mini"
                class="color-picker-style"
                :predefine="state.predefineColors"
                @change="themeChange('wordColor')"
              />
            </el-col>
            <el-col :span="4" style="padding-top: 8px; padding-left: 10px">
              {{ t('visualization.board_background') }}
            </el-col>
            <el-col :span="4" style="padding-top: 5px">
              <el-color-picker
                v-model="filterForm.innerBgColor"
                size="mini"
                class="color-picker-style"
                :predefine="state.predefineColors"
                @change="themeChange('innerBgColor')"
              />
            </el-col>
          </el-row>
        </div>
      </el-form>
    </el-col>
  </div>
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

const filterFormRef = ref(null)
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
  snapshotStore.recordSnapshotCache('renderChart')
}

onMounted(() => {
  eventBus.on('onThemeColorChange', initForm)
})
</script>

<style scoped lang="less">
.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item-slider :deep(.ed-form-item__label) {
  font-size: 12px;
  line-height: 38px;
}

.form-item :deep(.ed-form-item__label) {
  font-size: 12px;
}

.ed-select-dropdown__item {
  padding: 0 20px;
}

span {
  font-size: 12px;
}

.ed-form-item {
  margin-bottom: 6px;
}

.switch-style {
  position: absolute;
  right: 10px;
  margin-top: -4px;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}

.ed-divider__text {
  font-size: 8px;
  font-weight: 400;
  color: rgb(144, 147, 153);
}
</style>
