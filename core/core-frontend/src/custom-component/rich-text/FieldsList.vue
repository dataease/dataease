<template>
  <div @mousedown="fieldsAreaDown" class="field-main">
    <el-button
      v-for="field in fields"
      :key="field.id"
      :title="field.name"
      size="mini"
      class="field-area"
      @click="fieldSelect(field)"
    >
      {{ field.name }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { toRefs } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'

const props = defineProps({
  fields: {
    type: Array,
    default: () => []
  },
  element: {
    type: Object,
    default: null
  }
})

const { fields, element } = toRefs(props)

const fieldSelect = field => {
  useEmitt().emitter.emit('fieldSelect-' + element.value.id, field)
}

const fieldsAreaDown = e => {
  e.preventDefault()
}
</script>

<style lang="less" scoped>
.field-main {
  width: 183px;
  max-height: 300px;
  overflow-x: hidden;
  overflow-y: auto;
}

.field-area {
  width: 174px;
  margin: 4px 0 0 0;
  text-align: left;
  margin-left: 0px !important;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
