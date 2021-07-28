<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <complex-table
      :data="data"
      :columns="columns"
      :search-config="searchConfig"
      :pagination-config="paginationConfig"
      @search="search"
    >
      <template #toolbar>
        <!-- <el-button @click="create">{{ $t('plugin.local_install') }}</el-button> -->

        <el-upload
          :action="baseUrl+'api/plugin/upload'"
          :multiple="false"
          :show-file-list="false"
          :file-list="fileList"
          accept=".zip"
          :before-upload="beforeUpload"
          :on-success="uploadSuccess"
          :on-error="uploadFail"
          name="file"
          :headers="headers"
        >
          <el-button size="mini" type="primary" :disabled="uploading">
            <span v-if="!uploading" style="font-size: 12px;">{{ $t('plugin.local_install') }}</span>
            <span v-if="uploading" style="font-size: 12px;"><i class="el-icon-loading" /> {{ $t('dataset.uploading') }}</span>
          </el-button>
        </el-upload>
      </template>

      <el-table-column prop="name" :label="$t('plugin.name')" />
      <el-table-column prop="free" :label="$t('plugin.free')" />
      <el-table-column prop="cost" :label="$t('plugin.cost')" />

      <el-table-column :show-overflow-tooltip="true" prop="descript" :label="$t('plugin.descript')" />
      <el-table-column prop="version" :label="$t('plugin.version')" />
      <el-table-column prop="creator" :label="$t('plugin.creator')" />

      <el-table-column prop="installTime" :label="$t('plugin.install_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.installTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <fu-table-operations :buttons="buttons" label="操作" fix />
    </complex-table>

  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import ComplexTable from '@/components/business/complex-table'

// import { checkPermission } from '@/utils/permission'
import { formatCondition } from '@/utils/index'
import { pluginLists, uninstall } from '@/api/system/plugin'
import { getToken } from '@/utils/auth'
export default {

  components: { ComplexTable, LayoutContent },
  data() {
    return {
      header: '',
      columns: [],
      buttons: [
        // {
        //   label: this.$t('commons.delete'), icon: 'el-icon-delete', type: 'danger', click: this.del,
        //   show: checkPermission(['user:del'])
        // }
      ],
      searchConfig: {
        useQuickSearch: false,
        quickPlaceholder: this.$t('role.search_by_name'),
        components: [
          { field: 'name', label: this.$t('plugin.name'), component: 'FuComplexInput' }

        //   {
        //     field: 'u.enabled',
        //     label: '状态',
        //     component: 'FuComplexSelect',
        //     options: [
        //       { label: '启用', value: '1' },
        //       { label: '禁用', value: '0' }
        //     ],
        //     multiple: false
        //   }
        ]
      },
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      data: [],
      uploading: false,
      baseUrl: process.env.VUE_APP_BASE_API,
      fileList: [],
      headers: { Authorization: getToken() }

    }
  },
  mounted() {
    this.search()
  },

  methods: {

    search(condition) {
      const temp = formatCondition(condition)
      const param = temp || {}
      const { currentPage, pageSize } = this.paginationConfig
      pluginLists(currentPage, pageSize, param).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    },
    beforeUpload(file) {
      this.uploading = true
    },
    uploadFail(response, file, fileList) {
      this.uploading = false
    },
    uploadSuccess(response, file, fileList) {
      this.uploading = false
      this.search()
    },

    del(row) {
      this.$confirm(this.$t('user.delete_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        uninstall(row.pluginId).then(res => {
          this.search()
          this.$success('卸载成功')
        }).catch(() => {
          this.$error('卸载失败')
        })
      }).catch(() => {
        this.$info(this.$t('commons.delete_cancel'))
      })
    }

  }
}
</script>

<style scoped>

</style>
