<template>
  <el-col class="main-head">
    <div class="custom-split-line"></div>
    <span v-show="!searchText" class="custom-category">{{ label }}</span>
    <span v-show="searchText" class="custom-search">{{ label }}</span>
    <span v-if="searchText" class="custom-search-result">的搜索结果是{{ searchResult }}个</span>
  </el-col>
  <el-col
    v-for="(templateItem, index) in fullTemplateShowList"
    v-show="showFlagCheck(templateItem)"
    :key="templateItem.id + label"
    style="float: left; padding: 8px 8px 0; text-align: center; flex: 0"
    :style="{ width: templateSpan }"
  >
    <template-market-v2-item
      :key="index"
      :template="templateItem"
      :base-url="baseUrl"
      :width="templateCurWidth"
      :cur-position="curPosition"
      :create-auth="createAuth"
      @templateApply="templateApply"
      @templatePreview="templatePreview"
    />
  </el-col>
</template>

<script setup lang="ts">
import TemplateMarketV2Item from '@/views/template-market/component/TemplateMarketV2Item.vue'
import { computed } from 'vue'
const emits = defineEmits(['templateApply', 'templatePreview'])

const templateApply = params => {
  emits('templateApply', params)
}

const templatePreview = params => {
  emits('templatePreview', params)
}

const searchResult = computed(
  () => props.fullTemplateShowList.filter(item => showFlagCheck(item)).length
)

const showFlagCheck = template => {
  return template.showFlag && template.categoryNames?.includes(props.label)
}

const props = defineProps({
  searchText: {
    type: String
  },
  baseUrl: {
    type: String
  },
  templateCurWidth: {
    type: Number
  },
  curPosition: {
    type: String
  },
  templateSpan: {
    type: String
  },
  label: {
    type: String
  },
  fullTemplateShowList: {
    type: Array,
    default: () => []
  },
  createAuth: {
    type: Object,
    default() {
      return {
        PANEL: false,
        SCREEN: false
      }
    }
  }
})
</script>

<style lang="less" scoped>
.main-head {
  width: 100%;
  float: left;
  height: 24px;
  margin-top: 16px;
  display: inline;
  margin-bottom: 16px;
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
</style>
