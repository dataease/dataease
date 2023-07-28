<script lang="tsx" setup>
import { reactive, toRefs, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import {
  COLOR_PANEL,
  CHART_FONT_FAMILY,
  CHART_FONT_LETTER_SPACE,
  DEFAULT_TITLE_STYLE
} from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  },
  propertyInner: {
    type: Array<string>
  }
})

const emit = defineEmits(['onTextChange'])

const predefineColors = COLOR_PANEL
const fontFamily = CHART_FONT_FAMILY
const fontLetterSpace = CHART_FONT_LETTER_SPACE

const state = reactive({
  titleForm: JSON.parse(JSON.stringify(DEFAULT_TITLE_STYLE)),
  fontSize: []
})

watch(
  () => props.chart.customStyle,
  () => {
    init()
  },
  { deep: true }
)

const { chart } = toRefs(props)

const initFontSize = () => {
  const arr = []
  for (let i = 12; i <= 40; i = i + 2) {
    arr.push({
      name: i + '',
      value: i + ''
    })
  }
  state.fontSize = arr
}

const changeTitleStyle = () => {
  emit('onTextChange', state.titleForm)
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customStyle) {
    let customStyle = null
    if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
      customStyle = JSON.parse(JSON.stringify(chart.customStyle))
    } else {
      customStyle = JSON.parse(chart.customStyle)
    }
    if (customStyle.text) {
      state.titleForm = customStyle.text
    }
  }
}

const showProperty = prop => props.propertyInner?.includes(prop)

initFontSize()
init()
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="titleForm"
        :disabled="!state.titleForm.show"
        :model="state.titleForm"
        label-width="80px"
        size="small"
      >
        <el-form-item :label="t('chart.title')" class="form-item">
          <el-input
            :effect="props.themes"
            v-model="chart.title"
            size="small"
            :placeholder="t('chart.title')"
            clearable
            @blur="changeTitleStyle('title')"
          />
        </el-form-item>
        <el-form-item :label="t('chart.font_family')" class="form-item">
          <el-select
            :effect="props.themes"
            v-model="state.titleForm.fontFamily"
            :placeholder="t('chart.font_family')"
            @change="changeTitleStyle('fontFamily')"
          >
            <el-option
              v-for="option in fontFamily"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('chart.text_fontsize')" class="form-item">
          <el-select
            :effect="props.themes"
            v-model="state.titleForm.fontSize"
            :placeholder="t('chart.text_fontsize')"
            size="small"
            @change="changeTitleStyle('fontSize')"
          >
            <el-option
              v-for="option in state.fontSize"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('chart.text_color')" class="form-item">
          <el-color-picker
            v-model="state.titleForm.color"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeTitleStyle('color')"
          />
        </el-form-item>
        <el-form-item :label="t('chart.text_h_position')" class="form-item">
          <el-radio-group
            v-model="state.titleForm.hPosition"
            size="small"
            @change="changeTitleStyle('hPosition')"
          >
            <el-radio :effect="props.themes" label="left">{{ t('chart.text_pos_left') }}</el-radio>
            <el-radio :effect="props.themes" label="center">{{
              t('chart.text_pos_center')
            }}</el-radio>
            <el-radio :effect="props.themes" label="right">{{
              t('chart.text_pos_right')
            }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('chart.text_style')" class="form-item">
          <el-checkbox
            :effect="props.themes"
            v-model="state.titleForm.isItalic"
            @change="changeTitleStyle('isItalic')"
            >{{ t('chart.italic') }}
          </el-checkbox>
          <el-checkbox
            :effect="props.themes"
            v-model="state.titleForm.isBolder"
            @change="changeTitleStyle('isBolder')"
            >{{ t('chart.bolder') }}
          </el-checkbox>
        </el-form-item>
        <el-form-item :label="t('chart.letter_space')" class="form-item">
          <el-select
            :effect="props.themes"
            v-model="state.titleForm.letterSpace"
            :placeholder="t('chart.quota_letter_space')"
            @change="changeTitleStyle('letterSpace')"
          >
            <el-option
              v-for="option in fontLetterSpace"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('chart.font_shadow')" class="form-item">
          <el-checkbox
            :effect="props.themes"
            v-model="state.titleForm.fontShadow"
            @change="changeTitleStyle('fontShadow')"
            >{{ t('chart.font_shadow') }}
          </el-checkbox>
        </el-form-item>

        <!--          <el-form-item :label="t('chart.remark')" class="form-item">-->
        <!--            <el-checkbox-->
        <!--              v-model="state.titleForm.remarkShow"-->
        <!--              @change="changeTitleStyle('remarkShow')"-->
        <!--              >{{ t('chart.show') }}-->
        <!--            </el-checkbox>-->
        <!--          </el-form-item>-->
        <!--          <span v-show="state.titleForm.remarkShow">-->
        <!--            <el-form-item :label="t('chart.remark_edit')" class="form-item">-->
        <!--              <el-button-->
        <!--                :title="t('chart.edit')"-->
        <!--                icon="el-icon-edit"-->
        <!--                type="text"-->
        <!--                size="small"-->
        <!--                @click="editRemark"-->
        <!--              />-->
        <!--            </el-form-item>-->
        <!--            <el-form-item :label="t('chart.remark_bg_color')" class="form-item">-->
        <!--              <el-color-picker-->
        <!--                v-model="state.titleForm.remarkBackgroundColor"-->
        <!--                class="color-picker-style"-->
        <!--                :predefine="predefineColors"-->
        <!--                @change="changeTitleStyle('remarkBackgroundColor')"-->
        <!--              />-->
        <!--            </el-form-item>-->
        <!--          </span>-->
      </el-form>
    </el-col>

    <!--富文本编辑框-->
    <!--    <el-dialog-->
    <!--      v-if="showEditRemark"-->
    <!--      :title="t('chart.remark')"-->
    <!--      :visible="showEditRemark"-->
    <!--      :show-close="false"-->
    <!--      width="70%"-->
    <!--      class="dialog-css"-->
    <!--      append-to-body-->
    <!--    >-->
    <!--      <remark-editor-->
    <!--        :remark="titleForm.remark"-->
    <!--        :background="titleForm.remarkBackgroundColor"-->
    <!--        @onRemarkChange="onRemarkChange"-->
    <!--      />-->
    <!--      <div slot="footer" class="dialog-footer">-->
    <!--        <el-button size="small" @click="closeRemark">{{ t('chart.cancel') }} </el-button>-->
    <!--        <el-button type="primary" size="small" @click="changeRemark"-->
    <!--          >{{ t('chart.confirm') }}-->
    <!--        </el-button>-->
    <!--      </div>-->
    <!--    </el-dialog>-->
  </div>
</template>

<style lang="less" scoped></style>
