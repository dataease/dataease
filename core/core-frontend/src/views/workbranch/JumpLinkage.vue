<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive } from 'vue'
const { t } = useI18n()
const dialogVisible = ref(true)
const formInline = reactive({
  user: '',
  region: '',
  date: ''
})
const checkAll = ref(false)
const selectValue = ref(false)
const jumpType = ref('1')
const openType = ref('1')
const isIndeterminate = ref(true)
const checkedFields = ref(['Shanghai', 'Beijing'])
const fields = ['Shanghai', 'Beijing', 'Guangzhou', 'Shenzhen']

const handleCheckAllChange = (val: boolean) => {
  checkedFields.value = val ? fields : []
  isIndeterminate.value = false
}
const handleCheckedFieldsChange = (value: string[]) => {
  const checkedCount = value.length
  checkAll.value = checkedCount === fields.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < fields.length
}
</script>

<template>
  <el-dialog class="jump-linkage" v-model="dialogVisible" width="1000px" title="跳转设置">
    <div class="chart-dataset-name">
      <div class="chart-name">
        <span class="label">已选择图表:</span>
        <span class="name">
          <el-icon class="main-color">
            <Icon class="toolbar-icon" name="icon_left_outlined" />
          </el-icon>
          折线图1
        </span>
      </div>
      <div class="chart-name dataset">
        <span class="label">所用数据集:</span>
        <span class="name">
          <el-icon class="main-color">
            <Icon class="toolbar-icon" name="icon_left_outlined" />
          </el-icon>
          佩尔数据集
        </span>
      </div>
    </div>
    <div class="field-config">
      <div class="select-field">
        <div class="title">
          选择字段
          <el-switch v-model="selectValue" inactive-text="仅看已选" />
        </div>
        <div class="select-all">
          <el-checkbox
            v-model="checkAll"
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
            >全选</el-checkbox
          >
        </div>
        <div class="field-list">
          <el-checkbox-group v-model="checkedFields" @change="handleCheckedFieldsChange">
            <div
              v-for="field in fields"
              :key="field"
              class="list-item_primary"
              :class="[checkedFields.includes(field) ? 'active' : '']"
            >
              <el-checkbox :label="field">{{ field }}</el-checkbox>
            </div>
          </el-checkbox-group>
        </div>
      </div>
      <div class="config-panel">
        <div class="operation-type">
          <div class="type">
            <span class="label">跳转类型</span>
            <el-radio-group v-model="jumpType">
              <el-radio label="1">外部链接</el-radio>
              <el-radio label="2">系统仪表板</el-radio>
            </el-radio-group>
          </div>
          <div class="type">
            <span class="label">打开方式</span>
            <el-radio-group v-model="openType">
              <el-radio label="1">当前页面</el-radio>
              <el-radio label="2">新开页面</el-radio>
            </el-radio-group>
          </div>
        </div>
        <div class="panel-field">
          <el-form label-position="top" :inline="true" :model="formInline" class="panel-form-field">
            <el-form-item label="Approved by">
              <el-select v-model="formInline.region" placeholder="Activity zone" clearable>
                <el-option label="Zone one" value="shanghai" />
                <el-option label="Zone two" value="beijing" />
              </el-select>
            </el-form-item>
            <el-form-item label="Activity zone">
              <el-select v-model="formInline.region" placeholder="Activity zone" clearable>
                <el-option label="Zone one" value="shanghai" />
                <el-option label="Zone two" value="beijing" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button>{{ t('chart.cancel') }} </el-button>
        <el-button type="primary">{{ t('chart.confirm') }} </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="less">
.jump-linkage {
  font-family: PingFang SC;
  font-size: 14px;

  .chart-dataset-name {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    .chart-name {
      .label {
        color: #646a73;
      }
      .name {
        color: #1f2329;

        .main-color {
          color: #3370ff;
        }
      }
    }
    .dataset {
      margin-left: 24px;
    }
  }

  .field-config {
    width: 952px;
    height: 416px;
    border: 1px solid #dee0e3;
    border-radius: 4px;

    .select-field {
      height: 100%;
      float: left;
      width: 259px;
      border-right: 1px solid #dee0e3;

      .title {
        margin: 16px 16px 0 16px;
        color: #1f2329;
        font-weight: 500;
        display: flex;

        .ed-switch {
          margin-left: auto;
          .ed-switch__label {
            color: #646a73;
          }
        }
      }
      .select-all {
        height: 40px;
        display: flex;
        align-items: center;
        padding-left: 15px;
      }

      .field-list {
        .list-item_primary {
          border-radius: 0;
          padding: 12px 15px;
        }
      }
    }

    .config-panel {
      height: 100%;
      margin-left: 259px;
      width: calc(100% - 259px);
      .operation-type {
        padding: 16px;
        padding-bottom: 0;
        border-bottom: 1px solid #dee0e3;

        .type {
          margin-bottom: 16px;
          display: flex;
          align-items: center;

          .label {
            color: #646a73;
            margin-right: 16px;
          }
        }
      }

      .panel-field {
        padding: 16px;

        .panel-form-field {
          .ed-select {
            width: 270px;
          }
        }
      }
    }
  }
}
</style>
