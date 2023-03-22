<script setup lang="ts">
import { computed, ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { styleData, selectKey, optionMap } from '@/utils/attr'

const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)
const activeName = ref(curComponent.value.collapseName)

const styleKeys = computed(() => {
  if (curComponent) {
    const curComponentStyleKeys = Object.keys(curComponent.value.style)
    return styleData.filter(item => curComponentStyleKeys.includes(item.key))
  } else {
    return null
  }
})

const onChange = () => {
  curComponent.value.collapseName = activeName
}

const isIncludesColor = str => {
  return str.toLowerCase().includes('color')
}
</script>

<template>
  <div class="v-common-attr">
    <el-collapse v-model="activeName" accordion @change="onChange">
      <el-collapse-item title="通用样式" name="style">
        <el-form>
          <el-form-item v-for="({ key, label }, index) in styleKeys" :key="index" :label="label">
            <el-color-picker
              v-if="isIncludesColor(key)"
              v-model="curComponent.style[key]"
              show-alpha
            ></el-color-picker>
            <el-select v-else-if="selectKey.includes(key)" v-model="curComponent.style[key]">
              <el-option
                v-for="item in optionMap[key]"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
            <el-input v-else v-model.number="curComponent.style[key]" type="number" />
          </el-form-item>
        </el-form>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<style lang="scss">
.v-common-attr {
  .el-input-group__prepend {
    padding: 0 10px;
  }
}
</style>
