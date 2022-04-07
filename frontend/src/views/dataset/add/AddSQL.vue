<template>
  <el-col>
    <el-row>
      <el-row style="height: 26px;" class="title-text">
        <span style="line-height: 26px;">
          {{ param.tableId ? $t('dataset.edit_sql') : $t('dataset.add_sql_table') }}
        </span>
        <el-row style="float: right">
          <el-button size="mini" @click="cancel">
            {{ $t('dataset.cancel') }}
          </el-button>
          <el-button size="mini" type="primary" @click="save">
            {{ $t('dataset.confirm') }}
          </el-button>
        </el-row>
      </el-row>
      <el-divider/>
      <el-row>
        <el-form :inline="true">
          <el-form-item class="form-item">
            <el-select v-model="dataSource" filterable :placeholder="$t('dataset.pls_slc_data_source')" size="mini"
                       @change="changeDatasource()">
              <el-option
                v-for="item in options"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item class="form-item">
            <el-input v-model="name" size="mini" :placeholder="$t('commons.name')"/>
          </el-form-item>
          <el-form-item v-if="!param.tableId" class="form-item">
            <el-select v-model="mode" filterable :placeholder="$t('dataset.connect_mode')" size="mini">
              <el-option :label="$t('dataset.direct_connect')" value="0"/>
              <el-option :label="$t('dataset.sync_data')" value="1"
                         :disabled="disabledSync"/>
            </el-select>
          </el-form-item>

          <el-form-item v-if="mode === '1'" class="form-item">
            <el-select v-model="syncType" filterable :placeholder="$t('dataset.connect_mode')" size="mini">
              <el-option :label="$t('dataset.sync_now')" value="sync_now" :disabled="engineMode === 'simple'"/>
              <el-option :label="$t('dataset.sync_latter')" value="sync_latter"/>
            </el-select>
          </el-form-item>
        </el-form>
      </el-row>
      <el-row>
        <el-col style="min-width: 200px;">
          <codemirror
            ref="myCm"
            v-model="sql"
            class="codemirror"
            :options="sqlOption"
            @ready="onCmReady"
            @focus="onCmFocus"
            @input="onCmCodeChange"
          />
        </el-col>
      </el-row>
      <el-row style="margin-top: 10px;">
        <el-card class="box-card dataPreview" shadow="never">
          <div slot="header" class="clearfix">
            <span>{{ $t('dataset.data_preview') }}</span>
            <el-button style="float: right; padding: 3px 0" type="text" size="mini" @click="getSQLPreview">
              {{ $t('dataset.preview') }}
            </el-button>
          </div>
          <div class="text item">
            <ux-grid
              ref="plxTable"
              size="mini"
              style="width: 100%;"
              :height="height"
              :checkbox-config="{highlight: true}"
              :width-resize="true"
            >
              <ux-table-column
                v-for="field in fields"
                :key="field.fieldName"
                min-width="200px"
                :field="field.fieldName"
                :title="field.remarks"
                :resizable="true"
              />
            </ux-grid>
          </div>
          <span class="table-count">
            {{ $t('dataset.preview_show') }}
            <span class="span-number">1000</span>
            {{ $t('dataset.preview_item') }}
          </span>
        </el-card>
      </el-row>
    </el-row>
  </el-col>
</template>

<script>
import {post, listDatasource, isKettleRunning, disabledSyncDs} from '@/api/dataset/dataset'
import {codemirror} from 'vue-codemirror'
import {getTable} from '@/api/dataset/dataset'
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
import {engineMode} from "@/api/system/engine";

export default {
  name: 'AddSQL',
  components: {codemirror},
  props: {
    param: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      dataSource: '',
      options: [],
      name: '',
      sql: '',
      sqlOption: {
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
      data: [],
      fields: [],
      mode: '0',
      syncType: 'sync_now',
      height: 500,
      kettleRunning: false,
      selectedDatasource: {},
      engineMode: 'local',
      disabledSync: true,
      disabledSyncDs: disabledSyncDs
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    }
  },
  watch: {
    'param.tableId': {
      handler: function () {
        this.resetComponent()
        this.initTableInfo()
      }
    }
  },
  mounted() {
    window.onresize = () => {
      this.calHeight()
    }
    this.calHeight()
    this.initDataSource()
    this.$refs.myCm.codemirror.on('keypress', () => {
      this.$refs.myCm.codemirror.showHint()
    })

    this.initTableInfo()
  },
  created() {
    this.kettleState()
    engineMode().then(res => {
      this.engineMode = res.data
    })
  },
  methods: {
    kettleState() {
      isKettleRunning().then(res => {
        this.kettleRunning = res.data
      })
    },
    changeDatasource() {
      for (let i = 0; i < this.options.length; i++) {
        if (this.options[i].id === this.dataSource) {
          this.selectedDatasource = this.options[i]
          this.mode = '0'
          if (this.engineMode === 'simple' || (!this.kettleRunning || this.disabledSyncDs.indexOf(this.selectedDatasource.type) !== -1 )) {
            this.disabledSync = true
          } else {
            this.disabledSync = false
          }
        }
      }
    },
    calHeight() {
      const that = this
      setTimeout(function () {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 56 - 30 - 26 - 25 - 43 - 160 - 10 - 37 - 20 - 10 - 16
      }, 10)
    },
    initDataSource() {
      listDatasource().then(response => {
        this.options = response.data.filter(item => item.type !== 'api')
      })
    },

    initTableInfo() {
      if (this.param.tableId) {
        getTable(this.param.tableId).then(response => {
          const table = response.data
          this.name = table.name
          this.dataSource = table.dataSourceId
          this.mode = table.mode + ''
          this.sql = JSON.parse(table.info.replace(/\n/g, '\\n').replace(/\r/g, '\\r')).sql

          this.getSQLPreview()
        })
      }
    },

    getSQLPreview() {
      if (!this.dataSource || this.datasource === '') {
        this.$message({
          showClose: true,
          message: this.$t('dataset.pls_slc_data_source'),
          type: 'error'
        })
        return
      }
      post('/dataset/table/sqlPreview', {
        dataSourceId: this.dataSource,
        type: 'sql',
        // info: '{"sql":"' + this.sql + '"}',
        info: JSON.stringify({sql: this.sql.trim()})
      }).then(response => {
        this.fields = response.data.fields
        this.data = response.data.data
        const datas = this.data
        this.$refs.plxTable.reloadData(datas)
      })
    },

    save() {
      if (!this.dataSource || this.datasource === '') {
        this.$message({
          showClose: true,
          message: this.$t('dataset.pls_slc_data_source'),
          type: 'error'
        })
        return
      }
      if (!this.name || this.name === '') {
        this.$message({
          showClose: true,
          message: this.$t('dataset.pls_input_name'),
          type: 'error'
        })
        return
      }
      if (this.name.length > 50) {
        this.$message({
          showClose: true,
          message: this.$t('dataset.char_can_not_more_50'),
          type: 'error'
        })
        return
      }
      const table = {
        id: this.param.tableId,
        name: this.name,
        sceneId: this.param.id,
        dataSourceId: this.dataSource,
        type: 'sql',
        syncType: this.syncType,
        mode: parseInt(this.mode),
        // info: '{"sql":"' + this.sql + '"}',
        info: JSON.stringify({sql: this.sql.trim()})
      }
      post('/dataset/table/update', table).then(response => {
        // this.$store.dispatch('dataset/setSceneData', new Date().getTime())
        this.$emit('saveSuccess', table)
        this.cancel()
      })
    },

    cancel() {
      // this.dataReset()
      if (this.param.tableId) {
        this.$emit('switchComponent', {name: 'ViewTable', param: this.param.table})
      } else {
        this.$emit('switchComponent', {name: ''})
      }
    },

    showSQL(val) {
      this.sql = val || ''
    },
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {
      // console.log('the editor is focus!', cm)
    },
    onCmCodeChange(newCode) {
      // console.log(newCode)
      this.sql = newCode
      this.$emit('codeChange', this.sql)
    },

    resetComponent() {
      this.dataSource = ''
      this.name = ''
      this.sql = ''
      this.data = []
      this.fields = []
      this.mode = '0'
      this.syncType = 'sync_now'
    }
  }
}
</script>

<style scoped>
.el-divider--horizontal {
  margin: 12px 0;
}

.form-item {
  margin-bottom: 6px;
}

.el-checkbox {
  margin-bottom: 14px;
  margin-left: 0;
  margin-right: 14px;
}

.el-checkbox.is-bordered + .el-checkbox.is-bordered {
  margin-left: 0;
}

.codemirror {
  height: 160px;
  overflow-y: auto;
}

.codemirror >>> .CodeMirror-scroll {
  height: 160px;
  overflow-y: auto;
}

.dataPreview >>> .el-card__header {
  padding: 6px 8px;
}

.dataPreview >>> .el-card__body {
  padding: 10px;
}

span {
  font-size: 14px;
}

.span-number {
  color: #0a7be0;
}

.table-count {
  color: #606266;
}
</style>
