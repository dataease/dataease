<script>
import DeContainer from '@/components/dataease/DeContainer.vue'
import DataFillingFormSave from './save.vue'
import clickoutside from 'element-ui/src/utils/clickoutside.js'
import { filter, cloneDeep, find, concat, forEach, groupBy, keys, map, join } from 'lodash-es'
import { v4 as uuidv4 } from 'uuid'
import { EMAIL_REGEX, PHONE_REGEX } from '@/utils/validate'
import { getTableColumnData, getWithPrivileges } from '@/views/dataFilling/form/dataFilling'
import { getColumnList, listDatasource } from '@/api/dataset/dataset'
import { getTableList } from '@/api/system/datasource'
import GridTable from '@/components/gridTable/index.vue'

export default {
  name: 'DataFillingFormCreate',
  components: { GridTable, DeContainer, DataFillingFormSave },
  directives: {
    clickoutside
  },
  data: function() {
    const checkDuplicateOptionValidator = (rule, value, callback) => {
      if (!value) {
        return callback(new Error(this.$t('commons.component.required')))
      }
      const _list = filter(this.selectedComponentItem.settings.options, f => f.value === value)
      if (_list.length > 1) {
        callback(new Error(this.$t('data_fill.form.duplicate_error')))
      }
      callback()
    }
    return {
      baseLoading: false,
      selectLoading: false,
      loading: false,
      allDatasourceList: [],
      tableList: [],
      columnList: [],
      optionFormData: {},
      showDrawer: false,
      isEdit: false,
      disableCreateIndex: false,
      requiredRule: { required: true, message: this.$t('commons.required'), trigger: ['blur', 'change'] },
      duplicateOptionRule: { validator: checkDuplicateOptionValidator, trigger: ['blur', 'change'] },
      dateTypes: [
        { name: this.$t('chart.y'), value: 'year' },
        { name: this.$t('chart.y_M'), value: 'month' },
        { name: this.$t('chart.y_M_d'), value: 'date' },
        { name: this.$t('chart.y_M_d_H_m_s'), value: 'datetime' }
      ],
      dateRangeTypes: [
        { name: this.$t('chart.y_M'), value: 'monthrange' },
        { name: this.$t('chart.y_M_d'), value: 'daterange' },
        { name: this.$t('chart.y_M_d_H_m_s'), value: 'datetimerange' }
      ],
      inputTypes: [
        { type: 'text', name: this.$t('data_fill.form.text'), rules: [] },
        { type: 'number', name: this.$t('data_fill.form.number'), rules: [] },
        {
          type: 'tel',
          name: this.$t('data_fill.form.tel'),
          rules: [{
            pattern: PHONE_REGEX,
            message: this.$t('user.mobile_number_format_is_incorrect'),
            trigger: ['blur', 'change']
          }]
        },
        {
          type: 'email',
          name: this.$t('data_fill.form.email'),
          rules: [{
            pattern: EMAIL_REGEX,
            message: this.$t('user.email_format_is_incorrect'),
            trigger: ['blur', 'change']
          }]
        }
      ],
      showEditBindColumn: false,
      showCommitUpdateRule: false,
      asyncOptions: {},
      componentList: [
        {
          type: 'input',
          typeName: this.$t('commons.component.input'),
          icon: 'icon_single-line_outlined',
          order: 0,
          value: undefined,
          id: undefined,
          settings: {
            name: this.$t('commons.component.input'),
            placeholder: '',
            required: false,
            unique: false,
            inputType: 'text',
            mapping: {

              columnName: undefined,
              type: undefined
            }
          }
        },
        {
          type: 'textarea',
          typeName: this.$t('commons.component.textarea'),
          icon: 'icon_multi-line_outlined',
          order: 1,
          value: undefined,
          id: undefined,
          settings: {
            name: this.$t('commons.component.textarea'), placeholder: '', required: false,
            mapping: {

              columnName: undefined,
              type: undefined
            }
          }
        },
        {
          type: 'select',
          typeName: this.$t('commons.component.select'),
          icon: 'icon_down_outlined',
          order: 2,
          value: undefined,
          id: undefined,
          settings: {
            name: this.$t('commons.component.select'),
            options: [{ name: this.$t('data_fill.form.option') + ' 1', value: this.$t('data_fill.form.option') + ' 1' },
              { name: this.$t('data_fill.form.option') + ' 2', value: this.$t('data_fill.form.option') + ' 2' }],
            optionSourceType: 1,
            optionDatasource: undefined,
            optionTable: undefined,
            optionColumn: undefined,
            optionOrder: 'asc',
            placeholder: '',
            multiple: false, required: false,
            mapping: {
              columnName: undefined,
              type: undefined
            }
          }
        },
        {
          type: 'radio',
          typeName: this.$t('commons.component.radio'),
          icon: 'icon_radio_outlined',
          order: 3,
          value: undefined,
          id: undefined,
          settings: {
            name: this.$t('commons.component.radio'),
            options: [{ name: this.$t('data_fill.form.option') + ' 1', value: this.$t('data_fill.form.option') + ' 1' },
              { name: this.$t('data_fill.form.option') + ' 2', value: this.$t('data_fill.form.option') + ' 2' }],
            optionSourceType: 1,
            optionDatasource: undefined,
            optionTable: undefined,
            optionColumn: undefined,
            optionOrder: 'asc',
            required: false,
            mapping: {

              columnName: undefined,
              type: undefined
            }
          }
        },
        {
          type: 'checkbox',
          typeName: this.$t('commons.component.checkbox'),
          icon: 'icon_todo_outlined',
          order: 4,
          value: [],
          id: undefined,
          settings: {
            name: this.$t('commons.component.checkbox'),
            options: [{ name: this.$t('data_fill.form.option') + ' 1', value: this.$t('data_fill.form.option') + ' 1' },
              { name: this.$t('data_fill.form.option') + ' 2', value: this.$t('data_fill.form.option') + ' 2' }],
            optionSourceType: 1,
            optionDatasource: undefined,
            optionTable: undefined,
            optionColumn: undefined,
            optionOrder: 'asc',
            required: false,
            mapping: {

              columnName: undefined,
              type: undefined
            }
          }
        },
        {
          type: 'date',
          typeName: this.$t('commons.component.date'),
          icon: 'icon_calendar_outlined',
          order: 5,
          value: undefined,
          id: undefined,
          settings: {
            name: this.$t('commons.component.date'),
            enableTime: false, // 弃用
            dateType: 'date',
            placeholder: '',
            required: false,
            mapping: {

              columnName: undefined,
              type: undefined
            }
          }
        },
        {
          type: 'dateRange',
          typeName: this.$t('commons.component.dateRange'),
          icon: 'icon_calendar_outlined',
          order: 6,
          value: [],
          id: undefined,
          settings: {
            name: this.$t('commons.component.dateRange'),
            enableTime: false, // 弃用
            dateType: 'daterange',
            rangeSeparator: '-',
            startPlaceholder: '',
            endPlaceholder: '',
            required: false,
            mapping: {
              columnName1: undefined,
              columnName2: undefined,
              type: undefined
            }
          }
        }
      ],
      group: {
        componentListGroup1: {
          name: 'mFormGroup',
          pull: 'clone', // B组拖拽时克隆到A组
          put: false
        },
        componentListGroup2: {
          name: 'mFormGroup',
          pull: 'clone', // B组拖拽时克隆到A组
          put: false
        },
        formGroup: {
          name: 'mFormGroup',
          put: true
        }
      },
      formSettings: {
        id: undefined,
        name: this.$t('data_fill.form.untitled'),
        table: undefined,
        forms: [],
        createIndex: false,
        tableIndexes: [],
        folder: undefined,
        level: undefined,
        commitNewUpdate: false
      },
      selectedItemId: undefined,
      tempForms: []
    }
  },
  computed: {
    ruleUpdateFormList() {
      const filterList = filter(this.formSettings?.forms, f => !!f.settings?.updateRuleCheck)
      return map(filterList, f => { return f.settings?.name })
    },
    ruleUpdateFormListNames() {
      return join(this.ruleUpdateFormList, ', ')
    },
    datasourceList() {
      const dsMap = groupBy(this.allDatasourceList, d => d.type)
      const _types = []
      if (dsMap) {
        forEach(keys(dsMap), type => {
          if (type === 'mysql' || type === 'mariadb') {
            _types.push({
              name: dsMap[type][0]?.typeDesc,
              type: type,
              options: dsMap[type]
            })
          }
        })
      }
      return _types
    },
    componentList1() {
      return filter(this.componentList, c => c.order % 2 === 0)
    },
    componentList2() {
      return filter(this.componentList, c => c.order % 2 === 1)
    },
    selectedComponentItem() {
      if (this.selectedItemId) {
        return find(this.formSettings.forms, f => f.id === this.selectedItemId)
      }
      return undefined
    },
    selectedComponentItemInputTypes() {
      if (this.selectedComponentItem && this.selectedComponentItem.type === 'input') {
        if (this.isEdit && this.selectedComponentItem.old) {
          if (this.selectedComponentItem.settings.inputType === 'number') {
            return filter(this.inputTypes, t => t.type === 'number')
          } else {
            return filter(this.inputTypes, t => t.type !== 'number')
          }
        }
      }
      return this.inputTypes
    }

  },
  beforeDestroy() {
  },
  created() {
    this.isEdit = false
    this.disableCreateIndex = false
    if (this.$route.query.folder !== undefined) {
      this.formSettings.folder = this.$route.query.folder
    }
    if (this.$route.query.level !== undefined) {
      this.formSettings.level = this.$route.query.level
    }
    if (this.$route.query.copy !== undefined) {
      const id = this.$route.query.copy
      getWithPrivileges(id).then(res => {
        const tempData = res.data
        this.formSettings.commitNewUpdate = !!tempData.commitNewUpdate
        this.formSettings.folder = tempData.pid
        this.formSettings.level = tempData.level
        this.baseLoading = true
        this.initData(tempData, () => {
          this.baseLoading = false
        })
      })
    } else if (this.$route.query.id !== undefined) {
      const id = this.$route.query.id
      getWithPrivileges(id).then(res => {
        this.isEdit = true
        const tempData = cloneDeep(res.data)
        this.formSettings = tempData
        this.formSettings.commitNewUpdate = !!tempData.commitNewUpdate
        this.formSettings.table = tempData.tableName
        this.formSettings.folder = tempData.pid
        this.baseLoading = true
        this.initData(res.data, () => {
          if (res.data.createIndex) {
            forEach(this.formSettings.tableIndexes, f => {
              f.old = true
            })
            this.formSettings.oldTableIndexes = JSON.parse(res.data.tableIndexes)
          } else {
            this.formSettings.oldTableIndexes = []
          }

          this.disableCreateIndex = res.data.createIndex
          this.baseLoading = false
        })
      })
    }
    // 获取用户的数据源
    listDatasource().then(data => {
      this.allDatasourceList = data.data
    })
  },
  methods: {
    initData(data, callback) {
      const tempForms = filter(JSON.parse(data.forms), f => !f.removed)
      forEach(tempForms, f => {
        f.old = true
        if (f.type === 'checkbox' || f.type === 'select' && f.settings.multiple) {
          f.value = []
        }
        if (f.type === 'date' && f.settings.dateType === undefined) { // 兼容旧的
          f.settings.dateType = f.settings.enableTime ? 'datetime' : 'date'
        }
        if (f.type === 'dateRange' && f.settings.dateType === undefined) { // 兼容旧的
          f.settings.dateType = f.settings.enableTime ? 'datetimerange' : 'daterange'
        }
        f.settings.updateRuleCheck = !!f.settings.updateRuleCheck
      })
      this.initFormOptionsData(tempForms, () => {
        this.formSettings.forms = tempForms
        this.formSettings.oldForms = JSON.parse(data.forms)
        this.formSettings.tableIndexes = JSON.parse(data.tableIndexes)

        if (callback) {
          callback()
        }
      })
    },
    remoteMethod(query, item) {
      if (item.settings.optionSourceType === 1) {
        return
      }
      this.selectLoading = true
      let p
      if (query && query.trim() !== '') {
        p = getTableColumnData(item.settings.optionDatasource, item.settings.optionTable, item.settings.optionColumn, item.settings.optionOrder, query)
      } else {
        p = getTableColumnData(item.settings.optionDatasource, item.settings.optionTable, item.settings.optionColumn, item.settings.optionOrder)
      }
      const id = item.settings.optionDatasource + '_' + item.settings.optionTable + '_' + item.settings.optionColumn + '_' + item.settings.optionOrder
      p.then(res => {
        this.asyncOptions[id] = res.data
      }).finally(() => {
        this.selectLoading = false
      })
    },
    initFormOptionsData(forms, callback) {
      const queries = []
      const queryIds = []
      forEach(forms, f => {
        if (f.type === 'checkbox' || f.type === 'select' || f.type === 'radio') {
          if (f.settings && f.settings.optionSourceType === 2 && f.settings.optionDatasource && f.settings.optionTable && f.settings.optionColumn && f.settings.optionOrder) {
            const id = f.settings.optionDatasource + '_' + f.settings.optionTable + '_' + f.settings.optionColumn + '_' + f.settings.optionOrder

            const p = getTableColumnData(f.settings.optionDatasource, f.settings.optionTable, f.settings.optionColumn, f.settings.optionOrder)
            queries.push(p)
            queryIds.push(id)
          }
        }
      })

      if (queries.length > 0) {
        Promise.all(queries).then((val) => {
          for (let i = 0; i < queryIds.length; i++) {
            const id = queryIds[i]
            this.asyncOptions[id] = val[i].data
          }
        }).finally(() => {
          if (callback) {
            callback()
          }
        })
      } else {
        if (callback) {
          callback()
        }
      }
    },
    closeCreate: function() {
      // back to forms list
      if (this.$route.query.copy) {
        this.$router.replace({ name: 'data-filling-form', query: { id: this.$route.query.copy }})
      } else if (this.$route.query.id) {
        this.$router.replace({ name: 'data-filling-form', query: { id: this.$route.query.id }})
      } else {
        this.$router.replace('/data-filling/forms')
      }
    },
    onMoveInComponentList(e, originalEvent) {
      if (e.relatedContext?.component?.$el?.id === 'form-drag-place') {
        return true
      }
      return false
    },
    addComponent(e) {
      this.formSettings.forms = cloneDeep(this.formSettings.forms)

      this.formSettings.forms.forEach(f => {
        if (f.order !== undefined) {
          delete f.order
        }
        if (f.id === undefined) {
          f.id = uuidv4()
          this.selectedItemId = f.id
          this.$nextTick(() => {
            this.$refs['mRightForm'].validate()
          })
        }
      })
    },
    addComponentItem(item) {
      const _item = cloneDeep(item)
      delete _item.order
      _item.id = uuidv4()
      this.formSettings.forms.push(_item)

      this.selectedItemId = _item.id
      this.$nextTick(() => {
        this.$refs['mRightForm'].validate()
      })
    },
    removeItem(item, index) {
      this.formSettings.forms.splice(index, 1)
    },
    removeOption(list, index) {
      list.splice(index, 1)
    },
    onOptionValueChange(item, value) {
      item.name = value
    },
    copyItem(item, index) {
      const copyItem = cloneDeep(item)
      copyItem.id = uuidv4()
      delete copyItem.old
      delete copyItem.settings.mapping.columnName
      delete copyItem.settings.mapping.columnName1
      delete copyItem.settings.mapping.columnName2
      this.formSettings.forms.splice(index + 1, 0, copyItem)

      this.selectedItemId = copyItem.id
      this.$nextTick(() => {
        this.$refs['mRightForm'].validate()
      })
    },
    lostFocus() {
      this.selectedItemId = undefined
      this.$nextTick(() => {
        this.$refs['mRightFormBase'].validate()
      })
    },
    selectItem(id, showError) {
      this.selectedItemId = id
      this.$nextTick(() => {
        this.$refs['mRightForm'].validate((valid, invalidFields) => {
          if (showError && !valid) {
            this.$message({
              message: this.$t('data_fill.form.component_setting_error'),
              type: 'error',
              showClose: true
            })
          }
        })
      })
    },
    changeSelectMultiple(item, multiple) {
      if (multiple) {
        item.value = []
      } else {
        item.value = undefined
      }
      item.settings.mapping.type = undefined
    },
    getRules(item) {
      let rules = []
      if (item.settings.required) {
        rules.push(this.requiredRule)
      }
      if (item.type === 'input') {
        const inputRules = find(this.inputTypes, t => t.type === item.settings.inputType.rules)
        if (inputRules) {
          rules = concat(rules, inputRules)
        }
      }

      return rules
    },
    onOptionSourceTypeChange(type, itemSettings) {
      if (type === 2) {
        this.getAsyncOption({
          optionSourceType: type,
          optionDatasource: itemSettings.optionDatasource,
          optionTable: itemSettings.optionTable,
          optionColumn: itemSettings.optionColumn,
          optionOrder: itemSettings.optionOrder
        })
      }
    },
    getAsyncOption(itemSettings, callback) {
      if (itemSettings.optionSourceType === 2 && itemSettings.optionDatasource && itemSettings.optionTable && itemSettings.optionColumn && itemSettings.optionOrder) {
        const id = itemSettings.optionDatasource + '_' + itemSettings.optionTable + '_' + itemSettings.optionColumn + '_' + itemSettings.optionOrder
        if (this.asyncOptions[id] === undefined || this.asyncOptions[id].length === 0) {
          getTableColumnData(itemSettings.optionDatasource, itemSettings.optionTable, itemSettings.optionColumn, itemSettings.optionOrder).then(data => {
            this.asyncOptions[id] = data.data
          }).finally(() => {
            if (callback) {
              callback()
            }
          })
        } else {
          if (callback) {
            callback()
          }
        }
      }
    },
    openEditBindColumn(settings) {
      this.tableList = []
      this.columnList = []
      this.optionFormData = cloneDeep({
        optionSourceType: 2,
        optionDatasource: settings.optionDatasource,
        optionTable: settings.optionTable,
        optionColumn: settings.optionColumn,
        optionOrder: settings.optionOrder ?? 'asc'
      })
      const p1 = settings.optionDatasource ? getTableList(settings.optionDatasource) : undefined
      const p2 = settings.optionDatasource && settings.optionTable ? getColumnList(settings.optionDatasource, settings.optionTable) : undefined
      const promiseList = []
      if (p1) {
        promiseList.push(p1)
        if (p2) {
          promiseList.push(p2)
        }
      }
      if (promiseList.length > 1) {
        this.loading = true
        Promise.all(promiseList).then((val) => {
          this.tableList = val[0].data
          if (find(this.tableList, t => t.name === this.optionFormData.optionTable)) {
            if (promiseList.length > 1) {
              this.columnList = val[1].data
              if (!find(this.columnList, t => t.fieldName === this.optionFormData.optionColumn)) {
                this.optionFormData.optionColumn = undefined
              }
            }
          } else {
            this.optionFormData.optionTable = undefined
            this.optionFormData.optionColumn = undefined
          }
        }).finally(() => {
          this.loading = false
        })
      }
      this.showEditBindColumn = true
    },
    onDataSourceChange(datasource) {
      this.tableList = []
      this.columnList = []
      if (datasource) {
        this.loading = true
        getTableList(datasource).then(res => {
          this.tableList = res.data

          if (this.optionFormData.optionTable) {
            if (find(this.tableList, t => t.name === this.optionFormData.optionTable)) {
              this.onTableChange(datasource, this.optionFormData.optionTable)
            } else {
              this.optionFormData.optionTable = undefined
              this.optionFormData.optionColumn = undefined
            }
          }
        }).finally(() => {
          this.loading = false
        })
      }
    },
    onTableChange(datasource, table) {
      this.columnList = []
      if (datasource && table) {
        this.loading = true
        getColumnList(datasource, table).then(res => {
          this.columnList = res.data

          if (this.optionFormData.optionColumn) {
            if (!find(this.columnList, t => t.fieldName === this.optionFormData.optionColumn)) {
              this.optionFormData.optionColumn = undefined
            }
          }
        }).finally(() => {
          this.loading = false
        })
      }
    },
    closeEditBindColumn() {
      this.showEditBindColumn = false
    },
    doEditBindColumn() {
      this.$refs['optionForm'].validate((valid, invalidFields) => {
        if (valid) {
          this.loading = true
          this.getAsyncOption(this.optionFormData, () => {
            this.selectedComponentItem.settings.optionSourceType = this.optionFormData.optionSourceType
            this.selectedComponentItem.settings.optionDatasource = this.optionFormData.optionDatasource
            this.selectedComponentItem.settings.optionTable = this.optionFormData.optionTable
            this.selectedComponentItem.settings.optionColumn = this.optionFormData.optionColumn
            this.selectedComponentItem.settings.optionOrder = this.optionFormData.optionOrder
            this.loading = false

            this.closeEditBindColumn()
          })
        }
      })
    },
    addOption(list) {
      list.push({ name: '', value: '' })
    },
    toSave() {
      // 校验
      if (this.formSettings.name === undefined || this.formSettings.name.trim() === '') {
        this.$message({
          message: this.$t('data_fill.form.form_name_cannot_none'),
          type: 'error',
          showClose: true
        })
        this.lostFocus()
        return
      }
      if (this.formSettings.commitNewUpdate && this.ruleUpdateFormList.length === 0) {
        this.$message({
          message: this.$t('data_fill.form.form_update_rule_none'),
          type: 'error',
          showClose: true
        })
        this.lostFocus()
        return
      }
      if (this.formSettings.forms.length === 0) {
        this.$message({
          message: this.$t('data_fill.form.form_components_cannot_null'),
          type: 'warning',
          showClose: true
        })
        return
      }
      for (let i = 0; i < this.formSettings.forms.length; i++) {
        const f = this.formSettings.forms[i]
        if (f.settings.name === undefined || f.settings.name.trim() === '') {
          this.selectItem(f.id, true)
          return
        }
        if (f.type === 'dateRange') {
          if (f.settings.rangeSeparator === undefined || f.settings.rangeSeparator.trim() === '') {
            this.selectItem(f.id, true)
            return
          }
        }
        if (f.type === 'select' || f.type === 'radio' || f.type === 'checkbox') {
          if (f.settings.optionSourceType === 1) {
            if (f.settings.options.length === 0) {
              this.selectItem(f.id)
              this.$message({
                message: this.$t('data_fill.form.option_list_cannot_empty'),
                type: 'error',
                showClose: true
              })
              return
            } else {
              for (let j = 0; j < f.settings.options.length; j++) {
                const o = f.settings.options[j]
                const value = o.value
                if (value === undefined || value === '') {
                  this.selectItem(f.id, true)
                  return
                }
                const _list = filter(f.settings.options, f => f.value === value)
                if (_list.length > 1) {
                  this.selectItem(f.id, true)
                  return
                }
              }
            }
          } else {
            if (f.settings.optionDatasource === undefined ||
                  f.settings.optionTable === undefined ||
                  f.settings.optionColumn === undefined) {
              this.selectItem(f.id)
              this.$message({
                message: this.$t('data_fill.form.option_list_datasource_cannot_empty'),
                type: 'error',
                showClose: true
              })
              return
            }
          }
        }
      }

      this.showDrawer = true
    },
    openEditCommitRule() {
      this.tempForms = map(this.formSettings.forms, f => {
        return {
          id: f.id,
          updateRuleCheck: !!f.settings?.updateRuleCheck,
          typeName: f.typeName,
          name: f.settings?.name
        }
      })
      this.showCommitUpdateRule = true
    },
    closeEditCommitRule() {
      this.showCommitUpdateRule = false
    },
    confirmEditCommitRule() {
      forEach(this.formSettings.forms, f => {
        const temp = find(this.tempForms, tf => tf.id === f.id)
        if (temp) {
          this.$set(f.settings, 'updateRuleCheck', temp.updateRuleCheck)
        }
      })
      this.closeEditCommitRule()
    }

  }
}
</script>

<template>
  <div
    v-loading="baseLoading"
    class="data-filling-form"
  >
    <el-header class="de-header">
      <div class="panel-info-area">
        <!--back to panelList-->
        <svg-icon
          icon-class="icon_left_outlined"
          class="toolbar-icon-active icon20"
          @click="closeCreate"
        />
        <span
          v-if="$route.query.copy"
          class="text16 margin-left12"
        >
          {{ $t('data_fill.form.copy_new_form') }}
        </span>
        <span
          v-else-if="$route.query.id"
          class="text16 margin-left12"
        >
          {{ $t('data_fill.form.edit_form') }}
        </span>
        <span
          v-else
          class="text16 margin-left12"
        >
          {{ $t('data_fill.form.create_new_form') }}
        </span>
      </div>

      <el-button
        style="margin-right: 20px"
        @click="toSave"
      >{{ $t('commons.save') }}
      </el-button>
    </el-header>
    <de-container class="form-main-container">
      <div class="tools-window-left">
        <el-header class="sub-title-header">{{ $t('data_fill.form.component') }}</el-header>
        <div style="width: 100%; display: flex;">
          <div style="flex: 1; padding:8px 4px 8px 8px;">
            <draggable
              v-model="componentList1"
              :options="{group: group.componentListGroup1}"
              animation="300"
              ghost-class="ghostClass"
              chosen-class="chosenClass"
              :move="onMoveInComponentList"
            >
              <transition-group>
                <div
                  v-for="(item) in componentList1"
                  :key="item.type"
                  class="m-item base-component-item"
                  @click="addComponentItem(item)"
                >
                  <svg-icon
                    :icon-class="item.icon"
                    style="font-size: 16px; margin-right: 8px"
                  />
                  {{ item.typeName }}
                </div>
              </transition-group>
            </draggable>
          </div>
          <div style="flex: 1; padding:8px 8px 8px 4px;">
            <draggable
              v-model="componentList2"
              :options="{group: group.componentListGroup2}"
              animation="300"
              ghost-class="ghostClass"
              chosen-class="chosenClass"
              :move="onMoveInComponentList"
            >
              <transition-group>
                <div
                  v-for="(item) in componentList2"
                  :key="item.type"
                  class="m-item base-component-item"
                  @click="addComponentItem(item)"
                >
                  <svg-icon
                    :icon-class="item.icon"
                    style="font-size: 16px; margin-right: 8px"
                  />
                  {{ item.typeName }}
                </div>
              </transition-group>
            </draggable>
          </div>
        </div>
      </div>
      <de-main-container class="center-main">
        <div
          style="flex: 1; background: white;display: flex;flex-direction: column; overflow-x: hidden;position: relative"
          @click="lostFocus"
        >

          <div class="m-title">{{ formSettings.name }}</div>

          <div
            v-if="formSettings.forms.length === 0"
            class="drag-placeholder"
          >{{ $t('commons.component.add_component_hint') }}
          </div>

          <el-form
            ref="mForm"
            label-position="top"
            hide-required-asterisk
            class="form-drag-form"
            @submit.native.prevent
          >
            <draggable
              id="form-drag-place"
              v-model="formSettings.forms"
              group="mFormGroup"
              animation="300"
              class="form-drag-class"

              ghost-class="ghostClass"
              chosen-class="chosenClass"
              @add="addComponent"
            >
              <transition-group>

                <div
                  v-for="(item, $index) in formSettings.forms"
                  :key="item.id"
                  class="m-item m-form-item"
                  :class="{'selectedClass': item.id === selectedItemId}"
                  :data-var="tempId = item.settings ? item.settings.optionDatasource + '_' + item.settings.optionTable + '_' + item.settings.optionColumn + '_' + item.settings.optionOrder : 'unset'"
                  @click.stop="selectItem(item.id)"
                >
                  <div class="m-label-container">
                    <span style="width: unset">
                      {{ item.settings.name }}
                      <span
                        v-if="item.settings.required"
                        style="color: red"
                      >*</span>
                    </span>
                    <span class="btn-container">
                      <el-tooltip
                        effect="dark"
                        :content="$t('commons.copy')"
                        placement="top"
                      >
                        <div
                          class="btn-item"
                          @click.prevent.stop="copyItem(item, $index)"
                        >
                          <svg-icon icon-class="icon_copy_outlined" />
                        </div>
                      </el-tooltip>
                      <el-tooltip
                        effect="dark"
                        :content="$t('commons.delete')"
                        placement="top"
                      >
                        <div
                          class="btn-item"
                          @click.prevent.stop="removeItem(item, $index)"
                        >
                          <svg-icon icon-class="icon_delete-trash_outlined" />
                        </div>
                      </el-tooltip>
                    </span>
                  </div>
                  <el-form-item
                    prop="value"
                    class="form-item"
                  >
                    <el-input
                      v-if="item.type === 'input' && item.settings.inputType !== 'number'"
                      :key="item.id + item.settings.inputType"
                      v-model="item.value"
                      :type="item.settings.inputType"
                      :required="item.settings.required"
                      :placeholder="item.settings.placeholder"
                      size="small"
                    />
                    <el-input-number
                      v-if="item.type === 'input' && item.settings.inputType === 'number'"
                      :key="item.id + item.settings.inputType"
                      v-model="item.value"
                      :required="item.settings.required"
                      :placeholder="item.settings.placeholder"
                      style="width: 100%"
                      controls-position="right"
                      size="small"
                    />
                    <el-input
                      v-else-if="item.type === 'textarea'"
                      :key="item.id + 'textarea'"
                      v-model="item.value"
                      type="textarea"
                      :required="item.settings.required"
                      :placeholder="item.settings.placeholder"
                      size="small"
                    />
                    <el-select
                      v-else-if="item.type === 'select'"
                      :key="item.id + 'select'"
                      v-model="item.value"
                      :required="item.settings.required"
                      :placeholder="item.settings.placeholder"
                      style="width: 100%"
                      size="small"
                      filterable
                      :multiple="item.settings.multiple"
                      clearable
                      :remote="item.settings.optionSourceType !== 1"
                      :remote-method="(val)=>remoteMethod(val, item)"
                      :loading="selectLoading"
                      @focus="remoteMethod('', item)"
                    >
                      <el-option
                        v-for="(x, $index) in item.settings.optionSourceType === 1 ? item.settings.options : (asyncOptions[tempId] ? asyncOptions[tempId] : [])"
                        :key="$index"
                        :label="x.name"
                        :value="x.value"
                      />
                    </el-select>
                    <el-radio-group
                      v-else-if="item.type === 'radio'"
                      :key="item.id + 'radio'"
                      v-model="item.value"
                      :required="item.settings.required"
                      style="width: 100%"
                      size="small"
                    >
                      <el-radio
                        v-for="(x, $index) in item.settings.optionSourceType === 1 ? item.settings.options : (asyncOptions[tempId] ? asyncOptions[tempId] : [])"
                        :key="$index"
                        :label="x.value"
                      ><span :title="x.name">{{ x.name }}</span>
                      </el-radio>
                    </el-radio-group>
                    <el-checkbox-group
                      v-else-if="item.type === 'checkbox'"
                      :key="item.id + 'checkbox'"
                      v-model="item.value"
                      :required="item.settings.required"
                      size="small"
                    >
                      <el-checkbox
                        v-for="(x, $index) in item.settings.optionSourceType === 1 ? item.settings.options : (asyncOptions[tempId] ? asyncOptions[tempId] : [])"
                        :key="$index"
                        :label="x.value"
                      ><span :title="x.name">{{ x.name }}</span>
                      </el-checkbox>
                    </el-checkbox-group>
                    <el-date-picker
                      v-else-if="item.type === 'date'"
                      :key="item.id + 'date'"
                      v-model="item.value"
                      :required="item.settings.required"
                      :type="item.settings.dateType"
                      :placeholder="item.settings.placeholder"
                      style="width: 100%"
                      size="small"
                    />
                    <el-date-picker
                      v-else-if="item.type === 'dateRange'"
                      :key="item.id + 'dateRange'"
                      v-model="item.value"
                      :required="item.settings.required"
                      :type="item.settings.dateType"
                      :range-separator="item.settings.rangeSeparator"
                      :start-placeholder="item.settings.startPlaceholder"
                      :end-placeholder="item.settings.endPlaceholder"
                      style="width: 100%"
                      size="small"
                    />

                  </el-form-item>
                </div>

              </transition-group>
            </draggable>
          </el-form>
        </div>
      </de-main-container>
      <el-cantainer class="tools-window-right">

        <template v-if="selectedItemId !== undefined && selectedComponentItem !== undefined">
          <el-header class="sub-title-header">{{ $t('data_fill.form.component_setting') }}</el-header>
          <el-main style="height: calc(100vh - 60px - 56px);">
            <el-form
              ref="mRightForm"
              :model="selectedComponentItem.settings"
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
                  {{ $t('data_fill.form.title') }}
                  <span
                    style="color: red"
                  >*</span>
                </template>
                <el-input
                  v-model.trim="selectedComponentItem.settings.name"
                  required
                  size="small"
                  maxlength="50"
                  class="m-right-form"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item
                v-if="selectedComponentItem.type === 'dateRange' "
                prop="rangeSeparator"
                class="form-item"
                :rules="[requiredRule]"
              >
                <template #label>
                  {{ $t('data_fill.form.range_separator') }}
                  <span
                    style="color: red"
                  >*</span>
                </template>
                <el-select
                  v-model="selectedComponentItem.settings.rangeSeparator"
                  style="width: 100%"
                  required
                >
                  <el-option
                    label="-"
                    value="-"
                  />
                  <el-option
                    label="~"
                    value="~"
                  />
                </el-select>

                <!--                <el-input
                  v-model.trim="selectedComponentItem.settings.rangeSeparator"
                  required
                  size="small"
                  maxlength="3"
                  show-word-limit
                />-->
              </el-form-item>

              <el-form-item
                v-if="
                  selectedComponentItem.type === 'input' ||
                    selectedComponentItem.type === 'textarea' ||
                    selectedComponentItem.type === 'select' ||
                    selectedComponentItem.type === 'date'
                "
                prop="placeholder"
                class="form-item"
                :label="$t('data_fill.form.hint')"
                :rules="[
                  { maxlength: 50, message: $t('data_fill.form.input_limit_50'), trigger: 'blur' }]"
              >
                <el-input
                  v-model="selectedComponentItem.settings.placeholder"
                  :placeholder="$t('data_fill.form.input_limit_50')"
                  size="small"
                  class="m-right-form"
                  maxlength="50"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item
                v-if="selectedComponentItem.type === 'dateRange' "
                prop="startPlaceholder"
                class="form-item"
                :label="$t('data_fill.form.start_hint_word')"
                :rules="[
                  { maxlength: 50, message: $t('data_fill.form.input_limit_50'), trigger: 'blur' }]"
              >
                <el-input
                  v-model="selectedComponentItem.settings.startPlaceholder"
                  :placeholder="$t('data_fill.form.input_limit_50')"
                  size="small"
                  maxlength="50"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item
                v-if="selectedComponentItem.type === 'dateRange' "
                prop="endPlaceholder"
                class="form-item"
                :label="$t('data_fill.form.end_hint_word')"
                :rules="[
                  { maxlength: 50, message: $t('data_fill.form.input_limit_50'), trigger: 'blur' }]"
              >
                <el-input
                  v-model="selectedComponentItem.settings.endPlaceholder"
                  :placeholder="$t('data_fill.form.input_limit_50')"
                  size="small"
                  maxlength="50"
                  show-word-limit
                />
              </el-form-item>

              <div class="m-splitter" />

              <el-form-item
                v-if="selectedComponentItem.type === 'input'"
                prop="inputType"
                class="form-item"
                :label="$t('data_fill.form.input_type')"
                :rules="[requiredRule]"
              >
                <el-select
                  v-model="selectedComponentItem.settings.inputType"
                  style="width: 100%"
                  required
                  @change="selectedComponentItem.settings.mapping.type = undefined"
                >
                  <el-option
                    v-for="(x) in selectedComponentItemInputTypes"
                    :key="x.type"
                    :label="x.name"
                    :value="x.type"
                  />
                </el-select>
              </el-form-item>

              <el-form-item
                v-if="selectedComponentItem.type === 'date' || selectedComponentItem.type === 'dateRange'"
                prop="dateType"
                class="form-item"
                :label="$t('data_fill.form.date_type')"
                :rules="[requiredRule]"
              >
                <el-select
                  v-model="selectedComponentItem.settings.dateType"
                  style="width: 100%"
                  required
                >
                  <el-option
                    v-for="(x) in selectedComponentItem.type === 'date' ? dateTypes : dateRangeTypes"
                    :key="x.value"
                    :label="x.name"
                    :value="x.value"
                  />
                </el-select>
              </el-form-item>

              <div class="right-check-div">
                <div class="m-label-container">
                  <span style="width: unset; font-weight: bold">
                    {{ $t('data_fill.form.check') }}
                  </span>
                </div>
                <el-form-item
                  prop="required"
                  class="form-item"
                >
                  <el-checkbox v-model="selectedComponentItem.settings.required">
                    {{ $t('data_fill.form.set_required') }}
                  </el-checkbox>
                </el-form-item>
                <el-form-item
                  v-if="selectedComponentItem.type === 'input'"
                  prop="unique"
                  class="form-item"
                >
                  <el-checkbox
                    v-model="selectedComponentItem.settings.unique"
                  >
                    {{ $t('data_fill.form.set_unique') }}
                  </el-checkbox>
                </el-form-item>
                <el-form-item
                  v-if="selectedComponentItem.type === 'select'"
                  prop="multiple"
                  class="form-item"
                >
                  <el-checkbox
                    v-model="selectedComponentItem.settings.multiple"
                    :disabled="selectedComponentItem.old"
                    @change="changeSelectMultiple(selectedComponentItem, selectedComponentItem.settings.multiple)"
                  >
                    {{ $t('data_fill.form.set_multiple') }}
                  </el-checkbox>
                </el-form-item>

              </div>

              <div
                v-if="selectedComponentItem.type === 'select' || selectedComponentItem.type === 'radio' || selectedComponentItem.type === 'checkbox'"
              >

                <div class="m-splitter" />

                <el-form-item
                  prop="optionSourceType"
                  :label="$t('data_fill.form.option_value')"
                  class="form-item no-margin-bottom"
                >
                  <el-radio-group
                    v-model="selectedComponentItem.settings.optionSourceType"
                    size="small"
                    @change="onOptionSourceTypeChange(selectedComponentItem.settings.optionSourceType, selectedComponentItem.settings)"
                  >
                    <el-radio :label="1">
                      {{ $t('data_fill.form.custom') }}
                    </el-radio>
                    <el-radio :label="2">
                      {{ $t('data_fill.form.use_datasource') }}
                    </el-radio>
                  </el-radio-group>
                </el-form-item>

                <template v-if="selectedComponentItem.settings.optionSourceType === 1">
                  <el-button
                    type="text"
                    @click="addOption(selectedComponentItem.settings.options)"
                  >+ {{ $t('data_fill.form.add_option') }}
                  </el-button>

                  <div
                    v-for="(x,$index) in selectedComponentItem.settings.options"
                    :key="$index"
                    class="option-list-div"
                  >
                    <el-form-item
                      :prop="'options['+$index+'].value'"
                      class="form-item no-margin-bottom"
                      style="width: 100%"
                      :rules="[requiredRule, duplicateOptionRule]"
                    >
                      <el-input
                        v-model="x.value"
                        required
                        size="small"
                        minlength="1"
                        maxlength="50"
                        show-word-limit
                        @change="onOptionValueChange(x, x.value)"
                      />
                    </el-form-item>
                    <div
                      class="btn-item"
                      @click.prevent.stop="removeOption(selectedComponentItem.settings.options, $index)"
                    >
                      <svg-icon icon-class="icon_delete-trash_outlined" />
                    </div>
                  </div>
                </template>
                <template v-else>
                  <el-button
                    v-if="!selectedComponentItem.settings.optionColumn"
                    type="text"
                    @click="openEditBindColumn({})"
                  >+ {{ $t('data_fill.form.bind_column') }}
                  </el-button>
                  <div
                    v-else
                    style="display: flex; flex-direction: row; align-items: center; font-size: 14px;"
                  >
                    <div style="width: 28px;" />
                    <div
                      style="flex:2;
                              overflow: hidden;
                              text-overflow: ellipsis;"
                      :title="selectedComponentItem.settings.optionTable + ' (' +
                        selectedComponentItem.settings.optionColumn + ')'"
                    >
                      {{ selectedComponentItem.settings.optionTable }}
                      ({{ selectedComponentItem.settings.optionColumn }})
                    </div>
                    <div style="flex:1; color: #8F959E;">{{ $t('data_fill.form.bind_complete') }}</div>
                    <el-button
                      :title="$t('chart.edit')"
                      icon="el-icon-edit"
                      type="text"
                      @click="openEditBindColumn(selectedComponentItem.settings)"
                    />
                  </div>

                </template>

              </div>

            </el-form>
          </el-main>
        </template>
        <template v-else>
          <el-header class="sub-title-header">{{ $t('data_fill.form.form_setting') }}</el-header>
          <el-main style="height: calc(100vh - 60px - 56px);">
            <el-form
              ref="mRightFormBase"
              :model="formSettings"
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
                  v-model.trim="formSettings.name"
                  required
                  size="small"
                  maxlength="50"
                  show-word-limit
                />
              </el-form-item>

              <el-divider class="m-divider" />

              <el-form-item
                prop="commitNewUpdate"
                class="form-item"
                :rules="[requiredRule]"
              >
                <template #label>
                  {{ $t('data_fill.form.commit_type') }}
                </template>
                <el-radio-group
                  v-model="formSettings.commitNewUpdate"
                  size="small"
                >
                  <el-radio :label="false">
                    {{ $t('data_fill.form.commit_type_append') }}
                  </el-radio>
                  <el-radio :label="true">
                    {{ $t('data_fill.form.commit_type_update') }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>

              <template v-if="formSettings.commitNewUpdate">
                <el-button
                  v-if="ruleUpdateFormList.length === 0"
                  type="text"
                  @click="openEditCommitRule"
                >+ {{ $t('data_fill.form.commit_rule_add') }}
                </el-button>
                <template v-else>
                  <div
                    style="display: flex; flex-direction: row; align-items: center; font-size: 14px;"
                  >
                    <div style="width: 28px;" />
                    <div style="flex:2">
                      {{ $t('data_fill.form.commit_rule_settings') }}
                    </div>
                    <div style="flex:1; color: #8F959E;">{{ $t('data_fill.form.commit_rule_set') }}</div>
                    <el-button
                      :title="$t('chart.edit')"
                      icon="el-icon-edit"
                      type="text"
                      @click="openEditCommitRule"
                    />
                  </div>
                  <div style="padding-left: 28px; font-size: 14px;">{{ $t('data_fill.form.commit_rule') }}:&nbsp; {{ ruleUpdateFormListNames }}</div>
                </template>
              </template>
            </el-form>
          </el-main>
        </template>

      </el-cantainer>
    </de-container>

    <el-drawer
      :title="$t('data_fill.form.save_form')"
      :visible.sync="showDrawer"
      direction="btt"
      size="100%"
      append-to-body
      :with-header="false"
    >
      <data-filling-form-save
        v-if="showDrawer"
        :is-edit="isEdit"
        :disable-create-index="disableCreateIndex"
        :form.sync="formSettings"
        :show-drawer.sync="showDrawer"
      />
    </el-drawer>

    <el-dialog
      v-dialogDrag
      append-to-body
      :title="$t('data_fill.form.use_datasource')"
      :visible.sync="showEditBindColumn"
      :show-close="true"
      width="600px"
      class="m-dialog"
    >
      <el-container
        v-loading="loading"
        style="width: 100%"
        direction="vertical"
      >
        <el-form
          ref="optionForm"
          class="m-form"
          :model="optionFormData"
          label-position="right"
          label-width="140px"
          hide-required-asterisk
          @submit.native.prevent
        >
          <el-main>
            <el-form-item
              prop="optionDatasource"
              class="form-item"
              :label="$t('data_fill.form.datasource')"
              :rules="[requiredRule]"
            >
              <el-select
                v-model="optionFormData.optionDatasource"
                required
                style="width: 100%"
                size="small"
                filterable
                @change="onDataSourceChange"
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
              prop="optionTable"
              class="form-item"
              :label="$t('data_fill.form.table')"
              :rules="[requiredRule]"
            >
              <el-select
                v-model="optionFormData.optionTable"
                required
                style="width: 100%"
                size="small"
                filterable
                @change="onTableChange(optionFormData.optionDatasource, optionFormData.optionTable)"
              >
                <el-option
                  v-for="d in tableList"
                  :key="d.name"
                  :value="d.name"
                  :label="d.name"
                >{{ d.name }}
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
              prop="optionColumn"
              class="form-item"
              :label="$t('data_fill.form.column_name')"
              :rules="[requiredRule]"
            >
              <el-select
                v-model="optionFormData.optionColumn"
                required
                style="width: 100%"
                size="small"
                filterable
              >
                <el-option
                  v-for="d in columnList"
                  :key="d.fieldName"
                  :value="d.fieldName"
                  :label="d.fieldName"
                >{{ d.fieldName }}
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
              prop="optionOrder"
              class="form-item"
              :label="$t('data_fill.form.order')"
              :rules="[requiredRule]"
            >
              <el-radio-group
                v-model="optionFormData.optionOrder"
                required
                style="width: 100%"
                size="small"
              >
                <el-radio label="asc">{{ $t('data_fill.form.order_asc') }}</el-radio>
                <el-radio label="desc">{{ $t('data_fill.form.order_desc') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-main>
        </el-form>
      </el-container>
      <el-footer class="de-footer">
        <el-button @click="closeEditBindColumn">{{ $t("commons.cancel") }}</el-button>
        <el-button
          type="primary"
          @click="doEditBindColumn"
        >{{ $t("commons.confirm") }}
        </el-button>
      </el-footer>
    </el-dialog>

    <el-dialog
      v-dialogDrag
      append-to-body
      :title="$t('data_fill.form.commit_rule_settings')"
      :visible.sync="showCommitUpdateRule"
      :show-close="true"
      width="600px"
      class="m-dialog"
    >
      <el-container
        v-if="showCommitUpdateRule"
        v-loading="loading"
        style="width: 100%"
        direction="vertical"
      >
        <div style="padding: 10px 18px;display: flex;flex-direction: row; background: #c0cef3; margin-bottom: 12px; margin-top: -20px;">
          <i
            class="el-icon-info"
            style="color: #0049e0; margin-right: 4px;"
          />
          <div>
            提交数据时，将选择的组件作为更新条件，对表单中的已有数据进行匹配更新，更新规则如下：<br>
            1、当组件值与表单字段值同时匹配时，则更新表单数据；<br>
            2、当组件值与表单字段值不同时匹配时，将数据插入表单。
          </div>
        </div>
        <el-table
          ref="dataTable"
          style="width: 100%; height: 100%"
          height="600"
          stripe
          :data="tempForms"
          :columns="[]"
        >
          <el-table-column
            :label="$t('data_fill.data.recent_committer')"
          >
            <template slot-scope="scope">
              <el-checkbox v-model="scope.row.updateRuleCheck" />
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('data_fill.data.recent_committer')"
          >
            <template slot-scope="scope">
              {{ scope.row.name }}
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('data_fill.data.recent_committer')"
          >
            <template slot-scope="scope">
              {{ scope.row.typeName }}
            </template>
          </el-table-column>
        </el-table>

      </el-container>
      <el-footer class="de-footer">
        <el-button @click="closeEditCommitRule">{{ $t("commons.cancel") }}</el-button>
        <el-button
          type="primary"
          @click="confirmEditCommitRule"
        >{{ $t("commons.confirm") }}
        </el-button>
      </el-footer>
    </el-dialog>

  </div>
</template>

<style lang="scss" scoped>
.data-filling-form {
  .m-divider {
    width: 320px;
    margin: 24px -20px;
  }
  ::v-deep .el-form-item__error {
    position: relative;
  }

  .m-right-form {
    ::v-deep .el-input__inner {
      padding-right: 45px;
    }
  }

  .drag-placeholder {
    height: 68px;
    background: rgba(245, 246, 247, 1);
    border: 1px dashed rgba(187, 191, 196, 1);
    display: flex;
    border-radius: 4px;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    cursor: default;

    font-weight: 400;
    font-size: 14px;

    position: absolute;
    width: calc(100% - 120px);
    top: 88px;
    left: 50%;
    transform: translate(-50%, 0);
  }

  .de-header {
    height: 56px !important;
    padding: 0px !important;
    border-bottom: 1px solid #E6E6E6;
    background-color: var(--SiderBG, white);

    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .panel-info-area {
    padding-left: 20px;
  }

  .icon20 {
    font-size: 20px;
    color: var(--TextPrimary, #1F2329);
  }

  .margin-left12 {
    margin-left: 12px;
  }

  .text16 {
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    color: var(--TextPrimary, #1F2329);
  }

  .toolbar-icon-active {
    cursor: pointer;
    transition: 0.1s;
    border-radius: 3px;
  }

  .toolbar-icon-active:hover {
    background-color: rgba(31, 35, 41, 0.1);
    color: #3a8ee6;
  }

  .form-main-container {
    display: flex;
  }

  .center-main {
    background-color: rgb(247, 248, 250);
    height: calc(100vh - 56px);
    flex: 1;
    padding: 20px 20px 0 20px;

    display: flex;
  }

  .tools-window-left {
    width: 280px;
    background-color: #FFFFFF;
    border-right: 1px rgba(31, 35, 41, 0.15) solid;

  }

  .tools-window-right {
    width: 320px;
    background-color: #FFFFFF;
    border-left: 1px rgba(31, 35, 41, 0.15) solid;

    .m-label-container {
      font-weight: 400;
    }
  }

  .sub-title-header {
    border-bottom: 1px solid #E6E6E6;
    background-color: var(--SiderBG, white);

    display: flex;
    flex-direction: row;
    align-items: center;

    //styleName: Title 3 / 16px; font-family: PingFang SC; font-size: 16px; font-weight: 500; line-height: 24px; text-align: left;

  }

  .form-drag-form {
    flex: 1;
    display: flex;
    min-height: 32px;
    width: 100%;

    z-index: 1;
  }

  .form-drag-class {
    flex: 1;
    display: flex;
    min-height: 32px;
    width: 100%;

    padding: 0 60px;

    span:only-child {
      width: 100%;
    }

    .ghostClass {
      min-height: 68px;
      margin-top: 0 !important;
    }
  }

  .ghostClass {
    opacity: 1 !important;
    background: rgba(51, 112, 255, 0.1) !important;
    border: 1px dashed rgba(51, 112, 255, 1) !important;
  }

  .chosenClass {
    background-color: #f1f1f1 !important;
    opacity: 1;
  }

  .selectedClass {
    background-color: #f1f1f1 !important;
    opacity: 1;
    border: 1px solid rgba(51, 112, 255, 1) !important;
  }

  .dragClass {

    opacity: 1 !important;
    box-shadow: none !important;
    outline: none !important;
    background-image: none !important;
  }

  .m-item {
    width: 100%;
    border: solid 1px #eee;
    background-color: #f1f1f1;
    border-radius: 4px;
  }

  .base-component-item {
    margin: 8px 0;
    cursor: pointer;
    height: 32px;

    padding-left: 8px;
    padding-right: 8px;

    font-weight: 400;
    font-size: 12px;
    line-height: 20px;

    display: flex;
    flex-direction: row;
    align-items: center;
  }

  .base-component-item:hover {
    background: rgba(31, 35, 41, 0.1);
  }

  .m-form-item {
    margin-bottom: 10px;
    border-radius: 4px;

    border: solid 1px transparent;
    background-color: unset;

    padding: 8px 20px;
  }

  .m-form-item:hover {
    background-color: #f1f1f1;
    border: solid 1px #eee;
    cursor: pointer;

    .m-label-container {
      .btn-container {
        visibility: visible;
      }
    }

  }

  .m-title {
    margin: 40px 80px 20px;

    height: 28px;

    font-weight: 500;
    font-size: 20px;
    line-height: 28px;

    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .m-label-container {
    width: 100%;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;

    font-weight: 500;
    font-size: 14px;
    line-height: 22px;

    margin-bottom: 8px;

    .btn-container {
      display: flex;
      flex-direction: row;
      align-items: center;

      visibility: hidden;

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

  }

  .form-item {
    margin-bottom: 0;

    label {
      width: 100%;
    }

    .el-radio-group {
      label {
        width: unset;
        margin-bottom: 4px;
      }
    }

    .el-checkbox-group {
      label {
        width: unset;
        margin-bottom: 4px;
      }
    }
  }

  .m-splitter {
    background: rgba(31, 35, 41, 0.15);
    height: 1px;
    width: 100%;
    margin-bottom: 16px;
  }

  .tools-window-right {
    .form-item {

      margin-bottom: 16px;

      .el-form-item__label {
        line-height: 24px;
        font-size: 14px;
        font-weight: 400;
        color: rgba(31, 35, 41, 1);

      }
    }

    .no-margin-bottom {
      margin-bottom: 0;
    }

    .right-check-div {
      .form-item {
        margin-bottom: 0
      }

      .form-item:last-child {
        margin-bottom: 16px;
      }
    }

    .option-list-div {
      display: flex;
      flex-direction: row;
      align-items: center;

      margin-bottom: 8px;

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

      .btn-item:hover {
        background: rgba(31, 35, 41, 0.1);
      }

    }

  }

  ::v-deep .ed-radio {
    max-width: 100%;
  }

  ::v-deep .ed-checkbox-group {
    max-width: 100%;
  }

  ::v-deep .ed-checkbox {
    max-width: 100%;
  }

  ::v-deep .ed-radio__label {
    font-weight: normal;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  ::v-deep .ed-checkbox__label {
    font-weight: normal;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

}

.de-footer {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end
}
</style>
