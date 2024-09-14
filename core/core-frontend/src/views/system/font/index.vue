<script lang="ts" setup>
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import { onMounted, ref, computed } from 'vue'
import UploadDetail from './UploadDetail.vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { deleteById, edit, defaultFont } from '@/api/font'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { cloneDeep } from 'lodash-es'

const appearanceStore = useAppearanceStoreWithOut()

const fontKeyword = ref('')
const fontList = ref([])
const basePath = import.meta.env.VITE_API_BASEPATH

const uploadDetail = ref()
const loading = ref(false)
const uploadFont = (title, type, item) => {
  uploadDetail.value.init(title, type, item)
}

const listFont = async () => {
  loading.value = true
  await appearanceStore.setFontList()
  fontList.value = cloneDeep(appearanceStore.fontList)
  loading.value = false
}

const fontListComputed = computed(() => {
  return fontList.value.filter(ele => {
    return ele.name?.toLocaleLowerCase().includes(fontKeyword.value.trim().toLocaleLowerCase())
  })
})

const deleteFont = item => {
  if (item.isDefault) {
    ElMessage.warning('请先将其他字体设置为默认字体，再进行删除。')
    return
  }
  ElMessageBox.confirm('当前字体被删除后，使用该字体的组件将会使用默认字体，确定删除?', {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    loading.value = true
    deleteById(item.id)
      .then(() => {
        ElMessage.success('删除成功')
        listFont()
        getDefaultFont()
      })
      .finally(() => {
        loading.value = false
      })
  })
}

const setToDefault = item => {
  item.isDefault = 1
  loading.value = true
  edit(item)
    .then(() => {
      ElMessage.success('设置成功')
      getDefaultFont()
      listFont()
    })
    .finally(() => {
      loading.value = false
    })
}
const setDefaultFont = (url, name, fileTransName) => {
  let fontStyleElement = document.querySelector('#de-custom_font')
  if (!fontStyleElement && name) {
    fontStyleElement = document.createElement('style')
    fontStyleElement.setAttribute('id', 'de-custom_font')
    document.querySelector('head').appendChild(fontStyleElement)
  }
  fontStyleElement.innerHTML =
    name && fileTransName
      ? `@font-face {
              font-family: '${name}';
              src: url(${url});
              font-weight: normal;
              font-style: normal;
              }`
      : ''
  document.documentElement.style.setProperty('--de-custom_font', `${name ? name : ''}`)
  document.documentElement.style.setProperty('--van-base-font', `${name ? name : ''}`)
}
const getDefaultFont = () => {
  defaultFont().then(res => {
    const [font] = res || []
    setDefaultFont(
      `${basePath}/typeface/download/${font?.fileTransName}`,
      font?.name,
      font?.fileTransName
    )
  })
}
const uploadFilish = async () => {
  loading.value = true
  await appearanceStore.setFontList()
  fontList.value = cloneDeep(appearanceStore.fontList)
  loading.value = false
  getDefaultFont()
}

onMounted(() => {
  listFont()
})
</script>

<template>
  <div class="font-management_system" v-loading="loading">
    <div class="route-title">
      字体管理
      <div class="search-font">
        <el-input v-model="fontKeyword" clearable style="width: 240px" placeholder="搜索字体名称">
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"
                ><icon_searchOutline_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </template>
        </el-input>

        <el-button type="primary" @click="uploadFont('新建字体', 'create', {})">
          <template #icon>
            <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
          </template>
          添加字体
        </el-button>
      </div>
    </div>
    <div class="font-content_overflow">
      <div class="font-content_list">
        <div class="font-content_item" v-for="ele in fontListComputed" :key="ele">
          <span v-if="ele.isDefault" class="font-default">默认字体</span>
          <div class="font-name">
            {{ ele.name }} <span v-if="ele.isBuiltin" class="font-type"> 系统内置 </span>
          </div>
          <div :title="ele.fileName" class="font-update_time">
            更新时间： {{ new Date(ele.updateTime).toLocaleString() }}
            <span class="line"></span> 字库文件： {{ ele.fileName }}
          </div>
          <div class="font-upload_btn">
            <el-button
              v-if="!ele.fileTransName"
              @click="uploadFont('上传字库文件', 'uploadFile', ele)"
              secondary
              >上传字库文件</el-button
            >
            <el-button
              v-if="ele.fileTransName"
              @click="uploadFont('替换字库文件', 'uploadFile', ele)"
              secondary
              >替换字库文件</el-button
            >
            <el-button v-if="!ele.isDefault" @click="setToDefault(ele)" secondary
              >设为默认字体</el-button
            >
            <el-button v-if="ele.id !== '1'" @click="deleteFont(ele)" secondary>删除</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <UploadDetail @finish="uploadFilish" ref="uploadDetail"></UploadDetail>
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
  .font-content_overflow {
    height: calc(100vh - 146px);
    overflow-y: auto;
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
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
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
