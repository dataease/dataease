<template>
  <div>
    <el-dropdown
      trigger="click">
      <input
        id="input"
        ref="trackButton"
        type="button"
        hidden
      >
      <el-dropdown-menu
        class="track-menu"
        :append-to-body="false"
      >
        <el-dropdown-item
          v-for="(item, key) in trackMenu"
          :key="key"
          @click.native="trackMenuClick(item)"
        ><span class="menu-item">{{ i18n_map[item] }}</span></el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>

export default {
  props: {
    trackMenu: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      i18n_map: {
        drill: this.$t('panel.drill'),
        linkage: this.$t('panel.linkage'),
        jump: this.$t('panel.jump')
      }
    }
  },
  computed: {
  },
  methods: {
    trackButtonClick() {
      const _this = this
      setTimeout(() => {
        _this.$refs.trackButton.click()
      }, 50)
    },
    trackMenuClick(menu) {
      this.$emit('trackClick', menu)
    }
  }
}
</script>

<style lang="scss" scoped>
  .menu-item {
    font-size: 12px;
  }

  ::v-deep ul {
    width: 80px;
  }

  ::v-deep.el-popper[x-placement^=bottom] .popper__arrow {
    display: none;
  }

  ::v-deep.el-popper[x-placement^=bottom] {
    margin-top: -80px !important;
  }

  .track-menu {
    border: #3a8ee6 1px solid;
  }

</style>
