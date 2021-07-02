<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">

    <el-radio-group v-model="selectType" style="margin-bottom: 15px;" @change="typeChange">
      <el-radio-button v-for="(item,index) in msgTypes" :key="index" class="de-msg-radio-class" :label="item.value">{{ item.label }}</el-radio-button>

    </el-radio-group>
    <complex-table
      :data="data"
      :columns="columns"
      :pagination-config="paginationConfig"
      @select="select"
      @search="search"
    >

      <el-table-column prop="content" :label="$t('commons.name')">
        <template v-slot:default="scope">

          <span style="display: flex;flex: 1;">
            <span>
              <svg-icon v-if="!scope.row.status" icon-class="unread-msg" style="color: red;" />
              <svg-icon v-else icon-class="readed-msg" />
            </span>
            <span style="margin-left: 6px;" class="de-msg-a" @click="toDetail(scope.row)">
              {{ scope.row.content }}
            </span>
          </span>

        </template>
      </el-table-column>

      <el-table-column prop="createTime" :label="$t('commons.create_time')" width="180">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="type" :label="$t('datasource.type')" width="120">
        <template slot-scope="scope">
          <span>{{ getTypeName(scope.row.type) }}</span>
        </template>
      </el-table-column>

    </complex-table>

  </layout-content>
</template>

<script>

import LayoutContent from '@/components/business/LayoutContent'
import ComplexTable from '@/components/business/complex-table'
import { query, updateStatus } from '@/api/system/msg'
import { msgTypes, getTypeName } from '@/utils/webMsg'
export default {
  components: {
    LayoutContent,
    ComplexTable
  },
  data() {
    return {
      selectType: -1,
      //   msgTypes: [
      //     { value: -1, label: '全部类型' },
      //     { value: 0, label: '仪表板分享' },
      //     { value: 1, label: '数据集同步' }
      //   ],
      msgTypes: msgTypes,
      data: [],
      allTypes: [{ name: 'mysql', type: 'jdbc' }, { name: 'sqlServer', type: 'jdbc' }],

      columns: [],

      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    }
  },
  mounted() {
    this.search()
  },
  methods: {
    select(selection) {
    },

    search() {
      const param = {}

      if (this.selectType >= 0) {
        param.type = this.selectType
      }
      const { currentPage, pageSize } = this.paginationConfig
      query(currentPage, pageSize, param).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    },
    getTypeName(value) {
      return getTypeName(value)
    },
    typeChange(value) {
      this.search()
    },
    toDetail(row) {
      const param = { ...{ msgNotification: true, msgType: row.type }}
      this.$router.push({ name: 'panel', params: param })
      this.setReaded(row)
    },
    // 设置已读
    setReaded(row) {
      updateStatus(row.msgId).then(res => {
        this.search()
      })
    }
  }

}
</script>

<style lang="scss" scoped>
.de-msg-radio-class {
  padding: 0 5px;
  >>>.el-radio-button__inner {
    border-radius: 4px 4px 4px 4px !important;
    border-left: 1px solid #dcdfe6 !important;
    padding: 10px 10px;
  }

  >>>.el-radio-button__orig-radio:checked+.el-radio-button__inner {
    color: #fff;
    background-color: #0a7be0;
    border-color: #0a7be0;
    -webkit-box-shadow: 0px 0 0 0 #0a7be0;
    box-shadow: 0px 0 0 0 #0a7be0;
  }
}
.de-msg-a:hover {
    text-decoration: underline !important;
    color: #0a7be0 !important;
    cursor: pointer !important;

}

</style>
