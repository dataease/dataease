<template>
  <div class="calcu-field">
    <el-form
      ref="form"
      :model="fieldForm"
      class="de-form-item"
    >
      <el-form-item :label="$t('dataset.field_edit_name')">
        <el-input
          v-model="fieldForm.name"
          size="small"
          :placeholder="$t('dataset.input_edit_name')"
        />
      </el-form-item>
    </el-form>

    <div
      class="calcu-cont"
      style="height: 544px"
    >
      <div style="flex: 1">
        <el-row>
          <el-row style="max-width: 480px;">
            <span class="mb8">
              {{ $t('dataset.field_exp') }}
              <el-tooltip
                class="item"
                effect="dark"
                placement="bottom"
              >
                <div slot="content">
                  {{ $t('dataset.calc_tips.tip1') }}
                  <br>
                  {{ $t('dataset.calc_tips.tip2') }}
                </div>
                <i
                  class="el-icon-info"
                  style="cursor: pointer"
                />
              </el-tooltip>
            </span>
            <codemirror
              ref="myCm"
              v-model="fieldForm.originName"
              class="codemirror"
              :options="cmOption"
              @ready="onCmReady"
              @focus="onCmFocus"
              @input="onCmCodeChange"
            />
          </el-row>
          <el-row style="margin-top: 28px">
            <el-form
              ref="form"
              :model="fieldForm"
              class="de-form-item"
            >
              <el-form-item :label="$t('dataset.data_type')">
                <el-radio
                  v-model="fieldForm.groupType"
                  label="d"
                >{{
                  $t('chart.dimension')
                }}</el-radio>
                <el-radio
                  v-model="fieldForm.groupType"
                  label="q"
                >{{
                  $t('chart.quota')
                }}</el-radio>
              </el-form-item>
              <el-form-item :label="$t('dataset.field_type')">
                <el-radio
                  v-for="item in fields"
                  :key="item.value"
                  v-model="fieldForm.deType"
                  :label="item.value"
                >{{ item.label }}</el-radio>
              </el-form-item>
            </el-form>
          </el-row>
        </el-row>
      </div>
      <div class="padding-lr">
        <span class="mb8">
          {{ $t('dataset.click_ref_field') }}
          <el-tooltip
            class="item"
            effect="dark"
            placement="bottom"
          >
            <div slot="content">
              {{ $t('dataset.calc_tips.tip3') }}
              <br>
              {{ $t('dataset.calc_tips.tip4') }}
              <br>
              {{ $t('dataset.calc_tips.tip5') }}
            </div>
            <i
              class="el-icon-info"
              style="cursor: pointer"
            />
          </el-tooltip>
        </span>
        <el-input
          v-model="searchField"
          size="small"
          :placeholder="$t('dataset.edit_search')"
          prefix-icon="el-icon-search"
          style="margin-bottom: 12px"
          clearable
        />
        <div class="field-height">
          <span>{{ $t('chart.dimension') }}</span>
          <draggable
            v-if="dimensionData && dimensionData.length > 0"
            v-model="dimensionData"
            :options="{ group: { name: 'drag', pull: 'clone' }, sort: true }"
            animation="300"
            class="drag-list"
            :disabled="true"
          >
            <transition-group>
              <span
                v-for="item in dimensionData"
                :key="item.id"
                class="item-dimension"
                :title="item.name"
                @click="insertFieldToCodeMirror('[' + item.name + ']')"
              >
                <svg-icon
                  v-if="item.deType === 0"
                  icon-class="field_text"
                  class="field-icon-text"
                />
                <svg-icon
                  v-if="item.deType === 1"
                  icon-class="field_time"
                  class="field-icon-time"
                />
                <svg-icon
                  v-if="item.deType === 2 || item.deType === 3"
                  icon-class="field_value"
                  class="field-icon-value"
                />
                <svg-icon
                  v-if="item.deType === 5"
                  icon-class="field_location"
                  class="field-icon-location"
                />
                {{ item.name }}
              </span>
            </transition-group>
          </draggable>
          <div
            v-else
            class="class-na"
          >{{ $t('dataset.na') }}</div>
        </div>
        <div class="field-height">
          <span>{{ $t('chart.quota') }}</span>
          <draggable
            v-if="quotaData && quotaData.length > 0"
            v-model="quotaData"
            :options="{ group: { name: 'drag', pull: 'clone' }, sort: true }"
            animation="300"
            class="drag-list"
            :disabled="true"
          >
            <transition-group>
              <span
                v-for="item in quotaData"
                :key="item.id"
                class="item-quota"
                :title="item.name"
                @click="insertFieldToCodeMirror('[' + item.name + ']')"
              >
                <svg-icon
                  v-if="item.deType === 0"
                  icon-class="field_text"
                  class="field-icon-text"
                />
                <svg-icon
                  v-if="item.deType === 1"
                  icon-class="field_time"
                  class="field-icon-time"
                />
                <svg-icon
                  v-if="item.deType === 2 || item.deType === 3"
                  icon-class="field_value"
                  class="field-icon-value"
                />
                <svg-icon
                  v-if="item.deType === 5"
                  icon-class="field_location"
                  class="field-icon-location"
                />
                <span>{{ item.name }}</span>
              </span>
            </transition-group>
          </draggable>
          <div
            v-else
            class="class-na"
          >{{ $t('dataset.na') }}</div>
        </div>
      </div>
      <div class="padding-lr">
        <span class="mb8">
          {{ $t('dataset.click_ref_function') }}
          <el-tooltip
            class="item"
            effect="dark"
            placement="bottom"
          >
            <div slot="content">
              {{ $t('dataset.calc_tips.tip6') }}
              <br>
              {{ $t('dataset.calc_tips.tip7') }}
              <br>
              {{ $t('dataset.calc_tips.tip8') }}
              https://doris.apache.org/zh-CN/
            </div>
            <i
              class="el-icon-info"
              style="cursor: pointer"
            />
          </el-tooltip>
        </span>
        <el-input
          v-model="searchFunction"
          size="small"
          style="margin-bottom: 12px"
          :placeholder="$t('dataset.edit_search')"
          prefix-icon="el-icon-search"
          clearable
        />
        <el-row class="function-height">
          <div v-if="functionData && functionData.length > 0">
            <el-popover
              v-for="(item, index) in functionData"
              :key="index"
              class="function-pop"
              placement="right"
              width="200"
              trigger="hover"
              :open-delay="500"
            >
              <p class="pop-title">{{ item.name }}</p>
              <p class="pop-info">{{ item.func }}</p>
              <p class="pop-info">{{ item.desc }}</p>
              <span
                slot="reference"
                class="function-style"
                @click="insertParamToCodeMirror(item.func)"
              >{{ item.func }}</span>
            </el-popover>
          </div>
          <div
            v-else
            class="class-na"
          >{{ $t('chart.no_function') }}</div>
        </el-row>
      </div>
    </div>

    <div class="de-foot">
      <deBtn
        secondary
        @click="closeCalcField"
      >{{
        $t('dataset.cancel')
      }}</deBtn>
      <deBtn
        :disabled="!fieldForm.name || !fieldForm.originName"
        :loading="loading"
        type="primary"
        @click="saveCalcField"
      >{{ $t('dataset.confirm') }}</deBtn>
    </div>
  </div>
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
import { post } from '../../../api/dataset/dataset'

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
    },
    field: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      fieldForm: {
        id: null,
        name: '',
        groupType: 'd',
        deType: 0,
        originName: '',
        tableId: this.param.id,
        checked: 1,
        columnIndex:
          this.tableFields.dimensionList.length +
          this.tableFields.quotaList.length,
        size: 0,
        extField: 2
      },
      cmOption: {
        tabSize: 2,
        styleActiveLine: true,
        lineNumbers: true,
        line: true,
        mode: 'text/x-sql',
        theme: 'solarized',
        hintOptions: {
          // 自定义提示选项
          completeSingle: false // 当匹配只有一项的时候是否自动补全
        }
      },
      fields: [
        { label: this.$t('dataset.text'), value: 0 },
        { label: this.$t('dataset.time'), value: 1 },
        { label: this.$t('dataset.value'), value: 2 },
        {
          label:
            this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')',
          value: 3
        },
        { label: this.$t('dataset.location'), value: 5 }
      ],
      functions: [],
      searchField: '',
      searchFunction: '',
      dimensionData: [],
      quotaData: [],
      name2Auto: [],
      functionData: [],
      loading: false
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    }
  },
  watch: {
    param: function() {
      this.initFunctions()
    },
    field: {
      handler: function() {
        this.initField()
      },
      deep: true
    },
    tableFields: function() {
      this.dimensionData = JSON.parse(
        JSON.stringify(this.tableFields.dimensionList)
      ).filter((ele) => ele.extField === 0)
      this.quotaData = JSON.parse(
        JSON.stringify(this.tableFields.quotaList)
      ).filter((ele) => ele.extField === 0)
    },
    searchField: function(val) {
      if (val && val !== '') {
        this.dimensionData = JSON.parse(
          JSON.stringify(
            this.tableFields.dimensionList.filter(
              (ele) =>
                ele.name
                  .toLocaleLowerCase()
                  .includes(val.toLocaleLowerCase()) && ele.extField === 0
            )
          )
        )
        this.quotaData = JSON.parse(
          JSON.stringify(
            this.tableFields.quotaList.filter(
              (ele) =>
                ele.name
                  .toLocaleLowerCase()
                  .includes(val.toLocaleLowerCase()) && ele.extField === 0
            )
          )
        )
      } else {
        this.dimensionData = JSON.parse(
          JSON.stringify(this.tableFields.dimensionList)
        ).filter((ele) => ele.extField === 0)
        this.quotaData = JSON.parse(
          JSON.stringify(this.tableFields.quotaList)
        ).filter((ele) => ele.extField === 0)
      }
    },
    searchFunction: function(val) {
      if (val && val !== '') {
        this.functionData = JSON.parse(
          JSON.stringify(
            this.functions.filter((ele) => {
              return ele.func
                .toLocaleLowerCase()
                .includes(val.toLocaleLowerCase())
            })
          )
        )
      } else {
        this.functionData = JSON.parse(JSON.stringify(this.functions))
      }
    }
  },
  mounted() {
    this.loading = false
    this.$refs.myCm.codemirror.on('keypress', () => {
      this.$refs.myCm.codemirror.showHint()
    })
    this.initFunctions()
    this.dimensionData = JSON.parse(
      JSON.stringify(this.tableFields.dimensionList)
    ).filter((ele) => ele.extField === 0)
    this.quotaData = JSON.parse(
      JSON.stringify(this.tableFields.quotaList)
    ).filter((ele) => ele.extField === 0)
    this.initField()
  },
  methods: {
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {},
    onCmCodeChange(newCode) {
      this.fieldForm.originName = newCode
    },
    insertParamToCodeMirror(param) {
      const pos1 = this.$refs.myCm.codemirror.getCursor()
      const pos2 = {}
      pos2.line = pos1.line
      pos2.ch = pos1.ch
      this.$refs.myCm.codemirror.replaceRange(param, pos2)
    },
    insertFieldToCodeMirror(param) {
      const pos1 = this.$refs.myCm.codemirror.getCursor()
      const pos2 = {}
      pos2.line = pos1.line
      pos2.ch = pos1.ch
      this.$refs.myCm.codemirror.replaceRange(param, pos2)
      this.$refs.myCm.codemirror.markText(
        pos2,
        { line: pos2.line, ch: param.length + pos2.ch },
        { atomic: true, selectRight: true }
      )
    },

    initFunctions() {
      post('/dataset/function/listByTableId/' + this.param.id, null).then(
        (response) => {
          this.functions = response.data
          this.functionData = JSON.parse(JSON.stringify(this.functions))
        }
      )
    },

    initField() {
      if (this.field.id) {
        this.fieldForm = JSON.parse(JSON.stringify(this.field))
        this.name2Auto = []
        this.fieldForm.originName = this.setNameIdTrans(
          'id',
          'name',
          this.fieldForm.originName,
          this.name2Auto
        )
        setTimeout(() => {
          this.matchToAuto()
        }, 500)
      } else {
        this.fieldForm = JSON.parse(JSON.stringify(this.fieldForm))
      }
    },
    matchToAuto() {
      if (!this.name2Auto.length) return
      this.name2Auto.forEach((ele) => {
        const search = this.$refs.myCm.codemirror.getSearchCursor(ele, {
          line: 0,
          ch: 0
        })
        if (search.find()) {
          const { from, to } = search.pos
          this.$refs.myCm.codemirror.markText(
            { line: from.line, ch: from.ch - 1 },
            { line: to.line, ch: to.ch + 1 },
            { atomic: true, selectRight: true }
          )
        }
      })
    },
    closeCalcField() {
      this.resetField()
      this.$emit('onEditClose', {})
    },
    setNameIdTrans(from, to, originName, name2Auto) {
      let name2Id = originName
      const nameIdMap = [...this.dimensionData, ...this.quotaData].reduce(
        (pre, next) => {
          pre[next[from]] = next[to]
          return pre
        },
        {}
      )
      const on = originName.match(/\[(.+?)\]/g)
      if (on) {
        on.forEach((itm) => {
          const ele = itm.slice(1, -1)
          if (name2Auto) {
            name2Auto.push(nameIdMap[ele])
          }
          name2Id = name2Id.replace(`[${ele}]`, `[${nameIdMap[ele]}]`)
        })
      }
      return name2Id
    },

    saveCalcField() {
      const { id, name = [], deType, originName } = this.fieldForm
      if (name.length > 50) {
        this.$message.error(this.$t('dataset.field_name_less_50'))
        return
      }
      if (!id) {
        this.fieldForm.type = deType
        this.fieldForm.deExtractType = deType
        this.fieldForm.tableId = this.param.id
        this.fieldForm.columnIndex =
          this.tableFields.dimensionList.length +
          this.tableFields.quotaList.length
      }
      this.loading = true
      post('/dataset/field/save', {
        ...this.fieldForm,
        originName: this.setNameIdTrans('name', 'id', originName)
      })
        .then((response) => {
          localStorage.setItem('reloadDsData', 'true')
          this.closeCalcField()
          this.loading = false
        })
        .catch((res) => {
          this.loading = false
        })
    },

    resetField() {
      this.fieldForm = {
        id: null,
        name: '',
        groupType: 'd',
        deType: 0,
        originName: '',
        tableId: this.param.id,
        checked: 1,
        columnIndex:
          this.tableFields.dimensionList.length +
          this.tableFields.quotaList.length,
        size: 0,
        extField: 2
      }
      this.dimensionData = []
      this.quotaData = []
      this.searchField = ''
      this.searchFunction = ''
    }
  }
}
</script>

<style scoped>
.padding-lr {
  height: calc(100% - 80px);
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  padding: 12px;
  box-sizing: border-box;
  margin-left: 12px;
  width: 214px;
  overflow-y: hidden;
}
.field-height {
  height: calc(50% - 41px);
  margin-top: 4px;
}
.drag-list {
  height: calc(100% - 26px);
  overflow: auto;
}
.item-dimension {
  padding: 3px 8px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
  background-color: white;
  display: block;
  word-break: break-all;
  border-radius: 2px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.blackTheme .item-dimension {
  border: solid 1px;
  border-color: #495865;
  color: #f2f6fc;
  background-color: var(--MainBG);
}

.item-dimension + .item-dimension {
  margin-top: 2px;
}

.item-dimension:hover {
  border-color: var(--primary, #3370ff);
  background: #e8f4ff;
  cursor: pointer;
}

.blackTheme .item-dimension:hover {
  /* color: var(--Main); */
  background: var(--ContentBG);
  /* cursor: pointer; */
}

.item-quota {
  padding: 3px 8px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
  background-color: white;
  display: block;
  border-radius: 2px;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.blackTheme .item-quota {
  border: solid 1px;
  border-color: #495865;
  color: #f2f6fc;
  background-color: var(--MainBG);
}

.item-quota + .item-quota {
  margin-top: 2px;
}

.item-quota:hover {
  background: #f0f9eb;
  border-color: #b2d3a3;
  cursor: pointer;
}

.blackTheme .item-quota:hover {
  background: var(--ContentBG);
}
.function-style {
  color: var(--deTextPrimary, #1f2329);
  display: block;
  padding: 3px 8px;
  cursor: pointer;
  margin: 4px 0;
  word-break: break-word;
  border-radius: 2px;
  border: solid 1px #eee;
}

.blackTheme .function-style {
  border: solid 1px;
  border-color: #495865;
  color: #f2f6fc;
  background-color: var(--MainBG);
}
.function-style:hover {
  border-color: var(--primary, #3370ff);
  cursor: pointer;
}
.blackTheme .function-style:hover {
  background: var(--ContentBG);
}
.function-height {
  height: calc(100% - 81px);
  overflow: auto;
  margin-top: 4px;
}
.function-pop ::v-deep .el-popover {
  padding: 6px !important;
}
.pop-title {
  margin: 6px 0 0 0;
  font-size: 14px;
  font-weight: 500;
}
.pop-info {
  margin: 6px 0 0 0;
  font-size: 10px;
}

.class-na {
  margin-top: 8px;
  text-align: center;
  font-size: 14px;
  color: var(--deTextDisable);
}
</style>
