<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive } from 'vue'
import type { FormInstance } from 'element-plus-secondary'

interface FieldsItem {
  sourceField: string
  targetField: string
  targetPanel: string
}

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

const fieldFormRef = ref<FormInstance>()
const formFields = reactive<{
  fields: FieldsItem[]
}>({
  fields: [
    {
      sourceField: '1',
      targetField: '2',
      targetPanel: '3'
    },
    {
      sourceField: '5',
      targetField: '6',
      targetPanel: '7'
    }
  ]
})

const removeField = (item: FieldsItem) => {
  const index = formFields.fields.indexOf(item)
  if (index !== -1) {
    formFields.fields.splice(index, 1)
  }
}

const addField = () => {
  formFields.fields.push({
    sourceField: '',
    targetField: '',
    targetPanel: ''
  })
}

const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate(valid => {
    if (valid) {
      console.log('submit!')
    } else {
      console.log('error submit!')
      return false
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
}

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
          <div class="field-scroll">
            <el-form
              label-position="top"
              :inline="true"
              :model="formInline"
              class="panel-form-field"
            >
              <el-form-item label="当前仪表板">
                <el-select v-model="formInline.region" placeholder="Activity zone" clearable>
                  <el-option label="Zone one" value="shanghai" />
                  <el-option label="Zone two" value="beijing" />
                </el-select>
              </el-form-item>
              <el-icon class="join">
                <Icon name="join-join"></Icon>
              </el-icon>
              <el-form-item label="目标仪表板">
                <el-select v-model="formInline.region" placeholder="Activity zone" clearable>
                  <el-option label="Zone one" value="shanghai" />
                  <el-option label="Zone two" value="beijing" />
                </el-select>
              </el-form-item>
            </el-form>
            <el-form
              ref="fieldFormRef"
              hide-required-asterisk
              label-position="top"
              :inline="true"
              :model="formFields"
              class="field-form"
              :class="[formFields.fields.length > 1 ? 'show-trash' : 'only-one']"
            >
              <template v-for="(field, index) in formFields.fields" :key="field.sourceField">
                <el-form-item
                  label="源字段"
                  :prop="'fields.' + index + '.sourceField'"
                  :rules="{
                    required: true,
                    message: '源字段 can not be null',
                    trigger: 'change'
                  }"
                >
                  <el-select v-model="field.sourceField" placeholder="Activity zone" clearable>
                    <el-option label="Zone one" value="shanghai" />
                    <el-option label="Zone two" value="beijing" />
                  </el-select>
                </el-form-item>
                <el-icon class="join">
                  <Icon name="join-join"></Icon>
                </el-icon>
                <el-form-item
                  label="目标字段"
                  :prop="'fields.' + index + '.targetPanel'"
                  :rules="{
                    required: true,
                    message: '目标字段 can not be null',
                    trigger: 'change'
                  }"
                >
                  <el-select v-model="field.targetPanel" placeholder="Activity zone" clearable>
                    <el-option label="Zone one" value="shanghai" />
                    <el-option label="Zone two" value="beijing" />
                  </el-select>
                </el-form-item>
                <el-form-item
                  :label="'&nbsp;'"
                  :prop="'fields.' + index + '.targetField'"
                  :rules="{
                    required: true,
                    message: '字段 can not be null',
                    trigger: 'change'
                  }"
                >
                  <el-select v-model="field.targetField" placeholder="Activity zone" clearable>
                    <el-option label="Zone one" value="shanghai" />
                    <el-option label="Zone two" value="beijing" />
                  </el-select>
                </el-form-item>
                <el-button v-if="formFields.fields.length > 1" text @click="removeField(field)">
                  <template #icon>
                    <Icon name="icon_delete-trash_outlined"></Icon>
                  </template>
                </el-button>
              </template>
            </el-form>
          </div>
          <el-button class="add-field" text @click="addField">
            <template #icon>
              <Icon name="icon_add_outlined"></Icon>
            </template>
            添加联动视图字段
          </el-button>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="resetForm(fieldFormRef)">{{ t('chart.cancel') }} </el-button>
        <el-button @click="submitForm(fieldFormRef)" type="primary"
          >{{ t('chart.confirm') }}
        </el-button>
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
        padding: 9px;
        padding-bottom: 0;
        border-bottom: 1px solid #dee0e3;

        .type {
          margin-bottom: 9px;
          display: flex;
          align-items: center;

          .label {
            color: #646a73;
            margin-right: 16px;
          }
        }
      }

      .panel-field {
        height: 323px;

        .field-scroll {
          width: 100%;
          padding: 16px 16px 8px 16px;
          height: 280px;
          overflow: auto;
        }

        .add-field {
          margin-left: 16px;
        }
        .join {
          font-size: 18px;
          margin: 37px 8px 0;
        }

        .panel-form-field {
          .ed-form-item {
            margin: 0 0 24px 0;
          }
          .ed-select {
            width: 312px;
          }
        }

        .field-form {
          .ed-form-item {
            margin: 0 0 8px 0;
          }
          &.show-trash {
            .ed-form-item {
              &:nth-child(5n - 1) {
                margin-left: 8px;
              }
            }
            :nth-child(5n) {
              margin: 32px 0 0 5px;
              color: #646a73;
              font-size: 14px;
            }
          }

          &.only-one {
            .ed-form-item {
              &:nth-child(4n) {
                margin-left: 8px;
              }
            }
          }

          .ed-select {
            width: 196px;
          }
        }
      }
    }
  }
}
</style>
