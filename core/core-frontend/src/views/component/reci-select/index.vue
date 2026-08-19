<template>
  <div class="reci-select-container">
    <el-select
        v-model="reciVal"
        multiple
        ref="select"
        :reserve-keyword="false"
        :placeholder="t('common.selectText')"
        @change="setHeight"
        popper-class="reci-custom-email-popper"
        @visible-change="handleReciVal"
        style="width: 100%"
        @remove-tag="removeReciTag"
    >
      <template #tag="{ data }">
        <div ref="tags" class="ed-select__tags" :style="data">
          <div class="ed-select-tags-wrapper has-prefix">
            <el-tag
                @close="removeReciTag(id)"
                v-for="id in reciVal"
                :key="id"
                type="info"
                closable
            >
              <div class="reci-tag-item">
                <el-icon
                    class="user-item-icon"
                    :class="{
                    'item-icon-1': state.reciOptionMap.get(id)?.account,
                  }"
                >
                  <Icon>
                    <component
                        :is="
                        state.reciOptionMap.get(id)?.account ? userReci : role
                      "
                    ></component>
                  </Icon>
                </el-icon>
                <span>{{ state.reciOptionMap.get(id)?.name || id }}</span>
              </div>
            </el-tag>
          </div>
        </div>
      </template>
    </el-select>

    <el-dialog
        v-model="dialogVisible"
        width="840"
        destroy-on-close
        class="reci-dialog"
        align-center
        :z-index="2215"
        :close-on-click-modal="false"
    >
      <div class="reci-dialog-container">
        <div class="reci-dialog-header">
          <span>{{ t("threshold.choose_recipient") }}</span>
          <el-icon @click="closeDialog" class="dialog-close">
            <Icon name="icon_close_outlined"
            ><icon_close_outlined class="svg-icon"
            /></Icon>
          </el-icon>
        </div>
        <div class="reci-dialog-main">
          <div class="reci-left">
            <div class="search-head">
              <div class="user-key_word">
                <el-input
                    v-model="userKeyword"
                    clearable
                    :placeholder="t('commons.search')"
                    @input="triggerFilterUser"
                >
                  <template #prefix>
                    <el-icon>
                      <Icon name="icon_search-outline_outlined"
                      ><icon_searchOutline_outlined class="svg-icon"
                      /></Icon>
                    </el-icon>
                  </template>
                </el-input>
              </div>
            </div>
            <div v-if="showParent" class="entity-parent-container">
              <div class="parent-item" @click="expandList(0)">
                <el-icon class="role-icon">
                  <Icon name="role"><role class="svg-icon" /></Icon>
                </el-icon>
                <span>{{ t("system.role") }}</span>
                <el-icon>
                  <ArrowRight />
                </el-icon>
              </div>
              <div class="parent-item" @click="expandList(1)">
                <el-icon class="people-icon">
                  <Icon name="user-reci"><userReci class="svg-icon" /></Icon>
                </el-icon>
                <span>{{ t("system.user") }}</span>
                <el-icon>
                  <ArrowRight />
                </el-icon>
              </div>
            </div>

            <el-skeleton
                v-else-if="optionLoading"
                class="u-option-skeleton"
                animated
            >
              <template #template>
                <div class="i-option-skeleton-item" v-for="i in 9" :key="i">
                  <el-skeleton-item variant="text" class="t1and" />
                  <el-skeleton-item variant="circle" class="t2and" />
                  <el-skeleton-item variant="text" class="t3and" />
                </div>
              </template>
            </el-skeleton>

            <div v-else class="entity-kid-container">
              <div class="kid-title" v-if="!userKeyword">
                <span class="kid-title-first" @click="back2p">{{
                    t("threshold.recipient")
                  }}</span>
                <el-icon class="kid-title-i">
                  <ArrowRight />
                </el-icon>
                <span class="kid-title-span">{{
                    optionFlag ? t("system.user") : t("system.role")
                  }}</span>
              </div>
              <div class="select-all" v-if="!userKeyword">
                <el-checkbox
                    v-model="checkAll"
                    :label="t('component.allSelect')"
                    @change="checkAllChange"
                />
              </div>
              <virtual-checkbox-list
                  :class="userKeyword ? 'kid-search-content' : 'kid-content'"
                  v-model="state.selectedIdList"
                  :items="
                  state.reciOptions.filter(
                    (item) =>
                      (optionFlag === -1 || item['flag'] === optionFlag) &&
                      (!userKeyword ||
                        item.name
                          .toLowerCase()
                          .includes(userKeyword.toLowerCase()) ||
                        item.account
                          ?.toLowerCase()
                          .includes(userKeyword.toLowerCase())),
                  )
                "
                  key-prop="id"
                  :item-height="44"
                  @change="selectedChange"
              >
                <template #item="{ item }">
                  <el-icon
                      class="user-item-icon"
                      :class="{ 'item-icon-1': item['account'] }"
                  >
                    <Icon>
                      <component
                          :is="item['account'] ? userReci : role"
                      ></component>
                    </Icon>
                  </el-icon>
                  <div class="user-item-span">
                    <span>{{ item.name }}</span>
                    <span v-if="item['account']">{{
                        "(" + item.account + ")"
                      }}</span>
                  </div>
                </template>
              </virtual-checkbox-list>
            </div>
          </div>
          <div class="reci-right">
            <div class="reci-selected-head">
              <span>{{ selectedTips }}</span>
              <span @click="removeAll">{{ t("commons.clear") }}</span>
            </div>
            <virtual-list
                class="reci-selected-content"
                :style="{ height: 'calc(100% - 56px)' }"
                :data="state.selectedList"
                :item-size="44"
                item-key="id"
            >
              <template #default="{ item, index }">
                <div class="kid-item">
                  <el-icon
                      class="user-item-icon"
                      :class="{ 'item-icon-1': item['account'] }"
                  >
                    <Icon>
                      <component
                          :is="item['account'] ? userReci : role"
                      ></component>
                    </Icon>
                  </el-icon>
                  <div class="user-item-span">
                    <span>{{ item.name }}</span>
                    <span v-if="item['account']">{{
                        "(" + item.account + ")"
                      }}</span>
                  </div>
                  <el-icon
                      @click="removeItem(item.id)"
                      class="remove-hover-icon"
                  >
                    <Icon name="icon_close_outlined"
                    ><icon_close_outlined class="svg-icon"
                    /></Icon>
                  </el-icon>
                </div>
              </template>
            </virtual-list>
          </div>
        </div>
        <div class="reci-dialog-footer">
          <el-button secondary @click="closeDialog">{{
              t("commons.cancel")
            }}</el-button>
          <el-button
              :type="state.selectedList.length ? 'primary' : 'info'"
              :disabled="!state.selectedList?.length"
              @click="sureHandler"
          >{{ t("commons.add") }}</el-button
          >
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { useI18n } from "@/hooks/web/useI18n";
import { ref, nextTick, reactive, computed, watch, onMounted } from "vue";
import { ReciOption } from "./formUtil";
import role from "@/assets/svg/role.svg";
import userReci from "@/assets/svg/user-reci.svg";
import icon_close_outlined from "@/assets/svg/icon_close_outlined.svg";
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import { queryAllSubjectsApi } from "@/api/auth";
import VirtualCheckboxList from "../virtual-checkbox-list/index.vue";
import VirtualList from "../virtual-list/index.vue";

const { t } = useI18n();

const userKeyword = ref();
const checkAll = ref(false);
const showParent = ref(true);
const optionFlag = ref(-1);
const optionLoading = ref(false);
const dialogVisible = ref(false);
const select = ref();
const tags = ref();

const props = defineProps({
  modelValue: {
    type: Array as () => string[],
    default: () => [],
  },
  resourceId: {
    type: String,
    default: '',
  },
  resourceFlag: {
    type: Number,
    default: 0,
  },
});
const reciVal = ref<any[]>(props.modelValue);
const state = reactive({
  reciOptions: [] as ReciOption[],
  selectedIdList: [] as string[],
  selectedList: [] as ReciOption[],
  userOptions: [] as ReciOption[],
  roleOptions: [] as ReciOption[],
  reciOptionMap: new Map(),
});

const selectedTips = computed(() => {
  let roleCount = 0;
  let userCount = 0;
  state.selectedIdList.forEach((id) => {
    if (id.startsWith("0")) {
      roleCount++;
    } else {
      userCount++;
    }
  });
  return t("threshold.choose_recipient_tip", [userCount, roleCount]);
});
const emit = defineEmits(["update:modelValue", "change"]);

watch(reciVal, (newVal) => {
  emit("update:modelValue", newVal);
  emit("change", newVal);
});
watch(
    () => props.modelValue,
    (newVal) => {
      reciVal.value = newVal;
    },
);

const setHeight = () => {
  if (!select.value) return;
  setTimeout(() => {
    const input = select.value.$el.querySelector("input") as HTMLInputElement;
    const _tags = tags.value;
    nextTick(() => {
      input.style.minHeight = `${_tags.clientHeight + 8}px`;
    });
  }, 0);
};

const removeReciTag = (val) => {
  let len = reciVal.value.length;
  while (len--) {
    const id = reciVal.value[len];
    if (id === val) {
      reciVal.value.splice(len, 1);
    }
  }
  nextTick(() => {
    setHeight();
  });
};
const handleReciVal = (visible: boolean) => {
  if (visible) {
    state.selectedIdList = [...reciVal.value];
    state.selectedList = state.reciOptions.filter((item) =>
        state.selectedIdList.includes(item.id),
    );
    dialogVisible.value = true;
  }
};
const closeDialog = () => {
  dialogVisible.value = false;
  showParent.value = true;
  optionFlag.value = -1;
  optionLoading.value = false;
  userKeyword.value = "";
  state.selectedIdList = [];
  state.selectedList = [];
};
const triggerFilterUser = () => {
  checkAll.value = false;
  optionFlag.value = -1;
  optionLoading.value = false;
  if (userKeyword.value !== null && userKeyword.value !== "") {
    showParent.value = false;
    return;
  }
  showParent.value = true;
};

const expandList = async (flag: number) => {
  optionLoading.value = true;
  showParent.value = false;
  optionFlag.value = flag;
  optionLoading.value = false;
};

const back2p = () => {
  showParent.value = true;
  checkAll.value = false;
};

const removeItem = (id) => {
  let len = state.selectedList.length;
  while (len--) {
    const item = state.selectedList[len];
    if (item.id === id) {
      state.selectedList.splice(len, 1);
    }
    const idItem = state.selectedIdList[len];
    if (idItem === id) {
      state.selectedIdList.splice(len, 1);
    }
  }
};
const removeAll = () => {
  state.selectedList = [];
  state.selectedIdList = [];
  checkAll.value = false;
};

const selectedChange = (val) => {
  state.selectedList = val.map((id) => state.reciOptionMap.get(id));
};

const checkAllChange = (val) => {
  if (optionFlag.value === -1) {
    return;
  }
  const optionList = optionFlag.value ? state.userOptions : state.roleOptions;
  if (userKeyword.value && optionList?.length) {
    const options = optionList.filter((option) =>
        option.name
            .toLocaleLowerCase()
            .includes(userKeyword.value.toLocaleLowerCase()),
    );
    const optionIdList = options.map((node) => node.id);
    if (val) {
      options.forEach((item) => {
        if (!state.selectedIdList.includes(item.id)) {
          state.selectedIdList.push(item.id);
          state.selectedList.push(item);
        }
      });
    } else {
      let len = state.selectedList.length;
      while (len--) {
        const item = state.selectedList[len];
        if (optionIdList.includes(item.id)) {
          state.selectedList.splice(len, 1);
        }

        const idItem = state.selectedIdList[len];
        if (optionIdList.includes(idItem)) {
          state.selectedIdList.splice(len, 1);
        }
      }
    }
    return;
  }

  if (val) {
    if (!state.selectedIdList?.length) {
      state.selectedList = [...optionList];
      state.selectedIdList = optionList.map((item) => item.id);
    } else {
      const group = state.selectedList.reduce((acc, item) => {
        const { flag } = item;
        if (!acc[flag]) {
          acc[flag] = [];
        }
        acc[flag].push(item);
        return acc;
      }, {});
      const otherOptions = group[1 - optionFlag.value] || [];
      state.selectedList = [...optionList, ...otherOptions];
      state.selectedIdList = state.selectedList.map((item) => item.id);
    }
  } else {
    const group = state.selectedList.reduce((acc, item) => {
      const { flag } = item;
      if (!acc[flag]) {
        acc[flag] = [];
      }
      acc[flag].push(item);
      return acc;
    }, {});
    const otherOptions = group[1 - optionFlag.value] || [];
    if (otherOptions?.length) {
      state.selectedList = [...otherOptions];
      state.selectedIdList = state.selectedList.map((item) => item.id);
    } else {
      removeAll();
    }
  }
};

const loadReciOptions = async () => {
  if (!props.resourceId) return;
  const [userRes, roleRes] = await Promise.all([
    queryAllSubjectsApi(0),
    queryAllSubjectsApi(1),
  ]);
  state.userOptions = (userRes.data || []).map((item) => ({
    id: `1${item.id}`,
    name: item.name,
    account: item.account,
    flag: 1,
  }));
  state.roleOptions = (roleRes.data || []).map((item) => ({
    id: `0${item.id}`,
    name: item.name,
    flag: 0,
  }));
  state.reciOptions = [...state.userOptions, ...state.roleOptions];
  state.reciOptionMap = state.reciOptions.reduce((acc, item) => {
    acc.set(item.id, item);
    return acc;
  }, new Map());
  validateReci();
};
const validateReci = () => {
  if (reciVal.value?.length) {
    let len = reciVal.value.length;
    while (len--) {
      if (!state.reciOptionMap.get(reciVal.value[len])) {
        reciVal.value.splice(len, 1);
      }
    }
  }
};
const sureHandler = () => {
  reciVal.value = state.selectedIdList;
  closeDialog();
  setHeight();
};

onMounted(() => {
  loadReciOptions();
});

watch(() => [props.resourceId, props.resourceFlag], () => {
  reciVal.value = [];
  loadReciOptions();
});

defineExpose({
  setHeight,
});
</script>
<style lang="less">
.reci-custom-email-popper {
  display: none !important;
}
</style>

<style lang="less" scoped>
.kid-item-hidden {
  display: none !important;
}
.kid-item {
  height: 44px;
  line-height: 44px;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding-left: 16px;
  &:hover {
    background-color: #1f23291a;
  }
  :deep(.ed-checkbox__label) {
    display: flex;
    align-items: center;
  }
  .user-item-icon {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background-color: #f78704;
    color: #ffffff;
    margin-right: 8px;
  }
  .item-icon-1 {
    background-color: var(--ed-color-primary, #3370ff) !important;
  }
  .user-item-span {
    width: 300px;
    height: 22px;
    line-height: 22px;
    font-family: var(--de-custom_font, "PingFang");
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    overflow: hidden;
    text-overflow: ellipsis;
    -o-text-overflow: ellipsis;
    -webkit-text-overflow: ellipsis;
    -moz-text-overflow: ellipsis;
    white-space: nowrap;

    :nth-child(1) {
      color: #1f2329 !important;
    }

    :nth-child(2) {
      color: #8d9199;
    }
  }
  .remove-hover-icon {
    width: 16px;
    height: 16px;
    cursor: pointer;
    color: #8f959e;
    margin: 4px;
    &:hover {
      width: 24px;
      margin: 0px;
      height: 24px;
      border-radius: 6px;
      background-color: #1f23291a;
    }
  }
}
.u-option-skeleton {
  width: 100%;
}
.i-option-skeleton-item {
  display: flex;
  align-items: center;
  height: 40px;
  line-height: 40px;
  margin-left: 16px;
  .t1and {
    width: 16px;
    height: 16px;
  }
  .t2and {
    width: 24px;
    height: 24px;
    margin: auto 8px;
  }
  .t3and {
    width: 140px;
    height: 14px;
  }
}
.reci-select-container {
  width: 100%;
  :deep(.reci-dialog) {
    height: 580px !important;
    .ed-dialog__header {
      display: none !important;
    }
    .ed-dialog__body {
      height: 100% !important;
      .reci-dialog-container {
        height: 100% !important;
        .reci-dialog-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          span {
            height: 24px;
            line-height: 24px;
            font-family: var(--de-custom_font, "PingFang");
            font-size: 16px;
            font-weight: 500;
            line-height: 24px;
            text-align: left;
            color: #1f2329;
          }
          i {
            width: 20px;
            height: 20px;
            color: #646a73;
            padding: 3px;
            cursor: pointer;
          }
        }
        .reci-dialog-main {
          display: flex;
          height: 428px;
          margin: 24px 0;
          border-radius: 6px;
          border: 1px solid #dee0e3;
          .reci-left {
            height: 100%;
            width: 50%;
            border-right: 1px solid #dee0e3;
            .search-head {
              height: 56px;
              padding: 16px 16px 8px;
              .user-key_word {
                font-family: var(--de-custom_font, "PingFang");
                font-weight: 400;
                overflow-wrap: break-word;
                color: var(--ed-text-color-regular);
                margin: 0;
                padding: 0;
                box-sizing: border-box;
                align-items: center;
                display: flex;
                flex: 1;
                flex-wrap: wrap;
                font-size: var(--font-size);
                min-width: 0;
                position: relative;
                line-height: 32px;
                .ed-input__wrapper {
                  box-shadow: 0 0 0 1px
                  var(--ed-input-border-color, var(--ed-border-color)) inset !important;
                }
              }
            }
            .entity-parent-container {
              height: 76px;
              width: 100%;
              .parent-item {
                width: 100%;
                display: flex;
                height: 38px;
                line-height: 38px;
                padding: 0 16px;
                align-items: center;
                cursor: pointer;
                &:hover {
                  background-color: #1f23291a;
                }
                i {
                  width: 16px;
                  height: 16px;
                }
                .role-icon {
                  color: #f78704;
                }
                .people-icon {
                  color: var(--ed-color-primary, #3370ff);
                }
                span {
                  margin-left: 8px;
                  height: 22px;
                  line-height: 22px;
                  width: calc(100% - 40px);
                }
              }
            }
            .entity-kid-container {
              margin-top: 4px;
              height: calc(100% - 60px);
              .kid-title {
                height: 22px;
                line-height: 22px;
                margin-bottom: 4px;
                padding: 0 16px;
                display: flex;
                color: #646a73;
                align-items: center;
                .kid-title-first {
                  height: 22px;
                  line-height: 22px;
                  border-radius: 2px;
                  padding: 0 4px;
                  cursor: pointer;
                  &:hover {
                    background-color: var(--ed-color-primary-1a, #3370ff1a);
                    color: var(--ed-color-primary, #3370ff);
                  }
                }
                .kid-title-i {
                  width: 12px;
                  height: 12px;
                }
                .kid-title-span {
                  padding: 0 4px;
                  color: #1f2329 !important;
                }
              }
              .select-all {
                padding-left: 16px;
                height: 38px;
                display: flex;
                align-items: center;
                cursor: pointer;
                &:hover {
                  background-color: #1f23291a;
                }
              }
              .kid-content {
                height: calc(100% - 64px);
              }
              .kid-search-content {
                height: 100%;
              }
            }
          }
          .reci-right {
            height: 100%;
            width: 50%;
            .reci-selected-head {
              height: 56px;
              padding: 22px 16px 12px;
              display: flex;
              align-items: center;
              justify-content: space-between;
              :nth-child(1) {
                height: 22px;
                line-height: 22px;
                font-size: 14px;
                font-family: var(--de-custom_font, "PingFang");
                color: #1f2329;
              }
              :nth-child(2) {
                height: 26px;
                line-height: 26px;
                border-radius: 6px;
                padding: 0 4px;
                color: var(--ed-color-primary, #3370ff);
                &:hover {
                  background-color: var(--ed-color-primary-1a, #3370ff1a);
                }
              }
            }
            .reci-selected-content {
              height: calc(100% - 56px);
            }
          }
        }
        .reci-dialog-footer {
          height: 32px;
          float: right;
        }
      }
    }
  }
}
.reci-tag-item {
  height: 24px;
  line-height: 24px;
  display: flex;
  align-items: center;
  .user-item-icon {
    width: 18px;
    height: 18px;
    border-radius: 50%;
    background-color: #f78704;
    color: #ffffff;
    margin-right: 4px;
  }
  .item-icon-1 {
    background-color: #3370ff !important;
  }
}
.ed-select__tags {
  .ed-select-tags-wrapper {
    display: flex;
    flex-wrap: wrap;
    grid-row-gap: 4px;
  }
  :deep(.ed-tag) {
    margin: 0px 4px 0 0;
  }
}
</style>
