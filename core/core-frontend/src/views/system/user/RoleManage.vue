<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { Icon } from '@/components/icon-custom'
const nickName = ref('')
interface Tree {
  label: string
  children?: Tree[]
}

const handleNodeClick = (data: Tree) => {
  console.log(data)
}

const checkList = ref(['test0'])

const state = reactive({
  addedUserList: []
})
state.addedUserList = Array(40)
  .fill(1)
  .map((_, index) => ({
    username: 'test' + index,
    nickName: index + 'nickName'
  }))

const data: Tree[] = [
  {
    label: 'Level one 1',
    children: [
      {
        label: 'Level two 1-1',
        children: [
          {
            label: 'Level three 1-1-1'
          }
        ]
      }
    ]
  },
  {
    label: 'Level one 2',
    children: [
      {
        label: 'Level two 2-1',
        children: [
          {
            label: 'Level three 2-1-1'
          }
        ]
      },
      {
        label: 'Level two 2-2',
        children: [
          {
            label: 'Level three 2-2-1'
          }
        ]
      }
    ]
  },
  {
    label: 'Level one 3',
    children: [
      {
        label: 'Level two 3-1',
        children: [
          {
            label: 'Level three 3-1-1'
          }
        ]
      },
      {
        label: 'Level two 3-2',
        children: [
          {
            label: 'Level three 3-2-1'
          },
          {
            label: 'Level three 3-2-2'
          },
          {
            label: 'Level three 3-2-3'
          },
          {
            label: 'Level three 3-2-4'
          },
          {
            label: 'Level three 3-2-5'
          }
        ]
      }
    ]
  }
]

const defaultProps = {
  children: 'children',
  label: 'label'
}
</script>

<template>
  <div class="role-manage">
    <div class="role-list role-height">
      <div class="title">
        角色列表
        <el-button type="primary">
          <template #icon> <Icon name="icon_add_outlined"></Icon> </template>添加角色
        </el-button>
        <el-input class="m24 w100" v-model="nickName" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>

      <el-tree menu :data="data" :props="defaultProps" @node-click="handleNodeClick" />
    </div>
    <div class="added-user-list role-height">
      <div class="title">
        已添加用户
        <el-input v-model="nickName" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <div :key="ele.username" v-for="ele in state.addedUserList" class="user-list-item">
        {{ ele.username }}
      </div>
    </div>
    <div class="add-user-list role-height">
      <div class="title">
        角色列表
        <el-icon>
          <Icon name="icon_add_outlined"></Icon>
        </el-icon>
        <el-input class="m24 w100" v-model="nickName" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>

      <el-checkbox-group v-model="checkList">
        <div :key="ele.username" v-for="ele in state.addedUserList" class="user-list-item">
          <el-checkbox :label="ele.username" />
        </div>
      </el-checkbox-group>
    </div>
  </div>
</template>

<style lang="less" scoped>
.role-manage {
  display: flex;
  width: 100%;
  height: 100%;

  .role-height {
    height: calc(100vh - 170px);
    overflow: auto;
    position: relative;
  }

  .role-list {
    width: 269px;
    padding: 24px;
  }

  .title {
    display: flex;
    justify-content: space-between;
    font-family: PingFang SC;
    font-size: 20px;
    font-weight: 500;
    color: var(--TextPrimary, #1f2329);
    box-sizing: border-box;
    flex-wrap: wrap;
    position: sticky;
    top: 0;
    left: 24px;
    z-index: 5;
    background: white;
    &::before {
      content: '';
      width: 100%;
      height: 24px;
      top: -24px;
      position: absolute;
      z-index: 5;
      left: 0;
      background: white;
    }
  }

  .m24 {
    margin: 24px 0;
  }
  .w100 {
    width: 100%;
  }

  .added-user-list {
    flex: 1;
    border-right: 2px solid var(--MainBG, #f5f6f7);
    border-left: 2px solid var(--MainBG, #f5f6f7);

    .el-input {
      width: 120px;
      height: 32px;
    }
    .title {
      padding: 24px 24px 0 24px;
      width: 100%;
      height: 56px;
      &::before {
        display: none;
      }
    }

    .user-list-item {
      float: left;
      width: 150px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 24px 0 0 24px;
      border: 1px solid #ccc;
    }
  }

  .add-user-list {
    width: 269px;
    padding: 24px;

    .user-list-item {
      width: 100%;
      height: 30px;
      margin-bottom: 24px;
      padding-left: 24px;
      border: 1px solid #ccc;
    }
  }
}
</style>
