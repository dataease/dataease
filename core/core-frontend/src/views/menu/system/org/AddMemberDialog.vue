<template>
  <el-dialog
    v-model="dialogVisible"
    :title="t('org.add_member')"
    width="800px"
    destroy-on-close
    :close-on-click-modal="false"
  >
    <div class="member-dialog">
      <!-- Left: candidate users -->
      <div class="dialog-left">
        <div class="dialog-section-title">{{ t("org.select_member") }}</div>
        <div class="search-box">
          <el-input
            v-model="candidateKeyword"
            clearable
            :placeholder="t('commons.search')"
            @input="loadCandidates"
            prefix-icon="Search"
          />
        </div>
        <div class="candidate-toolbar">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">
            {{ t("dataset.check_all") }}
          </el-checkbox>
        </div>
        <div class="candidate-list">
          <div
            v-for="user in filteredCandidates"
            :key="user.id"
            class="candidate-item"
          >
            <el-checkbox
              :model-value="isSelected(user.id)"
              @change="toggleSelect(user)"
            >
              <span class="user-label">
                <el-icon><User /></el-icon>
                {{ user.name }}({{ user.account }})
              </span>
            </el-checkbox>
          </div>
          <el-empty
            v-if="!filteredCandidates.length"
            :description="t('commons.no_result')"
            :image-size="60"
          />
        </div>
      </div>

      <!-- Right: selected users -->
      <div class="dialog-right">
        <div class="selected-header">
          <span>{{ t("org.selected_count", [selectedUsers.length]) }}</span>
          <el-select
            v-model="selectedRoleIds"
            multiple
            filterable
            collapse-tags
            popper-class="role-mark_dialog"
            :placeholder="t('org.select_member_role')"
            size="small"
            style="flex: 1; min-width: 120px"
          >
            <el-option-group
              v-for="group in roleList"
              :key="group.label"
              :label="group.label"
            >
              <el-option
                v-for="role in group.options"
                :key="role.id"
                :value="role.id"
                :label="role.name"
              >
                <span>{{ role.name }}</span>
                <span v-if="role.root" class="role-mark">{{
                  $t("role.system")
                }}</span>
                <span v-else-if="role.readonly" class="role-mark-de">{{
                  $t("role.staff")
                }}</span>
                <span v-else class="role-mark-de">{{
                  $t("role.manager")
                }}</span>
              </el-option>
            </el-option-group>
          </el-select>
          <el-button text type="primary" size="small" @click="clearSelected">
            {{ t("sync_task.clear_button") }}
          </el-button>
        </div>
        <div class="selected-list">
          <div
            v-for="user in selectedUsers"
            :key="user.id"
            class="selected-item"
          >
            <span class="user-label">
              <el-icon><User /></el-icon>
              {{ user.name }}({{ user.account }})
            </span>
            <el-icon class="remove-icon" @click="removeSelected(user)">
              <Close />
            </el-icon>
          </div>
          <el-empty
            v-if="!selectedUsers.length"
            :description="t('org.no_selected_member')"
            :image-size="60"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">{{
        t("commons.cancel")
      }}</el-button>
      <el-button
        type="primary"
        @click="confirmAdd"
        :disabled="!selectedUsers.length"
      >
        {{ t("commons.add") }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, computed } from "vue";
import { useI18n } from "@/hooks/web/useI18n";
import { User, Close } from "@element-plus/icons-vue";
import { sysOrgMemberCandidatesApi, sysOrgMemberAddApi } from "./api";
import { roleOptionForUserApi } from "@/api/user";
import { ElMessage } from "element-plus-secondary";

const { t } = useI18n();

const emit = defineEmits(["saved"]);
const dialogVisible = ref(false);

const orgId = ref<string>();
const candidateKeyword = ref("");
const allCandidates = ref<any[]>([]);
const selectedUserIds = ref<Set<number>>(new Set());
const selectedRoleIds = ref<number[]>([]);
const roleList = ref([
  { label: t("role.system_role"), options: [] },
  { label: t("role.custom_role"), options: [] },
]);
const selectAll = ref(false);

const filteredCandidates = computed(() => {
  if (!candidateKeyword.value) return allCandidates.value;
  const kw = candidateKeyword.value.toLowerCase();
  return allCandidates.value.filter(
    (u) =>
      u.name.toLowerCase().includes(kw) || u.account.toLowerCase().includes(kw),
  );
});

const selectedUsers = computed(() => {
  return allCandidates.value.filter((u) => selectedUserIds.value.has(u.id));
});

const open = async (oid: string) => {
  orgId.value = oid;
  selectedUserIds.value = new Set();
  selectedRoleIds.value = [];
  candidateKeyword.value = "";
  selectAll.value = false;
  dialogVisible.value = true;
  await loadCandidates();
  await loadRoleOptions();
};

const loadCandidates = async () => {
  const res = await sysOrgMemberCandidatesApi({
    keyword: candidateKeyword.value,
  });
  allCandidates.value = res.data || [];
};

const loadRoleOptions = async () => {
  const res = await roleOptionForUserApi({ oid: orgId.value });
  const roles = res.data || [];
  roleList.value[0].options = roles.filter((r) => r.root);
  roleList.value[1].options = roles.filter((r) => !r.root);
};

const isSelected = (id: number) => selectedUserIds.value.has(id);

const toggleSelect = (user: any) => {
  if (selectedUserIds.value.has(user.id)) {
    selectedUserIds.value.delete(user.id);
  } else {
    selectedUserIds.value.add(user.id);
  }
  selectAll.value =
    selectedUserIds.value.size === filteredCandidates.value.length &&
    filteredCandidates.value.length > 0;
};

const handleSelectAll = (val: boolean) => {
  if (val) {
    filteredCandidates.value.forEach((u) => selectedUserIds.value.add(u.id));
  } else {
    selectedUserIds.value.clear();
  }
};

const removeSelected = (user: any) => {
  selectedUserIds.value.delete(user.id);
  selectAll.value = false;
};

const clearSelected = () => {
  selectedUserIds.value.clear();
  selectAll.value = false;
};

const confirmAdd = async () => {
  /* if (!selectedRoleIds.value?.length) {
    ElMessage.warning(t('org.select_member_role'))
    return
  } */
  await sysOrgMemberAddApi({
    orgId: orgId.value,
    userIds: Array.from(selectedUserIds.value),
    roleIds: selectedRoleIds.value,
  });
  ElMessage.success(t("common.add_success"));
  dialogVisible.value = false;
  emit("saved");
};

defineExpose({ open });
</script>

<style lang="less" scoped>
.member-dialog {
  display: flex;
  gap: 16px;
  height: 400px;
}
.dialog-left,
.dialog-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #dee0e3;
  border-radius: 4px;
  overflow: hidden;
}
.dialog-section-title {
  padding: 12px 16px 0;
  font-weight: 500;
  font-size: 14px;
}
.search-box {
  padding: 8px 12px;
}
.candidate-toolbar {
  padding: 0 12px 8px;
}
.candidate-list,
.selected-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 12px 12px;
}
.candidate-item {
  display: flex;
  align-items: center;
  padding: 0 12px;
  height: 32px;
  &:hover {
    background: #1f23291a;
  }
  .user-label {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
  }
}
.selected-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #dee0e3;
  font-size: 13px;
  color: #646a73;
  span {
    white-space: nowrap;
    flex-shrink: 0;
  }
  .ed-button {
    flex-shrink: 0;
  }
}
.selected-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px 0 16px;
  height: 34px;
  &:hover {
    background: #1f23291a;
  }
  .user-label {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
  }
  .remove-icon {
    cursor: pointer;
    color: #999;
    &:hover {
      color: #f54a45;
    }
  }
}
</style>
<style lang="less">
.role-mark_dialog {
  .role-mark,
  .role-mark-de {
    margin-left: 8px;
    border-radius: 2px;
    padding: 0 6px;
    font-size: 12px;
  }

  .role-mark {
    color: var(--ed-color-primary);
    background: var(--ed-color-primary-33);
    line-height: 20px;
  }

  .role-mark-de {
    background: #1f23291a;
    color: #646a73;
  }
}
</style>
