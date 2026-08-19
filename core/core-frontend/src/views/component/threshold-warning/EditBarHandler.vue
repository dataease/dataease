<template>
  <div
    v-if="show && isScreen"
    class="ed-divider ed-divider--horizontal custom-divider"
    role="separator"
    style="--ed-border-style: solid"
  ></div>
  <li
    v-if="show && !isScreen"
    role="separator"
    class="ed-dropdown-menu__item--divided"
  ></li>
  <li
    v-if="show"
    data-el-collection-item=""
    @click="openThresholdDialog"
    aria-disabled="false"
    class="ed-dropdown-menu__item"
    role="menuitem"
  >
    {{ t("threshold.drawer_title") }}
  </li>
</template>

<script lang="ts" setup>
import { toRefs, computed } from "vue";
import { propTypes } from "@/utils/propTypes";
import { useEmitt } from "@/hooks/web/useEmitt";
import { useI18n } from "@/hooks/web/useI18n";

const { t } = useI18n();
const props = defineProps({
  chart: propTypes.object.def({}),
  isScreen: propTypes.bool.def(false),
  resourceTable: propTypes.string.def("core"),
});

const { chart } = toRefs(props);

const excludeTypeList = [
  "chart-mix",
  "chart-mix-stack",
  "chart-mix-group",
  "chart-mix-dual-line",
  "multi-scatter",
];

const show = computed(() => {
  return (
    chart.value["id"] &&
    chart.value["component"] === "UserView" &&
    !excludeTypeList.includes(chart.value["type"] || chart.value["innerType"])
  );
});

const emit = defineEmits(["closeItem"]);
// method area
const openThresholdDialog = () => {
  const param = {
    isScreen: props.isScreen,
    viewId: chart.value.id,
    resourceTable: props.resourceTable,
  };
  useEmitt().emitter.emit("open-threshold-dialog", param);
  emit("closeItem");
};
</script>
