<template>
  <div v-if="activeAuth === 'resource' && selectedResourceId === '0' && item.weightLevel < 7"/>
  <div
    v-else-if="
      activeName === 'role' &&
      isRoleCheckboxHidden(scope.row, item.weightLevel, activeAuth)
    "
  />
  <div v-else class="de-auth-check">
    <!-- <el-tooltip
      v-if="scope.row.type === 1 && (scope.row.attrs?.root || scope.row.root)"
      class="box-item"
      effect="dark"
      :content="t('auth.inner_role_tips')"
      placement="top"
    >
      <el-checkbox
        class="user-role-per-checked de-auth-check"
        disabled
        v-model="roleChecked"
      ></el-checkbox>
    </el-tooltip> -->
    <el-popover
      v-if="
        scope.row['level' + item.weightLevel] &&
        scope.row['level' + item.weightLevel]['show'] &&
        !scope.row['value' + item.weightLevel]
      "
      placement="top-start"
      title=""
      :width="200"
      trigger="hover"
    >
      <template #reference>
        <el-checkbox
          class="user-role-per-checked de-auth-check"
          disabled
          v-model="roleChecked"
        ></el-checkbox>
      </template>

      <div class="role-auth-tips">
        <!-- 根角色 + 隐式权限：系统内置角色，权限无法编辑 -->
        <span v-if="scope.row.attrs?.root">
          {{ t("auth.inner_role_tips") }}
        </span>
        <!-- 非根角色：保持原有逻辑，显示角色/组织列表 + 独立授权 -->
        <template v-else>
          <span
            v-if="scope.row['level' + item.weightLevel]['roles'] && scope.row['level' + item.weightLevel]['roles'].size"
          >{{ t("auth.from_role") }}</span>
          <span
            :key="rname"
            v-for="(rname, index) in scope.row[
              'level' + item.weightLevel
            ]['roles']"
            >{{ index.toString() + 1 + "、" + rname }}</span
          >
          <span
            >{{ t("auth.auth_alone")
            }}<el-switch
              class="independent-auth"
              size="small"
              v-model="scope.row['independent' + item.weightLevel]"
              @change="independentAuth(scope.row, item.weightLevel)"
          /></span>
        </template>
      </div>
    </el-popover>
    <el-checkbox
      class="de-auth-check"
      v-show="
        !(
          scope.row['level' + item.weightLevel] &&
          scope.row['level' + item.weightLevel]['show'] &&
          !scope.row['value' + item.weightLevel]
        ) &&
        !(scope.row.attrs?.root && scope.row['level' + item.weightLevel]?.show)
      "
      v-model="scope.row['value' + item.weightLevel]"
      @change="rowWeightChanged(scope.row, item.weightLevel)"
    ></el-checkbox>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from "@/hooks/web/useI18n";
import { propTypes } from '@/utils/propTypes'
import { isRoleCheckboxHidden } from './options'
defineProps({
  activeAuth: propTypes.string.def('resource'),
  activeName: propTypes.string.def('user'),
  selectedResourceId: propTypes.string.def('0'),
  item: {
    type: Object,
    default: null
  },
  scope: {
    type: Object,
    default: null
  }
})
const { t } = useI18n();
const roleChecked = ref(true);

const emits = defineEmits(['independentAuth', 'rowWeightChanged'])
const independentAuth = (row, level) => {
  emits('independentAuth', row, level)
}

const rowWeightChanged = (row, level) => {
  emits('rowWeightChanged', row, level)
}

</script>

<style lang="less" scoped>
.de-auth-check {
  height: 23px !important;
}
.role-auth-tips {
  span {
    display: block;
  }
}
.independent-auth {
  margin-left: 5px;
}
.user-role-per-checked {
  margin-right: 0;
}
</style>