<template>
  <div v-loading="loading">
    <div
      v-for="(item, index) in items"
      :key="index"
      class="kv-row item"
    >

      <el-row
        type="flex"
        :gutter="20"
        justify="space-between"
        align="middle"
      >
        <span style="margin-left: 10px" />
        <i
          class="el-icon-top"
          style="cursor:pointer"
          @click="moveTop(index)"
        />
        <i
          class="el-icon-bottom"
          style="cursor:pointer;"
          @click="moveBottom(index)"
        />

        <el-col
          v-if="unShowSelect===false"
          class="item"
        >
          <el-input
            v-if="!suggestions"
            v-model="item.name"
            :disabled="isReadOnly"
            size="small"
            maxlength="200"
            :placeholder="keyText"
            show-word-limit
            @change="change"
          />
          <el-autocomplete
            v-if="suggestions"
            v-model="item.name"
            :disabled="isReadOnly"
            :maxlength="400"
            size="small"
            :fetch-suggestions="querySearch"
            :placeholder="keyText"
            show-word-limit
            @change="change"
          />
        </el-col>

        <el-col
          v-if="unShowSelect===true"
          class="item"
        >
          <el-input
            v-if="suggestions"
            v-model="item.name"
            :disabled="isReadOnly"
            size="small"
            maxlength="200"
            :placeholder="keyText"
            show-word-limit
          />
        </el-col>

        <el-col class="item">
          <el-input
            v-if="!needMock"
            v-model="item.value"
            :disabled="isReadOnly"
            size="small"
            :placeholder="unShowSelect?$t('commons.description'):valueText"
            show-word-limit
            @change="change"
          />
        </el-col>

        <el-col
          v-if="showDesc"
          class="item"
        >
          <el-input
            v-model="item.description"
            size="small"
            maxlength="200"
            :placeholder="$t('commons.description')"
            show-word-limit
          />
        </el-col>

        <el-col class="item kv-delete">
          <el-button
            size="mini"
            class="el-icon-delete-solid"
            circle
            :disabled="isDisable(index)"
            @click="remove(index)"
          />
        </el-col>
      </el-row>
    </div>

  </div>
</template>

<script>
import { KeyValue } from './ApiTestModel'
import Vue from 'vue'

export default {
  name: 'ApiKeyValue',
  components: {},
  props: {
    keyPlaceholder: String,
    valuePlaceholder: String,
    isShowEnable: {
      type: Boolean,
      default: false
    },
    unShowSelect: {
      type: Boolean,
      default: false
    },
    description: String,
    items: Array,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    suggestions: Array,
    needMock: {
      type: Boolean,
      default: false
    },
    showDesc: Boolean,
    appendToBody: {
      type: Boolean,
      default() {
        return false
      }
    }
  },
  data() {
    return {
      keyValues: [],
      loading: false,
      currentItem: {},
      isSelectAll: true
    }
  },
  computed: {
    keyText() {
      return this.keyPlaceholder || this.$t('datasource.key')
    },
    valueText() {
      return this.valuePlaceholder || this.$t('datasource.value')
    }
  },
  watch: {
    isSelectAll: function(to, from) {
      if (!from && to) {
        this.selectAll()
      } else if (from && !to) {
        this.invertSelect()
      }
    }
  },
  created() {
    if (this.items.length === 0 || this.items[this.items.length - 1].name) {
      this.items.push(new KeyValue({ enable: true, name: '', value: '' }))
    }
  },
  methods: {
    advanced(item) {
      this.currentItem = item
      this.$refs.variableAdvance.open()
    },
    funcFilter(queryString) {
      return (func) => {
        return (func.name.toLowerCase().indexOf(queryString.toLowerCase()) > -1)
      }
    },
    moveBottom(index) {
      if (this.items.length < 2 || index === this.items.length - 2) {
        return
      }
      const thisRow = this.items[index]
      const nextRow = this.items[index + 1]
      Vue.set(this.items, index + 1, thisRow)
      Vue.set(this.items, index, nextRow)
    },
    moveTop(index) {
      if (index === 0) {
        return
      }
      const thisRow = this.items[index]
      const lastRow = this.items[index - 1]
      Vue.set(this.items, index - 1, thisRow)
      Vue.set(this.items, index, lastRow)
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    remove: function(index) {
      // 移除整行输入控件及内容
      this.items.splice(index, 1)
      this.$emit('change', this.items)
    },
    change: function() {
      let isNeedCreate = true
      this.items.forEach((item, index) => {
        if (!item.name && !item.value) {
          // 没有空行，需要创建空行
          isNeedCreate = false
        }
      })
      if (isNeedCreate) {
        this.items.push(new KeyValue({ enable: true }))
      }
      this.$emit('change', this.items)
    },
    isDisable: function(index) {
      return this.items.length - 1 === index
    },
    querySearch(queryString, cb) {
      const suggestions = this.suggestions
      const results = queryString ? suggestions.filter(this.createFilter(queryString)) : suggestions
      cb(results)
    },
    createFilter(queryString) {
      return (restaurant) => {
        return (restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0)
      }
    },
    selectAll() {
      this.items.forEach(item => {
        item.enable = true
      })
    },
    invertSelect() {
      this.items.forEach(item => {
        item.enable = false
      })
    }
  }
}
</script>

<style scoped>
  .kv-description {
    font-size: 13px;
  }

  .kv-row {
    margin-top: 10px;
  }

  .kv-checkbox {
    width: 20px;
    margin-right: 10px;
  }

  .kv-delete {
    width: 60px;
  }

  .el-autocomplete {
    width: 100%;
  }

  i:hover {
    color: #783887;
  }
</style>
