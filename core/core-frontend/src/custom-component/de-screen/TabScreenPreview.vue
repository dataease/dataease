<script setup lang="ts">
import { computed, ref, watch, nextTick, onMounted } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useLinkStoreWithOut } from '@/store/modules/link'
import { getSubShareInfo } from '@/api/share'

const props = defineProps({
  screenId: {
    type: String,
    required: true
  },
  dvType: {
    type: String,
    default: 'dashboard'
  }
})

const { t } = useI18n()
const linkStore = useLinkStoreWithOut()

// 公共链接场景：父页面持有 linkToken。此时子资源需使用各自的公共链接独立访问，
// 而不能复用父级 token（token 与父资源 id 绑定，子请求会鉴权失败）。
const publicLinkMode = computed(() => !!linkStore.getLinkToken)

// 子资源公共链接检查结果
const subReady = ref(true)
const subUuid = ref('')
const checking = ref(false)

// 每个 tab 用独立 iframe 承载大屏/仪表板，从而与其它 tab 完全隔离（独立 JS 上下文与 dvMain store）
const frameSrc = computed(() => {
  // 公共链接场景下走子资源自身的公共链接页面（/de-link/<uuid>），
  // 由子页面在其独立上下文内完成鉴权（密码/有效期等）。
  // 注意：ShareProxy.setUuid 以 lastIndexOf('?') 作为 uuid 结束边界，
  // 因此此处不能在 hash 前加 ?时间戳，否则会破坏 uuid 解析（刷新已由 frameShow 重建保证）
  if (publicLinkMode.value) {
    return `#/de-link/${subUuid.value}?embeddedTab=true`
  }
  return `?${new Date().getTime()}#/preview?dvId=${props.screenId}&dvType=${
    props.dvType
  }&ignoreParams=true&embeddedTab=true`
})

// screenId 变化时重建 iframe，避免复用旧上下文
const frameShow = ref(true)

const rebuild = () => {
  frameShow.value = false
  nextTick(() => {
    frameShow.value = true
  })
}

const checkSubShare = async () => {
  if (!publicLinkMode.value) {
    subReady.value = true
    rebuild()
    return
  }
  checking.value = true
  try {
    const res = await getSubShareInfo(props.screenId)
    if (res?.exist && res?.uuid) {
      subUuid.value = res.uuid
      subReady.value = true
    } else {
      subReady.value = false
    }
  } catch (e) {
    subReady.value = false
  } finally {
    checking.value = false
    if (subReady.value) {
      rebuild()
    }
  }
}

watch(
  () => props.screenId,
  () => {
    checkSubShare()
  }
)

onMounted(() => {
  checkSubShare()
})
</script>

<template>
  <div class="tab-screen-wrapper">
    <div v-if="publicLinkMode && !checking && !subReady" class="tab-screen-tip">
      {{ t('link_ticket.sub_link_absent') }}
    </div>
    <iframe
      v-else-if="frameShow && (!publicLinkMode || subReady)"
      :id="'tab-screen-frame-' + props.screenId"
      :src="frameSrc"
      scrolling="auto"
      frameborder="0"
      class="tab-screen-frame"
    />
  </div>
</template>

<style lang="less" scoped>
.tab-screen-wrapper {
  width: 100%;
  height: 100%;
}
.tab-screen-frame {
  width: 100%;
  height: 100%;
  border: none;
}
.tab-screen-tip {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ed-text-color-secondary, #8f959e);
  font-size: 14px;
}
</style>
