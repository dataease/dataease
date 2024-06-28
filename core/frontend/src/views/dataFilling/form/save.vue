<script>
import { filter, forEach, find, split, get, groupBy, keys, includes, cloneDeep } from 'lodash-es'
import { listDatasource } from '@/api/system/datasource'
import { listForm, saveForm, updateForm } from '@/views/dataFilling/form/dataFilling'
import { hasDataPermission } from '@/utils/permission'

export default {
  name: 'DataFillingFormSave',
  props: {
    isEdit: {
      type: Boolean,
      default: false
    },
    disableCreateIndex: {
      type: Boolean,
      default: false
    },
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
        return callback(new Error(this.$t('commons.component.required')))
      }
      let count = 0
      forEach(this.computedFormList, f => {
        if (!f.deleted) {
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
        } else {
          // 后台会讲删除的字段名处理成uuid，正常不会有重复的
        }
      })
      if (count > 1) {
        callback(new Error(this.$t('data_fill.form.duplicate_error')))
      }
      callback()
    }
    const checkDuplicateIndexNameValidator = (rule, value, callback) => {
      if (!value) {
        return callback(new Error(this.$t('commons.component.required')))
      }
      let count = 0
      forEach(this.computedTableIndexList, f => {
        if (f.name === value) {
          count++
        }
      })
      if (count > 1) {
        callback(new Error(this.$t('data_fill.form.duplicate_error')))
      }
      callback()
    }
    const checkInvalidColumnValidator = (rule, value, callback) => {
      const f = split(rule.field, '.')[0]
      const _index = get(this.formData, f)
      if (_index.old) {
        // 旧的 index 跳过校验
        callback()
      }
      if (!value) {
        return callback(new Error(this.$t('commons.component.required')))
      }
      if (this.columnsList.length === 0) {
        return callback(new Error(this.$t('data_fill.form.value_not_exists')))
      }
      if (find(this.columnsList, c => c.value === value) === undefined) {
        callback(new Error(this.$t('data_fill.form.value_not_exists')))
      }
      callback()
    }
    const checkDuplicateIndexColumnValidator = (rule, value, callback, source) => {
      if (!value) {
        return callback(new Error(this.$t('commons.component.required')))
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
        callback(new Error(this.$t('data_fill.form.duplicate_error')))
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
      const dsMap = groupBy(this.allDatasourceList, d => d.type)
      const _types = [{
        name: this.$t('data_fill.form.default'),
        type: 'default',
        options: [{
          id: 'default-built-in',
          name: this.$t('data_fill.form.default_built_in')
        }]
      }]
      if (dsMap) {
        forEach(keys(dsMap), type => {
          if (type === 'mysql' || type === 'mariadb') {
            _types.push({
              name: dsMap[type][0]?.typeDesc,
              type: type,
              options: filter(dsMap[type], d => d.enableDataFill && d.enableDataFillCreateTable)
            })
          }
        })
      }
      return _types
    },
    selectDatasets() {
      const result = []
      this.flattenFolder(this.folders, result)
      return result
    },
    computedFormList() {
      if (this.isEdit) {
        const _list = []
        const columnIds = []
        for (let i = 0; i < this.formData.forms.length; i++) {
          const row = cloneDeep(this.formData.forms[i])
          columnIds.push(row.id)
          _list.push(row)
        }
        for (let i = 0; i < this.formData.oldForms.length; i++) {
          const row = cloneDeep(this.formData.oldForms[i])
          if (includes(columnIds, row.id)) {
            continue
          }
          row.deleted = true
          _list.push(row)
        }
        return _list
      } else {
        return this.formData.forms
      }
    },
    computedTableIndexList() {
      if (this.isEdit) {
        const _list = []
        const columnIds = []
        for (let i = 0; i < this.formData.tableIndexes.length; i++) {
          const row = this.formData.tableIndexes[i]
          columnIds.push(row.id)
          _list.push(row)
        }
        for (let i = 0; i < this.formData.oldTableIndexes.length; i++) {
          const row = this.formData.oldTableIndexes[i]
          if (includes(columnIds, row.id)) {
            continue
          }
          columnIds.push(row.id)
          _list.push(row)
        }
        return _list
      } else {
        return this.formData.tableIndexes
      }
    },
    allColumnsList() {
      const _list = []
      for (let i = 0; i < this.computedFormList.length; i++) {
        const row = this.computedFormList[i]
        if (row.type === 'dateRange') {
          if (row.settings.mapping.columnName1 !== undefined && row.settings.mapping.columnName1 !== '') {
            _list.push({
              name: !row.deleted ? row.settings.mapping.columnName1 : row.id + '_1',
              value: row.id + '_1',
              deleted: !!row.deleted
            })
          }
          if (row.settings.mapping.columnName2 !== undefined && row.settings.mapping.columnName2 !== '') {
            _list.push({
              name: !row.deleted ? row.settings.mapping.columnName2 : row.id + '_2',
              value: row.id + '_2',
              deleted: !!row.deleted
            })
          }
        } else {
          if (row.settings.mapping.columnName !== undefined && row.settings.mapping.columnName !== '' && row.settings.mapping.type !== 'text') {
            _list.push({
              name: !row.deleted ? row.settings.mapping.columnName : row.id,
              value: row.id,
              deleted: !!row.deleted
            })
          }
        }
      }

      return _list
    },
    columnsList() {
      return filter(this.allColumnsList, c => !c.deleted)
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
    const p1 = listDatasource()
    const p2 = listForm({ nodeType: 'folder' })

    Promise.all([p1, p2]).then((val) => {
      this.allDatasourceList = val[0].data

      this.folders = this.filterListDeep(val[1].data) || []
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
    filterListDeep(list) {
      return filter(list, item => {
        const hasChildren = item.children && item.children.length > 0
        if (item.children) {
          this.filterListDeep(item.children)
        }
        return hasDataPermission('manage', item.privileges) || hasChildren
      })
    },
    getTypeOptions(formOption) {
      const _options = []
      if (formOption.type !== 'date' &&
        formOption.type !== 'dateRange' &&
        formOption.settings.inputType !== 'number' &&
        formOption.type !== 'textarea' &&
        formOption.type !== 'checkbox' &&
        !(formOption.type === 'select' && formOption.settings.multiple)
      ) {
        _options.push({ value: 'nvarchar', label: this.$t('data_fill.database.nvarchar') })
      }
      if (formOption.type === 'checkbox' ||
        formOption.type === 'select' && formOption.settings.multiple ||
        formOption.type === 'textarea') {
        _options.push({ value: 'text', label: this.$t('data_fill.database.text') })
      }

      if (formOption.type === 'input' && formOption.settings.inputType === 'number') {
        _options.push({ value: 'number', label: this.$t('data_fill.database.number') })
        _options.push({ value: 'decimal', label: this.$t('data_fill.database.decimal') })
      }
      if (formOption.type === 'date' || formOption.type === 'dateRange') {
        _options.push({ value: 'datetime', label: this.$t('data_fill.database.datetime') })
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
        if (hasDataPermission('manage', data.privileges)) {
          this.formData.folder = data.id
          this.formData.level = data.level + 1
          this.folderTreeShow = false
        } else {
          this.formData.folder = undefined
          this.formData.level = undefined
        }
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
    doEdit() {
      this.loading = true
      this.$refs['mRightForm'].validate((valid) => {
        if (valid) {
          const data = {
            id: this.formData.id,
            name: this.formData.name,
            tableName: this.formData.table,
            datasource: this.formData.datasource,
            pid: this.formData.folder,
            level: this.formData.level,
            forms: JSON.stringify(this.formData.forms),
            createIndex: this.formData.createIndex,
            tableIndexes: JSON.stringify(this.formData.tableIndexes),
            commitNewUpdate: this.formData.commitNewUpdate,
            nodeType: 'form'
          }
          updateForm(data).then(res => {
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
            commitNewUpdate: this.formData.commitNewUpdate,
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
          {{ $t('data_fill.form.save_form') }}
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
        @submit.native.prevent
      >
        <el-form-item
          prop="name"
          class="form-item"
          :rules="[requiredRule]"
        >
          <template #label>
            {{ $t('data_fill.form.form_name') }}
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
            {{ $t('data_fill.form.folder') }}
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
          v-if="!isEdit"
          prop="datasource"
          class="form-item"
          :rules="[requiredRule]"
        >
          <template #label>
            {{ $t('data_fill.form.datasource') }}
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
          v-if="!isEdit"
          prop="table"
          class="form-item"
          :rules="[requiredRule]"
        >
          <template #label>
            {{ $t('data_fill.form.table_name') }}
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
            :label="$t('data_fill.form.form_column')"
          >
            <template slot-scope="scope">
              {{ scope.row.settings.name }}
            </template>
          </el-table-column>
          <el-table-column>
            <template
              slot="header"
            >
              {{ $t('data_fill.form.column_name') }}
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
                  :disabled="isEdit && scope.row.old"
                  :placeholder="$t('fu.search_bar.please_input')"
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
                    :disabled="isEdit && scope.row.old"
                    :placeholder="$t('data_fill.form.please_insert_start')"
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
                    :disabled="isEdit && scope.row.old"
                    :placeholder="$t('data_fill.form.please_insert_end')"
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
            :label="$t('data_fill.form.column_type')"
          >
            <template slot-scope="scope">
              <el-form-item
                :prop="'forms['+scope.$index+'].settings.mapping.type'"
                class="form-item no-margin-bottom"
                :rules="[requiredRule]"
              >
                <el-select
                  v-model="scope.row.settings.mapping.type"
                  :disabled="isEdit && scope.row.old"
                  :placeholder="$t('data_fill.form.please_select')"
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
              :disabled="disableCreateIndex"
              :label="$t('data_fill.form.create_index')"
              size="small"
            />
          </el-form-item>

          <el-button
            v-if="formData.createIndex"
            type="text"
            style="margin-left: 20px"
            @click="addIndex"
          >+ {{ $t('data_fill.form.add_index') }}
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
            :label="$t('data_fill.form.index_name')"
            width="300"
          >
            <template slot-scope="scope">
              <el-form-item
                :prop="'tableIndexes['+scope.$index+'].name'"
                class="form-item"
                :class="scope.row.columns.length === 1 && (isEdit && scope.row.old) ? 'no-margin-bottom' : ''"
                :rules="[requiredRule, duplicateIndexRule]"
              >
                <el-input
                  v-model="scope.row.name"
                  :disabled="isEdit && scope.row.old"
                  :placeholder="$t('fu.search_bar.please_input')"
                  size="small"
                  maxlength="50"
                  show-word-limit
                  required
                />
              </el-form-item>
            </template>
          </el-table-column>

          <el-table-column>
            <template
              slot="header"
              slot-scope="scope"
            >
              {{ $t('data_fill.form.index_column') }}
              <el-tooltip
                class="item"
                effect="dark"
                placement="bottom"
              >
                <div
                  slot="content"
                  v-html="$t('data_fill.form.create_index_hint')"
                />
                <i
                  class="el-icon-info"
                  style="cursor: pointer;"
                />
              </el-tooltip>
            </template>
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
                    :disabled="isEdit && scope.row.old"
                    :placeholder="$t('data_fill.form.please_select')"
                    size="small"
                    required
                    style="width: 100%"
                  >
                    <el-option
                      v-for="(x, $index) in (isEdit && scope.row.old ? allColumnsList : columnsList)"
                      :key="$index"
                      :value="x.value"
                      :label="x.name"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item
                  :prop="'tableIndexes['+scope.$index+'].columns['+$index+'].order'"
                  class="form-item no-margin-bottom"
                  :rules="[requiredRule]"
                >
                  <el-select
                    v-model="indexRow.order"
                    :disabled="isEdit && scope.row.old"
                    :placeholder="$t('data_fill.form.please_select')"
                    size="small"
                    required
                    style="width: 100%"
                  >
                    <el-option
                      value="asc"
                      :label="$t('data_fill.form.order_asc')"
                    />
                    <el-option
                      value="none"
                      :label="$t('data_fill.form.order_none')"
                    />
                    <el-option
                      value="desc"
                      :label="$t('data_fill.form.order_desc')"
                    />
                  </el-select>
                </el-form-item>
                <div
                  v-if="scope.row.columns.length > 1 && !(isEdit && scope.row.old)"
                  class="btn-item"
                  @click="removeIndexColumn(scope.row.columns, $index)"
                >
                  <svg-icon icon-class="icon_delete-trash_outlined" />
                </div>
              </div>
              <el-button
                v-if="scope.row.columns.length < 5 && !(isEdit && scope.row.old)"
                type="text"
                @click="addColumn(scope.row.columns)"
              >+ {{ $t('data_fill.form.add_column') }}
              </el-button>
            </template>
          </el-table-column>

          <el-table-column width="50">
            <template slot-scope="scope">
              <div
                v-if="!(isEdit && scope.row.old)"
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
      <el-button @click="closeSave">{{ $t("commons.cancel") }}</el-button>
      <el-button
        v-if="!isEdit"
        type="primary"
        @click="doSave"
      >{{ $t("commons.confirm") }}
      </el-button>
      <el-button
        v-else
        type="primary"
        @click="doEdit"
      >{{ $t("commons.confirm") }}
      </el-button>
    </el-footer>
  </el-container>
</template>

<style  lang="scss" scoped>
.DataFillingFormSave {

  height: 100%;

  ::v-deep .el-form-item__error {
    position: relative;
  }

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
