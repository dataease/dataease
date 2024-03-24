<template>
  <el-col>
    <el-row style="display: inherit; margin-top: 5px">
      <el-row>
        <el-input
          v-model="state.templateFilterText"
          :placeholder="t('visualization.filter_keywords')"
          size="small"
          clearable
          prefix-icon="el-icon-search"
        />
      </el-row>
      <el-row style="display: inherit; margin-top: 5px">
        <el-tree
          ref="templateTree"
          :default-expanded-keys="state.defaultExpandedKeys"
          :data="templateList"
          node-key="id"
          :expand-on-click-node="true"
          :filter-node-method="filterNode"
          :highlight-current="true"
          @node-click="nodeClick"
        >
          <template #default="{ data }">
            <span class="custom-tree-node">
              <span class="custom-label">
                <el-icon style="font-size: 18px" v-if="data.nodeType === 'folder'">
                  <Icon name="dv-folder"></Icon>
                </el-icon>
                <el-icon style="font-size: 18px" v-else-if="data.dvType === 'dashboard'">
                  <Icon name="dv-dashboard-spine"></Icon>
                </el-icon>
                <el-icon class="icon-screen-new" style="font-size: 18px" v-else>
                  <Icon name="icon_operation-analysis_outlined"></Icon>
                </el-icon>
                <span :title="data.name" class="custom-name">{{ data.name }}</span>
              </span>
            </span>
          </template>
        </el-tree>
      </el-row>
    </el-row>
  </el-col>
</template>

<script setup lang="ts">
import { findOne } from '@/api/template'
import { useI18n } from '@/hooks/web/useI18n'
import { reactive } from 'vue'
const { t } = useI18n()
const emits = defineEmits(['showCurrentTemplateInfo'])

defineProps({
  curCanvasType: {
    type: String,
    required: true
  },
  templateList: {
    type: Array,
    default: function () {
      return []
    }
  }
})

const state = reactive({
  templateFilterText: '',
  defaultExpandedKeys: [],
  currentTemplateShowList: []
})

const filterNode = (value, data) => {
  if (!value) return true
  return data.label.indexOf(value) !== -1
}

const nodeClick = data => {
  if (data.nodeType === 'template') {
    findOne(data.id).then(res => {
      emits('showCurrentTemplateInfo', res.data)
    })
  }
}
</script>

<style scoped lang="less">
.custom-label {
  display: flex;
  flex: 1 1 0%;
  width: 0px;
}

.custom-name {
  margin-left: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
</style>
