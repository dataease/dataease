<template>
  <layout-content>
    <complex-table
      :data="data"
      :columns="columns"
      :buttons="buttons"
      :header="header"
      :search-config="searchConfig"
      :pagination-config="paginationConfig"
      @select="select"
      @search="search"
    >
      <template #buttons>
        <fu-table-button icon="el-icon-circle-plus-outline" :label="$t('user.create')" @click="add" />
      </template>

      <el-table-column type="selection" fix />
      <el-table-column prop="username" label="ID" />
      <el-table-column prop="nickName" :label="$t('commons.name')" width="200" />
      <el-table-column prop="gender" label="性别" />

      <el-table-column :show-overflow-tooltip="true" prop="phone" width="100" label="电话" />
      <el-table-column :show-overflow-tooltip="true" width="135" prop="email" :label="$t('commons.email')" />
      <el-table-column :show-overflow-tooltip="true" prop="dept" :label="$t('commons.organization')">
        <template slot-scope="scope">
          <div>{{ scope.row.dept.deptName }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('commons.status')" width="120">
        <template v-slot:default="scope">
          <el-switch v-model="scope.row.enabled" :active-value="1" :inactive-value="0" inactive-color="#DCDFE6" @change="changeSwitch(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" :label="$t('commons.create_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <fu-table-operations :buttons="buttons" label="操作" fix />
    </complex-table>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { userLists } from '@/api/system/user'
import ComplexTable from '@/components/business/complex-table'
import CustomCondition from './CustomCondtion'
// import { GridButton } from '@/components/GridButton'
// import { checkPermission } from '@/utils/permisstion'

const buttonClick = function(row) {
  console.log(this.label + ': ' + row.id)
}

export default {
  name: 'UserManagement',
  components: { ComplexTable, LayoutContent },
  data() {
    return {
      header: '',
      columns: [],
      buttons: [
        {
          label: '编辑', icon: 'el-icon-edit', click: this.edit
        }, {
          label: '执行', icon: 'el-icon-video-play', click: buttonClick
        }, {
          label: '删除', icon: 'el-icon-delete', type: 'danger', click: buttonClick
        }, {
          label: '删除(权限)', icon: 'el-icon-delete', type: 'danger', click: buttonClick
        //   show: checkPermission('editor') // 必须有editor权限才能看到
        }, {
          label: '复制', icon: 'el-icon-document-copy', click: buttonClick
        }, {
          label: '定时任务', icon: 'el-icon-timer', click: buttonClick
        }
      ],
      searchConfig: {
        quickPlaceholder: '按 姓名/邮箱 搜索',
        components: [
          { field: 'name', label: '姓名', component: 'FuComplexInput', defaultOperator: 'eq' },
          { field: 'email', label: 'Email', component: 'FuComplexInput' },
          {
            field: 'status',
            label: '状态',
            component: 'FuComplexSelect',
            options: [
              { label: '运行中', value: 'Running' },
              { label: '成功', value: 'Success' },
              { label: '失败', value: 'Fail' }
            ],
            multiple: true
          },
          { field: 'create_time', label: '创建时间', component: 'FuComplexDateTime' },
          { field: 'user', component: CustomCondition } // 如何自定义搜索控件，看CustomCondition
        ]
      },
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      data: []
    }
  },
  created() {
    this.search()
  },
  methods: {
    select(selection) {
      console.log(selection)
    },
    edit(row) {
      console.log('编辑: ', row)
    },
    add() {

    },
    search(condition) {
      console.log(condition) // demo只查看搜索条件，没有搜索的实现
      const { currentPage, pageSize } = this.paginationConfig
      userLists(currentPage, pageSize, {}).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    }
  }
}
</script>

<style scoped>

</style>
