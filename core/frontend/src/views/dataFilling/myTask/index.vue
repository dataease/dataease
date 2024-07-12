<script>
import DeContainer from '@/components/dataease/DeContainer.vue'
import DeMainContainer from '@/components/dataease/DeMainContainer.vue'
import DeAsideContainer from '@/components/dataease/DeAsideContainer.vue'
import GridTable from '@/components/gridTable/index.vue'
import { getForm, searchFormMyTasks, searchTable } from '@/views/dataFilling/form/dataFilling'
import EditFormData from '@/views/dataFilling/form/EditFormData.vue'
import { forIn, includes, split, filter, forEach } from 'lodash-es'
import { hasPermission } from '@/directive/Permission'

export default {
  name: 'MyDataFillingJobs',
  components: { EditFormData, GridTable, DeAsideContainer, DeMainContainer, DeContainer },
  data() {
    return {
      activeName: 'my-tasks',
      types: [
        { key: 'todo', name: this.$t('data_fill.task.todo') },
        { key: 'finished', name: this.$t('data_fill.task.finished') },
        { key: 'expired', name: this.$t('data_fill.task.expired') }
      ],
      currentKey: 'todo',
      showDrawer: false,
      selectedDataId: undefined,
      selectedData: undefined,
      selectedKeyName: undefined,
      selectedTaskId: undefined,
      selectedFormTitle: '',
      selectedFormId: undefined,
      forms: [],
      createTitle: '',
      drawerReadonly: false,
      myTaskName: '',
      finishedTaskName: '',
      expiredTaskName: '',
      myTasks: [],
      finishedTasks: [],
      expiredTasks: [],
      myTaskPaginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      finishedTaskPaginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      expiredTaskPaginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    }
  },
  watch: {
    currentKey: {
      handler(newVal, oldVal) {
        if (newVal !== oldVal) {
          this.myTaskName = ''
          this.finishedTaskName = ''
          this.expiredTaskName = ''
        }
        if (newVal === 'finished') {
          this.searchTableFinishedTaskData()
        } else if (newVal === 'expired') {
          this.searchTableExpiredTaskData()
        } else {
          this.searchTableMyTaskData()
        }
      }
    }
  },
  mounted() {
    this.currentKey = 'todo'
    this.searchTableMyTaskData()
  },
  methods: {
    hasPermission(binding) {
      return hasPermission(binding)
    },
    changeKey(key) {
      this.currentKey = key
    },
    entryKey(type) {
      if (type === 'finished') {
        this.$refs.search2.focus()
        this.$refs.search2.blur()
      } else if (type === 'expired') {
        this.$refs.search3.focus()
        this.$refs.search3.blur()
      } else {
        this.$refs.search1.focus()
        this.$refs.search1.blur()
      }
    },
    searchTableMyTaskData() {
      searchFormMyTasks({ taskName: this.myTaskName }, this.myTaskPaginationConfig.currentPage, this.myTaskPaginationConfig.pageSize, 'todo').then(res => {
        if (res.data) {
          this.myTaskPaginationConfig.total = res.data.itemCount
          this.myTasks = res.data.listObject
        }
      })
    },
    searchTableFinishedTaskData() {
      searchFormMyTasks({ taskName: this.finishedTaskName }, this.finishedTaskPaginationConfig.currentPage, this.finishedTaskPaginationConfig.pageSize, 'finished').then(res => {
        if (res.data) {
          this.finishedTaskPaginationConfig.total = res.data.itemCount
          this.finishedTasks = res.data.listObject
        }
      })
    },
    searchTableExpiredTaskData() {
      searchFormMyTasks({ taskName: this.expiredTaskName }, this.expiredTaskPaginationConfig.currentPage, this.expiredTaskPaginationConfig.pageSize, 'expired').then(res => {
        if (res.data) {
          this.expiredTaskPaginationConfig.total = res.data.itemCount
          this.expiredTasks = res.data.listObject
        }
      })
    },
    myTaskHandleSizeChange(pageSize) {
      this.myTaskPaginationConfig.currentPage = 1
      this.myTaskPaginationConfig.pageSize = pageSize
      this.searchTableMyTaskData()
    },
    myTaskHandleCurrentChange(currentPage) {
      this.myTaskPaginationConfig.currentPage = currentPage
      this.searchTableMyTaskData()
    },
    finishedTaskHandleSizeChange(pageSize) {
      this.finishedTaskPaginationConfig.currentPage = 1
      this.finishedTaskPaginationConfig.pageSize = pageSize
      this.searchTableFinishedTaskData()
    },
    finishedTaskHandleCurrentChange(currentPage) {
      this.finishedTaskPaginationConfig.currentPage = currentPage
      this.searchTableFinishedTaskData()
    },
    expiredTaskHandleSizeChange(pageSize) {
      this.expiredTaskPaginationConfig.currentPage = 1
      this.expiredTaskPaginationConfig.pageSize = pageSize
      this.searchTableExpiredTaskData()
    },
    expiredTaskHandleCurrentChange(currentPage) {
      this.expiredTaskPaginationConfig.currentPage = currentPage
      this.searchTableExpiredTaskData()
    },
    getRestTime(time) {
      if (!time) {
        return this.$t('data_fill.task.no_time_limit')
      }
      const _time = new Date(time).getTime()
      const current = new Date().getTime()

      if (_time < current) {
        return this.$t('data_fill.task.expired')
      }
      const dateDiff = _time - current

      const dayDiff = Math.floor(dateDiff / (24 * 3600 * 1000))
      const monthDiff = Math.floor(dayDiff / 30)
      const yearDiff = Math.floor(monthDiff / 12)

      const leave1 = dateDiff % (24 * 3600 * 1000) // 计算天数后剩余的毫秒数
      const hours = Math.floor(leave1 / (3600 * 1000))// 计算出小时数
      // 计算相差分钟数
      const leave2 = leave1 % (3600 * 1000) // 计算小时数后剩余的毫秒数
      const minutes = Math.floor(leave2 / (60 * 1000))// 计算相差分钟数
      // 计算相差秒数
      const leave3 = leave2 % (60 * 1000) // 计算分钟数后剩余的毫秒数
      const seconds = Math.round(leave3 / 1000)

      if (yearDiff > 0 || monthDiff > 0) {
        const yearStr = yearDiff > 0 ? yearDiff + this.$t('cron.year') : ''
        const monthStr = monthDiff > 0 ? monthDiff + this.$t('cron.month') : ''
        return yearStr + monthStr
      }

      const dayStr = dayDiff > 0 ? dayDiff + this.$t('cron.day') : ''
      const hourStr = hours > 0 ? hours + this.$t('cron.hour') : ''

      if (dayDiff > 0) {
        return dayStr + hourStr
      }

      const minuteStr = minutes > 0 ? minutes + this.$t('cron.minute') : ''

      if (hours > 0) {
        return hourStr + minuteStr
      }

      const secondsStr = seconds > 0 ? seconds + this.$t('cron.second') : ''

      return minuteStr + secondsStr
    },
    editForm(row, readonly) {
      if (readonly) {
        this.drawerReadonly = true
      } else {
        this.drawerReadonly = false
      }

      this.createTitle = this.$t('data_fill.data_fill')
      this.forms = []

      this.selectedFormId = row.formId
      this.selectedTaskId = row.id

      this.selectedDataId = row.valueId
      this.selectedData = [{}]
      this.selectedKeyName = undefined
      if (row.valueId) {
        getForm(row.formId).then(res => {
          this.selectedFormTitle = res.data.name
          this.forms = JSON.parse(res.data.forms)

          const dateFormatColumns = []
          forEach(this.forms, f => {
            if (f.type === 'dateRange') {
              dateFormatColumns.push(f.settings?.mapping?.columnName1)
              dateFormatColumns.push(f.settings?.mapping?.columnName2)
            } else {
              if (f.type === 'date') {
                dateFormatColumns.push(f.settings?.mapping?.columnName)
              }
            }
          })

          searchTable(row.formId, {
            primaryKeyValueList: split(row.valueId, ','),
            currentPage: 1,
            pageSize: 0
          }).then(res => {
            if (res.data) {
              if (res.data.data.length === 0) {
                this.$message({
                  message: this.$t('data_fill.data.id_is') + row.valueId + this.$t('data_fill.data.data_not_found'),
                  type: 'warning',
                  showClose: true
                })
                return
              }
              this.selectedKeyName = res.data.key
              const _list = []
              for (let i = 0; i < res.data.data.length; i++) {
                const obj = {}
                forIn(res.data.data[i].data, (value, key) => {
                  if (includes(dateFormatColumns, key)) {
                    if (value) {
                      obj[key] = new Date(value)
                    } else {
                      obj[key] = undefined
                    }
                  } else {
                    obj[key] = value === null ? undefined : value
                  }
                })
                _list.push(obj)
              }

              this.selectedData = _list

              this.showDrawer = true
            }
          })
        })
      } else {
        getForm(row.formId).then(res => {
          this.selectedFormTitle = res.data.name
          this.forms = JSON.parse(res.data.forms)

          this.showDrawer = true
        })
      }
    },

    showForm(row) {
      this.editForm(row, true)
    },
    closeForm() {
      this.showDrawer = false
    },
    onSaveSuccess() {
      this.closeForm()

      if (this.currentKey === 'finished') {
        this.searchTableFinishedTaskData()
      } else if (this.currentKey === 'expired') {
        this.searchTableExpiredTaskData()
      } else {
        this.searchTableMyTaskData()
      }
    },
    tabClick() {
      if (this.activeName === 'forms') {
        this.$router.push('/data-filling/forms')
      }
    }

  }
}
</script>

<template>
  <de-container
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
    style="background-color: #f7f8fa"
  >

    <de-aside-container type="data-filling">
      <el-tabs
        v-model="activeName"
        class="tab-panel"
        @tab-click="tabClick"
      >
        <el-tab-pane
          name="my-tasks"
        >
          <span slot="label">{{ $t('data_fill.my_job') }}</span>

          <div style="padding:0 24px 16px">
            <div
              v-for="t in types"
              :key="t.key"
              class="type-node"
              :class="{'is-focus': t.key === currentKey}"
              @click="changeKey(t.key)"
            >
              {{ t.name }}
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane
          v-if="hasPermission(['data-filling-form:manage'])"
          name="forms"
        >
          <span
            slot="label"
          >
            {{ $t('data_fill.form_manage') }}
          </span>
        </el-tab-pane>
      </el-tabs>

    </de-aside-container>
    <de-main-container
      v-if="currentKey === 'todo'"
      style="display: flex; flex-direction: column"
    >
      <div class="view-table">
        <div style="margin-bottom: 12px; height: 32px;">
          <el-row>
            <el-col
              :span="8"
              :offset="16"
            >
              <el-input
                ref="search1"
                v-model="myTaskName"
                :placeholder="$t('data_fill.task.task_name')"
                prefix-icon="el-icon-search"
                class="name-email-search"
                size="small"
                clearable
                @blur="searchTableMyTaskData"
                @clear="entryKey('todo')"
                @keyup.enter.native="entryKey('todo')"
              />
            </el-col>
          </el-row>
        </div>
        <div style="flex: 1">
          <grid-table
            v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
            style="width: 100%; height: 100%"
            stripe
            :table-data="myTasks"
            :columns="[]"
            current-row-key="id"
            :pagination="myTaskPaginationConfig"
            @size-change="myTaskHandleSizeChange"
            @current-change="myTaskHandleCurrentChange"
          >
            <el-table-column
              key="taskName"
              prop="taskName"
              :label="$t('data_fill.task.task_name')"
            />
            <el-table-column
              key="restTime"
              prop="restTime"
              :label="$t('data_fill.task.task_remain_time')"
            >
              <template slot-scope="scope">
                {{ getRestTime(scope.row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column
              key="endTime"
              prop="endTime"
              :label="$t('data_fill.task.task_end_time')"
            >
              <template slot-scope="scope">
                {{ scope.row.endTime ? new Date(scope.row.endTime).format("yyyy-MM-dd hh:mm:ss") : '-' }}
              </template>
            </el-table-column>
            <el-table-column
              key="creatorName"
              prop="creatorName"
              :label="$t('data_fill.task.task_sender')"
            />
            <el-table-column
              key="startTime"
              prop="startTime"
              :label="$t('data_fill.task.task_distribute_time')"
            >
              <template slot-scope="scope">
                {{ new Date(scope.row.startTime).format("yyyy-MM-dd hh:mm:ss") }}
              </template>
            </el-table-column>
            <el-table-column
              key="id"
              :label="$t('data_fill.form.operation')"
              prop="id"
            >
              <template slot-scope="scope">
                <el-button
                  type="text"
                  @click="editForm(scope.row)"
                >
                  {{ $t('data_fill.task.start_filling') }}
                </el-button>
              </template>
            </el-table-column>
          </grid-table>
        </div>
      </div>
    </de-main-container>
    <de-main-container
      v-if="currentKey === 'finished'"
      style="display: flex; flex-direction: column"
    >
      <div class="view-table">
        <div style="margin-bottom: 12px; height: 32px;">
          <el-row>
            <el-col
              :span="8"
              :offset="16"
            >
              <el-input
                ref="search2"
                v-model="finishedTaskName"
                :placeholder="$t('data_fill.task.task_name')"
                prefix-icon="el-icon-search"
                class="name-email-search"
                size="small"
                clearable
                @blur="searchTableFinishedTaskData"
                @clear="entryKey('finished')"
                @keyup.enter.native="entryKey('finished')"
              />
            </el-col>
          </el-row>
        </div>
        <div style="flex: 1">
          <grid-table
            v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
            style="width: 100%; height: 100%"
            stripe
            :table-data="finishedTasks"
            :columns="[]"
            current-row-key="id"
            :pagination="finishedTaskPaginationConfig"
            @size-change="finishedTaskHandleSizeChange"
            @current-change="finishedTaskHandleCurrentChange"
          >
            <el-table-column
              key="taskName"
              prop="taskName"
              :label="$t('data_fill.task.task_name')"
            />
            <el-table-column
              key="finishTime"
              prop="finishTime"
              :label="$t('data_fill.task.task_finished_time')"
            >
              <template slot-scope="scope">
                {{ new Date(scope.row.finishTime).format("yyyy-MM-dd hh:mm:ss") }}
              </template>
            </el-table-column>
            <el-table-column
              key="creatorName"
              prop="creatorName"
              :label="$t('data_fill.task.task_sender')"
            />
            <el-table-column
              key="startTime"
              prop="startTime"
              :label="$t('data_fill.task.task_distribute_time')"
            >
              <template slot-scope="scope">
                {{ new Date(scope.row.startTime).format("yyyy-MM-dd hh:mm:ss") }}
              </template>
            </el-table-column>
            <el-table-column
              key="id"
              :label="$t('data_fill.form.operation')"
              prop="id"
            >
              <template slot-scope="scope">
                <el-button
                  type="text"
                  @click="editForm(scope.row)"
                >
                  {{ $t('data_fill.task.edit_data') }}
                </el-button>
                <el-button
                  type="text"
                  @click="showForm(scope.row)"
                >
                  {{ $t('data_fill.task.show_data') }}
                </el-button>
              </template>
            </el-table-column>
          </grid-table>
        </div>
      </div>
    </de-main-container>
    <de-main-container
      v-if="currentKey === 'expired'"
      style="display: flex; flex-direction: column"
    >
      <div class="view-table">
        <div style="margin-bottom: 12px; height: 32px;">
          <el-row>
            <el-col
              :span="8"
              :offset="16"
            >
              <el-input
                ref="search3"
                v-model="expiredTaskName"
                :placeholder="$t('data_fill.task.task_name')"
                prefix-icon="el-icon-search"
                class="name-email-search"
                size="small"
                clearable
                @blur="searchTableExpiredTaskData"
                @clear="entryKey('expired')"
                @keyup.enter.native="entryKey('expired')"
              />
            </el-col>
          </el-row>
        </div>
        <div style="flex: 1">
          <grid-table
            v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
            style="width: 100%; height: 100%"
            stripe
            :table-data="expiredTasks"
            :columns="[]"
            current-row-key="id"
            :pagination="expiredTaskPaginationConfig"
            @size-change="expiredTaskHandleSizeChange"
            @current-change="expiredTaskHandleCurrentChange"
          >
            <el-table-column
              key="taskName"
              prop="taskName"
              :label="$t('data_fill.task.task_name')"
            />
            <el-table-column
              key="endTime"
              prop="endTime"
              :label="$t('data_fill.task.task_expiration_time')"
            >
              <template slot-scope="scope">
                {{ scope.row.endTime ? new Date(scope.row.endTime).format("yyyy-MM-dd hh:mm:ss") : '-' }}
              </template>
            </el-table-column>
            <el-table-column
              key="creatorName"
              prop="creatorName"
              :label="$t('data_fill.task.task_sender')"
            />
            <el-table-column
              key="startTime"
              prop="startTime"
              :label="$t('data_fill.task.task_distribute_time')"
            >
              <template slot-scope="scope">
                {{ new Date(scope.row.startTime).format("yyyy-MM-dd hh:mm:ss") }}
              </template>
            </el-table-column>

          </grid-table>
        </div>
      </div>
    </de-main-container>

    <el-drawer
      :visible.sync="showDrawer"
      direction="btt"
      size="100%"
      append-to-body
      :with-header="false"
    >
      <edit-form-data
        v-if="showDrawer"
        :id="selectedDataId"
        :data-list.sync="selectedData"
        :key-name.sync="selectedKeyName"
        :user-task-id="selectedTaskId"
        :form-title.sync="selectedFormTitle"
        :form-id="selectedFormId"
        :forms.sync="forms"
        :title.sync="createTitle"
        :show-drawer.sync="showDrawer"
        :readonly.sync="drawerReadonly"
        @save-success="onSaveSuccess"
      />
    </el-drawer>

  </de-container>

</template>

<style  lang="scss" scoped>
.ms-aside-container {
  height: calc(100vh - 56px);
  padding: 0px;
  min-width: 260px;
  max-width: 460px;
}

.view-table {
  background: rgba(255, 255, 255, 1);
  padding: 24px 24px 48px;
  height: 100%;
  overflow-y: hidden;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;

}

.tab-panel {
  height: 100%;
  overflow-y: auto;
}

.tab-panel ::v-deep .el-tabs__nav-wrap {
  padding: 0 10px;
}

.tab-panel ::v-deep .el-tabs__nav-wrap::after {
  height: 1px;
}

.tab-panel ::v-deep .el-tabs__item {
  /* width: 10px; */
  padding: 0 10px;
}

::v-deep.ms-aside-container{
  padding: 0
}

.type-node {
  white-space: nowrap;
  outline: none;

  height: 40px;
  border-radius: 4px;

  display: flex;
  align-items: center;
  cursor: pointer;

  font-size: 14px;
  padding: 0 14px;

}

.type-node.is-focus {
  background-color: var(--deWhiteHover, #e0eaff);
  color: var(--primary, #3370ff);
}

.type-node:hover{
  background: rgba(31, 35, 41, 0.1);
}

</style>
