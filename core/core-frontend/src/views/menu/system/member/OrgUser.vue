<script lang="ts" setup>
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import icon_close_outlined from "@/assets/svg/icon_close_outlined.svg";
import { ref, computed, reactive, onBeforeUnmount } from "vue";
import { useI18n } from "@/hooks/web/useI18n";
import { userOptionForRoleApi, mountUserApi } from "@/api/user";
import { setColorName } from "@/utils/utils";
import { ElMessage, type CheckboxValueType } from "element-plus-secondary";
const { t } = useI18n();
const dialogVisible = ref(false);
const loading = ref(false);
const userKeyword = ref("");

interface User {
  id: string;
  name: string;
  check: boolean;
  hidden: boolean;
  account: string;
  colorName: string;
}
const props = defineProps({
  rid: {
    type: String,
  },
});
const state = reactive<{ selectUserList: User[] }>({
  selectUserList: [],
});
const checkAll = ref(false);
const isIndeterminate = ref(false);
const checkedUsers = computed<User[]>(() => {
  return state.selectUserList.filter((ele) => ele.check);
});
const triggerFilterUser = (val: string) => {
  state.selectUserList.forEach((item) => {
    setColorName(item, val);
    item["hidden"] =
      !!val && !item.name.toLocaleLowerCase().includes(val.toLocaleLowerCase());
  });

  handleCheckedUsersChange();
  if (!state.selectUserList.length) {
    checkAll.value = false;
    isIndeterminate.value = false;
  }
};
const handleCheckAllChange = (val: CheckboxValueType) => {
  if (userKeyword.value) {
    state.selectUserList
      .filter((ele) =>
        ele.name
          .toLocaleLowerCase()
          .includes(userKeyword.value.toLocaleLowerCase().trim())
      )
      .forEach((ele) => {
        ele.check = val as boolean;
      });
  } else {
    state.selectUserList.forEach((ele) => {
      ele.check = val as boolean;
    });
  }
  isIndeterminate.value = false;
};
const handleCheckedUsersChange = () => {
  const checkedUser = state.selectUserList.filter(
    (ele) =>
      ele.check &&
      ele.name
        .toLocaleLowerCase()
        .includes(userKeyword.value.toLocaleLowerCase().trim())
  );
  const checkedCount = checkedUser.length;
  const checkedCountHidden = state.selectUserList.filter((ele) =>
    ele.name
      .toLocaleLowerCase()
      .includes(userKeyword.value.toLocaleLowerCase().trim())
  ).length;
  checkAll.value = checkedCount === checkedCountHidden;
  isIndeterminate.value = checkedCount > 0 && checkedCount < checkedCountHidden;
};

const init = () => {
  dialogVisible.value = true;
  checkAll.value = false;
  isIndeterminate.value = false;
  userKeyword.value = "";
  optionSearch(props.rid);
};

const handleClearUser = (item) => {
  item.check = false;
  handleCheckedUsersChange();
};

const clearAll = () => {
  state.selectUserList.forEach((ele) => {
    ele.check = false;
  });
  checkAll.value = false;
  isIndeterminate.value = false;
};

const optionLoading = ref(false);
const optionSearch = (rid?: string) => {
  const param = { rid };
  if (rid) {
    optionLoading.value = true;
    userOptionForRoleApi(param)
      .then((res) => {
        if (res?.data?.length) {
          state.selectUserList = res.data;
        } else {
          state.selectUserList = [];
        }
      })
      .finally(() => {
        optionLoading.value = false;
      });
  }
};
const emits = defineEmits(["refresh-grid"]);

const bindUsers = () => {
  if (!checkedUsers.value.length) {
    dialogVisible.value = false;
    return;
  }
  const uids = checkedUsers.value.map((user) => user.id);
  const param = { rid: props.rid, uids };
  loading.value = true;
  mountUserApi(param).then(() => {
    ElMessage({
      message: t("role.bind_success"),
      type: "success",
    });
    emits("refresh-grid");
    loading.value = false;
    dialogVisible.value = false;
  });
};
const keyFunction = (e: any) => {
  if (e?.keyCode === 13) {
    bindUsers();
  }
};
const removeKeyDown = () => {
  window.removeEventListener("keydown", keyFunction);
};
const addKeyDown = () => {
  window.addEventListener("keydown", keyFunction);
};
onBeforeUnmount(() => {
  removeKeyDown();
});
defineExpose({
  init,
});
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :title="t('role.org_user_title')"
    width="600px"
    class="add-org-user-dialog"
    v-if="dialogVisible"
    @open="addKeyDown"
    @close="removeKeyDown"
  >
    <div class="add-org-user" v-loading="loading">
      <div class="select-user-list">
        <el-input v-model="userKeyword" clearable @input="triggerFilterUser">
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"
                ><icon_searchOutline_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </template>
        </el-input>
        <el-skeleton v-if="optionLoading" class="u-option-skeleton" animated>
          <template #template>
            <div class="i-option-skeleton-item" v-for="i in 11" :key="i">
              <el-skeleton-item variant="text" class="t2and" />
            </div>
          </template>
        </el-skeleton>
        <template
          v-else-if="!!state.selectUserList.filter((ele) => !ele.hidden).length"
        >
          <el-checkbox
            v-model="checkAll"
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
          >
            <div class="all-user-label">
              {{ t("chart.result_mode_all") }}
            </div>
          </el-checkbox>
          <el-checkbox
            @change="handleCheckedUsersChange"
            v-model="user.check"
            v-for="user in state.selectUserList"
            :key="user.id"
            :label="user.id"
            v-show="!user.hidden"
          >
            <div
              class="user-label"
              :title="user.name + '(' + user.account + ')'"
            >
              <span v-html="user.colorName || user.name"></span>
              <span>{{ "(" + user.account + ")" }}</span>
            </div>
          </el-checkbox>
        </template>
        <span class="no-result" v-else> {{ t("commons.no_result") }} </span>
      </div>
      <div class="selected-user-list">
        <div class="select-num">
          <span>{{
            t("user.selected_user", { msg: checkedUsers.length })
          }}</span>
          <el-button @click="clearAll" text>{{ t("commons.clear") }}</el-button>
        </div>
        <div class="user-list" v-for="user in checkedUsers" :key="user.id">
          <div class="info" :title="user.name + '(' + user.account + ')'">
            <span>{{ user.name }}</span>
            <span>{{ "(" + user.account + ")" }}</span>
          </div>
          <el-icon @click="handleClearUser(user)" class="hover-icon">
            <Icon name="icon_close_outlined"
              ><icon_close_outlined class="svg-icon"
            /></Icon>
          </el-icon>
        </div>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="dialogVisible = false">{{
          t("chart.cancel")
        }}</el-button>
        <el-button
          :disabled="loading || !checkedUsers.length"
          :type="!!checkedUsers.length ? 'primary' : 'info'"
          @click="bindUsers"
        >
          {{ t("common.add") }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less">
.add-org-user-dialog {
  .add-org-user {
    height: 428px;
    width: 100%;
    border: 1px solid #dee0e3;
    border-radius: 4px;
    display: flex;
    margin-top: 8px;
    .ed-checkbox {
      width: 100%;
      margin: 0 0 8px 0;
      padding-left: 24px;
      height: 32px;

      &:hover {
        background: #1f23291a;
      }
    }
    .all-user-label {
      color: #1f2329 !important;
    }
    .user-label {
      height: 22px;
      font-family: var(--de-custom_font, "PingFang");
      font-weight: 400;
      font-style: normal;
      overflow: hidden;
      text-overflow: ellipsis;
      -o-text-overflow: ellipsis;
      -webkit-text-overflow: ellipsis;
      -moz-text-overflow: ellipsis;
      white-space: nowrap;
      width: 210px;

      :nth-child(1) {
        font-size: 14px;
        line-height: 22px;
        color: #1f2329 !important;
      }

      :nth-child(2) {
        color: #8d9199;
        font-size: 12px;
        line-height: 20px;
        margin-top: 1px;
      }
    }

    .selected-user-list,
    .select-user-list {
      height: 100%;
      width: 50%;
      overflow-y: auto;
      position: relative;
      .no-result {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
      }
    }
    .select-user-list {
      padding-top: 24px;
      .ed-input {
        margin: 0 24px;
        margin-bottom: 8px;
        width: calc(100% - 48px);
      }
    }

    .selected-user-list {
      border-left: 1px solid #dee0e3;
      .select-num {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 21px 12px 16px 16px;
      }
      .user-list {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 12px 0 16px;
        height: 34px;
        &:hover {
          background: #1f23291a;
        }

        .info {
          font-family: var(--de-custom_font, "PingFang");
          font-weight: 400;
          font-style: normal;
          overflow: hidden;
          text-overflow: ellipsis;
          -o-text-overflow: ellipsis;
          -webkit-text-overflow: ellipsis;
          -moz-text-overflow: ellipsis;
          white-space: nowrap;
          width: 210px;
          :nth-child(1) {
            font-size: 14px;
            line-height: 22px;
          }

          :nth-child(2) {
            color: #8d9199;
            font-size: 12px;
            line-height: 20px;
            margin-top: 1px;
          }

          .hover-icon {
            cursor: pointer;
            font-size: 16px;
          }
        }
      }
    }
  }
}
.u-option-skeleton {
  width: 100%;
}
.i-option-skeleton-item {
  display: flex;
  align-items: center;
  justify-items: space-between;
  height: 32px;
  line-height: 32px;
  margin: 0 24px;
  .t2and {
    width: 100%;
    height: 14px;
  }
}
</style>
