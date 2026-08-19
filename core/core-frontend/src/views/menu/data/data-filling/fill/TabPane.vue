<script setup lang="ts">
import {onMounted} from "vue";
import {useI18n} from "@/hooks/web/useI18n";
const { t } = useI18n()

const emits = defineEmits(['loaded'])

onMounted(()=>{
  emits('loaded', { title: t('data_fill.my_job'), name: 'data-filling', shortName: t('data_fill.short_name') , disabled: false })
})
</script>

<template>
</template>

<style scoped lang="less">

</style>
