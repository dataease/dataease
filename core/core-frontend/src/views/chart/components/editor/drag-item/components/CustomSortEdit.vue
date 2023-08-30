<script lang="tsx" setup>
import draggable from 'vuedraggable'
import { getFieldData } from '@/api/chart'
import { reactive, watch, ref } from 'vue'
import { useCache } from '@/hooks/web/useCache'

const { wsCache } = useCache()
const loading = ref(false)

const state = reactive({
  sortList: []
})

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  field: {
    type: Object,
    required: true
  },
  fieldType: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['onSortChange'])

watch(
  () => props.chart,
  () => {
    init()
  },
  { deep: true }
)

const init = () => {
  loading.value = true
  const chart = props.chart
  if (!chart.chartExtRequest) {
    chart.chartExtRequest = {
      user: wsCache.get('user.uid')
    }
  }
  getFieldData(props.field.id, props.fieldType, chart)
    .then(response => {
      const strArr = response.data
      state.sortList = strArr.map(ele => {
        return transStr2Obj(ele)
      })
      onUpdate()
      loading.value = false
    })
    .catch(() => {
      loading.value = false
    })
}
const onUpdate = () => {
  const strArr = state.sortList.map(ele => {
    return transObj2Str(ele)
  })
  emit('onSortChange', strArr)
}

const transStr2Obj = str => {
  return { name: 'name', value: str }
}

const transObj2Str = obj => {
  return obj.value
}

init()
</script>

<template>
  <div>
    <draggable
      v-loading="loading"
      :list="state.sortList"
      :move="onMove"
      group="drag"
      animation="300"
      class="drag-list"
      item-key="name"
      @update="onUpdate"
    >
      <template #item="{ element }">
        <span :key="element.value" class="item-dimension" :title="element.value">
          <Icon name="icon_drag_outlined" class="item-icon" @click="removeItem"></Icon>
          <span class="item-span">
            {{ element.value }}
          </span>
        </span>
      </template>
    </draggable>
  </div>
</template>

<style lang="less" scoped>
.drag-list {
  overflow: auto;
  height: 50vh;
}

.item-dimension {
  padding: 2px;
  margin: 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
  background-color: white;
  display: flex;
  align-items: center;
}

.item-icon {
  cursor: move;
  margin: 0 2px;
  width: 20px;
  height: 20px;
}

.item-span {
  display: inline-block;
  width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.blackTheme .item-dimension {
  border: solid 1px;
  border-color: var(--TableBorderColor);
  color: var(--TextPrimary);
  background-color: var(--MainBG);
}

.item-dimension + .item-dimension {
  margin-top: 6px;
}

.item-dimension:hover {
  color: #1890ff;
  background: #e8f4ff;
  border-color: #a3d3ff;
  cursor: pointer;
}

.blackTheme .item-dimension:hover {
  color: var(--Main);
  background: var(--ContentBG);
  cursor: pointer;
}
</style>
