<template>
  <el-popover width="400" trigger="click" style="max-height: 400px; overflow-y: auto">
    <el-row>
      <el-col :span="11">
        <div class="ellipsis-area">{{ sourceLinkageInfo.targetViewName }}</div>
      </el-col>
      <el-col :span="11">
        <div class="ellipsis-area">{{ linkageInfo.targetViewName }}</div>
      </el-col>
    </el-row>

    <el-row class="match-area">
      <el-row v-for="(itemLinkage, index) in linkageInfo.linkageFields" :key="index">
        <el-col :span="11">
          <div class="select-filed">
            <el-select v-model="itemLinkage.sourceField" size="small" placeholder="请选择">
              <el-option
                v-for="item in sourceLinkageInfo.targetViewFields"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <span style="float: left">
                  <svg-icon
                    v-if="item.deType === 0"
                    icon-class="field_text"
                    class="field-icon-text"
                  />
                  <svg-icon
                    v-if="item.deType === 1"
                    icon-class="field_time"
                    class="field-icon-time"
                  />
                  <svg-icon
                    v-if="item.deType === 2 || item.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <svg-icon
                    v-if="item.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  />
                </span>
                <span class="name-area">{{ item.name }}</span>
              </el-option>
            </el-select>
          </div>
        </el-col>
        <el-col :span="11">
          <div class="select-filed">
            <el-select v-model="itemLinkage.targetField" size="small" placeholder="请选择">
              <el-option
                v-for="item in linkageInfo.targetViewFields"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <span style="float: left">
                  <svg-icon
                    v-if="item.deType === 0"
                    icon-class="field_text"
                    class="field-icon-text"
                  />
                  <svg-icon
                    v-if="item.deType === 1"
                    icon-class="field_time"
                    class="field-icon-time"
                  />
                  <svg-icon
                    v-if="item.deType === 2 || item.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <svg-icon
                    v-if="item.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  />
                </span>
                <span class="name-area">{{ item.name }}</span>
              </el-option>
            </el-select>
          </div>
        </el-col>
        <el-col :span="2">
          <div>
            <el-button
              :icon="Delete"
              text
              size="small"
              class="delete-area"
              @click="deleteLinkageField(index)"
            />
          </div>
        </el-col>
      </el-row>
    </el-row>

    <el-row class="bottom">
      <el-button size="small" type="success" :icon="Plus" round @click="addLinkageField(null, null)"
        >追加联动依赖字段</el-button
      >
    </el-row>

    <template #reference>
      <el-icon class="bar-base-icon"><Edit /></el-icon>
    </template>
  </el-popover>
</template>

<script setup lang="ts">
import { checkSameDataSet } from '@/api/chart'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { computed, defineEmits, onMounted, reactive, toRefs } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
const dvMainStore = dvMainStoreWithOut()
const { linkageSettingStatus, targetLinkageInfo, curLinkageView } = storeToRefs(dvMainStore)

const props = defineProps({
  element: {
    type: Object,
    required: true
  },
  active: {
    type: Boolean,
    required: false,
    default: false
  },
  // 当前模式 preview 预览 edit 编辑，
  activeModel: {
    type: String,
    required: false,
    default: 'preview'
  }
})

const { element, active, activeModel } = toRefs(props)
const emits = defineEmits(['showViewDetails'])

const state = reactive({
  componentType: null,
  linkageActiveStatus: false,
  editFilter: ['view', 'custom']
})

const linkageInfo = computed(() => {
  return targetLinkageInfo.value[element.value.id]
})

const sourceLinkageInfo = computed(() => {
  return targetLinkageInfo.value[curLinkageView.value.id]
})

const showViewDetails = () => {
  emits('showViewDetails')
}
const edit = () => {
  return null
}

const linkageEdit = () => {
  return null
}

const deleteLinkageField = index => {
  linkageInfo.value.linkageFields.splice(index, 1)
}

const addLinkageField = (sourceFieldId, targetFieldId) => {
  const linkageFieldItem = {
    sourceField: sourceFieldId,
    targetField: targetFieldId
  }
  linkageInfo.value.linkageFields.push(linkageFieldItem)
}

onMounted(() => {
  // 初始化映射关系 如果当前是相同的数据集且没有关联关系，则自动补充映射关系
  checkSameDataSet(curLinkageView.value.id, element.value.id).then(res => {
    if (res.data === 'YES' && linkageInfo.value.linkageFields.length === 0) {
      sourceLinkageInfo.value.targetViewFields.forEach(item => {
        addLinkageField(item.id, item.id)
      })
    }
  })
})
</script>

<style lang="less" scoped>
.custom-icon {
  color: white;
}
.name-area {
  float: left;
  color: #8492a6;
  font-size: 12px;
}
.slot-class {
  color: white;
}

.bottom {
  margin-top: 20px;
  text-align: center;
}
.ellipsis-area {
  margin-left: 10px;
  margin-right: 10px;
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  background-color: #f7f8fa;
  color: #3d4d66;
  font-size: 12px;
  line-height: 24px;
  height: 24px;
  border-radius: 3px;
}

.select-filed {
  margin-left: 10px;
  margin-right: 10px;
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  color: #3d4d66;
  font-size: 12px;
  line-height: 35px;
  height: 35px;
  border-radius: 3px;
}
.ed-popover {
  height: 200px;
  overflow: auto;
}

.bar-base-icon {
  height: 22px;
  width: 22px;
  color: #ffffff;
  &:hover {
    color: rgba(255, 255, 255, 0.5);
  }
  &:active {
    color: rgba(255, 255, 255, 0.7);
  }
}

.bottom {
  margin-top: 20px;
  justify-content: center;
}

.delete-area {
  float: left;
  margin-top: 5px;
}

.match-area {
  display: inline-block;
  height: 120px;
  overflow-y: auto;
}
</style>
