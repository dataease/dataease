<script setup lang="ts">
import { ref, reactive, shallowRef, nextTick } from 'vue'
import { GridTable } from '@/components/grid-table'
import { useI18n } from '@/hooks/web/useI18n'
import { loadUserFillingTask } from '@/views/menu/data/data-filling/fill/fill_api'
import DfDataListForm from '@/views/menu/data/data-filling/fill/DfDataListForm.vue'

const { t } = useI18n()
const activeName = ref('todo')
const paginationConfig = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const tabArray = [
  { label: t('v_query.to_be_filled'), name: 'todo' },
  { label: t('data_fill.form.status_1'), name: 'finished' }
]

const formatter = (_, __, val) => {
  return val ? new Date(val).toLocaleString() : '-'
}

const rowDataFormRef = ref()

const startFilling = data => {
  rowDataFormRef.value?.init(data.id, data.formId, true)
}

const handleTabClick = () => {
  paginationConfig.currentPage = 1
  nextTick(() => {
    getTableData()
  })
}

const getTableData = () => {
  loadUserFillingTask(
    {
      type: activeName.value,
      taskName: ''
    },
    paginationConfig.currentPage,
    paginationConfig.pageSize
  ).then(res => {
    const { total = 0, records = [] } = res?.data || {}
    tableData.value = records
    paginationConfig.total = total
  })
}

getTableData()

const tableData = shallowRef([])
const handleSizeChange = pageSize => {
  paginationConfig.currentPage = 1
  paginationConfig.pageSize = pageSize
  getTableData()
}
const handleCurrentChange = currentPage => {
  paginationConfig.currentPage = currentPage
  getTableData()
}
</script>
<template>
  <div class="msg-fill_form">
    <p class="router-title">{{ t('data_fill.fill_in_the_task') }}</p>
    <el-tabs @tab-click="handleTabClick" v-model="activeName">
      <el-tab-pane
        v-for="item in tabArray"
        :key="item.name"
        :label="item.label"
        :name="item.name"
      />
    </el-tabs>
    <div class="sys-setting-p">
      <div class="container-sys-param">
        <grid-table
          :table-data="tableData"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :pagination="paginationConfig"
        >
          <el-table-column
            key="taskName"
            prop="taskName"
            show-overflow-tooltip
            :label="t('data_fill.fill_in_the_task')"
          />
          <el-table-column
            key="finishTime"
            :formatter="formatter"
            v-if="activeName === 'finished'"
            prop="finishTime"
            :label="t('data_fill.task.task_finished_time')"
          />
          <el-table-column
            :formatter="formatter"
            key="endTime"
            prop="endTime"
            show-overflow-tooltip
            :label="t('data_fill.task.task_end_time')"
          />
          <el-table-column
            key="__operation"
            v-if="activeName === 'todo'"
            :label="t('commons.operating')"
            fixed="right"
            width="100"
          >
            <template #default="scope">
              <el-button text @click="startFilling(scope.row)">
                {{ t('data_fill.task.start_filling') }}
              </el-button>
            </template>
          </el-table-column>
        </grid-table>
      </div>
    </div>
  </div>
  <DfDataListForm ref="rowDataFormRef" @finish="handleTabClick" />
</template>
<style lang="less" scoped>
.msg-fill_form {
  padding: 16px 24px 24px;
  height: 100vh;
  .router-title {
    color: #1f2329;
    font-feature-settings: 'clig' off, 'liga' off;
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 20px;
    font-style: normal;
    font-weight: 500;
    line-height: 28px;
  }
  .sys-setting-p {
    width: 100%;
    height: calc(100vh - 176px);
    box-sizing: border-box;
    margin-top: 12px;
  }

  .container-sys-param {
    height: 100%;
    overflow: hidden;
    background: var(--ContentBG, #ffffff);
    border-radius: 4px;
    padding: 24px;
  }
}
</style>
