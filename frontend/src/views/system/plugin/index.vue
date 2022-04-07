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
      <!--  <el-table-column prop="free" :label="$t('plugin.free')">
        <template v-slot:default="scope">
          <span>{{ scope.row.free ? '是' : '否' }}</span>
        </template>
      </el-table-column> -->
      <el-table-column prop="cost" :label="$t('plugin.cost')" />

      <el-table-column :show-overflow-tooltip="true" prop="descript" :label="$t('plugin.descript')" />
      <el-table-column prop="version" :label="$t('plugin.version')" />
      <el-table-column prop="creator" :label="$t('plugin.creator')" />

      <el-table-column prop="installTime" :label="$t('plugin.install_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.installTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <fu-table-operations :buttons="buttons" :label="$t('commons.operating')" fix />
    </complex-table>

  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import ComplexTable from '@/components/business/complex-table'

// import { checkPermission } from '@/utils/permission'
import { formatCondition, formatQuickCondition } from '@/utils/index'
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
        {
          label: this.$t('plugin.un_install'), icon: 'el-icon-delete', type: 'danger', click: this.del,
          disabled: this.btnDisabled
        }
      ],
      searchConfig: {
        useQuickSearch: true,
        quickPlaceholder: this.$t('role.search_by_name'),
        components: [
          { field: 'name', label: this.$t('plugin.name'), component: 'DeComplexInput' }
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
      condition = formatQuickCondition(condition, 'name')
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
      const msg = response && response.message || '安装失败'
      this.$error(msg)
      this.uploading = false
    },
    uploadSuccess(response, file, fileList) {
      this.uploading = false
      this.search()
    },

    del(row) {
      this.$confirm(this.$t('plugin.uninstall_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        uninstall(row.pluginId).then(res => {
          this.search()
          this.$success(this.$t('plugin.un_install_success'))
        }).catch(() => {
          this.$error(this.$t('plugin.un_install_error'))
        })
      }).catch(() => {
        this.$info(this.$t('plugin.uninstall_cancel'))
      })
    },
    btnDisabled(row) {
      return row.pluginId < 4
    }

  }
}
</script>

<style scoped>

</style>
