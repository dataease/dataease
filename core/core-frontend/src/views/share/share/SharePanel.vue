<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { onMounted } from 'vue'
const { t } = useI18n()

const emits = defineEmits(['loaded'])
const panelInfo = {
  title: t('visualization.share_out'),
  name: 'share'
}
onMounted(() => {
  emits('loaded', panelInfo)
})
</script>
