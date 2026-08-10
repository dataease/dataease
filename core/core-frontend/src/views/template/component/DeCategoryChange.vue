<template>
  <div class="template-import">
    <el-form
      ref="templateImportForm"
      class="de-form-item"
      :model="state.templateInfo"
      :rules="state.templateInfoRules"
      label-position="top"
    >
      <el-form-item
        :label="t('template_manage.select_catalog')"
        prop="categories"
        style="margin-top: 16px"
      >
        <el-select v-model="state.templateInfo.categories" multiple style="width: 100%">
          <el-option
            v-for="option in templateCategories"
            :key="option.id"
            :label="option.name"
            :value="option.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <el-row> </el-row>
    <el-row class="de-root-class">
      <el-button secondary @click="cancel()">{{ t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="saveChange()">{{ t('commons.confirm') }}</el-button>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { batchUpdate, findOne } from '@/api/template'
import { ElMessage } from 'element-plus-secondary'
const emits = defineEmits(['closeBatchEditTemplateDialog', 'refresh'])
const { t } = useI18n()
const props = defineProps({
  templateCategories: {
    type: Array,
    required: true
  },
  templateIds: {
    type: Array,
    required: true
  }
})

const state = reactive({
  templateInfo: {
    categories: []
  },
  categories: [],
  templateInfoRules: {
    categories: [
      {
        required: true,
        message: t('template_manage.please_select_catalog'),
        trigger: 'change'
      }
    ]
  }
})

const initCategories = () => {
  if (!props.templateIds?.length || props.templateIds.length > 1) {
    state.templateInfo.categories = []
    return
  }
  findOne(props.templateIds[0]).then(rsp => {
    state.templateInfo.categories = rsp.data?.categories || []
  })
}

onMounted(() => {
  initCategories()
})

const cancel = () => {
  emits('closeBatchEditTemplateDialog')
}

const saveChange = () => {
  const params = {
    templateIds: props.templateIds,
    categories: state.templateInfo.categories
  }
  if (!state.templateInfo.categories.length) {
    ElMessage.warning(t('template_manage.please_select_catalog'))
    return false
  }
  batchUpdate(params).then(() => {
    ElMessage({
      message: t('template_manage.edit_success'),
      type: 'success',
      showClose: true
    })
    emits('refresh')
    emits('closeBatchEditTemplateDialog')
  })
}
</script>

<style scoped lang="less">
.de-root-class {
  justify-content: flex-end;
}
</style>
