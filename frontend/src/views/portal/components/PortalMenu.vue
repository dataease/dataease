<template>
  <div v-if="_checkArrayHasValue(subTreeDatas)">
    <template v-for="item in subTreeDatas">
      <template v-if="_checkArrayHasValue(item.children)">
        <!-- 一级 -->
        <el-submenu :key="item.id" :index="item.id">
          <template slot="title">
            <i class="el-icon-menu" v-if="item.showMenuIcon"></i>
            <span slot="title">{{ item.label + "a" }}</span>
          </template>
          <template v-if="_checkArrayHasValue(item.children)">
            <template v-for="sub in item.children">
              <template v-if="_checkArrayHasValue(sub.children)">
                <!-- 二级 -->
                <el-submenu :key="sub.id" :index="sub.id">
                  <template slot="title">
                    <i class="el-icon-menu" v-if="sub.showMenuIcon"></i>
                    <span slot="title">{{ sub.label + "b" }}</span>
                  </template>
                  <template v-if="_checkArrayHasValue(sub.children)">
                    <template v-for="subItem in sub.children">
                      <!-- 三级 -->
                      <el-menu-item :key="subItem.id" :index="subItem.id">
                        <template slot="title">
                          <i class="el-icon-menu" v-if="subItem.showMenuIcon"></i>
                          <span slot="title">{{ subItem.label + "c" }}</span>
                        </template>
                      </el-menu-item>
                    </template>
                  </template>
                </el-submenu>
              </template>
              <template v-else>
                <el-menu-item :key="sub.id" :index="sub.id">
                  <template slot="title">
                    <i class="el-icon-menu" v-if="sub.showMenuIcon"></i>
                    <span slot="title">{{ sub.label + "b" }}</span>
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
            <i class="el-icon-menu" v-if="item.showMenuIcon"></i>
            <span slot="title">{{ item.label + "aa" }}</span>
          </template>
        </el-menu-item>
      </template>
    </template>
  </div>
</template>

<script>
export default {
  props: {
    subTreeDatas: {
      type: Array,
      default: () => [],
    },
  },

  methods: {
    _checkArrayHasValue(arr) {
      return arr && arr.length;
    },
  },
};
</script>
