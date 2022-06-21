<template>
  <div>
    <el-col>
      <el-row>
        <el-col :span="8" style="text-overflow:ellipsis;overflow: hidden;white-space: nowrap;font-size: 14px">
          <span>{{ $t('panel.who_share') + ': ' + $store.getters.user.nickName }}</span>
        </el-col>

        <el-col :span="8" style="text-overflow:ellipsis;overflow: hidden;white-space: nowrap;font-size: 14px">
          <span>{{ $t('panel.when_share') + ': ' + (granterTime ? new Date(granterTime).format('yyyy-MM-dd hh:mm:ss') : '') }}</span>
        </el-col>

        <el-col :span="8" style="text-overflow:ellipsis;overflow: hidden;white-space: nowrap;font-size: 14px">
          <!-- 分享维度提醒 先注释 因为后面有可能还是需要的 -->
          <!-- <el-link type="primary" disabled class="share-checked">
            {{ $t('panel.org') }}
          </el-link>
          <el-link type="success" disabled class="share-checked">
            {{ $t('panel.role') }}
          </el-link>
          <el-link type="info" disabled class="share-checked">
            {{ $t('panel.user') }}
          </el-link> -->
        </el-col>
      </el-row>
      <el-row style="display: flex;">
        <el-col style="text-overflow:ellipsis;overflow: hidden;white-space: nowrap;font-size: 14px; width: 90px;">
          <span>{{ $t('panel.share_to') }}</span>
        </el-col>
        <el-col>
          <el-tag
            v-for="tag in dynamicTags"
            :key="tag.targetName"
            :type="tag.tagType"
            size="small"
            class="selected-tag"
            closable
            :disable-transitions="false"
            @close="handleClose(tag)"
          >
            {{ tag.targetName }}
          </el-tag>

          <el-tooltip class="item" effect="dark" :content="$t('commons.edit')" placement="bottom">
            <el-button type="primary" circle icon="el-icon-edit" class="button-new-tag" size="mini" @click="showEditPage" />
          </el-tooltip>
        </el-col>
      </el-row>

      <el-row />
    </el-col>

    <el-dialog
      v-dialogDrag
      :title="authTitle"
      :visible.sync="authVisible"
      width="800px"
      class="dialog-css"
    >
      <grant-auth v-if="authVisible" :resource-id="authResourceId" @close-grant="closeGrant" />
    </el-dialog>
  </div>

</template>

<script>
import GrantAuth from '../index'
import { shareTargets, removeShares } from '@/api/panel/share'
import bus from '@/utils/bus'
export default {
  name: 'ShareHead',
  components: { GrantAuth },
  data() {
    return {
      granter: '管理员',
      granterTime: '2021年11月9日',
      dynamicTags: [],

      authVisible: false,
      authTitle: '',
      authResourceId: null,
      targetDatas: [],
      tagTypes: { 0: 'info', 1: 'success', 2: 'primary' },
      checked: false
    }
  },
  computed: {
    panelInfo() {
      // this.initTagDatas()
      return this.$store.state.panel.panelInfo
    }
  },

  watch: {
    panelInfo(newVal, oldVla) {
      // 刷新 进行重新渲染

      this.$nextTick(() => {
        this.initTagDatas()
      })
    }
  },

  created() {
    this.initTagDatas()
  },
  methods: {
    handleClose(tag) {
      // this.dynamicTags.splice(this.dynamicTags.indexOf(tag), 1)
      if (!tag || !tag.shareId) return
      const param = {
        panelId: this.panelInfo.id,
        shareId: tag.shareId,
        targetId: tag.targetId,
        type: tag.type
      }
      removeShares(param).then(res => {
        this.initTagDatas()
      })
    },
    showEditPage() {
      this.authResourceId = this.panelInfo.id
      this.authTitle = '把[' + this.panelInfo.name + ']分享给'
      this.authVisible = true
    },
    closeGrant() {
      this.initTagDatas()
      this.authResourceId = null
      this.authVisible = false
    },
    initTagDatas() {
      shareTargets(this.panelInfo.id).then(res => {
        this.targetDatas = res.data
        this.dynamicTags = res.data.map(item => {
          item.tagType = this.tagTypes[item.type]
          this.granterTime = item.createTime
          return item
        }).sort((a, b) => b.type - a.type)
        if (!this.dynamicTags || this.dynamicTags.length === 0) {
          this.afterRemoveAll()
        }
      })
    },
    afterRemoveAll() {
      bus.$emit('refresh-my-share-out')
    }
  }
}
</script>

<style lang="scss" scoped>
  .button-new-tag {
    margin-left: 10px;

  }
  .share-checked {
    margin-left: 15px;
  }
  .selected-tag {
      margin: 0 2px;
  }

</style>
