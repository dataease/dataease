<script setup lang="ts">
import BackgroundOverallCommon from '@/components/visualization/component-background/BackgroundOverallCommon.vue'
import { ref, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
const emits = defineEmits(['onTitleBackgroundChange'])
const { t } = useI18n()
const activeName = ref('activeBackground')

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    element: any
  }>(),
  {
    showStyle: true,
    themes: 'dark'
  }
)

const { element } = toRefs(props)

const onTitleBackgroundChange = (params, paramsName) => {
  // do change
  if (params) {
    element.value.titleBackground[paramsName] = params
  }
  emits('onTitleBackgroundChange', element.value.titleBackground)
}
</script>

<template>
  <div class="tab-title-background">
    <el-tabs class="background-tabs" v-model="activeName" stretch>
      <el-tab-pane :label="t('visualization.active_title_background')" name="activeBackground">
        <background-overall-common
          :themes="themes"
          edit-position="tab"
          :common-background-pop="element.titleBackground.active"
          component-position="component"
          @onBackgroundChange="onTitleBackgroundChange($event, 'active')"
        />
      </el-tab-pane>
      <el-tab-pane :label="t('visualization.inactive_title_background')" name="inActiveBackground">
        <div class="background-label">
          <span>
            <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
              <el-checkbox
                size="small"
                :effect="themes"
                v-model="element.titleBackground.multiply"
                @change="onTitleBackgroundChange(null, null)"
              >
                {{ t('visualization.reuse_active_title_background') }}
              </el-checkbox>
            </el-form-item>
          </span>
        </div>
        <background-overall-common
          v-show="!element.titleBackground.multiply"
          :themes="themes"
          edit-position="tab"
          :common-background-pop="element.titleBackground.inActive"
          component-position="component"
          @onBackgroundChange="onTitleBackgroundChange($event, 'inActive')"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped lang="less">
.tab-title-background {
  width: 100%;
  height: 100%;
}
.background-label {
  font-weight: 500;
  font-size: 12px;
  margin-bottom: 8px;
  display: flex;
}
.background-tabs {
  --ed-tabs-header-height: 24px;

  :deep(.ed-tabs__active-bar) {
    height: 1px;
  }

  :deep(.ed-tabs__item) {
    font-size: 12px;
  }

  :deep(.ed-tabs__content) {
    padding: 12px 0;
  }
}
</style>
