<template>
  <el-menu
    :default-active="currentTreeNode.id"
    :mode="mode"
    menu-trigger="click"
    :background-color="themeColor"
    active-text-color="#409eff"
    @select="handleMenuSelect"
  >
    <template v-if="_checkArrayHasValue(subTreeDatas)">
      <template v-for="item in subTreeDatas">
        <template v-if="_checkArrayHasValue(item.children)">
          <!-- 一级 -->
          <el-submenu
            :key="item.id"
            :index="item.id"
            :popper-append-to-body="false"
          >
            <template slot="title">
              <i :class="[item.iconName]" />
              <span slot="title">{{ item.label }}</span>
            </template>
            <template v-if="_checkArrayHasValue(item.children)">
              <template v-for="sub in item.children">
                <template v-if="_checkArrayHasValue(sub.children)">
                  <!-- 二级 -->
                  <el-submenu
                    :key="sub.id"
                    :index="sub.id"
                    :popper-append-to-body="false"
                  >
                    <template slot="title">
                      <i :class="[sub.iconName]" />
                      <span slot="title">{{ sub.label }}</span>
                    </template>
                    <template v-if="_checkArrayHasValue(sub.children)">
                      <template v-for="subItem in sub.children">
                        <!-- 三级 -->
                        <el-menu-item :key="subItem.id" :index="subItem.id">
                          <template slot="title">
                            <i :class="[subItem.iconName]" />
                            <span slot="title">{{ subItem.label }}</span>
                          </template>
                        </el-menu-item>
                      </template>
                    </template>
                  </el-submenu>
                </template>
                <template v-else>
                  <el-menu-item :key="sub.id" :index="sub.id">
                    <template slot="title">
                      <i :class="[sub.iconName]" />
                      <span slot="title">{{ sub.label }}</span>
                    </template>
                  </el-menu-item>
                </template>
              </template>
            </template>
          </el-submenu>
        </template>
        <template v-else>
          <!-- 二级 -->
          <el-menu-item :key="item.id" :index="item.id">
            <template slot="title">
              <i :class="[item.iconName]" />
              <span slot="title">{{ item.label }}</span>
            </template>
          </el-menu-item>
        </template>
      </template>
    </template>
  </el-menu>
</template>

<script>
export default {
  props: {
    subTreeDatas: {
      type: Array,
      default: () => []
    },
    currentTreeNode: {
      type: Object,
      default: () => {}
    },
    themeColor: {
      type: String,
      default: ''
    },
    mode: {
      type: String,
      default: 'horizontal'
    }
  },

  methods: {
    _checkArrayHasValue(arr) {
      return arr && arr.length
    },

    handleMenuSelect(evt) {
      this.$emit('handleMenuSelect', evt)
    }
  }
}
</script>
