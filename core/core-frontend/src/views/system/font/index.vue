<script lang="ts" setup>
import { onMounted, ref } from 'vue'

import UploadDetail from './UploadDetail.vue'
import { deleteById, edit, list } from '@/api/font'
import { ElMessage } from 'element-plus-secondary'
const fontKeyword = ref('')
const fontList = ref([])

const uploadDetail = ref()
const uploadFont = (title, type, item) => {
  uploadDetail.value.init(title, type, item)
}

const listFont = () => {
  list({}).then(res => {
    fontList.value = res
  })
}

const deleteFont = item => {
  deleteById(item.id).then(() => {
    ElMessage.success('删除成功')
    listFont()
  })
}

const setToDefault = item => {
  item.isDefault = 1
  edit(item).then(() => {
    ElMessage.success('设置成功')
  })
}

const cancleDefault = item => {
  item.isDefault = 0
  edit(item).then(() => {
    ElMessage.success('取消成功')
  })
}

onMounted(() => {
  listFont()
})
</script>

<template>
  <div class="font-management_system">
    <div class="route-title">
      字体管理
      <div class="search-font">
        <el-input v-model="fontKeyword" clearable style="width: 240px" placeholder="搜索插件名称">
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>

        <el-button type="primary" @click="uploadFont('新建字体', 'create', {})">
          <template #icon>
            <Icon name="icon_add_outlined"></Icon>
          </template>
          添加字体
        </el-button>
      </div>
    </div>
    <div class="font-content_list">
      <div class="font-content_item" v-for="ele in fontList" :key="ele">
        <span v-if="ele.isDefault" class="font-default">默认字体</span>
        <div class="font-name">
          {{ ele.name }} <span v-if="ele.isBuiltin" class="font-type"> 系统内置 </span>
        </div>
        <div class="font-update_time">
          更新时间： {{ new Date(ele.updateTime).toLocaleString() }}
          <span class="line"></span> 字库文件： {{ ele.fileName }}
        </div>
        <div class="font-upload_btn">
          <el-button @click="uploadFont('上传字库文件', 'uploadFile', ele)" secondary
            >上传字库文件</el-button
          >
          <el-button v-if="!ele.isDefault" @click="setToDefault(ele)" secondary
            >设为默认字体</el-button
          >
          <el-button v-if="ele.isDefault" @click="cancleDefault(ele)" secondary
            >取消默认字体</el-button
          >
          <el-button @click="uploadFont('重命名', 'rename', ele)" secondary>重命名</el-button>
          <el-button @click="deleteFont(ele)" secondary>删除</el-button>
        </div>
      </div>
    </div>
  </div>
  <UploadDetail @finish="listFont" ref="uploadDetail"></UploadDetail>
</template>

<style lang="less" scoped>
.font-management_system {
  .route-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 20px;
    font-weight: 500;
    line-height: 28px;
    margin-bottom: 16px;

    .search-font {
      display: flex;
      align-items: center;
      justify-content: flex-end;

      .ed-button {
        margin-left: 12px;
      }
    }
  }
  .font-content_list {
    display: flex;
    flex-wrap: wrap;
    row-gap: 16px;
    justify-content: space-between;

    .font-content_item {
      border-radius: 4px;
      background: #fff;
      width: calc(50% - 8px);
      position: relative;
      padding: 24px;
      .font-default {
        width: 68px;
        height: 24px;
        background: #34c72433;
        position: absolute;
        right: 0;
        top: 0;
        font-size: 14px;
        font-weight: 400;
        line-height: 22px;
        color: #2ca91f;
        padding-left: 6px;
        border-bottom-left-radius: 4px;
      }

      .font-name {
        font-size: 16px;
        font-weight: 500;
        line-height: 24px;
        display: flex;
        align-items: center;
        margin-bottom: 4px;

        .font-type {
          width: 56px;
          height: 20px;
          border-radius: 2px;
          background: #3370ff33;
          font-size: 12px;
          font-weight: 400;
          line-height: 20px;
          color: #2b5fd9;
          margin-left: 8px;
          padding-left: 4px;
        }
      }

      .font-update_time {
        margin-bottom: 16px;
        font-size: 12px;
        font-weight: 400;
        line-height: 20px;
        color: #646a73;
        display: flex;
        align-items: center;
        .line {
          width: 1px;
          height: 14px;
          background: #1f232926;
          margin: 0 8px;
        }
      }

      .font-upload_btn {
        .ed-button {
          min-width: 0;
          padding: 4px 8px;
          font-size: 12px;
          font-weight: 400;
          line-height: 20px;
        }
      }
    }
  }
}
</style>
