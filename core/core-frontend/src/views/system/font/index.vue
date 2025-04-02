<script lang="ts" setup>
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import { onMounted, ref, computed } from 'vue'
import UploadDetail from './UploadDetail.vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { useI18n } from '@/hooks/web/useI18n'
import { deleteById, edit, defaultFont } from '@/api/font'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { cloneDeep } from 'lodash-es'

const appearanceStore = useAppearanceStoreWithOut()
const { t } = useI18n()
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
    ElMessage.warning(t('system.fonts_before_deleting'))
    return
  }
  ElMessageBox.confirm(t('system.sure_to_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    loading.value = true
    deleteById(item.id)
      .then(() => {
        ElMessage.success(t('common.delete_success'))
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
      ElMessage.success(t('system.setting_successful'))
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
      {{ t('system.font_management') }}
      <div class="search-font">
        <el-input
          v-model="fontKeyword"
          clearable
          style="width: 240px"
          :placeholder="t('system.search_font_name')"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"
                ><icon_searchOutline_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </template>
        </el-input>

        <el-button type="primary" @click="uploadFont(t('system.a_new_font'), 'create', {})">
          <template #icon>
            <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
          </template>
          {{ t('system.add_font') }}
        </el-button>
      </div>
    </div>
    <div class="font-content_overflow">
      <div class="font-content_list" v-if="fontListComputed.length">
        <div class="font-content_item" v-for="ele in fontListComputed" :key="ele">
          <span v-if="ele.isDefault" class="font-default">{{ t('system.default_font') }}</span>
          <div class="font-name">
            <span :title="ele.name" :class="!ele.isBuiltin && 'font-name_text'">{{
              ele.name
            }}</span>
            <span v-if="ele.isBuiltin" class="font-type"> {{ t('system.system_built_in') }} </span>
          </div>
          <div :title="ele.fileName" class="font-update_time">
            {{ t('system.update_time') }} {{ new Date(ele.updateTime).toLocaleString() }}
            <span class="line"></span>
            <span :title="ele.fileName" class="font-update_text"
              >{{ t('system.font_file') }} {{ ele.fileName }}</span
            >
          </div>
          <div class="font-upload_btn">
            <el-button
              v-if="!ele.fileTransName"
              @click="uploadFont(t('system.upload_font_file'), 'uploadFile', ele)"
              secondary
              >{{ t('system.upload_font_file') }}</el-button
            >
            <el-button
              v-if="ele.fileTransName"
              @click="uploadFont(t('system.replace_font_file'), 'uploadFile', ele)"
              secondary
              >{{ t('system.replace_font_file') }}</el-button
            >
            <el-button v-if="!ele.isDefault" @click="setToDefault(ele)" secondary>{{
              t('system.as_default_font')
            }}</el-button>
            <el-button v-if="ele.id !== '1'" @click="deleteFont(ele)" secondary>{{
              t('common.delete')
            }}</el-button>
          </div>
        </div>
      </div>
      <div style="height: 178px; margin-top: 142px" v-else>
        <empty-background :description="$t('work_branch.relevant_content_found')" img-type="tree" />
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
      padding-bottom: 16px;

      .font-default {
        min-width: 68px;
        height: 24px;
        background: #34c72433;
        position: absolute;
        right: 0;
        top: 0;
        font-size: 14px;
        font-weight: 400;
        line-height: 22px;
        color: #2ca91f;
        padding: 0 6px;
        border-bottom-left-radius: 4px;
      }

      .font-name {
        font-size: 16px;
        font-weight: 500;
        line-height: 24px;
        display: flex;
        align-items: center;
        margin-bottom: 4px;

        .font-name_text {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          min-width: 250px;
        }

        .font-type {
          min-width: 56px;
          height: 20px;
          border-radius: 2px;
          background: #3370ff33;
          font-size: 12px;
          font-weight: 400;
          line-height: 20px;
          color: #2b5fd9;
          margin-left: 8px;
          padding: 0 4px;
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

        .line {
          width: 1px;
          height: 14px;
          background: #1f232926;
          margin: 0 8px;
        }

        .font-update_text {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          min-width: 30px;
        }
      }

      .font-upload_btn {
        .ed-button {
          min-width: 0;
          margin: 0 12px 8px 0;
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
