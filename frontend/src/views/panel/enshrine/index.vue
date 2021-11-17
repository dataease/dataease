<template>
  <div>
    <el-table
      class="de-filter-data-table"
      :data="starDatas"
      :show-header="false"
      :highlight-current-row="true"
      style="width: 100%"
    >
      <el-table-column prop="name" :label="$t('commons.name')">
        <template :id="scope.row.storeId" slot-scope="scope">
          <div class="start-item">
            <div class="filter-db-row star-item-content" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;" @click="showPanel(scope.row)">
              <svg-icon icon-class="panel" class="ds-icon-scene" />
              <span> {{ scope.row.name }}</span>
            </div>
            <div class="star-item-close">
              <i class="el-icon-delete " @click="remove(scope.row)" />
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { deleteEnshrine, enshrineList } from '@/api/panel/enshrine'
import { uuid } from 'vue-uuid'
import { get } from '@/api/panel/panel'
import bus from '@/utils/bus'
export default {
  name: 'Enshrine',
  data() {
    return {
      starDatas: []
    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    }
  },
  created() {
    bus.$on('panle_start_list_refresh', this.refreshStarts)
    this.initData()
  },
  methods: {
    showPanel(row) {
      get('panel/group/findOne/' + row.panelGroupId).then(response => {
        this.$store.commit('setComponentData', this.resetID(JSON.parse(response.data.panelData)))
        this.$store.commit('setCanvasStyle', JSON.parse(response.data.panelStyle))
        const data = {
          id: row.panelGroupId,
          name: row.name
        }
        this.$store.dispatch('panel/setPanelInfo', data)
        bus.$emit('set-panel-show-type', 0)
      })
    },
    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.type !== 'custom' && (item.id = uuid.v1())
        })
      }

      return data
    },
    remove(row) {
      deleteEnshrine(row.panelGroupId).then(res => {
        this.initData()
        this.panelInfo && this.panelInfo.id && row.panelGroupId === this.panelInfo.id && this.setMainNull()
      })
    },
    initData() {
      enshrineList({}).then(res => {
        this.starDatas = res.data
      })
    },
    setMainNull() {
      this.$store.dispatch('panel/setPanelInfo', { id: null, name: '', preStyle: null })
    },
    refreshStarts(isStar) {
      this.initData()
      !isStar && this.setMainNull()
    }
  }
}
</script>

<style lang="scss" scoped>
.start-item {
    width: 100%;
    height: 25px;
    margin: 0 0 0 10px;
}
.star-item-content {
    width: calc(100% - 60px);
    position: absolute;
}
.star-item-close {
    width: 25px;
    right: 5px;
    position: absolute;
    display: none;
}
.start-item:hover {
    .star-item-close {
        display: block;
    }
}
</style>
