<template>
  <div class="template border-radius-12">
    <div class="photo">
      <img
        :src="imgUrlTrans(thumbnailUrl)"
        alt=""
        sizes=""
        fetchpriority="high"
        style="width: 100%"
      />
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
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  width: calc(20% - 16px);
  margin-left: 16px;
  height: auto;
  position: relative;
  padding-bottom: 39px;
  margin-bottom: 16px;

  .photo {
    box-sizing: border-box;
    padding: 4px;
    padding-bottom: 0;
    aspect-ratio: 1.774 / 1;
    width: 100%;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      display: block;
      object-fit: cover;
    }
  }

  .apply {
    box-sizing: border-box;
    padding: 8px 12px;
    background: #fff;
    border-top: 1px solid #d9d9d9;
    width: 100%;
    height: 39px;
    display: flex;
    flex-wrap: wrap;
    border-bottom-left-radius: 4px;
    border-bottom-right-radius: 4px;
    justify-content: space-between;
    position: absolute;
    bottom: 0;
    left: 0;

    .ed-button {
      min-width: 48px;
      height: 28px;
      display: none;
      font-size: 12px;
      line-height: 20px;
      padding: 0;
      margin-top: 8px;
      width: calc(50% - 6px);
      & + .ed-button {
        margin-left: 8px;
      }
    }
    .name {
      color: #1f2329;
      font-family: var(--de-custom_font, 'PingFang');
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
      height: 75px;
    }
    .ed-button {
      display: block;
    }
  }
}

.fix-height {
  height: 39px !important;
}

@media screen and (max-width: 1420px) {
  .template {
    width: calc(25% - 16px);
  }
}

@media screen and (max-width: 1200px) {
  .template {
    width: calc(33.3333% - 16px);
  }
}

@media screen and (max-width: 920px) {
  .template {
    width: calc(50% - 16px);
    min-width: 180px;
  }
}
</style>
