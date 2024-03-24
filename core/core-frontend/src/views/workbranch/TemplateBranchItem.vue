<template>
  <div class="template">
    <div class="photo">
      <div class="img" :style="classBackground"></div>
    </div>
    <div class="apply" :class="{ 'fix-height': !createAuth[template.templateType] }">
      <span :title="template.title" class="name ellipsis">
        {{ template.title }}
      </span>
      <el-button class="flex-center" secondary @click="templateInnerPreview">{{
        t('dataset.preview')
      }}</el-button>
      <el-button class="flex-center" type="primary" @click="apply">{{
        t('commons.apply')
      }}</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from '@/hooks/web/useI18n'
import { computed } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
const { t } = useI18n()

const emits = defineEmits(['templateApply', 'templatePreview'])

const props = defineProps({
  template: {
    type: Object,
    default() {
      return {}
    }
  },
  createAuth: {
    type: Object,
    default() {
      return {
        PANEL: false,
        SCREEN: false
      }
    }
  },
  baseUrl: {
    type: String
  }
})

const classBackground = computed(() => {
  return {
    background: `url(${imgUrlTrans(thumbnailUrl.value)}) no-repeat`,
    'background-size': `100% 100%`
  }
})

const thumbnailUrl = computed(() => {
  if (
    props.template.thumbnail.indexOf('http') > -1 ||
    props.template.thumbnail.indexOf('static-resource') > -1
  ) {
    return props.template.thumbnail
  } else {
    return props.baseUrl + props.template.thumbnail
  }
})

const apply = () => {
  emits('templateApply', props.template)
}

const templateInnerPreview = () => {
  emits('templatePreview', props.template.id)
}
</script>

<style scoped lang="less">
.template {
  overflow: hidden;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  display: flex;
  flex-wrap: wrap;
  min-width: 181px;
  height: 141px;
  margin-left: 16px;
  position: relative;

  .photo {
    padding: 4px;
    padding-bottom: 0;
    height: 101px;
    width: 100%;
    .img {
      width: 100%;
      height: 100%;
      border-top-left-radius: 4px;
      border-top-right-radius: 4px;
    }
  }

  .apply {
    padding: 8px 12px;
    background: #fff;
    border-top: 1px solid #d9d9d9;
    position: absolute;
    width: 100%;
    left: 0;
    bottom: 0;
    height: 39px;
    display: flex;
    flex-wrap: wrap;
    border-bottom-left-radius: 4px;
    border-bottom-right-radius: 4px;
    justify-content: space-between;

    .ed-button {
      min-width: 73px;
      height: 28px;
      display: none;
      font-size: 12px;
      line-height: 20px;
      padding: 0;
      margin-top: 8px;
      & + .ed-button {
        margin-left: 8px;
      }
    }
    .name {
      color: #1f2329;
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
      font-size: 14px;
      font-style: normal;
      font-weight: 500;
      line-height: 22px;
      width: 100%;
    }
  }

  &:hover {
    box-shadow: 0px 6px 24px 0px rgba(31, 35, 41, 0.08);
    .apply {
      transition: 0.3s;
      height: 73px;
    }
    .ed-button {
      display: block;
    }
  }
}

.fix-height {
  height: 39px !important;
}
</style>
