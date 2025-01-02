<script>
import { forEach, find, concat, cloneDeep, floor, map, filter, includes, split, keys, parseInt } from 'lodash-es'
import { PHONE_REGEX, EMAIL_REGEX } from '@/utils/validate'
import {
  getTableColumnData,
  newFormRowData,
  saveFormRowData,
  userFillFormData
} from '@/views/dataFilling/form/dataFilling'

export default {
  name: 'EditFormData',
  props: {
    keyName: {
      type: String,
      required: false
    },
    id: {
      type: String,
      required: false
    },
    userTaskId: {
      type: String,
      required: false
    },
    title: {
      type: String,
      required: false
    },
    formTitle: {
      type: String,
      required: true
    },
    formId: {
      type: String,
      required: true
    },
    forms: {
      type: Object,
      required: true
    },
    data: {
      type: Object,
      required: false
    },
    dataList: {
      type: Object,
      required: false
    },
    showDrawer: {
      type: Boolean,
      required: true
    },
    readonly: {
      type: Boolean,
      required: true
    }
  },
  data() {
    const checkDateRangeRequireValidator = (rule, value, callback) => {
      if (!value) {
        return callback(new Error(this.$t('commons.required')))
      }
      if (value.length < 2) {
        return callback(new Error(this.$t('commons.required')))
      }
      if (!value[0]) {
        return callback(new Error(this.$t('commons.required')))
      }
      if (!value[1]) {
        return callback(new Error(this.$t('commons.required')))
      }
      callback()
    }
    return {
      currentPage: 1,
      loading: false,
      selectLoading: false,
      asyncOptions: {},
      formData: [],
      requiredRule: { required: true, message: this.$t('commons.required'), trigger: ['blur', 'change'] },
      dateRangeRequiredRule: { validator: checkDateRangeRequireValidator, message: this.$t('commons.required'), trigger: ['blur', 'change'] },
      inputTypes: [
        { type: 'text', name: this.$t('data_fill.form.text'), rules: [] },
        { type: 'number', name: this.$t('data_fill.form.number'), rules: [] },
        {
          type: 'tel',
          name: this.$t('data_fill.form.tel'),
          rules: [{ pattern: PHONE_REGEX, message: this.$t('user.mobile_number_format_is_incorrect'), trigger: ['blur', 'change'] }]
        },
        {
          type: 'email',
          name: this.$t('data_fill.form.email'),
          rules: [{ pattern: EMAIL_REGEX, message: this.$t('user.email_format_is_incorrect'), trigger: ['blur', 'change'] }]
        }
      ],
      pickerOptions: {
        disabledDate: (time) => {
          return time.getTime() < new Date(0).getTime()
        }
      }
    }
  },
  computed: {
    allData() {
      if (this.dataList && this.dataList.length > 0) {
        return this.dataList
      }
      if (this.data) {
        return [this.data]
      }
      return [{}]
    }
  },
  watch: {},
  mounted() {
    const _tempForms = []
    this.formData = []
    this.asyncOptions = {}
    this.currentPage = 1
    forEach(this.allData, _data => {
      const _tempFormRow = []
      forEach(this.forms, v => {
        if (!v.removed) {
          const f = cloneDeep(v)
          if (f.type === 'date' && f.settings.dateType === undefined) { // 兼容旧的
            f.settings.dateType = f.settings.enableTime ? 'datetime' : 'date'
          }
          if (f.type === 'dateRange' && f.settings.dateType === undefined) { // 兼容旧的
            f.settings.dateType = f.settings.enableTime ? 'datetimerange' : 'daterange'
          }
          if (f.type === 'dateRange') {
            const _start = _data[f.settings.mapping.columnName1]
            const _end = _data[f.settings.mapping.columnName2]
            f.value = [_start, _end]
          } else {
            const _value = _data[f.settings.mapping.columnName]
            // 交给后面处理
            f.value = _value
          }
          _tempFormRow.push(f)
        }
      })
      _tempForms.push(_tempFormRow)
    })

    this.loading = true
    this.initFormOptionsData(_tempForms, () => {
      // 最后处理选项值
      for (let i = 0; i < _tempForms.length; i++) {
        const row = _tempForms[i]
        row.forEach(f => {
          if (f.type !== 'dateRange') {
            const _value = this.allData[i][f.settings.mapping.columnName]
            if (f.type === 'select' && f.settings.multiple || f.type === 'checkbox') {
              if (_value) {
              // 过滤一下选项值
                if (this.readonly) {
                  f.value = JSON.parse(_value)
                } else {
                  // const tempId = f.settings.optionDatasource + '_' + f.settings.optionTable + '_' + f.settings.optionColumn + '_' + f.settings.optionOrder
                  // const options = map(f.settings.optionSourceType === 1 ? f.settings.options : (this.asyncOptions[tempId] ? this.asyncOptions[tempId] : []), f => f.value)
                  // f.value = filter(JSON.parse(_value), v => includes(options, v))
                  f.value = JSON.parse(_value)
                }
              } else {
                f.value = []
              }
            } else if (f.type === 'select' && !f.settings.multiple || f.type === 'radio') {
              if (_value) {
                if (!this.readonly) {
                  // const tempId = f.settings.optionDatasource + '_' + f.settings.optionTable + '_' + f.settings.optionColumn + '_' + f.settings.optionOrder
                  // const options = map(f.settings.optionSourceType === 1 ? f.settings.options : (this.asyncOptions[tempId] ? this.asyncOptions[tempId] : []), f => f.value)
                  // if (!includes(options, _value)) {
                  //   f.value = undefined
                  // } else {
                  //   f.value = _value
                  // }
                  f.value = _value
                } else {
                  f.value = _value
                }
              }
            }
          }
          f.tempId = f.settings ? f.settings.optionDatasource + '_' + f.settings.optionTable + '_' + f.settings.optionColumn + '_' + f.settings.optionOrder : 'unset'
        })
      }
      // 赋值到表单
      this.formData = _tempForms
      this.loading = false
    })
  },
  methods: {
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
      // 同一个表单多条数据，展示的肯定也是相同的，所以取第一个
      forEach(forms[0], f => {
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
    getRules(item) {
      let rules = []
      if (item.settings.required) {
        rules.push(this.requiredRule)
        if (item.type === 'dateRange') {
          rules.push(this.dateRangeRequiredRule)
        }
      }
      if (item.type === 'input') {
        const inputRules = find(this.inputTypes, t => t.type === item.settings.inputType).rules
        if (inputRules) {
          rules = concat(rules, inputRules)
        }
      }
      return rules
    },
    closeDrawer() {
      this.$emit('update:showDrawer', false)
    },
    onNumberChange(item) {
      let value
      if (item.settings.mapping.type === 'number') {
        value = floor(item.value, 0)
      } else {
        value = floor(item.value, 8)
      }
      this.$nextTick(() => {
        item.value = value
      })
    },
    onPageChange(page) {
      this.currentPage = page
    },
    doSave() {
      this.loading = true
      this.$refs['mForm'].validate((valid, invalidFields) => {
        if (valid) {
          const req = []

          for (let i = 0; i < this.formData.length; i++) {
            const row = this.formData[i]
            const _data = {}
            forEach(row, f => {
              if (f.type === 'dateRange') {
                const _start = f.settings.mapping.columnName1
                const _end = f.settings.mapping.columnName2
                if (f.value) {
                  if (f.value[0]) {
                    _data[_start] = f.value[0].getTime()
                  }
                  if (f.value[1]) {
                    _data[_end] = f.value[1].getTime()
                  }
                }
              } else {
                const name = f.settings.mapping.columnName
                if (f.type === 'select' && f.settings.multiple || f.type === 'checkbox') {
                  if (f.value) {
                    _data[name] = JSON.stringify(f.value)
                  }
                } else if (f.type === 'date' && f.value) {
                  _data[name] = f.value.getTime()
                } else {
                  _data[name] = f.value
                }
              }
            })

            if (this.keyName) {
              _data[this.keyName] = this.allData[i][this.keyName]
            }

            req.push(_data)
          }

          if (this.userTaskId) {
            userFillFormData(this.userTaskId, req).then(res => {
              this.$emit('save-success')
            }).finally(() => {
              this.loading = false
            })
          } else {
            // 非任务都是针对单条数据进行提交
            if (this.id !== undefined) {
              // update
              saveFormRowData(this.formId, this.id, req[0]).then(res => {
                this.$emit('save-success')
              }).finally(() => {
                this.loading = false
              })
            } else {
              // insert
              newFormRowData(this.formId, req[0]).then(res => {
                this.$emit('save-success')
              }).finally(() => {
                this.loading = false
              })
            }
          }
        } else {
          // 获取第几页，切换到对应的页面
          const _key = keys(invalidFields)[0]
          const index = split(_key, ']')[0].replace('[', '')
          this.currentPage = parseInt(index) + 1

          this.loading = false
        }
      })
    }
  }

}
</script>

<template>
  <el-container
    v-loading="loading"
    class="DataFillingSave"
  >
    <el-header class="de-header">
      <div class="panel-info-area">
        <span class="text16 margin-left12">
          {{ title? title: (readonly? $t('data_fill.task.show_data'): $t('data_fill.task.edit_data')) }}
        </span>
      </div>

      <div style="padding-right: 20px">
        <i
          class="el-icon-close"
          style="cursor: pointer"
          @click="closeDrawer"
        />
      </div>
    </el-header>
    <el-main class="de-main">
      <div style="width: 80%">
        <div class="m-title">{{ formTitle }}</div>
      </div>
      <el-form
        ref="mForm"
        class="m-form"
        label-position="top"
        hide-required-asterisk
        :model="formData"
        @submit.native.prevent
      >
        <div
          v-for="(row, $index1) in formData"
          v-show="currentPage === $index1 + 1"
          :key="$index1"
        >
          <div
            v-for="(item, $index2) in row"
            :key="item.id"
            class="m-item m-form-item"
          >

            <div class="m-label-container">
              <span style="width: unset">
                {{ item.settings.name }}
                <span
                  v-if="item.settings.required"
                  style="color: red"
                >*</span>
              </span>
            </div>
            <el-form-item
              :prop="'['+ $index1 +']['+ $index2 +'].value'"
              class="form-item"
              :readonly="readonly"
              :rules="getRules(item)"
            >
              <el-input
                v-if="item.type === 'input' && item.settings.inputType !== 'number'"
                v-model="item.value"
                :type="item.settings.inputType"
                :required="item.settings.required"
                :readonly="readonly"
                :placeholder="item.settings.placeholder"
                size="small"
                :show-word-limit="item.value !== undefined && item.value !== null && item.value.length > 250"
                maxlength="255"
              />
              <el-input-number
                v-if="item.type === 'input' && item.settings.inputType === 'number'"
                v-model="item.value"
                :required="item.settings.required"
                :disabled="readonly"
                :placeholder="item.settings.placeholder"
                style="width: 100%"
                controls-position="right"
                :precision="item.settings.mapping.type === 'number' ? 0 : undefined"
                size="small"
                :min="-999999999999"
                :max="999999999999"
                @change="onNumberChange(item)"
                @blur="onNumberChange(item)"
                @keyup.enter.native="onNumberChange(item)"
              />
              <el-input
                v-else-if="item.type === 'textarea'"
                v-model="item.value"
                type="textarea"
                :required="item.settings.required"
                :readonly="readonly"
                :placeholder="item.settings.placeholder"
                size="small"
              />
              <el-select
                v-else-if="item.type === 'select'"
                v-model="item.value"
                :required="item.settings.required"
                :disabled="readonly"
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
                  v-for="(x, $index) in item.settings.optionSourceType === 1 ? item.settings.options : (asyncOptions[item.tempId] ? asyncOptions[item.tempId] : [])"
                  :key="$index"
                  :label="x.name"
                  :value="x.value"
                />
              </el-select>
              <el-radio-group
                v-else-if="item.type === 'radio'"
                v-model="item.value"
                :required="item.settings.required"
                :disabled="readonly"
                style="width: 100%"
                size="small"
              >
                <el-radio
                  v-for="(x, $index) in item.settings.optionSourceType === 1 ? item.settings.options : (asyncOptions[item.tempId] ? asyncOptions[item.tempId] : [])"
                  :key="$index"
                  :label="x.value"
                ><span :title="x.name">{{ x.name }}</span>
                </el-radio>
              </el-radio-group>
              <el-checkbox-group
                v-else-if="item.type === 'checkbox'"
                v-model="item.value"
                :required="item.settings.required"
                :disabled="readonly"
                size="small"
              >
                <el-checkbox
                  v-for="(x, $index) in item.settings.optionSourceType === 1 ? item.settings.options : (asyncOptions[item.tempId] ? asyncOptions[item.tempId] : [])"
                  :key="$index"
                  :label="x.value"
                ><span :title="x.name">{{ x.name }}</span>
                </el-checkbox>
              </el-checkbox-group>
              <el-date-picker
                v-else-if="item.type === 'date'"
                v-model="item.value"
                :required="item.settings.required"
                :readonly="readonly"
                :type="item.settings.dateType"
                :placeholder="item.settings.placeholder"
                style="width: 100%"
                size="small"
                :picker-options="pickerOptions"
              />
              <el-date-picker
                v-else-if="item.type === 'dateRange'"
                v-model="item.value"
                :required="item.settings.required"
                :readonly="readonly"
                :type="item.settings.dateType"
                :range-separator="item.settings.rangeSeparator"
                :start-placeholder="item.settings.startPlaceholder"
                :end-placeholder="item.settings.endPlaceholder"
                style="width: 100%"
                size="small"
                :picker-options="pickerOptions"
              />
            </el-form-item>
          </div>
        </div>
      </el-form>
    </el-main>
    <el-footer
      class="de-footer"
    >
      <div class="de-footer-container">
        <el-pagination
          v-if="allData.length > 1"
          ref="mPagerRef"
          layout="prev, pager, next"
          page-size="1"
          :total="allData.length"
          :current-page="currentPage"
          @current-change="onPageChange"
        />
      </div>
      <el-button @click="closeDrawer">{{ $t("commons.cancel") }}</el-button>
      <el-button
        v-if="!readonly"
        type="primary"
        @click="doSave"
      >{{ $t("commons.confirm") }}
      </el-button>
    </el-footer>
  </el-container>
</template>

<style  lang="scss" scoped>
.DataFillingSave {

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
    justify-content: flex-end;

    .de-footer-container{
      flex: 1;
      display: flex;
      flex-direction: row;
      align-items: center;
      justify-content: center;
    }
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

  .m-title {
    margin: 40px 20px 20px;

    height: 28px;

    font-weight: 500;
    font-size: 20px;
    line-height: 28px;

    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .m-item {
    width: 100%;
    border: solid 1px #eee;
    background-color: #f1f1f1;
    border-radius: 4px;
  }

  .m-form-item {
    margin-bottom: 10px;
    border-radius: 4px;

    border: solid 1px transparent;
    background-color: unset;

    padding: 8px 20px;
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
</style>
