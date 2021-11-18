<template>
  <div>
    <div class="union-container">
      <div class="current-node ds-node">
        <svg-icon icon-class="ds-db" class="ds-icon-db" />
        <span class="node-name">123</span>
        <span class="node-menu">
          <el-dropdown trigger="click" size="small">
            <span class="el-dropdown-link">
              <el-button icon="el-icon-more" type="text" size="small" />
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item icon="el-icon-edit-outline">
                <span style="font-size: 12px;">edit</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </span>
      </div>
      <div>
        <div class="children-node node-container" :style="{height:'40px'}">
          <div class="node-line">
            <svg-icon icon-class="inner-join" class="join-icon" />
            <svg class="join-svg-container">
              <path fill="none" stroke="#dcdfe6" :d="pathExt" />
            </svg>
          </div>
          <div class="ds-node">
            <svg-icon icon-class="ds-db" class="ds-icon-db" />
            <span class="node-name">123</span>
            <span class="node-menu">
              <el-dropdown trigger="click" size="small">
                <span class="el-dropdown-link">
                  <el-button icon="el-icon-more" type="text" size="small" />
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item icon="el-icon-edit-outline">
                    <span style="font-size: 12px;">edit</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>
          </div>
        </div>
        <div class="children-node node-container" :style="{height:'120px'}">
          <div class="node-line">
            <svg-icon icon-class="right-join" class="join-icon" />
            <svg class="join-svg-container">
              <path fill="none" stroke="#dcdfe6" :d="pathMoreExt+'l0,80'" />
            </svg>
          </div>
          <div class="ds-node">
            <svg-icon icon-class="ds-db" class="ds-icon-db" />
            <span class="node-name">123</span>
            <span class="node-menu">
              <el-dropdown trigger="click" size="small">
                <span class="el-dropdown-link">
                  <el-button icon="el-icon-more" type="text" size="small" />
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item icon="el-icon-edit-outline">
                    <span style="font-size: 12px;">edit</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>
          </div>

          <div>
            <div class="children-node node-container" :style="{height:'40px'}">
              <div class="node-line">
                <svg-icon icon-class="left-join" class="join-icon" />
                <svg class="join-svg-container">
                  <path fill="none" stroke="#dcdfe6" :d="pathExt" />
                </svg>
              </div>
              <div class="ds-node">
                <svg-icon icon-class="ds-db" class="ds-icon-db" />
                <span class="node-name">123</span>
                <span class="node-menu">
                  <el-dropdown trigger="click" size="small">
                    <span class="el-dropdown-link">
                      <el-button icon="el-icon-more" type="text" size="small" />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item icon="el-icon-edit-outline">
                        <span style="font-size: 12px;">edit</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </div>
            </div>
            <div class="children-node node-container" :style="{height:'40px'}">
              <div class="node-line">
                <svg-icon icon-class="left-join" class="join-icon" />
                <svg class="join-svg-container">
                  <path fill="none" stroke="#dcdfe6" :d="pathMoreExt" />
                </svg>
              </div>
              <div class="ds-node">
                <svg-icon icon-class="ds-db" class="ds-icon-db" />
                <span class="node-name">123</span>
                <span class="node-menu">
                  <el-dropdown trigger="click" size="small">
                    <span class="el-dropdown-link">
                      <el-button icon="el-icon-more" type="text" size="small" />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item icon="el-icon-edit-outline">
                        <span style="font-size: 12px;">edit</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </div>
            </div>
            <div class="children-node node-container" :style="{height:'40px'}">
              <div class="node-line">
                <svg-icon icon-class="left-join" class="join-icon" />
                <svg class="join-svg-container">
                  <path fill="none" stroke="#dcdfe6" :d="pathMore" />
                </svg>
              </div>
              <div class="ds-node">
                <svg-icon icon-class="ds-db" class="ds-icon-db" />
                <span class="node-name">123</span>
                <span class="node-menu">
                  <el-dropdown trigger="click" size="small">
                    <span class="el-dropdown-link">
                      <el-button icon="el-icon-more" type="text" size="small" />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item icon="el-icon-edit-outline">
                        <span style="font-size: 12px;">edit</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </div>
            </div>
          </div>
        </div>
        <div class="children-node node-container" :style="{height:'40px'}">
          <div class="node-line">
            <svg-icon icon-class="inner-join" class="join-icon" />
            <svg class="join-svg-container">
              <path fill="none" stroke="#dcdfe6" :d="pathMore" />
            </svg>
          </div>
          <div class="ds-node">
            <svg-icon icon-class="ds-db" class="ds-icon-db" />
            <span class="node-name">123</span>
            <span class="node-menu">
              <el-dropdown trigger="click" size="small">
                <span class="el-dropdown-link">
                  <el-button icon="el-icon-more" type="text" size="small" />
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item icon="el-icon-edit-outline">
                    <span style="font-size: 12px;">edit</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AddUnion',
  data() {
    return {
      dataset: [{
        currentDs: {},
        childrenDs: [
          {
            currentDs: {},
            childrenDs: [],
            unionToParent: [
              {
                parentField: {},
                currentField: {}
              }
            ]
          }
        ],
        unionToParent: []
      }],
      path: 'm0,13 l28,0 m24,0 l28,0',
      pathExt: 'm0,13 l28,0 m24,0 l28,0 M14,13  l0,27',
      pathMore: 'M14,0 l0,13 l14,0 m24,0 l28,0',
      pathMoreExt: 'M14,0 l0,13 l14,0 m24,0 l28,0 M14,13  l0,27'
    }
  }
}
</script>

<style scoped>
.union-container{
  display: flex;
  padding: 10px;
  width:100%;
  height:400px;
  overflow: auto;
}
.ds-node{
  width:140px;
  height: 26px;
  line-height: 26px;
  border: #dcdfe6 solid 1px;
  min-width: 140px;
  color: var(--TextPrimary,#606266);
  font-size: 14px;
  display: flex;
  align-items: center;
  padding: 0 6px;
}
.node-container{
  display: flex;
  position: relative;
}
.join-icon{
  height: 26px;
  font-size: 24px;
  line-height: 26px;
  position: absolute;
  left: 28px;
  color:#dcdfe6;
}
.join-svg-container{
  width:80px;
}
.node-name{
  flex: 1;
  text-overflow: ellipsis;
  white-space: pre;
  overflow: hidden;
}
.ds-node .node-menu{
  visibility: hidden;
}
.ds-node:hover .node-menu{
  visibility: visible;
}
.ds-node:hover{
  cursor: pointer;
  border: var(--Main,#2681ff) solid 1px;
}
.node-line{
  display: flex;
  position: relative;
}
.join-icon:hover{
  cursor: pointer;
  color: var(--Main,#2681ff);
}
</style>
