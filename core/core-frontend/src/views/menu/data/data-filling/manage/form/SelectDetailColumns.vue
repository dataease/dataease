<script lang="ts" setup>
import {computed, ref} from "vue";
import {useI18n} from "@/hooks/web/useI18n";
import {filter, find, remove} from "lodash-es";
import draggable from 'vuedraggable'
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg";
import {Icon} from "@/components/icon-custom";
import {OptionItem} from "../../data-filling";

const {t} = useI18n();

const showDialog = ref(false)
const loading = ref(false)


interface OptionItemWithCheck extends OptionItem {
  checked: boolean
}

const filterStr = ref<string>('')

const form = ref<{
  optionList: Array<OptionItem>,
  settingList: Array<OptionItem>
}>({
  optionList: [],
  settingList: []
})

function init(optionList: Array<OptionItem>, settingList: Array<OptionItem>) {
  filterStr.value = ''
  if (!optionList || optionList.length == 0) {
    return
  }
  form.value.optionList = JSON.parse(JSON.stringify(optionList))
  const list: Array<OptionItem> = [];
  (settingList ?? []).forEach((i: OptionItem) => {
    if (find(form.value.optionList, o => o.fieldName === i.fieldName)) {
      list.push(i)
    }
  })
  form.value.settingList = list
  showDialog.value = true
}

const computedOptionList = computed<Array<OptionItemWithCheck>>(() => {
  const list: Array<OptionItemWithCheck> = []
  for (let i = 0; i < form.value.optionList.length; i++) {
    const o = form.value.optionList[i]
    const checked = find(form.value.settingList, s => s.fieldName === o.fieldName) != undefined
    if (filterStr.value.length > 0) {
      if (o.fieldName.includes(filterStr.value)) {
        list.push({
          ...o,
          checked
        })
      }
    } else {
      list.push({
        ...o,
        checked
      })
    }
  }
  return list
})

const checkAll = computed(() => {
  return filter(computedOptionList.value, o => o.checked).length === computedOptionList.value.length && computedOptionList.value.length > 0
})

function onClickCheckAll() {
  const tempList: Array<OptionItemWithCheck> = JSON.parse(JSON.stringify(computedOptionList.value))
  if (checkAll.value) {
    for (let i = 0; i < tempList.length; i++) {
      const o = tempList[i]
      if (o.checked) {
        onClickOption(o)
      }
    }
  } else {
    for (let i = 0; i < tempList.length; i++) {
      const o = tempList[i]
      if (!o.checked) {
        onClickOption(o)
      }
    }
  }
}

function onClickOption(item: OptionItemWithCheck) {
  if (item.checked) {
    remove(form.value.settingList, s => s.fieldName === item.fieldName)
  } else {
    let displayName = item.displayName
    if (!item.displayName || item.displayName.trim() === '') {
      displayName = item.fieldName
    }
    form.value.settingList.push({
      fieldName: item.fieldName,
      displayName: displayName
    })
  }
}


const emits = defineEmits(['close'])

function clearSelection() {
  form.value.settingList = []
}

function handleClearItem(item) {
  remove(form.value.settingList, s => s.fieldName === item.fieldName)
}

function confirm() {
  //允许清空，所以可以直接确认
  emits('close', form.value.settingList)
  showDialog.value = false
}

const dscDialogRef = ref()

function cancel() {
  showDialog.value = false
}

function afterClose() {
  form.value = {
    optionList: [],
    settingList: []
  }
}


defineExpose({
  init
});

</script>

<template>
  <el-dialog
      ref="dscDialogRef"
      :title="t('data_fill.form.add_detail_columns')"
      destroy-on-close
      v-model="showDialog"
      :show-close="true"
      @close="afterClose"
      width="800px"
      class="m-dialog"
  >
    <div class="sdc-container" v-loading="loading">
      <div class="sdc-sub-container left-container">
        <div class="sdc-filter">
          <el-input clearable v-model="filterStr" style="width: 100%;">
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined">
                  <icon_searchOutline_outlined class="svg-icon"/>
                </Icon>
              </el-icon>
            </template>
          </el-input>
          <el-checkbox v-model="checkAll" @click.prevent="onClickCheckAll" style="margin-top:8px;">
            {{ t('data_source.all') }}
          </el-checkbox>
        </div>
        <el-main class="sdc-list">
          <div v-for="o in computedOptionList" style="height: 32px">
            <el-checkbox :key="o.fieldName" v-model="o.checked"
                         @click.prevent="onClickOption(o)">
              {{ o.fieldName }}
            </el-checkbox>
          </div>
        </el-main>
      </div>
      <div class="sdc-sub-container right-container">
        <div class="sdc-filter">
          <div style="display: flex; flex-direction: row; align-items: center; justify-content: space-between;">
            <div>
              {{ t('deDataset.selected') }}
              <span class="num">{{ form.settingList.length }}</span>
              {{ t('deDataset.item') }}
            </div>
            <el-button text @click="clearSelection">
              {{ t("user.clear_button") }}
            </el-button>
          </div>
          <el-row style="font-weight: bold; margin-top: 20px;" gutter="8">
            <el-col :span="10">
              {{ t('data_fill.form.display_name') }}
            </el-col>
            <el-col :span="10" class="align-center">
              {{ t('data_set.field') }}
            </el-col>
            <el-col :span="4" class="align-center">
              {{ t('data_fill.form.operation') }}
            </el-col>
          </el-row>
        </div>
        <el-main class="sdc-list">
          <draggable
              :list="form.settingList"
              item-key="fieldName"
              class="list-group"
              ghost-class="ghost"
              @start="dragging = true"
              @end="dragging = false"
          >
            <template #item="{ element }">
              <div class="list-group-item">
                <el-row style="margin-bottom: 8px;" gutter="8">
                  <el-col :span="10" class="align-center">
                    <el-input style="width: 100%" v-model="element.displayName"/>
                  </el-col>
                  <el-col :span="10" class="align-center">
                    {{ element.fieldName }}
                  </el-col>
                  <el-col :span="4" class="align-center">
                    <el-button
                        text
                        @click="handleClearItem(element)"
                    >
                      <template #icon>
                        <Icon name="icon_delete-trash_outlined">
                          <icon_deleteTrash_outlined/>
                        </Icon>
                      </template>
                    </el-button>
                  </el-col>
                </el-row>
              </div>
            </template>
          </draggable>
        </el-main>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="cancel">
          {{ t("chart.cancel") }}
        </el-button>
        <el-button
            :disabled="loading"
            type="primary"
            @click="confirm"
        >
          {{ t("commons.confirm") }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less">
.sdc-container {
  height: 428px;
  width: 100%;
  border: 1px solid #dee0e3;
  border-radius: 4px;
  margin: 24px 0;
  display: flex;
  flex-direction: row;

  .sdc-sub-container {
    display: flex;
    flex-direction: column;

    .sdc-list {
      flex: 1;
      padding: 0 20px;

      display: flex;
      flex-direction: column;
    }

    .sdc-filter {
      padding: 20px 20px 8px;
    }
  }

  .right-container {
    flex: 3;
  }

  .left-container {
    flex: 2;
    border-right: 1px solid #dee0e3;
  }

  .align-center {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center
  }

  .ghost {
    opacity: 0.5;
    background: #c8ebfb;
  }

  .list-group-item {
    background: white;
  }
}
</style>
