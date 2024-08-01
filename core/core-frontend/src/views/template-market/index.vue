<template>
  <el-row
    class="template-outer-body"
    :class="{ 'template-outer-body-padding': outPaddingState }"
    v-loading="state.loading"
    v-show="state.initReady"
  >
    <market-preview-v2
      v-show="previewModel === 'marketPreview'"
      :preview-id="state.templatePreviewId"
      :create-auth="createAuth"
      @closePreview="closePreview"
      @templateApply="templateApply"
    ></market-preview-v2>
    <el-row v-if="previewModel === 'createPreview'" class="main-container">
      <el-row class="market-head">
        <el-icon class="custom-back-icon hover-icon" @click="previewModel = 'full'"
          ><ArrowLeft
        /></el-icon>
        <span>{{ state.curTemplate.title }}1 </span>
        <el-row class="head-right">
          <el-button
            secondary
            :disabled="state.curTemplateIndex === 0"
            style="float: right"
            @click="preOne"
            >上一个</el-button
          >
          <el-button
            :disabled="state.curTemplateIndex === state.curTemplateShowFilter.length - 1"
            style="float: right"
            secondary
            @click="nextOne"
            >下一个</el-button
          >
          <el-button
            style="float: right"
            type="primary"
            @click="templateApply(state.curTemplate)"
            >{{ t('visualization.apply_this_template') }}</el-button
          >
          <el-divider class="custom-divider-line" direction="vertical" />
          <el-icon class="custom-market-icon hover-icon_custom" @click="close"><Close /></el-icon>
        </el-row>
      </el-row>
      <el-row class="template-area">
        <el-row class="img-main-create">
          <img style="height: 100%" :src="curTemplateImg" alt="" />
        </el-row>
      </el-row>
    </el-row>
    <el-row v-show="previewModel === 'full'" class="main-container">
      <el-row class="market-head">
        <span>{{ title }} </span>
        <el-row class="head-right">
          <el-input
            class="title-search"
            v-model="state.searchText"
            prefix-icon="Search"
            :placeholder="t('visualization.enter_template_name_tips')"
            :clearable="true"
          />
          <el-select class="title-type" v-model="state.templateSourceType" placeholder="Select">
            <el-option
              v-for="item in state.templateSourceOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-select
            v-if="['branchCreate', 'branch'].includes(state.curPosition)"
            class="title-type"
            v-model="state.templateType"
            placeholder="Select"
          >
            <el-option
              v-for="item in state.templateTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-select class="title-type" v-model="state.templateClassifyType" placeholder="Select">
            <el-option
              v-for="item in state.templateClassifyOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <template v-if="['branchCreate', 'create'].includes(state.curPosition)">
            <el-divider class="custom-divider-line" direction="vertical" />
            <el-icon class="custom-market-icon hover-icon_custom" @click="close"><Close /></el-icon>
          </template>
        </el-row>
      </el-row>
      <el-row class="template-area">
        <div class="template-left">
          <el-tree
            v-if="state.treeShow"
            menu
            class="custom-market-tree"
            v-model="state.marketActiveTab"
            :data="categoriesComputed"
            :props="state.treeProps"
            node-key="label"
            default-expand-all
            highlight-current
            :current-node-key="state.marketActiveTab"
            @node-click="nodeClick"
          />
        </div>
        <div
          v-show="state.networkStatus && state.hasResult"
          id="template-show-area"
          class="template-right"
        >
          <el-row v-show="state.marketActiveTab !== '推荐'">
            <category-template-v2
              :search-text="state.searchText"
              :label="state.marketActiveTab"
              :full-template-show-list="state.currentMarketTemplateShowList"
              :template-span="state.templateSpan"
              :base-url="state.baseUrl"
              :template-cur-width="state.templateCurWidth"
              :cur-position="state.curPosition"
              :create-auth="createAuth"
              @templateApply="templateApply"
              @templatePreview="templatePreview"
            ></category-template-v2>
          </el-row>
          <el-row v-show="state.marketActiveTab === '推荐'">
            <el-row
              style="display: inline; width: 100%; margin-bottom: 16px"
              v-for="(categoryItem, index) in categoriesComputed"
              :key="index"
            >
              <category-template-v2
                v-if="categoryItem.label !== '最近使用'"
                :search-text="state.searchText"
                :label="categoryItem.label"
                :full-template-show-list="state.currentMarketTemplateShowList"
                :template-span="state.templateSpan"
                :base-url="state.baseUrl"
                :template-cur-width="state.templateCurWidth"
                :cur-position="state.curPosition"
                :create-auth="createAuth"
                @templateApply="templateApply"
                @templatePreview="templatePreview"
              ></category-template-v2>
            </el-row>
          </el-row>
        </div>
        <el-row v-show="state.networkStatus && !state.hasResult" class="template-empty">
          <div style="text-align: center">
            <Icon name="no_result" style="margin-bottom: 16px; font-size: 75px"></Icon>
            <br />
            <span>没有找到相关模板</span>
          </div>
        </el-row>
        <el-row v-show="!state.networkStatus" class="template-empty">
          {{ t('visualization.market_network_tips') }}
        </el-row>
      </el-row>
    </el-row>
  </el-row>
  <XpackComponent ref="openHandler" jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI=" />
</template>

<script setup lang="ts">
import { searchMarket } from '@/api/templateMarket'
import { useEmbedded } from '@/store/modules/embedded'
import { useAppStoreWithOut } from '@/store/modules/app'
import elementResizeDetectorMaker from 'element-resize-detector'
import { nextTick, reactive, watch, onMounted, ref, computed } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
import { useCache } from '@/hooks/web/useCache'
import MarketPreviewV2 from '@/views/template-market/component/MarketPreviewV2.vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import CategoryTemplateV2 from '@/views/template-market/component/CategoryTemplateV2.vue'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { XpackComponent } from '@/components/plugin'
import { useEmitt } from '@/hooks/web/useEmitt'
import { Base64 } from 'js-base64'
const { t } = useI18n()
const { wsCache } = useCache()
const embeddedStore = useEmbedded()
const appStore = useAppStoreWithOut()
const interactiveStore = interactiveStoreWithOut()

// full 正常展示 marketPreview 模板中心预览 createPreview 创建界面预览
const previewModel = ref('full')
const emits = defineEmits(['close'])

const close = () => {
  emits('close')
}

const title = computed(() => (state.curPosition === 'branch' ? '模板中心' : '使用模板新建'))
const isEmbedded = computed(() => appStore.getIsDataEaseBi || appStore.getIsIframe)
const state = reactive({
  initReady: true,
  curPosition: 'branch',
  pid: null,
  treeProps: {
    value: 'label',
    label: 'label'
  },
  templateType: 'all', // 模板类型 仪表板 数据大屏
  templateSourceType: 'all', // 模板来源 模板市场 模板管理
  templateClassifyType: 'all', // 模板分类 样式模板 应用模板
  treeShow: true,
  templateClassifyOptions: [
    {
      value: 'all',
      label: '全部分类'
    },
    {
      value: 'app',
      label: '应用模板'
    },
    {
      value: 'template',
      label: '样式模板'
    }
  ],
  templateSourceOptions: [
    {
      value: 'all',
      label: '全部来源'
    },
    {
      value: 'market',
      label: '模板市场'
    },
    {
      value: 'manage',
      label: '模板管理'
    }
  ],
  templateTypeOptions: [
    {
      value: 'all',
      label: '全部类型'
    },
    {
      value: 'PANEL',
      label: '仪表板'
    },
    {
      value: 'SCREEN',
      label: '数据大屏'
    }
  ],
  loading: false,
  hasResult: true,
  templateMiniWidth: 250,
  templateCurWidth: 310,
  templateSpan: '25%',
  previewVisible: false,
  templatePreviewId: '',
  marketTabs: [],
  marketActiveTab: null,
  searchText: null,
  dvCreateForm: {
    resourceName: null,
    name: null,
    pid: null,
    nodeType: 'panel',
    templateUrl: null,
    newFrom: 'new_market_template',
    templateId: null,
    panelType: 'self',
    panelStyle: {},
    panelData: '[]'
  },
  panelGroupList: [],
  curApplyTemplate: null,
  folderSelectShow: false,
  baseUrl: 'https://dataease.io/templates',
  currentMarketTemplateShowList: [],
  curTemplateShowFilter: [],
  curTemplateIndex: 0,
  curTemplate: null,
  networkStatus: true,
  rule: {
    name: [
      {
        required: true,
        message: t('visualization.template_name_tips'),
        trigger: 'blur'
      }
    ],
    pid: [
      {
        required: true,
        message: '',
        trigger: 'blur'
      }
    ]
  }
})

const createAuth = computed(() => {
  const authMap = interactiveStore.getData
  return {
    PANEL: authMap['0']?.menuAuth && authMap['0']?.anyManage,
    SCREEN: authMap['1']?.menuAuth && authMap['1']?.anyManage
  }
})

const categoriesComputed = computed(() => {
  let result
  if (state.templateSourceType === 'all') {
    result = state.marketTabs
  } else {
    result = state.marketTabs.filter(
      category => category.source === 'public' || category.source === state.templateSourceType
    )
  }
  return result
})

const curTemplateImg = computed(() => {
  if (
    state.curTemplate.thumbnail.indexOf('http') > -1 ||
    state.curTemplate.thumbnail.indexOf('static-resource') > -1
  ) {
    return imgUrlTrans(state.curTemplate.thumbnail)
  } else {
    return imgUrlTrans(state.baseUrl + state.curTemplate.thumbnail)
  }
})

const outPaddingState = computed(() => {
  return state.curPosition === 'branch' && previewModel.value !== 'marketPreview'
})

const optInit = params => {
  state.initReady = false
  state.curPosition = params.curPosition
  state.templateType = params.templateType
  previewModel.value = 'full'
  state.pid = params.pid
  nextTick(() => {
    state.initReady = true
  })
}
watch(
  () => state.searchText,
  () => {
    initTemplateShow()
  }
)

watch(
  () => state.templateType,
  () => {
    initTemplateShow()
  }
)

watch(
  () => state.templateSourceType,
  () => {
    state.treeShow = false
    initTemplateShow()
    nextTick(() => {
      state.treeShow = true
      initStyle()
    })
  }
)
watch(
  () => state.templateClassifyType,
  () => {
    state.treeShow = false
    initTemplateShow()
    nextTick(() => {
      state.treeShow = true
      initStyle()
    })
  }
)

const nodeClick = data => {
  state.marketActiveTab = data.label
  initTemplateShow()
}
const closePreview = () => {
  previewModel.value = 'full'
}

const initMarketTemplate = async () => {
  await searchMarket()
    .then(rsp => {
      state.baseUrl = rsp.data.baseUrl
      state.currentMarketTemplateShowList = rsp.data.contents
      state.marketTabs = rsp.data.categories
      state.marketActiveTab = state.marketTabs[1].label
      initStyle()
      initTemplateShow()
    })
    .catch(() => {
      state.networkStatus = false
    })
}

const initStyle = () => {
  nextTick(() => {
    const tree = document.querySelector('.custom-market-tree')
    // 创建横线元素
    const line = document.createElement('hr')
    line.classList.add('custom-line')

    // 将横线元素插入到第一个选项后面
    tree.firstElementChild.appendChild(line)
  })
}

const preOne = () => {
  if (state.curTemplateIndex > 0) {
    state.curTemplateIndex--
    state.curTemplate = state.curTemplateShowFilter[state.curTemplateIndex]
  }
}

const nextOne = () => {
  if (state.curTemplateIndex < state.curTemplateShowFilter.length - 1) {
    state.curTemplateIndex++
    state.curTemplate = state.curTemplateShowFilter[state.curTemplateIndex]
  }
}

const templateApply = template => {
  state.curApplyTemplate = template
  state.dvCreateForm.name = template.title
  state.dvCreateForm.nodeType = template.templateType
  if (template.source === 'market') {
    state.dvCreateForm.newFrom = 'new_market_template'
    state.dvCreateForm.templateUrl = template.metas.theme_repo
    state.dvCreateForm.resourceName = template.id
  } else {
    state.dvCreateForm.newFrom = 'new_inner_template'
    state.dvCreateForm.templateId = template.id
  }
  apply(template)
}

const apply = template => {
  if (state.dvCreateForm.newFrom === 'new_market_template' && !state.dvCreateForm.templateUrl) {
    ElMessage.warning('未获取模板下载链接请联系模板市场官方')
    return false
  }
  const templateTemplate = {
    newFrom: state.dvCreateForm.newFrom,
    templateUrl: state.dvCreateForm.templateUrl,
    resourceName: state.dvCreateForm.resourceName,
    templateId: state.dvCreateForm.templateId
  }
  state.curApplyTemplate.recentUseTime = Date.now()
  state.curApplyTemplate.categoryNames.push('最近使用')
  const baseUrl =
    (['dataV', 'SCREEN'].includes(state.dvCreateForm.nodeType)
      ? '#/dvCanvas?opt=create&createType=template'
      : '#/dashboard?opt=create&createType=template') +
    '&templateParams=' +
    encodeURIComponent(Base64.encode(JSON.stringify(templateTemplate)))
  let newWindow = null
  if (isEmbedded.value) {
    embeddedStore.clearState()
    embeddedStore.setCreateType('template')
    embeddedStore.setTemplateParams(
      encodeURIComponent(Base64.encode(JSON.stringify(templateTemplate)))
    )
    embeddedStore.setOpt('create')

    if (state.pid) {
      embeddedStore.setPid(state.pid)
    }
    useEmitt().emitter.emit(
      'changeCurrentComponent',
      ['dataV', 'SCREEN'].includes(state.dvCreateForm.nodeType)
        ? 'VisualizationEditor'
        : 'DashboardEditor'
    )
    return
  }
  if (state.pid) {
    newWindow = window.open(baseUrl + `&pid=${state.pid}`, '_blank')
  } else {
    newWindow = window.open(baseUrl, '_blank')
  }
  initOpenHandler(newWindow)
}
const openHandler = ref(null)
const initOpenHandler = newWindow => {
  if (openHandler?.value) {
    const pm = {
      methodName: 'initOpenHandler',
      args: newWindow
    }
    openHandler.value.invokeMethod(pm)
  }
}

const initTemplateShow = () => {
  let tempHasResult = false
  state.currentMarketTemplateShowList.forEach(template => {
    template.showFlag = templateShow(template)
    if (template.showFlag) {
      tempHasResult = true
    }
  })
  if (state.currentMarketTemplateShowList.length > 0) {
    state.hasResult = tempHasResult
  }
}

const templateShow = templateItem => {
  let searchMarch = false
  let templateTypeMarch = false
  let templateSourceTypeMarch = false
  let templateClassifyTypeMarch = false
  if (!state.searchText || templateItem.title.indexOf(state.searchText) > -1) {
    searchMarch = true
  }

  if (state.templateType === 'all' || templateItem.templateType === state.templateType) {
    templateTypeMarch = true
  }

  if (state.templateSourceType === 'all' || templateItem.source === state.templateSourceType) {
    templateSourceTypeMarch = true
  }

  if (
    state.templateClassifyType === 'all' ||
    templateItem.classify === state.templateClassifyType
  ) {
    templateClassifyTypeMarch = true
  }
  return searchMarch && templateTypeMarch && templateSourceTypeMarch && templateClassifyTypeMarch
}

const templatePreview = previewId => {
  if (state.curPosition === 'branch') {
    // 模板中心模式
    state.templatePreviewId = previewId
    previewModel.value = 'marketPreview'
  } else {
    state.curTemplateShowFilter =
      state.marketActiveTab === '推荐'
        ? state.currentMarketTemplateShowList.filter(ele => ele.showFlag)
        : state.currentMarketTemplateShowList.filter(
            ele => ele.showFlag && ele.categoryNames?.includes(state.marketActiveTab)
          )
    state.curTemplateIndex = state.curTemplateShowFilter.findIndex(temp => temp.id === previewId)
    state.curTemplate = state.curTemplateShowFilter[state.curTemplateIndex]
    previewModel.value = 'createPreview'
  }
}

onMounted(() => {
  previewInit()
  initMarketTemplate()
  const erd = elementResizeDetectorMaker()
  const templateMainDom = document.getElementById('template-show-area')
  // 监听div变动事件
  if (templateMainDom) {
    erd.listenTo(templateMainDom, () => {
      nextTick(() => {
        const offsetWidth = templateMainDom.offsetWidth - 26
        const curSeparator = Math.trunc(offsetWidth / state.templateMiniWidth)
        state.templateSpan = 100 / Math.trunc(offsetWidth / state.templateMiniWidth) + '%'
        state.templateCurWidth = Math.trunc(offsetWidth / curSeparator) - 33
      })
    })
  }
})

const previewInit = () => {
  const previewId = wsCache.get('template-preview-id')
  if (previewId) {
    templatePreview(previewId)
    wsCache.delete('template-preview-id')
  }
}

defineExpose({
  optInit
})
</script>

<style lang="less" scoped>
.template-outer-body-padding {
  padding: 24px;
}
.template-outer-body {
  .main-container {
    display: flex;
    flex-wrap: nowrap;
    flex-direction: column;
    width: 100%;
    height: 100%;
    .market-head {
      height: 56px;
      background: #ffffff;
      align-items: center;
      padding: 12px 24px;
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      span {
        font-size: 16px;
        font-color: #1f2329;
        font-weight: 500;
      }
      .head-right {
        flex: 1;
        justify-content: right;
        .title-search {
          width: 320px;
        }
        .title-type {
          margin-left: 12px;
          width: 104px;
        }
      }
    }
    .template-area {
      height: calc(100vh - 135px);
      .template-left {
        padding: 8px;
        width: 204px;
        height: 100%;
        overflow-y: auto;
        background: #ffffff;
      }
      .template-right {
        flex: 1;
        display: inline;
        height: 100%;
        background: rgba(239, 240, 241, 1);
        overflow-y: auto;
        padding: 0 12px 16px 12px;
      }

      .template-empty {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
        background: rgba(239, 240, 241, 1);
        overflow-y: auto;
      }
    }
  }
}

.main-head {
  width: 100%;
  float: left;
  height: 24px;
  margin-top: 16px;
  display: inline;
  .custom-split-line {
    margin: 4px 8px 0 12px;
    width: 2px;
    height: 16px;
    background: var(--ed-color-primary);
    float: left;
  }
  .custom-category {
    float: left;
    font-weight: 500;
    font-size: 16px;
    color: rgba(31, 35, 41, 1);
  }
  .custom-search {
    float: left;
    font-weight: 500;
    font-size: 16px;
    color: var(--ed-color-primary);
    margin-right: 8px;
  }

  .custom-search-result {
    float: left;
    font-weight: 500;
    font-size: 16px;
    color: rgba(31, 35, 41, 1);
    margin-right: 8px;
  }
}

.custom-divider-line {
  height: 16px;
  margin: 8px 14px 0 16px !important;
}

.custom-market-icon {
  font-size: 20px;
  margin-top: 4px;
  height: 24px !important;
  width: 24px !important;
  cursor: pointer;

  &.hover-icon_custom {
    border-radius: 4px;
    color: #646a73;

    &[aria-expanded='true'] {
      background: rgba(31, 35, 41, 0.1);
    }

    &:hover {
      background: rgba(31, 35, 41, 0.1);
    }

    &:active {
      background: rgba(31, 35, 41, 0.2);
    }
  }
}

.custom-back-icon {
  font-size: 20px;
  cursor: pointer;
  margin-right: 8px;
}

.img-main-create {
  display: inherit;
  justify-content: center;
  width: 100%;
  background: #0f1114;
  overflow-x: auto;
  overflow-y: hidden;
  height: 100%;
}
</style>

<style lang="less">
.custom-line {
  margin: 4px;
  background: rgba(31, 35, 41, 0.15);
  border: 0;
  height: 1px;
}
</style>
