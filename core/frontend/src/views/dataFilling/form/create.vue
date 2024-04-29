<script>
import DeContainer from '@/components/dataease/DeContainer.vue'
import DataFillingFormSave from './save.vue'
import clickoutside from 'element-ui/src/utils/clickoutside.js'
import { filter, cloneDeep, find, concat } from 'lodash-es'
import { v4 as uuidv4 } from 'uuid'
import { EMAIL_REGEX, PHONE_REGEX } from '@/utils/validate'

export default {
  name: 'DataFillingFormCreate',
  components: { DeContainer, DataFillingFormSave },
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
      moveId: undefined,
      showDrawer: false,
      requiredRule: { required: true, message: this.$t('commons.required'), trigger: ['blur', 'change'] },
      duplicateOptionRule: { validator: checkDuplicateOptionValidator, trigger: ['blur', 'change'] },
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
            enableTime: false,
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
            enableTime: false,
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
        level: undefined
      },
      selectedItemId: undefined
    }
  },
  computed: {
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
    }

  },
  beforeDestroy() {
  },
  created() {
    if (this.$route.query.folder !== undefined) {
      this.formSettings.folder = this.$route.query.folder
    }
    if (this.$route.query.level !== undefined) {
      this.formSettings.level = this.$route.query.level
    }
  },
  methods: {
    closeCreate: function() {
      // back to forms list
      this.$router.replace('/data-filling/forms')
    },
    onMoveInComponentList(e, originalEvent) {
      if (e.relatedContext?.component?.$el?.id === 'form-drag-place') {
        return true
      }
      return false
    },
    addInComponentList(e) {
      console.log(e)
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
        if (item.settings.mapping.type === 'text') {
          item.settings.mapping.type = undefined
        }
      }
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
          }
        }
      }

      this.showDrawer = true
    }

  }
}
</script>

<template>
  <div class="data-filling-form">
    <el-header class="de-header">
      <div class="panel-info-area">
        <!--back to panelList-->
        <svg-icon
          icon-class="icon_left_outlined"
          class="toolbar-icon-active icon20"
          @click="closeCreate"
        />
        <span class="text16 margin-left12">
          {{ $t('data_fill.form.create_new_form') }}
        </span>
      </div>

      <el-button
        style="margin-right: 20px"
        @click="toSave"
      >{{ $t('commons.save') }}</el-button>
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
                      :key="item.id + 'radio'"
                      v-model="item.value"
                      :required="item.settings.required"
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
                      :key="item.id + 'checkbox'"
                      v-model="item.value"
                      :required="item.settings.required"
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
                      :key="item.id + 'date'"
                      v-model="item.value"
                      :required="item.settings.required"
                      type="date"
                      :placeholder="item.settings.placeholder"
                      style="width: 100%"
                      size="small"
                    />
                    <el-date-picker
                      v-else-if="item.type === 'date' && item.settings.enableTime"
                      :key="item.id + 'dateEnableTime'"
                      v-model="item.value"
                      :required="item.settings.required"
                      type="datetime"
                      :placeholder="item.settings.placeholder"
                      style="width: 100%"
                      size="small"
                    />
                    <el-date-picker
                      v-else-if="item.type === 'dateRange' && !item.settings.enableTime"
                      :key="item.id + 'dateRangeEnableTime'"
                      v-model="item.value"
                      :required="item.settings.required"
                      type="daterange"
                      :range-separator="item.settings.rangeSeparator"
                      :start-placeholder="item.settings.startPlaceholder"
                      :end-placeholder="item.settings.endPlaceholder"
                      style="width: 100%"
                      size="small"
                    />
                    <el-date-picker
                      v-else-if="item.type === 'dateRange' && item.settings.enableTime"
                      :key="item.id + 'datetimerangeRangeEnableTime'"
                      v-model="item.value"
                      :required="item.settings.required"
                      type="datetimerange"
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
                    v-for="(x) in inputTypes"
                    :key="x.type"
                    :label="x.name"
                    :value="x.type"
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
                    @change="changeSelectMultiple(selectedComponentItem, selectedComponentItem.settings.multiple)"
                  >
                    {{ $t('data_fill.form.set_multiple') }}
                  </el-checkbox>
                </el-form-item>
                <el-form-item
                  v-if="selectedComponentItem.type === 'date' || selectedComponentItem.type === 'dateRange'"
                  prop="multiple"
                  class="form-item"
                >
                  <el-checkbox
                    v-model="selectedComponentItem.settings.enableTime"
                  >
                    {{ $t('data_fill.form.use_datetime') }}
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
                  >
                    <el-radio :label="1">
                      {{ $t('data_fill.form.custom') }}
                    </el-radio>
                  </el-radio-group>
                </el-form-item>

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
        :form.sync="formSettings"
        :show-drawer.sync="showDrawer"
      />
    </el-drawer>

  </div>
</template>

<style  lang="scss" scoped>
.data-filling-form {
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

}
</style>
