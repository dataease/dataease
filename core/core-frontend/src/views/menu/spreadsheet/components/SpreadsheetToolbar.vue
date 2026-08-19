<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import Icon from '@/components/icon-custom/src/Icon.vue'
import { useI18n } from '@/hooks/web/useI18n'
import icon_left_outlined from '@/assets/svg/icon_left_outlined.svg'
import icon_sheet_datareference_outlined from '@/assets/svg/icon_sheet-datareference_outlined.svg'
import icon_pc_fullscreen from '@/assets/svg/icon_pc_fullscreen.svg'
import dvRecoverOutlined from '@/assets/svg/dv-recover_outlined.svg'
import dvCancelPublish from '@/assets/svg/icon_undo_outlined.svg'
import { SpreadsheetPublishStatus } from '../api'

const { t } = useI18n()

const props = defineProps<{
  name: string
  hasChanges?: boolean
  saving?: boolean
  publishing?: boolean
  recovering?: boolean
  status: SpreadsheetPublishStatus
}>()

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'save'): void
  (e: 'export'): void
  (e: 'preview'): void
  (e: 'fullscreen-preview'): void
  (e: 'publish'): void
  (e: 'recover-published'): void
  (e: 'cancel-publish'): void
  (e: 'replace-dataset'): void
  (e: 'rename', value: string): void
}>()

let nameEdit = ref(false)
let inputName = ref('')
let nameInput = ref(null)

const editCanvasName = () => {
  nameEdit.value = true
  inputName.value = props.name
  nextTick(() => {
    nameInput.value?.focus()
  })
}

const closeEditCanvasName = () => {
  nameEdit.value = false
  const nextName = inputName.value?.trim()

  if (!nextName) {
    inputName.value = props.name
    return
  }

  if (nextName === props.name) {
    inputName.value = props.name
    return
  }

  if (nextName.length > 64 || nextName.length < 1) {
    ElMessage.warning(t('components.length_1_64_characters'))
    editCanvasName()
    return
  }

  emit('rename', nextName)
  inputName.value = nextName
}

const handleBack = () => {
  if (props.hasChanges) {
    ElMessageBox.confirm(t('components.sure_to_exit'), {
      confirmButtonType: 'primary',
      type: 'warning',
      autofocus: false,
      showClose: false
    }).then(() => {
      emit('back')
    })
  } else {
    emit('back')
  }
}

const handleSave = () => {
  emit('save')
}

const handleExport = () => {
  emit('export')
}

const handlePreview = () => {
  emit('preview')
}

const handleFullscreenPreview = () => {
  emit('fullscreen-preview')
}

const handlePublish = () => {
  emit('publish')
}

const handleRecoverPublished = () => {
  emit('recover-published')
}

const handleCancelPublish = () => {
  emit('cancel-publish')
}

const handleReplaceDataset = () => {
  emit('replace-dataset')
}

const mutating = computed(() => props.saving || props.publishing || props.recovering)
</script>

<template>
  <div class="spreadsheet-toolbar-main">
    <div class="spreadsheet-toolbar">
      <!-- Back icon -->
      <el-icon class="custom-el-icon back-icon" @click="handleBack">
        <Icon name="icon_left_outlined">
          <icon_left_outlined class="svg-icon toolbar-icon" />
        </Icon>
      </el-icon>

      <!-- Left area - name -->
      <div class="left-area">
        <span id="spreadsheet-name" class="name-area" @dblclick="editCanvasName">
          {{ name }}
        </span>
      </div>

      <!-- Middle area - empty or can add tools later -->
      <div class="middle-area"></div>

      <!-- Right area - action buttons -->
      <div class="right-area">
        <el-tooltip
          effect="dark"
          :content="t('spreadsheet.dataset_replacement.title')"
          placement="bottom"
        >
          <button
            type="button"
            class="dataset-replacement-button"
            :aria-label="t('spreadsheet.dataset_replacement.title')"
            @click="handleReplaceDataset"
          >
            <Icon name="icon_sheet-datareference_outlined">
              <icon_sheet_datareference_outlined class="dataset-replacement-icon" />
            </Icon>
          </button>
        </el-tooltip>

        <!-- Export button -->
        <!-- <el-tooltip effect="dark" :content="t('chart.export')" placement="bottom">
          <el-button class="custom-normal-button" @click="handleExport">
            <el-icon style="margin-right: 4px">
              <Icon name="icon_download_outlined">
                <icon_download_outlined class="svg-icon" />
              </Icon>
            </el-icon>
            {{ t('chart.export') }}
          </el-button>
        </el-tooltip> -->

        <!-- Preview dropdown -->
        <el-dropdown trigger="hover">
          <el-button class="preview-button" @click="handlePreview">
            {{ t('visualization.preview') }}
          </el-button>
          <template #dropdown>
            <el-dropdown-menu class="drop-style">
              <el-dropdown-item @click="handleFullscreenPreview">
                <el-icon style="margin-right: 8px; font-size: 16px">
                  <Icon name="icon_pc_fullscreen">
                    <icon_pc_fullscreen class="svg-icon" />
                  </Icon>
                </el-icon>
                {{ t('visualization.fullscreen_preview') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- Save button -->
        <el-button type="primary" :loading="saving" :disabled="mutating" @click="handleSave">
          {{ t('data_set.save') }}
        </el-button>

        <!-- Publish button -->
        <el-dropdown
          trigger="hover"
          :disabled="status === SpreadsheetPublishStatus.Unpublished || mutating"
        >
          <el-button
            type="primary"
            class="publish-button"
            :loading="publishing"
            :disabled="mutating"
            @click="handlePublish"
          >
            {{ t('spreadsheet.publish') }}
          </el-button>
          <template #dropdown>
            <el-dropdown-menu class="drop-style">
              <el-dropdown-item
                v-if="status === SpreadsheetPublishStatus.SavedUnpublished"
                :disabled="mutating"
                @click="handleRecoverPublished"
              >
                <el-icon class="handle-icon">
                  <Icon name="icon_left_outlined">
                    <dv-recover-outlined class="svg-icon toolbar-icon" />
                  </Icon>
                </el-icon>
                {{ t('spreadsheet.recover_publish') }}
              </el-dropdown-item>
              <el-dropdown-item
                v-if="
                  status === SpreadsheetPublishStatus.Published ||
                  status === SpreadsheetPublishStatus.SavedUnpublished
                "
                :disabled="mutating"
                @click="handleCancelPublish"
              >
                <el-icon class="handle-icon">
                  <Icon name="icon_left_outlined">
                    <dv-cancel-publish class="svg-icon toolbar-icon" />
                  </Icon>
                </el-icon>
                {{ t('spreadsheet.unpublish') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- Name edit input -->
    <Teleport v-if="nameEdit" :to="'#spreadsheet-name'">
      <input
        ref="nameInput"
        v-model="inputName"
        @blur="closeEditCanvasName"
        @keyup.enter="closeEditCanvasName"
      />
    </Teleport>
  </div>
</template>

<style lang="less" scoped>
.drop-style {
  :deep(.ed-dropdown-menu__item) {
    padding: 5px 12px !important;
  }
  :deep(.ed-dropdown-menu__item:not(.is_disabled):focus) {
    color: inherit;
    background-color: rgba(31, 35, 41, 0.1);
  }
}

.spreadsheet-toolbar-main {
  position: relative;
}

.spreadsheet-toolbar {
  height: @top-bar-height;
  white-space: nowrap;
  overflow-x: auto;
  background: #050e21;
  color: #ffffff;
  display: flex;
  align-items: center;
  transition: 0.5s;

  .back-icon {
    margin-left: 20px;
    font-size: 20px;
    cursor: pointer;
  }

  .left-area {
    margin-left: 14px;
    min-width: 200px;
    display: flex;
    flex-direction: column;

    .name-area {
      position: relative;
      line-height: 24px;
      height: 24px;
      font-size: 16px;
      max-width: 300px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      cursor: pointer;

      input {
        position: absolute;
        left: 0;
        width: 100%;
        color: #fff;
        background-color: #050e21;
        outline: none;
        border: 1px solid #295acc;
        border-radius: 4px;
        padding: 0 4px;
        height: 100%;
      }
    }
  }

  .middle-area {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .right-area {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding-right: 16px;
    gap: 12px;
  }

  .dataset-replacement-button {
    display: inline-flex;
    flex: 0 0 28px;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    padding: 4px;
    color: rgba(255, 255, 255, 0.6);
    background: transparent;
    border: 0;
    border-radius: 6px;
    outline: none;
    cursor: pointer;

    &:hover,
    &:focus-visible {
      color: rgba(255, 255, 255, 0.6);
      background: rgba(255, 255, 255, 0.1);
    }

    &:active {
      background: rgba(255, 255, 255, 0.2);
    }

    .dataset-replacement-icon {
      display: block;
      width: 20px;
      height: 20px;
    }
  }

  .custom-el-icon {
    margin-left: 15px;
    color: #ffffff;
    cursor: pointer;
    vertical-align: -0.2em;
  }

  .toolbar-icon {
    width: 20px;
    height: 20px;
  }
}

.preview-button {
  border-color: rgba(255, 255, 255, 0.3);
  color: #ffffff;
  background-color: #050e21;

  &:hover,
  &:focus {
    background-color: #121a2c;
    border-color: #595f6b;
  }

  &:active {
    border-color: #616774;
    background-color: #1e2637;
  }
}

.custom-normal-button {
  background-color: transparent;
  border-color: #a6a6a6 !important;
  color: #ffffff !important;

  &:hover {
    color: #ffffff;
    background-color: #ffffff1a !important;
  }

  &:active {
    color: #ffffff;
    background-color: #ffffff33 !important;
  }

  &.is-disabled {
    color: var(--ed-button-disabled-text-color) !important;
  }
}
</style>
