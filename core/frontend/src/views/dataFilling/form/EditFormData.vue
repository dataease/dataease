<script>
import { forEach, find, concat, cloneDeep, floor } from 'lodash-es'
import { PHONE_REGEX, EMAIL_REGEX } from '@/utils/validate'
import { newFormRowData, saveFormRowData, userFillFormData } from '@/views/dataFilling/form/dataFilling'

export default {
  name: 'EditFormData',
  props: {
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
      loading: false,
      formData: {},
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
  watch: {},
  mounted() {
    this.formData = []
    forEach(this.forms, v => {
      const f = cloneDeep(v)
      if (f.type === 'dateRange') {
        const _start = this.data[f.settings.mapping.columnName1]
        const _end = this.data[f.settings.mapping.columnName2]
        f.value = [_start, _end]
      } else {
        const _value = this.data[f.settings.mapping.columnName]
        if (f.type === 'select' && f.settings.multiple || f.type === 'checkbox') {
          if (_value) {
            f.value = JSON.parse(_value)
          } else {
            f.value = []
          }
        } else {
          f.value = _value
        }
      }
      this.formData.push(f)
    })
  },
  methods: {
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
    doSave() {
      this.loading = true
      this.$refs['mForm'].validate((valid) => {
        if (valid) {
          const _data = {}
          forEach(this.formData, f => {
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

          if (this.userTaskId) {
            userFillFormData(this.userTaskId, _data).then(res => {
              this.$emit('save-success')
            }).finally(() => {
              this.loading = false
            })
          } else {
            if (this.id !== undefined) {
              // update
              saveFormRowData(this.formId, this.id, _data).then(res => {
                this.$emit('save-success')
              }).finally(() => {
                this.loading = false
              })
            } else {
              // insert
              newFormRowData(this.formId, _data).then(res => {
                this.$emit('save-success')
              }).finally(() => {
                this.loading = false
              })
            }
          }
        } else {
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
          v-for="(item, $index) in formData"
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
            :prop="'['+ $index +'].value'"
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
              :multiple="item.settings.multiple"
              clearable
            >
              <el-option
                v-for="(x, $index) in item.settings.options"
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
                v-for="(x, $index) in item.settings.options"
                :key="$index"
                :label="x.value"
              >{{ x.name }}
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
                v-for="(x, $index) in item.settings.options"
                :key="$index"
                :label="x.value"
              >{{ x.name }}
              </el-checkbox>
            </el-checkbox-group>
            <el-date-picker
              v-else-if="item.type === 'date' && !item.settings.enableTime"
              v-model="item.value"
              :required="item.settings.required"
              :readonly="readonly"
              type="date"
              :placeholder="item.settings.placeholder"
              style="width: 100%"
              size="small"
              :picker-options="pickerOptions"
            />
            <el-date-picker
              v-else-if="item.type === 'date' && item.settings.enableTime"
              v-model="item.value"
              :required="item.settings.required"
              :readonly="readonly"
              type="datetime"
              :placeholder="item.settings.placeholder"
              style="width: 100%"
              size="small"
              :picker-options="pickerOptions"
            />
            <el-date-picker
              v-else-if="item.type === 'dateRange' && !item.settings.enableTime"
              v-model="item.value"
              :required="item.settings.required"
              :readonly="readonly"
              type="daterange"
              :range-separator="item.settings.rangeSeparator"
              :start-placeholder="item.settings.startPlaceholder"
              :end-placeholder="item.settings.endPlaceholder"
              style="width: 100%"
              size="small"
              :picker-options="pickerOptions"
            />
            <el-date-picker
              v-else-if="item.type === 'dateRange' && item.settings.enableTime"
              v-model="item.value"
              :required="item.settings.required"
              :readonly="readonly"
              type="datetimerange"
              :range-separator="item.settings.rangeSeparator"
              :start-placeholder="item.settings.startPlaceholder"
              :end-placeholder="item.settings.endPlaceholder"
              style="width: 100%"
              size="small"
              :picker-options="pickerOptions"
            />

          </el-form-item>
        </div>
      </el-form>
    </el-main>
    <el-footer
      class="de-footer"
    >
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
}
</style>
