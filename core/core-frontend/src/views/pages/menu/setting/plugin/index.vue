<template>
  <p class="router-title">{{ t('system.plugin_management') }}</p>
  <div class="plugin-top-area">
    <div class="plugin-top-search">
      <el-input
        v-model="keyword"
        clearable
        :placeholder="t('system.search_plugin_name')"
        style="width: 240px"
        @change="search"
      >
        <template #prefix>
          <el-icon>
            <Icon name="icon_search-outline_outlined"><icon_searchOutline_outlined /></Icon>
          </el-icon>
        </template>
      </el-input>
    </div>
    <PluginUpload @on-success="installSuccess">
      <el-button type="primary">
        <template #icon>
          <Icon name="icon_upload_outlined"><icon_upload_outlined class="svg-icon" /></Icon>
        </template>
        {{ t('system.local_installation') }}
      </el-button>
    </PluginUpload>
  </div>
  <div class="plugin-container">
    <el-scrollbar>
      <div class="plugin-flag">
        <div
          v-for="item in state.flagArray"
          :key="item"
          class="plugin-flag-item"
          :class="{ 'is-active': activeFlag === item }"
          @click="flagChange(item)"
        >
          <span>{{ t(`plugin.flag-${item}`) }}</span>
        </div>
      </div>
      <div v-if="!Object.keys(state.pluginMap)?.length" class="empty-plugin-div">
        <el-empty
          v-if="state.originData?.length"
          class="plugin-empty"
          :image="nothingSearch"
          :description="t('system.relevant_content_found')"
        />
        <el-empty
          v-else
          class="plugin-empty"
          :image="nothingNone"
          :description="t('system.no_plugins_yet')"
        >
          <PluginUpload @on-success="installSuccess">
            <el-button style="z-index: 5" type="primary">
              <template #icon>
                <Icon name="icon_upload_outlined"><icon_upload_outlined class="svg-icon" /></Icon>
              </template>
              {{ t('system.local_installation') }}
            </el-button>
          </PluginUpload>
        </el-empty>
      </div>
      <div v-else class="plugin-scroller">
        <div v-for="(list, key) in state.pluginMap" :key="key" class="plugin-flag-container">
          <div class="flag-title">
            <span class="flag-title-left" />
            <span class="flag-title-right">{{ t(`plugin.flag-${key}`) }}</span>
          </div>
          <div class="flag-item-container">
            <div v-for="item in list" :key="item['id']" class="plugin-card">
              <div class="content">
                <div class="content-icon">
                  <Icon :static-content="item['icon']">
                    <pluginDefault></pluginDefault>
                  </Icon>
                </div>
                <div class="content-info">
                  <div class="info-top">
                    <div class="plugin-card-name">{{ item['name'] }}</div>
                    <div class="plugin-card-version">{{ item['version'] }}</div>
                  </div>
                  <div class="info-bottom">
                    <span>
                      {{
                        `${t('system.installation_time')}${timestampFormatDate(
                          item['installTime']
                        )}`
                      }}
                    </span>
                    <span class="bottom-delimiter" />
                    <el-tooltip
                      class="box-item"
                      effect="dark"
                      :content="`${t('system.developer')}${item['developer']}`"
                      placement="top-start"
                    >
                      <span class="bottom-developer">
                        {{ `${t('system.developer')}${item['developer']}` }}
                      </span>
                    </el-tooltip>
                  </div>
                </div>
              </div>
              <div class="plugin-btn">
                <el-button secondary @click="updateHandler(item)">{{
                  t('commons.update')
                }}</el-button>
                <el-button secondary @click="uninstall(item)">{{
                  t('commons.uninstall')
                }}</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <PluginUpload :is-edit="true" :id="currentId" @on-success="installSuccess">
        <el-button class="plugin-edit-btn" ref="updateLoader" />
      </PluginUpload>
    </el-scrollbar>
  </div>
</template>

<script lang="ts" setup>
import pluginDefault from '@/assets/svg/plugin-default.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import icon_upload_outlined from '@/assets/svg/icon_upload_outlined.svg'
import { ref, reactive, onMounted } from 'vue'
import { loadPluginApi, PluginItem, unInstallApi } from '@/views/menu/setting/plugin/data'
import { useI18n } from '@/hooks/web/useI18n'
import nothingNone from '@/assets/img/nothing-none-gray.png'
import nothingSearch from '@/assets/img/nothing-tree.png'
import PluginUpload from '@/views/menu/setting/plugin/PluginUpload.vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
const { t } = useI18n()
const keyword = ref('')

const updateLoader = ref()
const currentId = ref()
const activeFlag = ref('all')
const state = reactive({
  flagArray: ['all', 'ds', 'view', 'df', 'sync-source', 'sync-sink'],
  pluginMap: {},
  originData: [] as PluginItem[]
})

const flagChange = val => {
  keyword.value = ''
  activeFlag.value = val
  const data = state.originData.filter(item => val === 'all' || item['flag'] === val)
  state.pluginMap = {}
  data.forEach(item => {
    state.pluginMap[item.flag] = state.pluginMap[item.flag] || []
    state.pluginMap[item.flag].push(item)
  })
}

const search = (val?: string) => {
  const data = state.originData.filter(
    item => !val || item.name.toLocaleLowerCase().includes(val.toLocaleLowerCase())
  )
  state.pluginMap = {}
  data.forEach(item => {
    state.pluginMap[item.flag] = state.pluginMap[item.flag] || []
    state.pluginMap[item.flag].push(item)
  })
}

const loadPlugin = () => {
  loadPluginApi().then(res => {
    state.originData = res.data
    search()
  })
}
const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return new Date(value)['format']()
}
const updateHandler = item => {
  ElMessageBox.confirm(t('role.confirm_delete'), {
    confirmButtonType: 'primary',
    type: 'warning',
    autofocus: false,
    confirmButtonText: '',
    cancelButtonText: t('dataset.cancel'),
    dangerouslyUseHTMLString: true,
    message:
      '<strong style="font-size: 16px;">' +
      t('system.update_the_plugin') +
      '</strong></br>' +
      `<span style="font-size: 14px;font-weight: 400;display: block;margin-top: 8px;">${t(
        'system.to_take_effect_update'
      )}</span>`,
    showClose: false
  }).then(() => {
    currentId.value = item['id']
    updateLoader.value.ref.click()
  })
}
const uninstall = item => {
  ElMessageBox.confirm(t('role.confirm_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    confirmButtonText: t('commons.uninstall'),
    cancelButtonText: t('dataset.cancel'),
    dangerouslyUseHTMLString: true,
    message:
      '<strong style="font-size: 16px;">' +
      t('system.uninstall_the_plugin') +
      '</strong></br>' +
      `<span style="font-size: 14px;font-weight: 400;display: block;margin-top: 8px;">${t(
        'system.to_take_effect_de'
      )}</span>`,
    showClose: false
  }).then(() => {
    unInstallApi(item['id']).then(() => {
      loadPlugin()
      ElMessage.success(t('system.uninstall_successful'))
    })
  })
}

const installSuccess = () => {
  if (currentId.value) {
    currentId.value = null
    loadPlugin()
    ElMessage.success(t('system.update_successful'))
    return
  }
  loadPlugin()
  ElMessage.success(t('system.installation_successful'))
}
onMounted(() => {
  loadPlugin()
})
</script>

<style lang="less" scoped>
.router-title {
  color: #1f2329;
  font-feature-settings: 'clig' off, 'liga' off;
  font-family: var(--de-custom_font, 'PingFang');
  font-size: 20px;
  font-style: normal;
  font-weight: 500;
  line-height: 28px;
}
.plugin-top-area {
  position: absolute;
  display: flex;
  top: 70px;
  right: 24px;
  // width: 354px;
  height: 32px;
  .plugin-top-search {
    width: 240px;
    height: 32px;
    margin-right: 16px;
  }
}
.plugin-container {
  padding-top: 16px;
  width: 100%;
  height: calc(100% - 45px);
  position: relative;
  overflow: hidden;
  border-radius: 12px;
  .plugin-flag {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    grid-column-gap: 12px;
    height: 28px;
    .plugin-flag-item {
      height: 28px;
      padding: 2px 8px;
      border-radius: 6px;
      line-height: 28px;
      background-color: #1f23291a;
      color: #1f2329;
      display: flex;
      align-items: center;
      cursor: pointer;
      &:hover {
        color: #245bdb;
      }
      span {
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 14px;
        font-weight: 400;
        line-height: 22px;
        text-align: center;
      }
    }
    .is-active {
      background-color: var(--ed-color-primary-33, #3370ff33);
      color: var(--ed-color-primary, #3370ff);
    }
  }
  .empty-plugin {
    margin-top: 160px;
  }
}
.plugin-flag-container {
  padding-top: 16px;
  .flag-title {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    height: 24px;
    .flag-title-left {
      height: 16px;
      width: 0;
      border-left: 2px solid var(--ed-color-primary, #3370ff);
    }
    .flag-title-right {
      margin-left: 8px;
      height: 24px;
      font-family: var(--de-custom_font, 'PingFang');
      font-size: 16px;
      font-weight: 500;
      line-height: 24px;
      text-align: left;
      color: #1f2329;
    }
  }
  .flag-item-container {
    display: flex;
    flex-wrap: wrap;
    grid-column-gap: 16px;
    grid-row-gap: 16px;
    .plugin-card {
      min-width: 370px;
      width: calc(50% - 8px);
      height: 136px;
      padding: 24px;
      background: #fff;
      border-radius: 12px;
      cursor: pointer;
      .content {
        height: 44px;
        width: 100%;
        display: flex;
        .content-icon {
          width: 40px;
          height: 40px;
          border-radius: 8px;
          padding: 4px;
          border: 1px solid #dee0e3;
          margin-right: 8px;
          svg {
            width: 100%;
            height: 100%;
          }
        }
        .content-info {
          width: calc(100% - 52px);
          height: 44px;
          .info-top {
            width: 100%;
            height: 22px;
            display: flex;
            align-items: center;
            .plugin-card-name {
              height: 22px;
              font-size: 14px;
              font-weight: 500;
              line-height: 22px;
              font-family: var(--de-custom_font, 'PingFang');
              color: #1f2329;
            }
            .plugin-card-version {
              padding: 1px 4px;
              background-color: var(--ed-color-primary-33, #3370ff33);
              color: var(--ed-color-primary, #3370ff);
              font-size: 12px;
              font-weight: 400;
              height: 20px;
              line-height: 20px;
              margin-left: 8px;
            }
          }
          .info-bottom {
            margin-top: 2px;
            display: flex;
            align-items: center;
            width: 100%;
            height: 20px;
            font-size: 12px;
            font-weight: 400;
            line-height: 20px;
            color: #646a73;
            min-width: 290px;
            :first-child {
              min-width: 176px;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
            .bottom-delimiter {
              display: inline-block;
              height: 12px;
              line-height: 12px;
              margin: 0 8px;
              border-left: 1px solid #1f232926;
            }
            .bottom-developer {
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
          }
        }
      }
      .plugin-btn {
        height: 28px;
        margin-top: 16px;

        :deep(button) {
          height: 28px;
          line-height: 28px;
          min-width: 48px;
          padding: 3px 8px;
          + .ed-button {
            margin-left: 8px;
          }
          span {
            height: 22px;
            line-height: 22px;
            font-size: 12px;
            padding: 5px 4px;
          }
        }
      }
    }
  }
}
.plugin-edit-btn {
  display: none;
}
.empty-plugin-div {
  width: 100%;
  height: 155px;
  margin-top: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  .plugin-empty {
    height: 155px;
    width: 248px;
    margin: 0;
    padding: 0;
    :deep(.ed-empty__image) {
      height: 125px;
      width: 125px;
      margin: auto;
    }
    :deep(.ed-empty__description) {
      line-height: 22px;
      margin-top: 8px !important;
      p {
        font-size: 14px;
      }
    }
  }
}
.plugin-scroller {
  height: calc(100vh - 169px);
}
</style>
