<template>
  <div
    v-if="
      scope.row['disabled'] ||
      (activeAuth === 'resource' && item.weightLevel < 7 && scope.row.id === '0') ||
      (excludeMenuFolder && (activeAuth === 'menu' && !scope.row.leaf))
    "
  />
  <div v-else class="de-auth-check">
    <el-popover
      v-if="
        (scope.row['level' + item.weightLevel] &&
          scope.row['level' + item.weightLevel]['show'] &&
          !scope.row['value' + item.weightLevel])
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
        <div v-if="!rootRole">

          <span v-if="scope.row[
              'level' + item.weightLevel
            ]['orgs'] && scope.row[
              'level' + item.weightLevel
            ]['orgs'].size">{{ t("auth.from_org") }}</span>
          <span
            :key="oname"
            v-for="(oname, index) in scope.row[
              'level' + item.weightLevel
            ]['orgs']"
            >{{ index as number + 1 + "、" + oname }}</span
          >

          <span v-if="scope.row['level' + item.weightLevel]['roles'] && scope.row['level' + item.weightLevel]['roles'].size">
            {{ t("auth.from_role") }}
          </span>
          <span
            :key="rname"
            v-for="(rname, index) in scope.row['level' + item.weightLevel]['roles']"
          >
            {{ index as number + 1 + "、" + rname }}
          </span>
          
          
          <span
            >{{ t("auth.auth_alone")
            }}<el-switch
              class="independent-auth"
              size="small"
              v-model="scope.row['independent' + item.weightLevel]"
              @change="
                independentAuth(scope.row, item.weightLevel)
              "
          /></span>
        </div>
        <div v-else>
          {{ t("auth.inner_role_tips") }}
        </div>
      </div>
    </el-popover>
    <el-checkbox
      class="de-auth-check"
      v-show="
        !(
          scope.row['level' + item.weightLevel] &&
          scope.row['level' + item.weightLevel]['show'] &&
          !scope.row['value' + item.weightLevel]
        )
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
defineProps({
  activeAuth: propTypes.string.def('resource'),
  rootRole: propTypes.bool.def(false),
  excludeMenuFolder: propTypes.bool.def(true),
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