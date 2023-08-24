<template>
  <div>
    <el-table
      class="de-filter-data-table"
      :data="starData"
      :show-header="false"
      :highlight-current-row="true"
      style="width: 100%"
    >
      <el-table-column
        prop="name"
        :label="$t('commons.name')"
      >
        <template
          slot-scope="scope"
        >
          <div class="start-item">
            <div
              class="filter-db-row star-item-content"
              style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
              @click="showPanel(scope.row)"
            >
              <svg-icon
                :icon-class="'panel-'+scope.row.status"
                class="ds-icon-scene"
              />
              <span :class="scope.row.status"> {{ scope.row.name }}</span>
            </div>
            <div class="star-item-close">
              <i
                class="el-icon-delete "
                @click="remove(scope.row)"
              />
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
import { initPanelData, viewPanelLog } from '@/api/panel/panel'
import bus from '@/utils/bus'

export default {
  name: 'Enshrine',
  data() {
    return {
      starData: []
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
  beforeDestroy() {
    bus.$off('panle_start_list_refresh', this.refreshStarts)
  },
  methods: {
    showPanel(row) {
      initPanelData(row.panelGroupId, false, function() {
        viewPanelLog({ panelId: row.panelGroupId }).then(res => {
          bus.$emit('set-panel-show-type', 0)
        })
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
        const msg = this.$t('commons.cancel') + this.$t('panel.store') + this.$t('commons.success')
        this.$success(msg)
        this.initData()
        this.panelInfo && this.panelInfo.id && row.panelGroupId === this.panelInfo.id && this.setMainNull()
      })
    },
    initData() {
      enshrineList({}).then(res => {
        this.starData = res.data
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

  .unpublished {
    color: #b2b2b2
  }

  .publish {
  }

</style>
