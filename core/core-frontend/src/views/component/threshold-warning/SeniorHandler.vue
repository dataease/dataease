<template>
  <div class="threshold-senior ed-collapse-item" :class="'ed-collapse-item--' + $props.themes" v-if="showDom">
    <div class="ed-collapse-item" :class="'ed-collapse--' + $props.themes">
      <div
          role="tab"
          aria-expanded="false"
          aria-controls="ed-collapse-content-8773"
          aria-describedby="ed-collapse-content-8773"
          @click="switchActive"
      >
        <div
            id="ed-collapse-head-8773"
            class="ed-collapse-item__header"
            role="button"
            tabindex="0"
        >
          <i
              class="ed-icon ed-collapse-item__arrow"
              :class="{ 'is-active': active }"
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024">
              <path
                  fill="currentColor"
                  d="M384 192v640l384-320.064z"
              ></path>
            </svg>
          </i
          >{{ t("threshold.module_name") }}
        </div>
      </div>
      <div
          id="ed-collapse-content-8773"
          class="ed-collapse-item__wrap"
          role="tabpanel"
          aria-hidden="true"
          aria-labelledby="ed-collapse-head-8773"
          data-old-padding-top=""
          data-old-padding-bottom=""
          data-old-overflow=""
          :style="{ display: active ? 'block' : 'none' }"
      >
        <div class="ed-collapse-item__content">
          <div class="inner-container">
            <span class="label" :class="'label-' + props.themes">{{
                t("threshold.setting")
              }}</span>
            <div class="right-btns">
              <template v-if="anyThreshold">
                <span
                    class="set-text-info"
                    :class="{ 'set-text-info-dark': themes === 'dark' }"
                >
                  {{ t('visualization.already_setting') }}
                </span>
              </template>

              <button
                  v-if="anyThreshold"
                  class="circle-button_icon"
                  style="margin: 0 8px;"
                  :title="t('common.delete')"
                  @click="deleteThreshold"
              >
                <el-icon size="14px">
                  <Icon name="icon_delete-trash_outlined">
                    <icon_deleteTrash_outlined class="svg-icon"/>
                  </Icon>
                </el-icon>
              </button>

              <button class="circle-button_icon" :title="t('chart.edit')" @click="openThresholdDialog">
                <el-icon size="14px">
                  <Icon name="icon_edit_outlined">
                    <icon_edit_outlined class="svg-icon"/>
                  </Icon>
                </el-icon>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import {ref, onMounted, toRefs, watch} from 'vue'
import {useI18n} from '@/hooks/web/useI18n'
import {propTypes} from '@/utils/propTypes'
import {useEmitt} from '@/hooks/web/useEmitt'
import {queryAnyThresholdApi, delWithChartApi} from './api'
import {ElMessage, ElMessageBox} from "element-plus-secondary";

const {t} = useI18n()

const props = defineProps({
  chart: propTypes.object.def({}),
  isScreen: propTypes.bool.def(false),
  resourceTable: propTypes.string.def('core'),
  themes: {
    type: String,
    default: "dark",
  },
});
const {chart} = toRefs(props);
const anyThreshold = ref(false);

const excludeTypeList = ["chart-mix", "chart-mix-stack", "chart-mix-group", 'chart-mix-dual-line', 'multi-scatter'];
const showDom = ref(false);

const queryAnyThreshold = async () => {
  const res = await queryAnyThresholdApi(chart.value.id, props.resourceTable);
  anyThreshold.value = res.data;
};

const openThresholdDialog = () => {
  const param = {
    isScreen: props.isScreen,
    viewId: chart.value.id,
    resourceTable: props.resourceTable
  }
  useEmitt().emitter.emit("open-threshold-dialog", param);
};

const deleteThreshold = () => {
  ElMessageBox.confirm(t("data_source.sure_to_delete"), {
    confirmButtonText: t("commons.delete"),
    cancelButtonText: t("commons.cancel"),
    showCancelButton: true,
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    showClose: false,
    callback: (action) => {
      if (action === 'confirm') {
        delWithChartApi(chart.value.id, props.resourceTable).then(() => {
          queryAnyThreshold();
          ElMessage.success(t("common.delete_success"));
        });
      }
    }
  })
};
const refreshThreshold = (id) => {
  if (chart?.value?.id === id) {
    queryAnyThreshold();
  }
};

const active = ref(false);
const switchActive = () => {
  active.value = !active.value;
};

const init = async () => {
  if (
      excludeTypeList.includes(chart.value["type"] || chart.value["innerType"])
  ) {
    showDom.value = false;
    return;
  }
  showDom.value = true;
  await queryAnyThreshold();
}

watch(
    () => chart.value?.id,
    () => {
      if (chart.value?.id) {
        init()
      }
    }
)
onMounted(async () => {
  init()
  useEmitt({name: "refresh-threshold-status", callback: refreshThreshold});
});
</script>

<style lang="less" scope>
.inner-container {
  margin-bottom: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  flex-direction: row;
  justify-content: space-between;

  .label {
    cursor: default;
    color: #646a73;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    line-height: 16px;
  }

  .right-btns {
    height: 16px;
    display: flex;
    align-items: center;
    flex-direction: row;
  }

  .set-text-info {
    cursor: default;
    padding: 1.5px 4px;
    border-radius: 2px;
    background: rgba(31, 35, 41, 0.1);

    color: #646a73;

    font-size: 10px;
    font-style: normal;
    font-weight: 500;
    line-height: 13px;

    &.set-text-info-dark {
      color: #a6a6a6;
      background: rgba(235, 235, 235, 0.1);
    }
  }
}

.threshold-senior {
  width: 100%;

  .ed-collapse-item {
    margin-bottom: 0px !important;
  }

  :deep(.tab) {
    cursor: pointer;
  }

  .ed-collapse-item__content {
    padding: 16px 8px 0 !important;
  }
}
</style>
