<template>
  <el-col>
    <el-row>
      <el-row style="height: 26px;">
        <span style="line-height: 26px;">
          {{ $t('dataset.add_sql_table') }}
        </span>
        <el-row style="float: right">
          <el-button size="mini" @click="cancel">
            {{ $t('dataset.cancel') }}
          </el-button>
          <el-button size="mini" type="primary">
            {{ $t('dataset.confirm') }}
          </el-button>
        </el-row>
      </el-row>
      <el-divider />
      <el-row>
        <el-form :inline="true">
          <el-form-item class="form-item">
            <el-select v-model="dataSource" filterable :placeholder="$t('dataset.pls_slc_data_source')" size="mini">
              <el-option
                v-for="item in options"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item class="form-item">
            <el-input v-model="name" size="mini" placeholder="名称" />
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
    </el-row>
  </el-col>
</template>

<script>
import { post, listDatasource } from '@/api/dataset/dataset'
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

export default {
  name: 'AddSQL',
  components: { codemirror },
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
        hintOptions: {
          completeSingle: true
        }
      }
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    }
  },
  watch: {},
  mounted() {
    this.initDataSource()
  },
  methods: {
    initDataSource() {
      listDatasource().then(response => {
        this.options = response.data
      })
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
      console.log(newCode)
      this.sql = newCode
      this.$emit('codeChange', this.sql)
    },
    cancel() {
      // this.dataReset()
      this.$emit('switchComponent', { name: '' })
    }
  }
}
</script>

<style scoped lang="scss">
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
    height: auto;
    min-height: 100px;
  }
  .codemirror >>> .CodeMirror-scroll {
    height: auto;
    min-height: 100px;
  }
</style>
