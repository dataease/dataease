<template>
  <el-container
    v-loading="loading"
    class="DataFillingSave"
  >
    <el-header class="de-header">
      <div class="panel-info-area">
        <span class="text16 margin-left12">
          {{ id? $t('data_fill.task.edit_task'): $t('data_fill.task.create_task') }}
        </span>
      </div>

      <div style="padding-right: 20px">
        <i
          class="el-icon-close"
          style="cursor: pointer"
          @click="reset"
        />
      </div>
    </el-header>
    <el-main class="de-main">
      <div
        v-if="userLoadFinish && showDrawer"
        class="new-task-form"
      >
        <el-form
          ref="taskForm"
          class="de-form-item"
          :model="form"
          :rules="rule"
          size="small"
          label-width="100px"
          @submit.native.prevent
        >
          <div class="row-rules marTop0">
            <span>{{ $t("datasource.base_info") }}</span>
          </div>
          <el-form-item
            :label="$t('xpacktask.name')"
            prop="name"
          >
            <el-input
              v-model="form.name"
              @input="setTitle"
            />
          </el-form-item>

          <el-form-item
            :label="$t('commons.user')"
            prop="userValues"
          >
            <el-select
              ref="userSelect"
              v-model="form.userValues"
              style="width: 100%"
              filterable
              multiple
              :popper-append-to-body="false"
              :placeholder="
                $t('commons.please_select') + $t('emailtask.recipients')
              "
            >
              <el-option
                v-for="item in users"
                :key="item.nickName"
                :label="item.nickName"
                :value="item.username"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            :label="$t('commons.role')"
            prop="roleList"
          >
            <el-select
              ref="roleSelect"
              v-model="form.roleList"
              style="width: 100%"
              filterable
              multiple
              :popper-append-to-body="false"
              :placeholder="
                $t('commons.please_select') + $t('commons.role')
              "
            >
              <el-option
                v-for="item in roleOptions"
                :key="item.name"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            :label="$t('commons.organization')"
            prop="orgList"
          >
            <treeselect
              ref="orgTreeSelect"
              v-model="form.orgList"
              class="email-task-tree"
              size="mini"
              :multiple="true"
              :flat="true"
              :sort-value-by="sortValueBy"
              :options="orgTreeData"
              :placeholder="$t('commons.please_select') + $t('commons.organization')"
            />
          </el-form-item>

          <div class="row-rules marTop0">
            <span>{{ $t("emailtask.send_config") }}</span>
          </div>

          <el-form-item
            :label="$t('emailtask.rate_type')"
            prop="rateType"
          >
            <el-radio-group v-model="rateType">
              <el-radio
                v-for="rate in rateTypeList"
                :key="rate.label"
                :label="rate.value"
              >{{ $t(rate.label) }}</el-radio>
            </el-radio-group>

            <template v-if="rateType === 'single_task'">
              <div
                class="rate-type-time"
                style="padding-top: 28px; padding-bottom: 0"
              >
                <span
                  class="prefix"
                  style="margin-bottom: 28px; "
                >{{ $t('data_fill.task.task_distribute_time') }}</span>
                <el-form-item
                  prop="publishStartTime"
                  style="flex:1"
                >
                  <el-date-picker
                    v-model="form.publishStartTime"
                    :clearable="false"
                    style="width: 100%"
                    type="datetime"
                    :picker-options="startPickerOptions"
                    class="de-time-range"
                    :placeholder="$t('commons.please_select')"
                  />
                </el-form-item>
              </div>
              <div
                class="rate-type-time"
                style="padding-top: 0; padding-bottom: 0"
              >
                <span
                  class="prefix"
                  style="margin-bottom: 28px"
                >{{ $t('data_fill.task.task_end_time') }}</span>
                <el-form-item
                  prop="publishEndTime"
                  style="flex:1"
                >
                  <el-date-picker
                    v-model="form.publishEndTime"
                    :clearable="false"
                    style="width: 100%"
                    type="datetime"
                    :picker-options="startPickerOptions"
                    :placeholder="$t('commons.please_select')"
                    class="de-time-range"
                    default-time="23:59:59"
                  />
                </el-form-item>
              </div>
            </template>
            <template v-else-if="rateType === 'simple_repeat'">
              <div class="rate-type-time">

                <el-select
                  v-model="timeTypeValue"
                  class="w140"
                >
                  <el-option
                    v-for="item in timeType"
                    :key="item.value"
                    :label="$t(item.label)"
                    :value="item.value"
                  />
                </el-select>
                <el-select
                  v-if="timeTypeValue === 1"
                  v-model="weekTypeValue"
                  class="w140"
                >
                  <el-option
                    v-for="item in weekType"
                    :key="item.value"
                    :label="$t(item.label)"
                    :value="item.value"
                  />
                </el-select>
                <el-select
                  v-if="timeTypeValue === 2"
                  v-model="monthTypeValue"
                  class="w140"
                >
                  <el-option
                    v-for="item in monthType"
                    :key="item.value"
                    :label="(item.label + $t('dynamic_time.date'))"
                    :value="item.value"
                  />
                </el-select>
                <el-time-picker
                  v-model="timePicker"
                  :clearable="false"
                  class="w140"
                  :picker-options="{
                    selectableRange: '00:00:00 - 23:59:59',
                  }"
                />
                <span class="tail">{{ $t('cron.every_exec') }}</span>
              </div>
              <div class="rate-type-time second-row">
                <span class="prefix">{{ $t('data_fill.task.task_finish_in') }}</span>
                <el-input-number
                  v-model.number="form.publishRangeTime"
                  class="w140"
                  :min="1"
                  :max="100"
                  :step="1"
                  :precision="0"
                  @change="onPublishRangeTimeChange(form)"
                  @blur="onPublishRangeTimeChange(form)"
                  @keyup.enter.native="onPublishRangeTimeChange(form)"
                />
                <el-select
                  v-model="form.publishRangeTimeType"
                  class="w140"
                >
                  <el-option
                    v-for="item in simpleTimeType"
                    :key="item.value"
                    :label="$t(item.label)"
                    :value="item.value"
                  />
                </el-select>

                <span class="tail">{{ $t('data_fill.task.task_finish_in_suffix') }}</span>
              </div>
            </template>
          </el-form-item>

          <template v-if="rateType === 'simple_repeat'">
            <el-form-item
              :label="$t('emailtask.start_time')"
              prop="startTime"
            >
              <el-date-picker
                v-model="form.startTime"
                :clearable="false"
                class="de-time-range"
                type="datetime"
                :picker-options="startPickerOptions"
                :placeholder="
                  $t('commons.please_select') + $t('emailtask.start_time')
                "
              />
            </el-form-item>

            <el-form-item
              :label="$t('emailtask.end_time')"
              prop="endTime"
            >
              <el-date-picker
                v-model="form.endTime"
                class="de-time-range"
                type="datetime"
                :clearable="false"
                :picker-options="startPickerOptions"
                popper-class="link-date-picker-class"
                :placeholder="$t('commons.please_select') + $t('emailtask.end_time')"
                default-time="23:59:59"
              />
            </el-form-item>
          </template>
        </el-form>
      </div>
    </el-main>
    <el-footer
      class="de-footer"
    >
      <el-button @click="reset">{{ $t("commons.cancel") }}</el-button>
      <el-button
        type="primary"
        @click="save"
      >{{ $t("commons.confirm") }}
      </el-button>
    </el-footer>
  </el-container>
</template>

<script>
import { timeType, weekType, monthType, simpleTimeType } from './options'
import { roleGrid, userListsWithOutPage } from '@/api/system/user'
import { loadTable } from '@/api/system/dept'
import { saveFormTasks, searchFormTasks } from '@/views/dataFilling/form/dataFilling'
import { cloneDeep } from 'lodash-es'
export default {
  name: 'TaskEmailForm',
  props: {
    id: {
      type: String,
      required: false
    },
    formId: {
      type: String,
      required: true
    },
    showDrawer: {
      type: Boolean,
      required: true
    }
  },
  data() {
    return {
      loading: false,
      showTable: false,
      showMedia: false,
      formType: 'add',
      rateType: 'single_task',
      timeType,
      weekType,
      monthType,
      simpleTimeType,
      timeTypeValue: 1,
      weekTypeValue: 1,
      monthTypeValue: 1,
      timePicker: new Date(),
      roleOptions: [],
      orgTreeData: [],
      sortValueBy: 'ORDER_SELECTED',
      form: {
        name: '',
        rateType: -1,
        rateVal: undefined,
        publishStartTime: undefined,
        publishEndTime: undefined,
        publishRangeTimeType: 0,
        publishRangeTime: 1,
        userValues: [],
        roleList: [],
        orgList: []
      },
      rule: {
        name: [
          {
            required: true,
            trigger: 'blur',
            message: this.$t('commons.cannot_be_null')
          },
          {
            min: 1,
            max: 50,
            message: this.$t('commons.input_limit', [1, 50]),
            trigger: 'blur'
          }
        ],

        userValues: [
          {
            required: false,
            trigger: ['blur', 'change'],
            validator: this.validateReci
          }
        ],

        rateType: [
          {
            required: true,
            trigger: 'blur',
            message: this.$t('commons.cannot_be_null')
          }
        ],
        rateVal: [
          {
            required: true,
            trigger: 'blur',
            message: this.$t('commons.cannot_be_null')
          }
        ],
        startTime: [
          {
            required: true,
            trigger: 'blur',
            message: this.$t('commons.cannot_be_null')
          },
          {
            required: true,
            validator: this.validateMin,
            trigger: 'blur'
          }
        ],
        endTime: [
          {
            required: true,
            trigger: 'blur',
            message: this.$t('commons.cannot_be_null')
          },
          {
            required: true,
            validator: this.validateMax,
            trigger: 'blur'
          }
        ],
        publishStartTime: [
          {
            required: true,
            trigger: 'blur',
            message: this.$t('commons.cannot_be_null')
          },
          {
            required: true,
            validator: this.validatePublishMin,
            trigger: 'blur'
          }
        ],
        publishEndTime: [
          {
            required: true,
            trigger: 'blur',
            message: this.$t('commons.cannot_be_null')
          },
          {
            required: true,
            validator: this.validatePublishMax,
            trigger: 'blur'
          }
        ],
        roleList: [
          {
            required: false,
            trigger: ['blur', 'change'],
            validator: this.validateReci
          }
        ],
        orgList: [
          {
            required: false,
            trigger: ['blur', 'change'],
            validator: this.validateReci
          }
        ]
      },
      roles: [],
      originName: null,
      panelData: [],
      users: [],
      radio: 'default',

      rateTypeList: [
        {
          label: 'emailtask.single_task',
          value: 'single_task'
        },
        {
          label: 'emailtask.simple_repeat',
          value: 'simple_repeat'
        }
      ],

      format: '',
      htmlContent: '',
      previewHtml: '',
      previewDialog: false,
      startPickerOptions: {
        disabledDate: (time) => {
          return time.getTime() < Date.now() - 8.64e7
        }
      },
      sourceForm: {},
      panelLoadFinish: false,
      userLoadFinish: false,
      viewLoaded: false,
      showEditor: false
    }
  },

  computed: {},
  watch: {

  },
  created() {
    this.loadRoles()
    this.loadOrgs()
    this.loadUsers()
  },
  mounted() {
    if (this.id) {
      searchFormTasks(this.formId, { id: this.id }, 1, 10).then(res => {
        if (res.data) {
          const _data = res.data.listObject[0]

          this.form = {
            name: _data.name,
            rateType: _data.rateType,
            rateVal: _data.rateVal,
            publishStartTime: _data.publishStartTime ? new Date(_data.publishStartTime) : undefined,
            publishEndTime: _data.publishEndTime ? new Date(_data.publishEndTime) : undefined,
            startTime: _data.startTime ? new Date(_data.startTime) : undefined,
            endTime: _data.endTime ? new Date(_data.endTime) : undefined,
            publishRangeTimeType: _data.publishRangeTimeType ? _data.publishRangeTimeType : 0,
            publishRangeTime: _data.publishRangeTime ? _data.publishRangeTime : 1,
            userValues: _data.reciUsers ? _data.reciUsers.split(',') : [],
            roleList: _data.roleList ? _data.roleList.split(',') : [],
            orgList: _data.orgList ? _data.orgList.split(',') : []
          }

          this.sourceForm = cloneDeep(this.form)

          if (_data.rateType === -1) {
            this.rateType = 'single_task'
          } else {
            this.rateType = 'simple_repeat'
            this.timePicker = new Date(_data.rateVal)
            this.timeTypeValue = _data.rateType

            if (this.timeTypeValue === 1) {
              this.weekTypeValue = this.timePicker.getDate()
            }

            if (this.timeTypeValue === 2) {
              this.monthTypeValue = this.timePicker.getDate()
            }
          }
        }
      })
    }
  },
  destroyed() {
  },
  methods: {

    beforeClearAll(val) {
      this.beforeClearAll()
      return true
    },
    onRemarkChange(value) {
      this.htmlContent = value
    },

    reset() {
      this.$emit('update:showDrawer', false)
    },

    save() {
      this.loading = true

      const request = this.form

      request.rateType = -1

      if (this.rateType !== 'single_task') {
        request.rateType = this.timeTypeValue
        let dayTime = '01'
        if (this.timeTypeValue === 1) {
          dayTime = `0${this.weekTypeValue}`
        }

        if (this.timeTypeValue === 2) {
          dayTime =
            this.monthTypeValue < 10
              ? `0${this.monthTypeValue}`
              : this.monthTypeValue
        }
        const hms = this.timePicker
          ? new Date(this.timePicker).format('hh:mm:ss')
          : '00:00:01'
        request.rateVal = `2024-03-${dayTime} ${hms}`

        delete request.publishStartTime
        delete request.publishEndTime
      } else {
        delete request.publishRangeTimeType
        delete request.publishRangeTime
      }
      const param = {
        request: JSON.parse(JSON.stringify(request))
      }

      this.$refs.taskForm.validate((valid) => {
        if (valid) {
          param.request.reciUsers = null

          if (param.request.userValues) {
            const valueResult = this.resolveUserValue(param.request.userValues)

            if (valueResult && valueResult.reciUsers) {
              param.request.reciUsers = Array.isArray(valueResult.reciUsers) ? valueResult.reciUsers.join() : valueResult.reciUsers
            }
          }

          if (param.request.roleList) {
            param.request.roleList = Array.isArray(param.request.roleList) ? param.request.roleList.join() : param.request.roleList
          }

          if (param.request.orgList) {
            param.request.orgList = Array.isArray(param.request.orgList) ? param.request.orgList.join() : param.request.orgList
          }

          delete param.request.userValues

          if (this.id) {
            param.request.id = this.id
          }

          saveFormTasks(this.formId, param.request).then(res => {
            this.$emit('save-success')
          }).finally(() => {
            this.loading = false
          })
        } else {
          this.loading = false
          return false
        }
      })
    },

    buildUserValue(recipients, reciUsers) {
      const userValues = []
      reciUsers.forEach(user => {
        !userValues.includes(user) && userValues.push(user)
      })

      recipients.forEach(recipient => {
        const tempUser = this.userWithEmail(recipient)
        if (tempUser && tempUser.username) {
          !userValues.includes(tempUser.username) && userValues.push(tempUser.username)
        } else {
          !userValues.includes(recipient) && userValues.push(recipient)
        }
      })
      return userValues
    },
    userWithRecipient(recipient) {
      for (let index = 0; index < this.users.length; index++) {
        const element = this.users[index]
        if (recipient === element.username) {
          return element
        }
      }
      return null
    },
    userWithEmail(email) {
      for (let index = 0; index < this.users.length; index++) {
        const element = this.users[index]
        if (email === element.email) {
          return element
        }
      }
      return null
    },
    resolveUserValue(userValues) {
      const result = {}
      userValues.forEach(value => {
        const user = this.userWithRecipient(value)
        if (user) {
          if (!result.reciUsers) {
            result.reciUsers = []
          }
          result.reciUsers.push(value)
        } else {
          if (!result.recipients) {
            result.recipients = []
          }
          result.recipients.push(value)
        }
      })
      return result
    },

    loadUsers(callBack) {
      userListsWithOutPage({}).then((res) => {
        this.userLoadFinish = true
        this.users = res.data
        callBack && callBack()
      })
    },
    loadRoles() {
      roleGrid(1, 0, {}).then((res) => {
        this.roleOptions = res.data.listObject.map(item => {
          return {
            id: item.roleId.toString(),
            name: item.name
          }
        })
      })
    },
    convertOrgNode(data) {
      return data.map(item => {
        const tempItem = {
          id: item.deptId,
          label: item.name,
          pid: item.pid
        }
        if (item.hasChildren) {
          tempItem.children = null
        }
        return tempItem
      })
    },
    loadOrgs() {
      loadTable({ pid: null }).then((res) => {
        const data = this.convertOrgNode(res.data)
        this.orgTreeData = this.buildTree(data)
      })
    },
    buildTree(arrs) {
      const idMapping = arrs.reduce((acc, el, i) => {
        acc[el.id] = i
        return acc
      }, {})
      const roots = []
      arrs.forEach(el => {
        // 判断根节点
        if (el.pid === null || el.pid === 0) {
          roots.push(el)
          return
        }
        // 用映射表找到父元素
        const parentEl = arrs[idMapping[el.pid]]
        // 把当前元素添加到父元素的`children`数组中
        parentEl.children = [...(parentEl.children || []), el]
      })
      return roots
    },

    setTitle(val) {
      this.form.title = val
    },

    validateMin(rule, value, callback) {
      if (this.id && value?.getTime() === this.sourceForm.startTime?.getTime()) { return callback() }
      if (!value) return callback()
      const val = new Date(value)
      if (val.getTime() < Date.now() - 60 * 1000) {
        return callback(new Error('不能小于当前时间'))
      }
      if (this.form.endTime) {
        if (val.getTime() >= new Date(this.form.endTime).getTime()) {
          return callback(new Error('不能大于结束时间'))
        }
      }
      return callback()
    },
    validateMax(rule, value, callback) {
      if (this.id && value?.getTime() === this.sourceForm.endTime?.getTime()) { return callback() }
      if (!value) return callback()
      const val = new Date(value)
      if (val.getTime() < Date.now()) {
        return callback(new Error('不能小于当前时间'))
      }

      if (this.form.startTime) {
        if (val.getTime() <= new Date(this.form.startTime).getTime()) {
          return callback(new Error('不能小于开始时间'))
        }
      }
      return callback()
    },
    validatePublishMin(rule, value, callback) {
      if (this.id && value?.getTime() === this.sourceForm?.publishStartTime?.getTime()) { return callback() }
      if (!value) return callback()
      const val = new Date(value)
      if (val.getTime() < Date.now() - 60 * 1000) {
        return callback(new Error('不能小于当前时间'))
      }
      if (this.form.publishEndTime) {
        if (val.getTime() >= new Date(this.form.publishEndTime).getTime()) {
          return callback(new Error('不能大于结束时间'))
        }
      }
      return callback()
    },
    validatePublishMax(rule, value, callback) {
      if (this.id && value?.getTime() === this.sourceForm.publishEndTime?.getTime()) { return callback() }
      if (!value) return callback()
      const val = new Date(value)
      if (val.getTime() < Date.now()) {
        return callback(new Error('不能小于当前时间'))
      }

      if (this.form.publishStartTime) {
        if (val.getTime() <= new Date(this.form.publishStartTime).getTime()) {
          return callback(new Error('不能小于开始时间'))
        }
      }
      return callback()
    },
    validateWaitTime(rule, value, callback) {
      if (value === null || typeof value === 'undefined') {
        return callback(new Error(this.$t('commons.cannot_be_null')))
      }
      const reg = /^([0-9]{1,2}|30)$/
      if (!reg.test(value)) {
        return callback(new Error(this.$t('emailtask.wat_time_limit')))
      }
      callback()
    },
    validateReci(rule, value, callback) {
      if ((!this.form.userValues || this.form.userValues.length === 0) &&
        (!this.form.roleList || this.form.roleList.length === 0) &&
        (!this.form.orgList || this.form.orgList.length === 0)) {
        return callback(new Error('收件人、角色、组织至少选择一项'))
      }
      return callback()
    },
    onPublishRangeTimeChange(form) {
      if (!form.publishRangeTime) {
        this.$nextTick(() => {
          form.publishRangeTime = 1
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.inline {
  display: flex;

  .el-form-item {
    margin-right: 30px !important;
  }
}
.email-task-tree >>> .vue-treeselect__control {
  height: 28px !important;
}

.email-task-tree >>> .vue-treeselect__menu-container {
  z-index: 2000 !important;
}

.row-rules {
  display: flex;
  align-items: center;
  position: relative;
  font-family: PingFang SC;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  padding-left: 10px;
  margin: 16px 0;

  &::before {
    content: "";
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    height: 14px;
    width: 2px;
    background: #3370ff;
  }
}
.marTop0 {
  margin-top: 0;
}
</style>

<style lang="scss">
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
}

.new-task-form {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding-bottom: 24px;
  & > :nth-child(1) {
    width: 600px;
  }

  textarea {
    height: 92px;
    width: 100%;
  }

  .el-textarea .el-input__count {
    font-family: PingFang SC;
    font-size: 12px;
    font-weight: 400;
    color: #8f959e;
    line-height: 20px;
    padding: 0 12px 6px 0;
    right: 1px;
    bottom: 1px;
    border-bottom-right-radius: 4px;
  }
}

.de-time-range {
  width: 100% !important;
}

.rate-type-time {
  width: 100%;
  border-radius: 4px;
  padding: 20px;
  background-color: var(--MainBG, #f5f6f7);
  display: flex;
  align-items: center;

  .w140 {
    width: 140px !important;
    margin-right: 8px;
  }

  .el-input__inner {
    background: var(--ContentBG, #ffffff) !important;
  }

  .w160 {
    width: 160px !important;
  }

  .prefix {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    color: var(--deTextPrimary, #1f2329);
    margin-right: 8px;
  }

  .tail {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    color: var(--deTextPrimary, #1f2329);
  }
}
.second-row {
  padding-top: 0;
}

.de-foot-layout {
  position: absolute;
  width: calc(100% - 48px);
  height: 80px;
  bottom: 0;
  right: 24px;
  background-color: var(--MainBG, #fff);
  box-shadow: 0px -2px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  .cont {
    width: 600px;
    text-align: right;
  }
}
</style>
