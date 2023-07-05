<script lang="ts" setup>
import { ref, reactive, nextTick } from 'vue'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import type { DatasetDetail } from '@/api/dataset'
import { getDsDetails } from '@/api/dataset'
import { cloneDeep } from 'lodash-es'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const { componentData } = storeToRefs(dvMainStore)

const dialogVisible = ref(false)
const renameInput = ref()
const valueSource = ref([])
const options = [
  {
    label: '所用数据集',
    options: [
      {
        value: 'Shanghai',
        label: 'Shanghai'
      },
      {
        value: 'Beijing',
        label: 'Beijing'
      }
    ]
  },
  {
    label: '其他数据集',
    options: [
      {
        value: 'Chengdu',
        label: 'Chengdu'
      },
      {
        value: 'Shenzhen',
        label: 'Shenzhen'
      },
      {
        value: 'Guangzhou',
        label: 'Guangzhou'
      },
      {
        value: 'Dalian',
        label: 'Dalian'
      }
    ]
  }
]
const conditions = ref([])
const displayType = ref('')
const checkAll = ref(false)
const activeConditionForRename = reactive({
  id: '',
  name: '',
  visible: false
})
const curComponent = ref()
const manual = ref()
const activeCondition = ref('')
const isIndeterminate = ref(true)
const fields = ref<DatasetDetail[]>()
let componentId = ''
const typeList = [
  {
    label: '重命名',
    command: 'rename'
  },
  {
    label: '删除',
    command: 'del'
  }
]
const checkedFields = ref([])
const handleCheckAllChange = (val: boolean) => {
  checkedFields.value = val ? fields.value.map(ele => ele.id) : []
  isIndeterminate.value = false
}
const handleCheckedFieldsChange = (value: string[]) => {
  const checkedCount = value.length
  checkAll.value = checkedCount === fields.value.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < fields.value.length
}

const cancelClick = () => {
  dialogVisible.value = false
}

const confirmClick = () => {
  let obj = componentData.value.find(ele => ele.id === componentId)
  obj.propValue = cloneDeep(conditions.value)
  dialogVisible.value = false
}

const cancelValueSource = () => {
  manual.value.hide()
}

const confirmValueSource = () => {
  curComponent.value.valueSource = cloneDeep(valueSource.value.filter(ele => ele.trim()))
}

const init = (id: string, queryId: string) => {
  componentId = id
  conditions.value = cloneDeep(componentData.value.find(ele => ele.id === id).propValue) || []
  curComponent.value = conditions.value.find(ele => ele.id === queryId)
  activeCondition.value = queryId
  valueSource.value = cloneDeep(curComponent.value.valueSource)
  if (!valueSource.value.length) {
    valueSource.value.push('')
  }
  checkedFields.value = Object.keys(curComponent.value.checkedFieldsMap) || []
  dialogVisible.value = true
  getDsDetails(['1668815327340331008', '1666719324084699136'])
    .then(res => {
      res.forEach(ele => {
        const { dimensionList, quotaList } = ele.fields
        ele.list = [...dimensionList, ...quotaList]
      })
      fields.value = res
    })
    .finally(() => {
      handleCheckedFieldsChange(checkedFields.value)
    })
}

const handleCondition = ele => {
  activeCondition.value = ele.id
}

const addOperation = (cmd, condition, index) => {
  switch (cmd) {
    case 'del':
      conditions.value.splice(index, 1)
      break
    case 'rename':
      Object.assign(activeConditionForRename, condition)
      nextTick(() => {
        renameInput.value[index].focus()
      })
      break
    default:
      break
  }
}

const renameInputBlur = () => {
  conditions.value.some(ele => {
    if (activeConditionForRename.id === ele.id) {
      ele.name = activeConditionForRename.name
      return true
    }
    return false
  })
  activeConditionForRename.id = ''
}

defineExpose({
  init
})
</script>

<template>
  <el-dialog
    class="query-condition-configuration"
    v-model="dialogVisible"
    width="1200px"
    title="查询条件设置"
    @click.stop
    @mousedown.stop
    @mousedup.stop
  >
    <div class="container">
      <div class="query-condition-list">
        <div class="title">
          查询条件
          <el-icon>
            <Icon name="icon_add_outlined"></Icon>
          </el-icon>
        </div>
        <div
          v-for="(condition, index) in conditions"
          :key="condition.id"
          @click="handleCondition(condition)"
          class="list-item_primary"
          :class="condition.id === activeCondition && 'active'"
        >
          <el-icon>
            <Icon name="more_v"></Icon>
          </el-icon>
          <div class="label">
            {{ condition.name }}
          </div>
          <div class="condition-icon flex-align-center">
            <handle-more
              @handle-command="cmd => addOperation(cmd, condition, index)"
              :menu-list="typeList"
              icon-name="more_v"
              placement="bottom-end"
            ></handle-more>
            <el-icon @click.stop="condition.visible = !condition.visible" v-if="condition.visible">
              <Icon name="icon_visible_outlined"></Icon>
            </el-icon>
            <el-icon @click.stop="condition.visible = !condition.visible" v-else>
              <Icon name="de_pwd_invisible"></Icon>
            </el-icon>
            <div @click.stop v-if="activeConditionForRename.id === condition.id" class="rename">
              <el-input
                @blur="renameInputBlur"
                ref="renameInput"
                v-model="activeConditionForRename.name"
              ></el-input>
            </div>
          </div>
        </div>
      </div>
      <div class="chart-field">
        <div class="title">选择图表及字段</div>
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
            <div v-for="field in fields" :key="field.id" class="list-item">
              <el-checkbox :label="field.id"
                ><el-icon> <Icon name="icon_add_outlined"></Icon> </el-icon
                ><span class="checkbox-name ellipsis">{{ field.name }}</span></el-checkbox
              >
              <span class="dataset ellipsis">{{ field.name }}</span>
              <el-select v-model="curComponent.checkedFieldsMap[field.id]" clearable>
                <el-option
                  v-for="ele in field.list"
                  :key="ele.id"
                  :label="ele.name"
                  :value="ele.id"
                />
              </el-select>
            </div>
          </el-checkbox-group>
        </div>
      </div>
      <div class="condition-configuration">
        <div class="title">查询条件配置</div>
        <div class="configuration-list">
          <div class="list-item">
            <div class="label">展示类型</div>
            <div class="value">
              <el-select v-model="displayType" clearable>
                <el-option label="Zone one" value="shanghai" />
                <el-option label="Zone two" value="beijing" />
              </el-select>
            </div>
          </div>
          <div class="list-item top-item">
            <div class="label">选项值来源</div>
            <div class="value">
              <div class="value">
                <el-radio-group v-model="curComponent.optionValueSource">
                  <el-radio :label="0">自动</el-radio>
                  <el-radio :label="1">选择数据集</el-radio>
                  <el-radio :label="2">手动输入</el-radio>
                </el-radio-group>
              </div>
              <template v-if="curComponent.optionValueSource === 1">
                <div class="value">
                  <el-select v-model="curComponent.dataset.id" clearable>
                    <el-option-group
                      v-for="group in options"
                      :key="group.label"
                      :label="group.label"
                    >
                      <el-option
                        v-for="item in group.options"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      >
                        <div class="flex-align-center">
                          <el-icon>
                            <Icon name="icon_add_outlined"></Icon>
                          </el-icon>
                          <span class="ellipsis" style="width: 250px">{{ item.label }}</span>
                        </div>
                      </el-option>
                    </el-option-group>
                  </el-select>
                </div>
                <div class="value">
                  <el-select v-model="curComponent.dataset.field.id" clearable>
                    <el-option label="Zone one" value="shanghai" />
                    <el-option label="Zone two" value="beijing" />
                  </el-select>
                </div>
              </template>
              <div v-if="curComponent.optionValueSource === 2" class="value">
                <el-popover
                  placement="bottom-start"
                  popper-class="manual-input"
                  ref="manual"
                  :width="358"
                  trigger="click"
                >
                  <template #reference>
                    <el-button text>
                      <template #icon>
                        <Icon name="edit-in"></Icon>
                      </template>
                      编辑
                    </el-button>
                  </template>
                  <div class="manual-input-container">
                    <div class="title">手工输入</div>
                    <div class="select-value">
                      <span> 选项值 </span>
                      <div :key="index" v-for="(_, index) in valueSource" class="select-item">
                        <el-input v-model="valueSource[index]"></el-input>
                        <el-button @click="valueSource.splice(index, 1)" class="value" text>
                          <template #icon>
                            <Icon name="icon_delete-trash_outlined"></Icon>
                          </template>
                        </el-button>
                      </div>
                      <el-button @click="valueSource.push('')" text>
                        <template #icon>
                          <Icon name="icon_add_outlined"></Icon>
                        </template>
                        添加选项值
                      </el-button>
                    </div>
                    <div class="manual-footer flex-align-center">
                      <el-button @click="cancelValueSource">{{ t('chart.cancel') }} </el-button>
                      <el-button @click="confirmValueSource" type="primary"
                        >{{ t('chart.confirm') }}
                      </el-button>
                    </div>
                  </div>
                </el-popover>
              </div>
            </div>
          </div>
          <div class="list-item">
            <div class="label">选项类型</div>
            <div class="value">
              <el-radio-group v-model="curComponent.multiple">
                <el-radio :label="false">单选</el-radio>
                <el-radio :label="true">多选</el-radio>
              </el-radio-group>
            </div>
          </div>
          <div class="list-item">
            <div class="label">
              <el-checkbox v-model="curComponent.parametersCheck" label="绑定参数" />
            </div>
            <div class="parameters">
              <el-select v-model="curComponent.parameters" clearable>
                <el-option label="Zone one" value="shanghai" />
                <el-option label="Zone two" value="beijing" />
              </el-select>
            </div>
          </div>
          <div class="list-item">
            <div class="label">
              <el-checkbox v-model="curComponent.defaultValueCheck" label="设置默认值" />
            </div>
            <div class="parameters">
              <el-select v-model="curComponent.defaultValue" clearable>
                <el-option label="Zone one" value="shanghai" />
                <el-option label="Zone two" value="beijing" />
              </el-select>
            </div>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancelClick">{{ t('chart.cancel') }} </el-button>
        <el-button @click="confirmClick" type="primary">{{ t('chart.confirm') }} </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="less">
.query-condition-configuration {
  .container {
    font-size: 14px;
    font-family: PingFang SC;
    width: 1152px;
    height: 454px;
    border-radius: 4px;
    border: 1px solid #dee0e3;
    display: flex;
    .query-condition-list {
      height: 100%;
      background: #f5f6f7;
      border-right: 1px solid #dee0e3;
      width: 208px;

      .title {
        padding: 16px;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .list-item_primary {
        border-radius: 0;
        position: relative;
        .label {
          width: 75%;
        }

        .rename {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: #fff;
          padding: 4px 10px;
          z-index: 5;
        }
      }
    }

    .chart-field {
      border-right: 1px solid #dee0e3;
      height: 100%;
      padding: 16px;
      width: 474px;

      .select-all {
        height: 40px;
      }

      .field-list {
        .list-item {
          height: 32px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 8px;
          .ed-checkbox__label {
            display: inline-flex;
            align-items: center;
            .checkbox-name {
              width: 110px;
            }
          }

          .dataset {
            color: #646a73;
            font-size: 14px;
            height: 22px;
            width: 90px;
            line-height: 22px;
            margin-left: 8px;
          }

          .ed-select {
            width: 172px;
          }
        }
      }
    }

    .condition-configuration {
      padding: 16px;
      width: 467px;
      .title {
        margin-bottom: 16px;
      }

      .configuration-list {
        .list-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 16px;
          flex-wrap: wrap;

          &.top-item {
            .label {
              margin-bottom: auto;
              padding-top: 10px;
            }
          }
          .label {
            width: 100px;
            color: #1f2329;
          }

          .value {
            width: 321px;
            .value {
              margin-top: 8px;
            }
            .ed-select {
              width: 321px;
            }
          }

          .parameters {
            margin-left: auto;
            .ed-select {
              width: 415px;
            }
          }
        }
      }
    }
  }
}
.manual-input {
  height: 490px;
  padding: 0 !important;

  .manual-input-container {
    .title {
      padding: 16px;
    }
    .select-value {
      padding: 0 0 16px 16px;
      height: 374px;
      overflow-y: auto;
      .value {
        color: #646a73;
        margin-left: 6px;
        font-size: 20px;
      }

      .select-item {
        margin: 8px 0;
        .ed-input {
          width: 298px;
        }
      }
    }
    .manual-footer {
      padding: 16px;
      height: 63px;
      border-top: 1px solid rgba(31, 35, 41, 0.15);
      justify-content: flex-end;
    }
  }
}
</style>
