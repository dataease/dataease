<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import type { Field, Node } from './util'
const props = defineProps<{ target: Field | Node; table?: boolean }>()
const emit = defineEmits<{ change: [value: Partial<Field & Node>] }>()
const { t } = useI18n()
const { t: tr } = useI18n('datasourceMetadata')
const visible = ref(false)
const draft = ref<Partial<Field & Node>>({})
const references = ref<{ value: string; meaning?: string | null }[]>([])
const open = () => {
  draft.value = {
    semanticBusinessName: props.target.semanticBusinessName ?? '',
    semanticDescription: props.target.semanticDescription ?? '',
    semanticAliases: props.target.semanticAliases ?? '',
    semanticGrain: props.target.semanticGrain ?? ''
  }
  references.value = JSON.parse(props.target.semanticValueReferences || '[]')
  visible.value = true
}
const save = () => {
  emit('change', {
    ...draft.value,
    semanticValueReferences: JSON.stringify(references.value),
    semanticConfirmed: true
  })
  visible.value = false
}
</script>
<template>
  <el-button class="semantic-edit-button" text type="primary" size="small" @click="open">{{
    tr('edit')
  }}</el-button>
  <el-drawer
    v-model="visible"
    :title="table ? tr('tableSemantics') : tr('fieldSemantics')"
    size="560px"
    append-to-body
    destroy-on-close
  >
    <el-form label-position="top">
      <el-form-item v-if="!table" :label="tr('businessName')">
        <el-input v-model="draft.semanticBusinessName" maxlength="255" />
      </el-form-item>
      <el-form-item :label="tr('description')">
        <el-input v-model="draft.semanticDescription" type="textarea" :rows="4" maxlength="4000" />
      </el-form-item>
      <template v-if="!table">
        <el-form-item :label="tr('aliases')"
          ><el-input v-model="draft.semanticAliases" maxlength="1000"
        /></el-form-item>
        <el-form-item :label="tr('valueReferences')">
          <div class="semantic-references">
            <div v-for="(item, index) in references" :key="index" class="semantic-reference">
              <el-input v-model="item.value" :placeholder="tr('referenceValue')" maxlength="255" />
              <el-input
                v-model="item.meaning"
                :placeholder="tr('referenceMeaning')"
                maxlength="255"
              />
              <el-button text @click="references.splice(index, 1)">{{
                tr('removeReference')
              }}</el-button>
            </div>
            <el-button
              :disabled="references.length >= 20"
              @click="references.push({ value: '', meaning: '' })"
              >{{ tr('addReference') }}</el-button
            >
          </div>
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button
        type="primary"
        :disabled="
          (!table && !draft.semanticBusinessName?.trim()) ||
          references.some(item => !item.value.trim())
        "
        @click="save"
        >{{ t('common.sure') }}</el-button
      >
    </template>
  </el-drawer>
</template>
<style scoped lang="less">
@media (hover: hover) {
  .semantic-edit-button {
    opacity: 0;
    pointer-events: none;
  }

  :global(.dataset-semantic-cell:hover .semantic-edit-button),
  .semantic-edit-button:focus-visible {
    opacity: 1;
    pointer-events: auto;
  }
}

.semantic-references {
  width: 100%;
}
.semantic-reference {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
</style>
