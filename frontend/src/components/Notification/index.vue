<template>

  <el-popover

    v-model="visible"
    width="350"
    trigger="click"
    placement="top-end"
    style="display: flex;align-items: center;"
    class="international"
  >
    <div style="height: 30px;">
      <div style="float: left;font-size:16px;font-weight:bold;">
        <span>站内消息通知</span>
      </div>
      <div v-if="showSetting" style="float: right;">
        <a href="#" style="text-detext-decoratext-decoration:none;cursor:point;" @click="msgSetting">消息规则</a>
      </div>

    </div>
    <el-divider class="msg-line-class" />
    <el-table
      class="de-msg-data-table"
      :data="data"
      :show-header="false"
      :highlight-current-row="true"
      style="width: 100%"
    >
      <el-table-column prop="content" :label="$t('commons.name')">
        <template slot-scope="scope">
          <div class="start-item">
            <div class="filter-db-row star-item-content" @click="showDetail(scope.row)">
              <!-- <svg-icon icon-class="panel" class="ds-icon-scene" /> -->
              <div class="title-div"><span>【{{ getTypeName(scope.row.type) }}】&nbsp;&nbsp;{{ scope.row.content }}</span></div>
              <div class="title-div"><span>{{ scope.row.createTime | timestampFormatDate }}</span></div>
            </div>
            <!-- <div class="star-item-close">
              <i class="el-icon-delete " @click="remove(scope.row)" />
            </div> -->
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div class="msg-foot-class">
      <el-row style="padding: 5px 0;margin-bottom: -5px;cursor:point;" @click="showMore">
        <span @click="showMore">查看更多</span>
      </el-row>
    </div>

    <div slot="reference">
      <div>
        <svg-icon
          class-name="notification"
          icon-class="notification"
        />
        <span class="msg-number">9</span>
      </div>
    </div>
  </el-popover>
</template>

<script>
import { query, updateStatus } from '@/api/system/msg'
import { msgTypes, getTypeName } from '@/utils/webMsg'
export default {
  data() {
    return {
      msgTypes: msgTypes,
      showSetting: false,
      data: [],
      visible: false,
      paginationConfig: {
        currentPage: 1,
        pageSize: 5,
        total: 0
      }
    }
  },
  computed: {

  },
  created() {
    this.search()
  },
  methods: {
    handClick(lang) {
      console.log(lang)
    },
    showDetail(row) {
      const param = { ...{ msgNotification: true, msgType: row.type }}
      this.visible = false
      this.$router.push({ name: 'panel', params: param })
      this.setReaded(row.msgId)
    },
    remove(row) {

    },
    msgSetting() {

    },
    showMore() {
      const routerName = 'sys-msg-web-all'
      this.visible = false
      this.$router.push({ name: routerName })
      this.$emit('refresh-top-bar')
    },
    search() {
      const param = {}
      const { currentPage, pageSize } = this.paginationConfig
      query(currentPage, pageSize, param).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    },
    getTypeName(value) {
      return getTypeName(value)
    },
    open() {
      this.visible = true
    },
    // 设置已读
    setReaded(msgId) {
      updateStatus(msgId).then(res => {
        this.search()
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.msg-number {
    min-width: 14px;
    text-align: center;
    line-height: 14px;
    display: inline-block;
    position: fixed;
    right: 155px;
    top: 8px;
    background: red;
    color: #fff;
    border-radius: 17px;
    padding: 4px 7px;
    font-size: 16px;
    transform: scale(.7);
    font-family: Tahoma!important;
}
.title-div {

    width: 100%;
    span {
      overflow: hidden;
      text-overflow: ellipsis;
      -o-text-overflow: ellipsis;
      white-space:nowrap;
      width:290px;
      height:24px;
      display:block;
      font-size: 10px !important;
    }
}
.msg-line-class {
    margin: 0 0 !important;
}
.msg-foot-class {
  padding-top: 5px;
  :hover {
    cursor: pointer;
    background-color: #f4f4f5;
  }
}
</style>
