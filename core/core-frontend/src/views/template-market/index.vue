<template>
  <el-row
    class="template-outer-body"
    :class="{ 'template-outer-body-padding': !previewModel }"
    v-loading="state.loading"
  >
    <market-preview-v2
      v-show="previewModel"
      :preview-id="state.templatePreviewId"
      @closePreview="closePreview"
      @templateApply="templateApply"
    ></market-preview-v2>
    <el-row v-show="!previewModel" class="main-container">
      <el-row class="market-head">
        <span>模版市场 </span>
        <el-row class="head-right">
          <el-input
            class="title-search"
            v-model="state.searchText"
            prefix-icon="Search"
            size="small"
            :placeholder="t('visualization.enter_template_name_tips')"
            :clearable="true"
          />
          <el-select
            class="title-type"
            v-model="state.templateType"
            size="small"
            placeholder="Select"
          >
            <el-option
              v-for="item in state.templateTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-row>
      </el-row>
      <el-row class="template-area">
        <div class="template-left">
          <el-tree
            menu
            :data="state.marketTabs"
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
          <el-col
            v-for="templateItem in state.currentMarketTemplateShowList"
            v-show="templateItem.showFlag"
            :key="templateItem.id"
            style="padding: 24px 12px 0; text-align: center; flex: 0"
            :style="{ width: state.templateSpan }"
          >
            <template-market-v2-item
              :key="'outer-' + templateItem.id"
              :template="templateItem"
              :base-url="state.baseUrl"
              :width="state.templateCurWidth"
              @templateApply="templateApply"
              @templatePreview="templatePreview"
            />
          </el-col>
        </div>
        <el-row v-show="state.networkStatus && !state.hasResult" class="template-empty">
          <div style="text-align: center">
            <Icon name="no_result" style="margin-bottom: 16px; font-size: 75px"></Icon>
            <br />
            <span>没有找到相关模版</span>
          </div>
        </el-row>
        <el-row v-show="!state.networkStatus" class="template-empty">
          {{ t('visualization.market_network_tips') }}
        </el-row>
      </el-row>
    </el-row>
  </el-row>
</template>

<script setup lang="ts">
import { getCategoriesObject, searchMarket } from '@/api/templateMarket'
import elementResizeDetectorMaker from 'element-resize-detector'
import { nextTick, reactive, watch, onMounted, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
import { decompression } from '@/api/visualization/dataVisualization'
import { useCache } from '@/hooks/web/useCache'
import TemplateMarketV2Item from '@/views/template-market/component/TemplateMarketV2Item.vue'
import MarketPreviewV2 from '@/views/template-market/component/MarketPreviewV2.vue'
const { t } = useI18n()
const { wsCache } = useCache()

const previewModel = ref(false)

const state = reactive({
  treeProps: {
    value: 'label',
    label: 'label'
  },
  templateType: 'all',
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
      label: '大屏'
    }
  ],
  loading: false,
  hasResult: true,
  templateMiniWidth: 270,
  templateCurWidth: 310,
  templateSpan: '25%',
  previewVisible: false,
  templatePreviewId: '',
  marketTabs: null,
  marketActiveTab: null,
  searchText: null,
  dvCreateForm: {
    resourceName: null,
    name: null,
    pid: null,
    nodeType: 'panel',
    templateUrl: null,
    newFrom: 'new_market_template',
    panelType: 'self',
    panelStyle: {},
    panelData: '[]'
  },
  panelGroupList: [],
  curApplyTemplate: null,
  folderSelectShow: false,
  baseUrl: 'https://dataease.io/templates',
  currentMarketTemplateShowList: [],
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

watch(
  () => state.searchText,
  value => {
    initTemplateShow()
  }
)

watch(
  () => state.templateType,
  value => {
    initTemplateShow()
  }
)
const nodeClick = data => {
  state.marketActiveTab = data.label
  initTemplateShow()
}
const closePreview = () => {
  previewModel.value = false
}

const initMarketTemplate = async () => {
  await searchMarket()
    .then(rsp => {
      state.baseUrl = rsp.data.baseUrl
      state.currentMarketTemplateShowList = rsp.data.contents
    })
    .catch(() => {
      state.networkStatus = false
    })
  getCategoriesObject()
    .then(rsp => {
      state.marketTabs = rsp.data
      state.marketActiveTab = state.marketTabs[0].label
      initStyle()
      initTemplateShow()
    })
    .catch(() => {
      state.networkStatus = false
    })
}

const initStyle = () => {
  nextTick(() => {
    const tree = document.querySelector('.ed-tree')
    // 创建横线元素
    const line = document.createElement('hr')
    line.classList.add('custom-line')

    // 将横线元素插入到第一个选项后面
    tree.firstElementChild.appendChild(line)
  })
}
const normalizer = node => {
  // 去掉children=null的属性
  if (node.children === null || node.children === 'null') {
    delete node.children
  }
}

const templateApply = template => {
  state.curApplyTemplate = template
  state.dvCreateForm.name = template.title
  state.dvCreateForm.templateUrl = template.metas.theme_repo
  state.dvCreateForm.resourceName = template.id
  apply()
}

const apply = () => {
  if (!state.dvCreateForm.templateUrl) {
    ElMessage.warning('未获取模板下载链接请联系模板市场官方')
    return false
  }
  state.loading = true
  decompression(state.dvCreateForm)
    .then(response => {
      state.loading = false
      const templateData = response.data
      // do create
      wsCache.set(`de-template-data`, JSON.stringify(templateData))
      const baseUrl =
        templateData.type === 'dataV'
          ? '#/dvCanvas?opt=create&createType=template'
          : '#/dashboard?opt=create&createType=template'
      window.open(baseUrl, '_blank')
    })
    .catch(() => {
      state.loading = false
    })
}
const handleClick = item => {
  // do handleClick
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
  let categoryMarch = false
  let searchMarch = false
  let templateTypeMarch = false
  if (state.marketActiveTab === '最近使用') {
    if (templateItem.recentUseTime) {
      categoryMarch = true
    }
  } else if (state.marketActiveTab === '推荐') {
    if (templateItem.suggest === 'Y') {
      categoryMarch = true
    }
  } else {
    templateItem.categories.forEach(category => {
      if (category.name === state.marketActiveTab) {
        categoryMarch = true
      }
    })
  }

  if (!state.searchText || templateItem.title.indexOf(state.searchText) > -1) {
    searchMarch = true
  }

  if (state.templateType === 'all' || templateItem.templateType === state.templateType) {
    templateTypeMarch = true
  }
  return categoryMarch && searchMarch && templateTypeMarch
}

const templatePreview = previewId => {
  state.templatePreviewId = previewId
  previewModel.value = true
}

onMounted(() => {
  previewInit()
  initMarketTemplate()
  const erd = elementResizeDetectorMaker()
  const templateMainDom = document.getElementById('template-show-area')
  // 监听div变动事件
  if (templateMainDom) {
    erd.listenTo(templateMainDom, element => {
      nextTick(() => {
        const curSeparator = Math.trunc(templateMainDom.offsetWidth / state.templateMiniWidth)
        state.templateSpan =
          100 / Math.trunc(templateMainDom.offsetWidth / state.templateMiniWidth) + '%'
        state.templateCurWidth = Math.trunc(templateMainDom.offsetWidth / curSeparator) - 33
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
        display: inherit;
        height: 100%;
        background: rgba(239, 240, 241, 1);
        overflow-y: auto;
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
</style>

<style lang="less">
.custom-line {
  margin: 4px;
  background: rgba(31, 35, 41, 0.15);
  border: 0;
  height: 1px;
}
</style>
