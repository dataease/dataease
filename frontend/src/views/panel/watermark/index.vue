<template>
  <el-row style="padding-top: 20px">
    <el-col :span="10">
      <el-form
        ref="watermarkForm"
        :model="watermarkForm"
        label-width="120px"
        size="mini"
      >
        <el-form-item label="">
          <el-checkbox
            v-model="watermarkForm.enablePanelCustom"
          >允许仪表板单独打开或者关闭水印
          </el-checkbox>
        </el-form-item>
        <el-form-item label="水印设置">
          <el-switch v-model="watermarkForm.enable"/>
        </el-form-item>
        <el-form-item label="内容">
          <el-select
            v-model="watermarkForm.type"
          >
            <el-option
              label="自定义公式"
              value="custom"
            />
            <el-option
              label="账号"
              value="userName"
            />
            <el-option
              label="昵称"
              value="nickName"
            />
            <el-option
              label="IP"
              value="ip"
            />
            <el-option
              label="当前时间"
              value="time"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-show="watermarkForm.type==='custom'">
          <codemirror
            ref="myCm"
            v-model="watermarkForm.content"
            class="codemirror"
            :options="cmOption"
            @ready="onCmReady"
            @focus="onCmFocus"
            @input="onCmCodeChange"
          />
        </el-form-item>

        <el-form-item label="水印颜色">
          <el-color-picker
            v-model="watermarkForm.watermark_color"
            :predefine="predefineColors"
            size="mini"
          />
        </el-form-item>
        <el-form-item label="水印字号">
          <el-input-number
            v-model="watermarkForm.watermark_fontsize"
            :min="12"
            :max="64"
            size="mini"
          />
        </el-form-item>
        <el-form-item label="水印间距">
          横向：
          <el-input-number
            v-model="watermarkForm.watermark_x_space"
            :max="400"
            size="mini"
          />
          px

          纵向：
          <el-input-number
            v-model="watermarkForm.watermark_y_space"
            :max="400"
            size="mini"
          />
          px
        </el-form-item>
      </el-form>
      <el-row style="text-align: center">
        <el-button
          size="mini"
          type="text"
          @click="preview"
        >预览
        </el-button>
        <el-button
          size="mini"
          type="i"
          @click="cancel"
        >取消
        </el-button>
        <el-button
          type="primary"
          size="mini"
          @click="save"
        >保存
        </el-button>
      </el-row>
    </el-col>
    <el-col :span="14" style="padding: 20px;border-left: 1px solid gainsboro;">
      <div id="watermark-demo" style="height: 100%;width: 100%;position: relative">
        <img width="100%" src="@/assets/watermark-demo.png">
      </div>
    </el-col>
  </el-row>
</template>

<script>


import draggable from 'vuedraggable'
import { codemirror } from 'vue-codemirror'
// 核心样式
import 'codemirror/lib/codemirror.css'
// 引入主题后还需要在 options 中指定主题才会生效
import 'codemirror/theme/solarized.css'
import 'codemirror/mode/sql/sql.js'
// require active-line.js
import 'codemirror/addon/selection/active-line.js'
// closebrackets
import 'codemirror/addon/edit/closebrackets.js'
// keyMap
import 'codemirror/mode/clike/clike.js'
import 'codemirror/addon/edit/matchbrackets.js'
import 'codemirror/addon/comment/comment.js'
import 'codemirror/addon/dialog/dialog.js'
import 'codemirror/addon/dialog/dialog.css'
import 'codemirror/addon/search/searchcursor.js'
import 'codemirror/addon/search/search.js'
import 'codemirror/keymap/emacs.js'
// 引入代码自动提示插件
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/sql-hint'
import 'codemirror/addon/hint/show-hint'
import { getNow, watermark } from '@/components/canvas/tools/watermark'

export default {
  name: 'Watermark',
  components: { codemirror, draggable },
  props: {},
  data() {
    return {
      cmOption: {
        tabSize: 2,
        styleActiveLine: true,
        lineNumbers: true,
        line: true,
        mode: 'text/x-textile',
        theme: 'solarized',
        hintOptions: { // 自定义提示选项
          completeSingle: false // 当匹配只有一项的时候是否自动补全
        }
      },
      watermarkForm: {
        enable: false,
        enablePanelCustom: false,
        type: 'userName',
        content: '',
        watermark_color: '#999999',
        watermark_x_space: 100,
        watermark_y_space: 100,
        watermark_fontsize: 20
      },
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
      ]
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    }
  },
  watch: {},
  created() {

  },
  mounted() {
    this.initWatermark()
  },
  methods: {
    preview() {

    },
    cancel() {

    },
    save() {

    },
    initWatermark() {
      const settings = {
        watermark_txt: '王嘉豪' + getNow(),
        watermark_color: this.watermarkForm.watermark_color,
        watermark_x_space: this.watermarkForm.watermark_x_space,
        watermark_y_space: this.watermarkForm.watermark_y_space,
        watermark_fontsize: this.watermarkForm.watermark_fontsize
      }
      const now = getNow()
      watermark(settings, 'watermark-demo')
    },
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {
    },
    onCmCodeChange(newCode) {
      // this.fieldForm.originName = newCode
    }
  }
}
</script>

<style lang="scss" scoped>

::v-deep .CodeMirror {
  height: 190px !important;
}

.codemirror {
  height: 190px;
  overflow-y: auto;
  font-size: 12px;
}

.codemirror ::v-deep .CodeMirror-scroll {
  height: 200px;
  overflow-y: auto;
  font-size: 12px;
}

</style>
