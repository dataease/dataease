<template>
  <div
    class="sys-org-setting"
    :class="{ 'is-light-org': navigateBg && navigateBg === 'light' }"
    ref="divRef"
    v-click-outside="openTree"
  >
    <span class="org-span">{{ name || "---" }}</span>
    <el-icon class="el-icon-animate">
      <Icon name="icon_expand-down_filled"
        ><icon_expandDown_filled class="svg-icon"
      /></Icon>
    </el-icon>
  </div>
  <el-popover
    ref="popoverRef"
    :virtual-ref="divRef"
    trigger="click"
    title=""
    :popper-style="{ padding: '5px 0' }"
    popper-class="utree-popover"
    width="280"
    virtual-triggering
  >
    <el-input
      :placeholder="t('org.search_placeholder')"
      v-model="keyword"
      clearable
      class="org-search-input"
      @input="search"
    >
      <template #prefix>
        <el-icon>
          <Icon name="icon_search-outline_outlined"
            ><icon_searchOutline_outlined class="svg-icon"
          /></Icon>
        </el-icon>
      </template>
    </el-input>
    <el-scrollbar class="org-tree-container" max-height="256px">
      <el-tree
        :props="props"
        :data="state.pageOption"
        node-key="id"
        lazy
        :load="loadNode"
        :default-expanded-keys="state.expandKeys"
        :expand-on-click-node="false"
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <div
            :title="node.label"
            class="custom-tree-node"
            :class="
              node.disabled
                ? 'org-node-disabled'
                : data.id === oid
                ? 'active'
                : ''
            "
          >
            <span>{{ node.label }}</span>
            <el-icon v-if="data.id === oid">
              <Icon name="icon_done_outlined"
                ><icon_done_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </div>
        </template>
      </el-tree>
    </el-scrollbar>
    <div class="pagination-count" v-if="showPage">
      <el-pagination
        :hide-on-single-page="true"
        small
        class="tree-page-content"
        layout="prev, pager, next, jumper"
        v-model:current-page="state.pageInfo.currentPage"
        :pager-count="3"
        :total="state.pageInfo.total"
        @update:current-page="handleCurrentChange"
      />
    </div>
  </el-popover>
</template>

<script lang="ts" setup>
import icon_expandDown_filled from "@/assets/svg/icon_expand-down_filled.svg";
import icon_done_outlined from "@/assets/svg/icon_done_outlined.svg";
import { ref, reactive, onMounted, unref, computed } from "vue";
import { switchOrg } from "@/api/user";
import { useUserStoreWithOut } from "@/store/modules/user";
import { useEmitt } from "@/hooks/web/useEmitt";
import { useAppearanceStoreWithOut } from "@/store/modules/appearance";
import request from "@/config/axios";
import { useI18n } from "@/hooks/web/useI18n";
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
const { t } = useI18n();

const appearanceStore = useAppearanceStoreWithOut();
interface OrgTreeNode {
  id: string | number;
  name: string;
  readOnly: boolean;
  children?: OrgTreeNode[];
}
const navigateBg = computed(() => appearanceStore.getNavigateBg);
const userStore = useUserStoreWithOut();

const name = ref();

const oid = computed(() => userStore.getOid);
const keyword = ref("");
const timer = ref();
const showPage = computed(() => {
  return state.pageInfo.total > 9;
});
const props = {
  value: "id",
  label: "name",
  disabled: "readOnly",
  isLeaf: "leaf",
};
const state = reactive({
  orgOption: [] as OrgTreeNode[],
  pageOption: [] as OrgTreeNode[],
  expandKeys: [] as string[],
  pageInfo: {
    currentPage: 1,
    pageSize: 10,
    total: 88,
  },
});
const divRef = ref();
const popoverRef = ref();

const openTree = () => {
  unref(popoverRef).popperRef?.delayHide?.();
};

const handleNodeClick = (data) => {
  if (data.readOnly) {
    return;
  }
  switchHandler(data.id);
};

const switchHandler = (id: number | string) => {
  switchOrg(id).then((res) => {
    const token = res.data.token;
    userStore.setToken(token);
    userStore.setExp(res.data.exp);
    userStore.setTime(Date.now());
    window.location.reload();
  });
};

const loadNode = (node, resolve) => {
  const param = {};
  if (node?.data?.id) {
    if (state.expandKeys.includes(node.data.id)) {
      resolve(node.data.children || []);
      return;
    }
    param["pid"] = node.data.id;
  }
  request.post({ url: "/org/lazyMounted", data: param }).then((res) => {
    const childNodes = res.data.nodes as OrgTreeNode[];
    name.value = res.data.name;
    if (!node?.data?.id) {
      state.orgOption = res.data.nodes as OrgTreeNode[];
      formatPageInfo();
      resolve(state.pageOption);
      return;
    }
    resolve(childNodes);
  });
};

const search = () => {
  if (timer.value) {
    clearTimeout(timer.value);
    timer.value = null;
  }
  timer.value = setTimeout(() => {
    const param = {
      keyword: keyword.value,
    };
    request.post({ url: "/org/lazyMounted", data: param }).then((res) => {
      state.orgOption = res.data.nodes as OrgTreeNode[];
      state.expandKeys = res.data.expandKeyList || [];
      formatPageInfo();
    });
    clearTimeout(timer.value);
    timer.value = null;
  }, 300);
};
const formatPageInfo = () => {
  const len = state.orgOption?.length || 0;
  state.pageInfo.total = len;
  if (len) {
    state.pageOption = state.orgOption.slice(0, state.pageInfo.pageSize);
  } else {
    state.pageOption = [];
  }
};
const handleCurrentChange = (pageNum) => {
  const start = (pageNum - 1) * state.pageInfo.pageSize;
  const end = Math.min(start + state.pageInfo.pageSize, state.pageInfo.total);
  state.pageOption = state.orgOption.slice(start, end);
};
onMounted(() => {
  console.log("init org-switcher");
  useEmitt({
    name: "refresh-org-options",
    callback: function () {
      search();
    },
  });
});
</script>

<style lang="less" scoped>
.is-light-org {
  color: var(--ed-color-black) !important;
  &:hover {
    background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1)) !important;
  }
}
.el-icon-animate {
  font-size: 14px !important;
}
.utree-popover {
  padding: var(--ed-popover-padding) 0 !important;
  .org-search-input {
    padding: 8px 8px;
    border-bottom: 1px solid var(--MainBG, #f5f6f7);
  }
  .pagination-count {
    width: 100%;
    height: 32px;
    padding: 8px 4px;
    border-top: 1px solid var(--MainBG, #f5f6f7);
    .tree-page-content {
      width: 100%;
      :deep(.ed-pagination__jump) {
        margin-left: 0px !important;
      }
      :deep(.ed-pagination__classifier) {
        display: none;
      }
    }
  }
}
.sys-org-setting {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 10px;
  padding: 5px;
  height: 30px;
  max-width: 210px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.8);
  &:hover {
    background-color: #1e2738;
  }
  .org-span {
    font-size: 14px;
    font-weight: 400;
    max-width: 180px !important;
    overflow: hidden;
    text-overflow: ellipsis;
    -o-text-overflow: ellipsis;
    white-space: nowrap;
    margin-right: 4px;
  }
}
.org-tree-container {
  width: 100%;
}

.custom-tree-node {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: space-between;
  padding-right: 10px;
  font-size: 14px;
  font-weight: 400;
  height: 22px;
  line-height: 22px;
  span {
    width: 182px;
    overflow: hidden;
    text-overflow: ellipsis;
    -o-text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.active {
  color: var(--ed-color-primary) !important;
}
.org-node-disabled {
  background: var(--ed-fill-color-blank) !important;
  color: var(--ed-text-color-placeholder) !important;
  cursor: not-allowed !important;
}
</style>
