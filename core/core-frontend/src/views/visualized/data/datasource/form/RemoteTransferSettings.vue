<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElIcon } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import icon_down_outlined from '@/assets/svg/icon_down_outlined.svg'
import icon_down_outlined1 from '@/assets/svg/icon_down_outlined-1.svg'
import { useI18n } from '@/hooks/web/useI18n'

interface TransferSettings {
  maxResponseSizeMb?: number
  maxFileSizeMb?: number
  transferTimeoutSeconds?: number
}
const props = withDefaults(
  defineProps<{ model: TransferSettings; file?: boolean; prefix?: string }>(),
  { file: false, prefix: '' }
)
const emit = defineEmits<{ 'update:model': [value: TransferSettings] }>()
const { t } = useI18n()
const expanded = ref(false)
const sizeKey = computed(() => (props.file ? 'maxFileSizeMb' : 'maxResponseSizeMb'))
const maximum = computed(() => (props.file ? 1024 : 64))
const size = computed({
  get: () => props.model[sizeKey.value] ?? (props.file ? 100 : 16),
  set: value => emit('update:model', { ...props.model, [sizeKey.value]: value })
})
const timeout = computed({
  get: () => props.model.transferTimeoutSeconds ?? 120,
  set: value => emit('update:model', { ...props.model, transferTimeoutSeconds: value })
})
const validate = (value: number, max: number, callback: (error?: Error) => void) => {
  callback(
    Number.isInteger(value) && value >= 1 && value <= max
      ? undefined
      : new Error(t('remote_transfer.range', [max]))
  )
}
</script>

<template>
  <div class="remote-transfer-settings">
    <el-form-item>
      <button
        type="button"
        class="de-expand"
        :aria-expanded="expanded"
        @click="expanded = !expanded"
      >
        {{ t('datasource.priority') }}
        <el-icon>
          <Icon><component :is="expanded ? icon_down_outlined : icon_down_outlined1" /></Icon>
        </el-icon>
      </button>
    </el-form-item>
    <div v-show="expanded">
      <el-row :gutter="24" class="transfer-fields">
        <el-col :span="12">
          <el-form-item
            :label="`${t(
              file ? 'remote_transfer.file_size' : 'remote_transfer.response_size'
            )}(MB)`"
            :prop="`${prefix}${sizeKey}`"
            :rules="{ validator: (_rule, _value, callback) => validate(size, maximum, callback) }"
          >
            <el-input-number
              v-model="size"
              controls-position="right"
              autocomplete="off"
              :min="1"
              :max="maximum"
              :precision="0"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            :label="`${t('remote_transfer.timeout')}(${t('common.second')})`"
            :prop="`${prefix}transferTimeoutSeconds`"
            :rules="{ validator: (_rule, _value, callback) => validate(timeout, 1800, callback) }"
          >
            <el-input-number
              v-model="timeout"
              controls-position="right"
              autocomplete="off"
              :min="1"
              :max="1800"
              :precision="0"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="transfer-help">
        {{ t(file ? 'remote_transfer.file_help' : 'remote_transfer.help') }}
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.de-expand {
  padding: 0;
  border: 0;
  background: transparent;
  font-family: var(--de-custom_font, 'PingFang');
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  color: var(--ed-color-primary);
  cursor: pointer;
  display: inline-flex;
  align-items: center;

  .ed-icon {
    margin-left: 4px;
  }
}
.transfer-fields {
  :deep(.ed-form-item) {
    margin-bottom: 16px;
  }

  .ed-input-number {
    width: 100%;
  }

  :deep(.is-controls-right > span) {
    background: var(--ed-fill-color-blank);
  }
}
.transfer-help {
  margin-bottom: 16px;
  color: var(--ed-text-color-secondary);
  font-size: 12px;
  line-height: 20px;
}
</style>
