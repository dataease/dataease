<script>
import { filter, forEach, find, split, get } from 'lodash-es'
import { listDatasource, listDatasourceType } from '@/api/system/datasource'
import { listForm, saveForm } from '@/views/dataFilling/form/dataFilling'

export default {
  name: 'DataFillingFormSave',
  props: {
    form: {
      type: Object,
      required: true
    },
    showDrawer: {
      type: Boolean,
      required: true
    }
  },
  data: function() {
    const checkDuplicateNameValidator = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('必填'))
      }
      let count = 0
      forEach(this.formData.forms, f => {
        if (f.type === 'dateRange') {
          if (f.settings.mapping.columnName1 === value) {
            count++
          }
          if (f.settings.mapping.columnName2 === value) {
            count++
          }
        } else {
          if (f.settings.mapping.columnName === value) {
            count++
          }
        }
      })
      if (count > 1) {
        callback(new Error('重复'))
      }
      callback()
    }
    const checkDuplicateIndexNameValidator = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('必填'))
      }
      let count = 0
      forEach(this.formData.tableIndexes, f => {
        if (f.name === value) {
          count++
        }
      })
      if (count > 1) {
        callback(new Error('重复'))
      }
      callback()
    }
    const checkInvalidColumnValidator = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('必填'))
      }
      if (this.columnsList.length === 0) {
        return callback(new Error('值不存在'))
      }
      if (find(this.columnsList, c => c === value) === undefined) {
        callback(new Error('值不存在'))
      }
      callback()
    }
    const checkDuplicateIndexColumnValidator = (rule, value, callback, source) => {
      if (!value) {
        return callback(new Error('必填'))
      }
      const f = split(rule.field, '.')[0]
      const _list = get(this.formData, f)

      let count = 0
      forEach(_list.columns, f => {
        if (f.column === value) {
          count++
        }
      })
      if (count > 1) {
        callback(new Error('重复'))
      }
      callback()
    }
    return {
      loading: false,
      requiredRule: { required: true, message: this.$t('commons.required'), trigger: ['blur', 'change'] },
      duplicateRule: { validator: checkDuplicateNameValidator, trigger: 'blur' },
      duplicateIndexRule: { validator: checkDuplicateIndexNameValidator, trigger: 'blur' },
      duplicateIndexColumnRule: { validator: checkDuplicateIndexColumnValidator, trigger: ['blur', 'change'] },
      invalidColumnRule: { validator: checkInvalidColumnValidator, trigger: ['blur', 'change'] },
      formData: {},
      folders: [],
      allDatasourceTypes: [],
      allDatasourceList: [],
      folderTreeShow: false
    }
  },
  computed: {
    datasourceList() {
      const _types = filter(this.allDatasourceTypes, t => t.type === 'mysql' || t.type === 'mariadb')
      forEach(_types, t => {
        t.options = filter(this.allDatasourceList, d => d.type === t.type)
      })
      return _types
    },
    selectDatasets() {
      const result = []
      this.flattenFolder(this.folders, result)
      return result
    },
    columnsList() {
      const _list = []
      for (let i = 0; i < this.formData.forms.length; i++) {
        const row = this.formData.forms[i]
        if (row.type === 'dateRange') {
          if (row.settings.mapping.columnName1 !== undefined && row.settings.mapping.columnName1 !== '') {
            _list.push(row.settings.mapping.columnName1)
          }
          if (row.settings.mapping.columnName2 !== undefined && row.settings.mapping.columnName2 !== '') {
            _list.push(row.settings.mapping.columnName2)
          }
        } else {
          if (row.settings.mapping.columnName !== undefined && row.settings.mapping.columnName !== '' && row.settings.mapping.type !== 'text') {
            _list.push(row.settings.mapping.columnName)
          }
        }
      }

      return _list
    }
  },
  watch: {
    formData: {
      handler(newVal, oldVal) {
        this.$emit('update:form', newVal)
      },
      deep: true
    }
  },
  mounted() {
    this.loading = true
    this.formData = this.form

    forEach(this.formData.forms, f => {
      f.settings.mapping.typeOptions = this.getTypeOptions(f)
      if (!f.settings.mapping.type) {
        f.settings.mapping.type = f.settings.mapping.typeOptions[0].value
      }
    })
    const p1 = listDatasourceType()
    const p2 = listDatasource()
    const p3 = listForm({ nodeType: 'folder' })

    Promise.all([p1, p2, p3]).then((val) => {
      this.allDatasourceTypes = val[0].data

      this.allDatasourceList = val[1].data

      this.folders = val[2].data || []
      if (this.formData.folder) {
        this.$nextTick(() => {
          this.$refs.tree.setCurrentKey(this.formData.folder)
          this.$refs.tree.setCheckedKeys([this.formData.folder])
        })
      }
    }).finally(() => {
      this.loading = false
    })
  },
  methods: {
    getTypeOptions(formOption) {
      const _options = []
      if (formOption.type !== 'date' &&
        formOption.type !== 'dateRange' &&
        formOption.settings.inputType !== 'number' &&
        formOption.type !== 'textarea' &&
        formOption.type !== 'checkbox' &&
        !(formOption.type === 'select' && formOption.settings.multiple)
      ) {
        _options.push({ value: 'nvarchar', label: '字符串' })
      }
      if (formOption.type === 'checkbox' ||
        formOption.type === 'select' && formOption.settings.multiple ||
        formOption.type === 'textarea') {
        _options.push({ value: 'text', label: '长文本' })
      }

      if (formOption.type === 'input' && formOption.settings.inputType === 'number') {
        _options.push({ value: 'number', label: '整型数字' })
        _options.push({ value: 'decimal', label: '小数数字' })
      }
      if (formOption.type === 'date' || formOption.type === 'dateRange') {
        _options.push({ value: 'datetime', label: '日期' })
      }
      return _options
    },
    flattenFolder(list, result = []) {
      forEach(list, item => {
        result.push(item)
        if (item.children && item.children.length > 0) {
          this.flattenFolder(item.children, result)
        }
      })
      return result
    },
    closeSave() {
      this.$emit('update:showDrawer', false)
    },
    validateForm() {
      this.$nextTick(() => {
        this.$refs['mRightForm'].validate()
      })
    },
    addIndex() {
      this.formData.tableIndexes.push({
        name: undefined,
        columns: [{
          column: undefined,
          order: 'none'
        }]
      })
    },
    removeIndex(index) {
      this.formData.tableIndexes.splice(index, 1)
    },
    removeIndexColumn(list, index) {
      list.splice(index, 1)
    },
    addColumn(list) {
      list.push({
        column: undefined,
        order: 'none'
      })
    },
    nodeClick(data) {
      this.$nextTick(() => {
        this.formData.folder = data.id
        this.formData.level = data.level + 1
        this.folderTreeShow = false
      })
    },
    filterMethod(val) {
      if (!val) this.$refs.tree.filter(val)
      this.$refs.tree.filter(val)
    },
    filterNode(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    doSave() {
      this.loading = true
      this.$refs['mRightForm'].validate((valid) => {
        if (valid) {
          const data = {
            name: this.formData.name,
            tableName: this.formData.table,
            datasource: this.formData.datasource,
            pid: this.formData.folder,
            level: this.formData.level,
            forms: JSON.stringify(this.formData.forms),
            createIndex: this.formData.createIndex,
            tableIndexes: JSON.stringify(this.formData.tableIndexes),
            nodeType: 'form'
          }
          saveForm(data).then(res => {
            this.closeSave()
            this.$router.replace({ name: 'data-filling-form', query: { id: res.data }})
          }).finally(() => {
            this.loading = false
          })
        } else {
          this.loading = false
          return false
        }
      })
    }
  }
}
</script>

<template>
  <el-container
    v-loading="loading"
    class="DataFillingFormSave"
  >
    <el-header class="de-header">
      <div class="panel-info-area">
        <span class="text16 margin-left12">
          保存表单
        </span>
      </div>

      <div style="padding-right: 20px">
        <i
          class="el-icon-close"
          style="cursor: pointer"
          @click="closeSave"
        />
      </div>
    </el-header>
    <el-main class="de-main">
      <el-form
        ref="mRightForm"
        class="m-form"
        :model="formData"
        label-position="top"
        hide-required-asterisk
      >
        <el-form-item
          prop="name"
          class="form-item"
          :rules="[requiredRule]"
        >
          <template #label>
            表单名称
            <span
              style="color: red"
            >*</span>
          </template>
          <el-input
            v-model.trim="formData.name"
            required
            size="small"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item
          prop="folder"
          class="form-item"
          :rules="[requiredRule]"
        >
          <template #label>
            所属文件夹
            <span
              style="color: red"
            >*</span>
          </template>
          <el-popover
            v-model="folderTreeShow"
            placement="bottom-start"
            popper-class="user-popper dataset-filed"
            width="552"
            trigger="click"
          >
            <el-tree
              ref="tree"
              :data="folders"
              node-key="id"
              class="de-tree"
              :expand-on-click-node="false"
              highlight-current
              :filter-node-method="filterNode"
              default-expand-all
              @node-click="nodeClick"
            >
              <span
                slot-scope="{ data }"
                class="custom-tree-node-dataset"
              >
                <span>
                  <svg-icon icon-class="scene" />
                </span>
                <span
                  style="
                  margin-left: 6px;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                "
                  :title="data.name"
                >{{ data.name }}</span>
              </span>
            </el-tree>
            <el-select
              slot="reference"
              v-model="formData.folder"
              filterable
              popper-class="tree-select-dataset"
              style="width: 100%"
              :filter-method="filterMethod"
              :placeholder="$t('commons.please_select')"
              required
            >
              <el-option
                v-for="item in selectDatasets"
                :key="item.label"
                :label="item.label"
                :value="item.id"
              />
            </el-select>
          </el-popover>

        </el-form-item>

        <el-form-item
          prop="datasource"
          class="form-item"
          :rules="[requiredRule]"
        >
          <template #label>
            数据源
            <span
              style="color: red"
            >*</span>
          </template>
          <el-select
            v-model="formData.datasource"
            filterable
            size="small"
            style="width: 100%"
          >
            <el-option-group
              v-for="(x, $index) in datasourceList"
              :key="$index"
              :label="x.name"
            >
              <el-option
                v-for="d in x.options"
                :key="d.id"
                :value="d.id"
                :label="d.name"
              >{{ d.name }}
              </el-option>
            </el-option-group>
          </el-select>
        </el-form-item>

        <el-form-item
          prop="table"
          class="form-item"
          :rules="[requiredRule]"
        >
          <template #label>
            数据库表名
            <span
              style="color: red"
            >*</span>
          </template>
          <el-input
            v-model.trim="formData.table"
            required
            size="small"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-table
          :data="formData.forms"
          border
          stripe
          style="width: 100%"
        >
          <el-table-column
            label="表单字段"
          >
            <template slot-scope="scope">
              {{ scope.row.settings.name }}
            </template>
          </el-table-column>
          <el-table-column>
            <template
              slot="header"
              slot-scope="scope"
            >
              数据库表字段名称
            </template>
            <template slot-scope="scope">
              <el-form-item
                v-if="scope.row.type !== 'dateRange'"
                :prop="'forms['+scope.$index+'].settings.mapping.columnName'"
                class="form-item no-margin-bottom"
                :rules="[requiredRule, duplicateRule]"
              >
                <el-input
                  v-model.trim="scope.row.settings.mapping.columnName"
                  placeholder="请输入"
                  size="small"
                  maxlength="50"
                  show-word-limit
                  required
                />
              </el-form-item>
              <template v-else>
                <el-form-item
                  :prop="'forms['+scope.$index+'].settings.mapping.columnName1'"
                  class="form-item no-margin-bottom"
                  :rules="[requiredRule, duplicateRule]"
                >
                  <el-input
                    v-model.trim="scope.row.settings.mapping.columnName1"
                    placeholder="请输入开始时间"
                    size="small"
                    maxlength="50"
                    show-word-limit
                    required
                  />
                </el-form-item>
                <el-form-item
                  :prop="'forms['+scope.$index+'].settings.mapping.columnName2'"
                  class="form-item no-margin-bottom"
                  :rules="[requiredRule, duplicateRule]"
                >
                  <el-input
                    v-model.trim="scope.row.settings.mapping.columnName2"
                    placeholder="请输入结束时间"
                    size="small"
                    maxlength="50"
                    show-word-limit
                    required
                  />
                </el-form-item>
              </template>
            </template>
          </el-table-column>
          <el-table-column
            label="数据库字段类型"
          >
            <template slot-scope="scope">
              <el-form-item
                :prop="'forms['+scope.$index+'].settings.mapping.type'"
                class="form-item no-margin-bottom"
                :rules="[requiredRule]"
              >
                <el-select
                  v-model="scope.row.settings.mapping.type"
                  placeholder="请选择"
                  size="small"
                  required
                  style="width: 100%"
                >
                  <el-option
                    v-for="o in scope.row.settings.mapping.typeOptions"
                    :key="o.value"
                    :value="o.value"
                    :label="o.label"
                  />

                </el-select>
              </el-form-item>
            </template>
          </el-table-column>
        </el-table>

        <div style="display: flex">
          <el-form-item
            prop="createIndex"
            class="form-item no-margin-bottom"
          >
            <el-checkbox
              v-model="formData.createIndex"
              label="创建索引"
              size="small"
            />
          </el-form-item>

          <el-button
            v-if="formData.createIndex"
            type="text"
            style="margin-left: 20px"
            @click="addIndex"
          >+ 新增索引
          </el-button>
        </div>

        <el-table
          v-if="formData.createIndex"
          :data="formData.tableIndexes"
          border
          stripe
          style="width: 100%"
        >
          <el-table-column
            label="索引名称"
            width="300"
          >
            <template slot-scope="scope">
              <el-form-item
                :prop="'tableIndexes['+scope.$index+'].name'"
                class="form-item"
                :rules="[requiredRule, duplicateIndexRule]"
              >
                <el-input
                  v-model="scope.row.name"
                  placeholder="请输入"
                  size="small"
                  maxlength="50"
                  show-word-limit
                  required
                />
              </el-form-item>
            </template>
          </el-table-column>

          <el-table-column
            label="索引字段"
          >
            <template slot-scope="scope">
              <div
                v-for="(indexRow, $index) in scope.row.columns"
                :key="$index"
                style="display: flex; flex-direction: row; align-items: center;"
              >
                <el-form-item
                  :prop="'tableIndexes['+scope.$index+'].columns['+$index+'].column'"
                  class="form-item no-margin-bottom"
                  :rules="[requiredRule, invalidColumnRule, duplicateIndexColumnRule]"
                  style="flex: 1"
                >
                  <el-select
                    v-model="indexRow.column"
                    placeholder="请选择"
                    size="small"
                    required
                    style="width: 100%"
                  >
                    <el-option
                      v-for="(x, $index) in columnsList"
                      :key="$index"
                      :value="x"
                      :label="x"
                    >{{ x }}
                    </el-option>
                  </el-select>
                </el-form-item>

                <el-form-item
                  :prop="'tableIndexes['+scope.$index+'].columns['+$index+'].order'"
                  class="form-item no-margin-bottom"
                  :rules="[requiredRule]"
                >
                  <el-select
                    v-model="indexRow.order"
                    placeholder="请选择"
                    size="small"
                    required
                    style="width: 100%"
                  >
                    <el-option
                      value="asc"
                      label="顺序"
                    />
                    <el-option
                      value="none"
                      label="默认排序"
                    />
                    <el-option
                      value="desc"
                      label="倒序"
                    />
                  </el-select>
                </el-form-item>
                <div
                  v-if="scope.row.columns.length > 1"
                  class="btn-item"
                  @click="removeIndexColumn(scope.row.columns, $index)"
                >
                  <svg-icon icon-class="icon_delete-trash_outlined" />
                </div>
              </div>
              <el-button
                type="text"
                @click="addColumn(scope.row.columns)"
              >+ 新增字段
              </el-button>
            </template>
          </el-table-column>

          <el-table-column width="50">
            <template slot-scope="scope">
              <div
                class="btn-item"
                @click="removeIndex(scope.$index)"
              >
                <svg-icon icon-class="icon_delete-trash_outlined" />
              </div>
            </template>
          </el-table-column>
        </el-table>

      </el-form>

    </el-main>
    <el-footer class="de-footer">
      <el-button @click="closeSave">取消</el-button>
      <el-button
        type="primary"
        @click="doSave"
      >保存
      </el-button>
    </el-footer>
  </el-container>
</template>

<style  lang="scss" scoped>
.DataFillingFormSave {

  height: 100%;

  .de-header {
    height: 56px !important;
    padding: 0px !important;
    border-bottom: 1px solid #E6E6E6;
    background-color: var(--SiderBG, white);

    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between
  }

  .de-footer {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-end
  }

  .panel-info-area {
    padding-left: 20px;
  }

  .de-main {
    display: flex;
    align-items: center;
    flex-direction: column;

    .m-form {
      width: 80%;
    }
  }

  .no-margin-bottom {
    margin-bottom: 0;
  }

  .btn-item {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;

    width: 24px;
    height: 24px;

    margin-left: 8px;

    border-radius: 4px;

    cursor: pointer;
  }

  .btn-item:first-child {
    margin-left: unset;
  }

  .btn-item:hover {
    background: rgba(31, 35, 41, 0.1);
  }

}
.dataset-filed {
  height: 400px;
  overflow-y: auto;
}

.tree-select-dataset {
  display: none;
}
</style>
