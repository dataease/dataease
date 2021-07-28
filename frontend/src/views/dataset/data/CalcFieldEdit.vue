<template>
  <el-row>
    <el-form ref="form" :model="fieldForm" size="mini" class="row-style">
      <el-form-item>
        <span style="width: 60px;font-size: 12px">{{ $t('dataset.field_name') }}</span>
        <el-input v-model="fieldForm.name" size="mini" :placeholder="$t('dataset.input_name')" />
      </el-form-item>
    </el-form>

    <el-row style="height: 420px;">
      <el-col :span="14" style="height: 100%">
        <el-row>
          <el-row>
            <span>{{ $t('dataset.field_exp') }}</span>
            <codemirror
              ref="myCm"
              v-model="fieldExp"
              class="codemirror"
              :options="cmOption"
              @ready="onCmReady"
              @focus="onCmFocus"
              @input="onCmCodeChange"
            />
          </el-row>
          <el-row style="margin-top: 10px;">
            <el-form ref="form" :model="fieldForm" size="mini" class="row-style">
              <el-form-item>
                <span style="width: 80px;font-size: 12px">{{ $t('dataset.data_type') }}</span>
                <el-radio-group v-model="fieldForm.groupType" size="mini">
                  <el-radio-button label="d">{{ $t('chart.dimension') }}</el-radio-button>
                  <el-radio-button label="q">{{ $t('chart.quota') }}</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <span style="width: 80px;font-size: 12px">{{ $t('dataset.field_type') }}</span>
                <el-select v-model="fieldForm.deType" size="mini">
                  <el-option
                    v-for="item in fields"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  >
                    <span style="float: left">
                      <svg-icon v-if="item.value === 0" icon-class="field_text" class="field-icon-text" />
                      <svg-icon v-if="item.value === 1" icon-class="field_time" class="field-icon-time" />
                      <svg-icon v-if="item.value === 2 || item.value === 3" icon-class="field_value" class="field-icon-value" />
                      <svg-icon v-if="item.value === 5" icon-class="field_location" class="field-icon-location" />
                    </span>
                    <span style="float: left; color: #8492a6; font-size: 12px">{{ item.label }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </el-row>
        </el-row>
      </el-col>
      <el-col :span="10" style="height: 100%;border-left: 1px solid #E6E6E6;">
        <el-col :span="12" style="height: 100%">
          <span>{{ $t('dataset.click_ref_field') }}</span>
          <div class="padding-lr field-height">
            <span>{{ $t('chart.dimension') }}</span>
            <draggable
              v-model="tableFields.dimensionList"
              :options="{group:{name: 'drag',pull:'clone'},sort: true}"
              animation="300"
              class="drag-list"
              :disabled="true"
            >
              <transition-group>
                <span v-for="item in tableFields.dimensionList" :key="item.id" class="item-dimension" :title="item.name" @click="insertParamToCodeMirror(item.id)">
                  <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
                  <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
                  <svg-icon v-if="item.deType === 2 || item.deType === 3" icon-class="field_value" class="field-icon-value" />
                  <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
                  {{ item.name }}
                </span>
              </transition-group>
            </draggable>
          </div>
          <div class="padding-lr field-height">
            <span>{{ $t('chart.quota') }}</span>
            <draggable
              v-model="tableFields.quotaList"
              :options="{group:{name: 'drag',pull:'clone'},sort: true}"
              animation="300"
              class="drag-list"
              :disabled="true"
            >
              <transition-group>
                <span v-for="item in tableFields.quotaList" :key="item.id" class="item-quota" :title="item.name" @click="insertParamToCodeMirror(item.id)">
                  <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
                  <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
                  <svg-icon v-if="item.deType === 2 || item.deType === 3" icon-class="field_value" class="field-icon-value" />
                  <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
                  <span>{{ item.name }}</span>
                </span>
              </transition-group>
            </draggable>
          </div>
        </el-col>
        <el-col :span="12" style="height: 100%">
          <span>{{ $t('dataset.click_ref_function') }}</span>
          <el-row class="padding-lr function-height">
            <span v-for="(item,index) in functions" :key="index" class="function-style" @click="insertParamToCodeMirror(item.name)">{{ item.name }}</span>
          </el-row>
        </el-col>
      </el-col>
    </el-row>
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

export default {
  name: 'CalcFieldEdit',
  components: { codemirror, draggable },
  props: {
    param: {
      type: Object,
      required: true
    },
    tableFields: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      fieldForm: {
        name: '',
        groupType: 'd',
        deType: 0
      },
      fieldExp: '',
      cmOption: {
        tabSize: 2,
        styleActiveLine: true,
        lineNumbers: true,
        line: true,
        mode: 'text/x-sql',
        theme: 'solarized',
        hintOptions: { // 自定义提示选项
          completeSingle: false // 当匹配只有一项的时候是否自动补全
        }
      },
      fields: [
        { label: this.$t('dataset.text'), value: 0 },
        { label: this.$t('dataset.time'), value: 1 },
        { label: this.$t('dataset.value'), value: 2 },
        { label: this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')', value: 3 },
        { label: this.$t('dataset.location'), value: 5 }
      ],
      functions: [
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' },
        { name: 'ABS(n)' }
      ]
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    }
  },
  mounted() {
    this.$refs.myCm.codemirror.on('keypress', () => {
      this.$refs.myCm.codemirror.showHint()
    })
  },
  methods: {
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {
      // console.log('the editor is focus!', cm)
    },
    onCmCodeChange(newCode) {
      // console.log(newCode)
      this.fieldExp = newCode
    },
    insertParamToCodeMirror(param) {
      const pos1 = this.$refs.myCm.codemirror.getCursor()
      const pos2 = {}
      pos2.line = pos1.line
      pos2.ch = pos1.ch
      this.$refs.myCm.codemirror.replaceRange(param, pos2)
    }
  }
}
</script>

<style scoped>
  .row-style>>>.el-form-item__label{
    font-size: 12px;
  }
  .row-style>>>.el-form-item--mini.el-form-item{
    margin-bottom: 10px;
  }
  .row-style>>>.el-form-item__content{
    display: flex;
    flex-direction: row;
    width: 100%;
  }

  .codemirror {
    height: 300px;
    overflow-y: auto;
    font-size: 12px;
  }
  .codemirror >>> .CodeMirror-scroll {
    height: 300px;
    overflow-y: auto;
    font-size: 12px;
  }

  .padding-lr {
    padding: 0 6px;
  }
  .field-height{
    height: calc(50% - 20px);
  }
  .drag-list {
    height: calc(100% - 26px);
    overflow:auto;
  }
  .item-dimension {
    padding: 2px 10px;
    margin: 2px 2px 0 2px;
    border: solid 1px #eee;
    text-align: left;
    color: #606266;
    /*background-color: rgba(35,46,64,.05);*/
    background-color: white;
    display: block;
    word-break: break-all;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .item-dimension + .item-dimension {
    margin-top: 2px;
  }

  .item-dimension:hover {
    color: #1890ff;
    background: #e8f4ff;
    border-color: #a3d3ff;
    cursor: pointer;
  }

  .item-quota {
    padding: 2px 10px;
    margin: 2px 2px 0 2px;
    border: solid 1px #eee;
    text-align: left;
    color: #606266;
    /*background-color: rgba(35,46,64,.05);*/
    background-color: white;
    display: block;
    word-break: break-all;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .item-quota + .item-quota {
    margin-top: 2px;
  }

  .item-quota:hover {
    color: #67c23a;
    background: #f0f9eb;
    border-color: #b2d3a3;
    cursor: pointer;
  }
  span{
    font-size: 12px;
  }
  .function-style{
    color: #0a7be0;
    display: block;
    padding: 2px 4px;
    cursor: pointer;
    margin: 4px 0;
  }
  .function-height{
    height: calc(100% - 20px);
    overflow: auto;
  }
</style>
