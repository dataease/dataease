<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-row class="watermark-table__content">
    <el-scrollbar>
      <el-row class="watermark-main-outer">
        <el-col class="main-col-left" :span="12">
          <el-form
            ref="watermarkForm"
            :model="state.watermarkForm"
            label-width="120px"
            size="middle"
          >
            <el-form-item
              class="watermark-ed-form-item"
              :label="t('watermark.enable')"
              style="text-align: left"
            >
              <el-switch
                size="middle"
                v-model="state.watermarkForm.enable"
                @change="enableChange"
              ></el-switch>
            </el-form-item>
            <el-form-item class="watermark-ed-form-item" label="" style="text-align: left">
              <el-checkbox
                :disabled="!state.watermarkForm.enable"
                :label="t('watermark.excel_enable')"
                v-model="state.watermarkForm.excelEnable"
              ></el-checkbox>
            </el-form-item>
            <el-form-item class="watermark-ed-form-item" label="" style="text-align: left">
              <el-checkbox
                :disabled="!state.watermarkForm.enable"
                :label="t('watermark.enable_panel_custom')"
                v-model="state.watermarkForm.enablePanelCustom"
              ></el-checkbox>
            </el-form-item>
            <el-form-item
              class="watermark-ed-form-item"
              :label="t('watermark.content')"
              style="text-align: left"
            >
              <el-select :disabled="!state.watermarkForm.enable" v-model="state.watermarkForm.type">
                <el-option :label="t('watermark.custom_content')" value="custom" />
                <el-option :label="t('watermark.account')" value="userName" />
                <el-option :label="t('watermark.nick_name')" value="nickName" />
                <el-option :label="t('watermark.ip')" value="ip" />
                <el-option :label="t('watermark.now')" value="time" />
              </el-select>
            </el-form-item>

            <el-form-item
              class="watermark-ed-form-item"
              label=""
              v-show="state.watermarkForm.type === 'custom'"
              style="text-align: left"
            >
              <params-tips></params-tips>
              <el-input
                :disabled="!state.watermarkForm.enable"
                v-model="state.watermarkForm.content"
                type="textarea"
                :autosize="{ minRows: 4, maxRows: 4 }"
              />
            </el-form-item>

            <el-form-item
              class="watermark-ed-form-item"
              :label="t('watermark.watermark_color')"
              style="text-align: left"
            >
              <el-color-picker
                :disabled="!state.watermarkForm.enable"
                v-model="state.watermarkForm.watermark_color"
                :predefine="state.predefineColors"
                is-custom
                :trigger-width="80"
              />
            </el-form-item>
            <el-form-item
              class="watermark-ed-form-item"
              :label="t('watermark.watermark_font_size')"
              style="text-align: left"
            >
              <el-input-number
                :disabled="!state.watermarkForm.enable"
                v-model="state.watermarkForm.watermark_fontsize"
                :min="10"
                :max="32"
                size="middle"
                @change="onValChange('watermark_fontsize')"
              />
              &nbsp;px
            </el-form-item>
            <el-form-item
              class="watermark-ed-form-item"
              :label="t('watermark.horizontal')"
              style="text-align: left"
            >
              <el-input-number
                :disabled="!state.watermarkForm.enable"
                v-model="state.watermarkForm.watermark_x_space"
                :min="10"
                :max="400"
                @change="onValChange('watermark_x_space')"
              />
              &nbsp;px
            </el-form-item>
            <el-form-item
              class="watermark-ed-form-item"
              :label="t('watermark.vertical')"
              style="text-align: left"
            >
              <el-input-number
                :disabled="!state.watermarkForm.enable"
                v-model="state.watermarkForm.watermark_y_space"
                :min="10"
                :max="400"
                @change="onValChange('watermark_y_space')"
              />
              &nbsp;px
            </el-form-item>
          </el-form>
          <el-row style="margin-left: 53px; text-align: left">
            <el-button size="middle" @click="cancel">{{ t('watermark.reset') }} </el-button>
            <el-button size="middle" @click="preview">{{ t('watermark.preview') }} </el-button>
            <el-button type="primary" size="middle" @click="save"
              >{{ t('watermark.save') }}
            </el-button>
          </el-row>
        </el-col>
        <el-col :span="12" style="height: 100%">
          <div
            id="watermark-demo"
            style="position: relative; width: 100%; height: 100%; padding: 20px"
          >
            <div class="demo-preview">
              <img style="height: 100%" src="@/assets/img/watermark-demo-light.png" />
            </div>
            <div class="demo-preview" style="margin-top: 15px">
              <img style="height: 100%" src="@/assets/img/watermark-demo-dark.png" />
            </div>
          </div>
        </el-col>
      </el-row>
    </el-scrollbar>
  </el-row>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { watermarkFind, watermarkSave } from '@/api/watermark'
import { ElMessage } from 'element-plus-secondary/es'
import { personInfoApi } from '@/api/user'
import { getNow, watermark } from '@/components/watermark/watermark'
import { useI18n } from '@/hooks/web/useI18n'
import ParamsTips from '@/views/menu/system/watermark/ParamsTips.vue'
const { t } = useI18n()

const state = reactive({
  userLoginInfo: {
    username: '',
    nickName: '',
    ip: ''
  },
  cmOption: {
    tabSize: 2,
    styleActiveLine: true,
    lineNumbers: true,
    line: true,
    mode: 'text/x-textile',
    theme: 'solarized',
    hintOptions: {
      // 自定义提示选项
      completeSingle: false // 当匹配只有一项的时候是否自动补全
    }
  },
  watermarkFormSource: null,
  predefineColors: [
    '#ff4500',
    '#ff8c00',
    '#ffd700',
    '#90ee90',
    '#00ced1',
    '#1e90ff',
    '#c71585',
    '#999999',
    '#000000',
    '#FFFFFF'
  ],
  watermarkForm: {
    enable: false,
    excelEnable: false,
    enablePanelCustom: false,
    type: 'userName',
    content: '${time}-${ip}-${nickName}',
    watermark_color: '#999999',
    watermark_x_space: 100,
    watermark_y_space: 100,
    watermark_fontsize: 20
  }
})

const onValChange = valKey => {
  if (state.watermarkForm[valKey] === null || state.watermarkForm[valKey] < 10) {
    nextTick(() => {
      state.watermarkForm[valKey] = 10
    })
  }
}

const enableChange = val => {
  initWatermark()
}

const preview = () => {
  initWatermark()
}

const cancel = () => {
  state.watermarkForm = { ...state.watermarkFormSource }
  const params = {
    settingContent: JSON.stringify(state.watermarkForm)
  }
  watermarkSave(params).then(rsp => {
    //ignore
  })
  initWatermark()
}

const save = () => {
  const params = {
    settingContent: JSON.stringify(state.watermarkForm)
  }
  watermarkSave(params).then(rsp => {
    ElMessage.success('保存成功')
  })
}

const findData = callback => {
  watermarkFind().then(rsp => {
    callback(rsp)
  })
}

const findUserData = callback => {
  personInfoApi().then(rsp => {
    callback(rsp)
  })
}

const initData = () => {
  findData(res => {
    state.watermarkForm = JSON.parse(res.data.settingContent)
    state.watermarkFormSource = { ...state.watermarkForm }
    initWatermark()
  })
}

const initWatermark = () => {
  let watermark_txt
  let watermark_width = 120
  if (state.watermarkForm.type === 'custom') {
    watermark_txt = state.watermarkForm.content
    watermark_txt = watermark_txt.replaceAll('${ip}', state.userLoginInfo.ip)
    watermark_txt = watermark_txt.replaceAll('${username}', state.userLoginInfo.account)
    watermark_txt = watermark_txt.replaceAll('${nickName}', state.userLoginInfo.name)
    watermark_txt = watermark_txt.replaceAll('${time}', getNow())
    watermark_width = watermark_txt.length * state.watermarkForm.watermark_fontsize * 0.75
    watermark_width = watermark_width > 350 ? 350 : watermark_width
  } else if (state.watermarkForm.type === 'nickName') {
    watermark_txt = state.userLoginInfo.name
  } else if (state.watermarkForm.type === 'ip') {
    watermark_txt = state.userLoginInfo.ip
    watermark_width = watermark_txt.length * state.watermarkForm.watermark_fontsize + 30
  } else if (state.watermarkForm.type === 'time') {
    watermark_txt = getNow()
    watermark_width = 200
  } else {
    watermark_txt = state.userLoginInfo.account
  }
  const settings = {
    watermark_enable: state.watermarkForm.enable,
    watermark_txt: watermark_txt,
    watermark_width: watermark_width,
    watermark_color: state.watermarkForm.watermark_color,
    watermark_x_space: state.watermarkForm.watermark_x_space,
    watermark_y_space: state.watermarkForm.watermark_y_space,
    watermark_fontsize: state.watermarkForm.watermark_fontsize + 'px'
  }
  // 清理历史水印
  const historyWatermarkDom = document.getElementById('watermark-demo-de-watermark-server')
  if (historyWatermarkDom) {
    historyWatermarkDom.remove()
  }
  if (state.watermarkForm.enable) {
    watermark(settings, 'watermark-demo')
  }
}

onMounted(() => {
  findUserData(res => {
    state.userLoginInfo = res.data
    initData()
  })
})
</script>

<style lang="less" scoped>
.demo-preview {
  height: calc(50vh - 130px);
  width: 100%;
  float: left;
  overflow-x: auto;
  overflow-y: hidden;
  text-align: center;
}

.main-col-left {
  overflow-y: auto;
  min-width: 240px;
  padding-right: 20px;
  padding-top: 20px;
  height: 100%;
  border-right: 1px solid gainsboro;
}

.watermark-main-outer {
  border: 1px solid gainsboro;
  height: calc(100vh - 152px);
  min-width: 800px;
  min-height: 500px;
  overflow-y: hidden;
}

.watermark-table__content {
  padding: 24px;
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100% - 8px) !important;
  box-sizing: border-box;
  border-radius: 12px;
  margin-top: 8px;
}

.watermark-ed-form-item {
  margin-bottom: 16px;
  ::v-deep(.ed-checkbox) {
    height: 24px !important;
  }
}
</style>
