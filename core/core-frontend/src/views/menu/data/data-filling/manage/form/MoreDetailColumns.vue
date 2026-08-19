<script setup lang="ts">
import {useI18n} from "@/hooks/web/useI18n";
import {computed} from "vue";

const {t} = useI18n();

const props = withDefaults(defineProps<{
  details: Array<{ name: string, value: any }>
  show: boolean
}>(), {
  details: () => [],
  show: false
})

const emit = defineEmits(["update:show"]);

const showDialog = computed({
  get() {
    return props.show
  },
  set(v) {
    emit("update:show", v)
  }
})

function closeDialog() {
  showDialog.value = false
}

</script>

<template>
  <el-dialog
      :title="t('data_fill.form.show_more_detail')"
      destroy-on-close
      v-model="showDialog"
      :show-close="true"
      width="800px"
      class="m-dialog"
      close-on-click-modal
  >
    <el-main>
      <table>
        <tr v-for="d in details">
          <td class="label-no-warp">{{d.name}}</td>
          <td class="value-item">{{d.value}}</td>
        </tr>
      </table>
    </el-main>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="closeDialog">
          {{ t("chart.close") }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.label-no-warp {
  white-space: nowrap;
  width: unset;
  min-width: 100px !important;
  color: var(--ed-color-info);
  padding-right: 4px;
  display: flex;
  align-items: flex-start;
}
.value-item {
}
</style>
