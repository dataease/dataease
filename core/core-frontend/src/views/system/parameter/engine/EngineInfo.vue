<template>
  <EngineInfoTemplate ref="engineInfoTemplate" setting-key="engine" @edit="edit" />
  <engine-edit ref="editor" @saved="refresh" />
</template>

<script lang="ts" setup>
import { ref } from 'vue'

import EngineInfoTemplate from '@/views/system/parameter/engine/EngineInfoTemplate.vue'
import EngineEdit from '@/views/system/parameter/engine/EngineEdit.vue'
const editor = ref()
const engineInfoTemplate = ref()

const edit = () => {
  editor?.value.edit()
}

const refresh = () => {
  engineInfoTemplate?.value.getEngine()
}
</script>
