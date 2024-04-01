<script lang="ts" setup>
import { useUserStoreWithOut } from '@/store/modules/user'
import userImg from '@/assets/img/user.png'
import { mountedOrg, switchOrg } from '@/api/user'
import { ref, onMounted, computed } from 'vue'
import OrgCell from '@/views/mobile/components/OrgCell.vue'
import { useRouter } from 'vue-router'
import { logoutApi } from '@/api/login'
import { logoutHandler } from '@/utils/logout'
import VanNavBar from 'vant/es/nav-bar'
import VanImage from 'vant/es/image'
import 'vant/es/image/style'
import 'vant/es/nav-bar/style'

interface OrgTreeNode {
  id: string | number
  name: string
  readOnly: boolean
  children?: OrgTreeNode[]
}
const userStore = useUserStoreWithOut()
const { push } = useRouter()
const navBarTitle = ref('组织')
const name = ref('')
const showNavBar = ref(true)
const logout = async () => {
  await logoutApi()
  logoutHandler()
  push('/login')
}

const orgClick = () => {
  showNavBar.value = false
}
const activeDirectName = ref('')

let orgOption = null
const findName = () => {
  const stack = [...orgOption]
  while (stack.length) {
    const item = stack.pop()
    if (item?.id === userStore.getOid) {
      name.value = item?.name
      break
    }
    if (item?.children?.length) {
      item.children.forEach(kid => stack.push(kid))
    }
  }
}

let directIdCopy = []
let directNameCopy = []

const dfsOrgTree = (arr, depth) => {
  arr.forEach(item => {
    const { name, id } = item
    if (depth <= directIdCopy.length) {
      if (depth < directIdCopy.length) {
        directIdCopy = directIdCopy.slice(0, depth)
        directNameCopy = directNameCopy.slice(0, depth)
      }
      directIdCopy.splice(directIdCopy.length - 1, 1, id)
      directNameCopy.splice(directNameCopy.length - 1, 1, name)
    } else {
      directIdCopy.push(id)
      directNameCopy.push(name)
    }

    let nextDepth = depth + 1

    if (id === userStore.getOid) {
      directName.value = [...directNameCopy]
      directId.value = [...directIdCopy]
      nextDepth = 999
    }
    if (item?.children?.length && nextDepth !== 999) {
      dfsOrgTree(item?.children, nextDepth)
    }
  })
}

onMounted(() => {
  mountedOrg().then(res => {
    orgOption = res.data as OrgTreeNode[]
    tableData.value = res.data as OrgTreeNode[]
    findName()
    dfsOrgTree(orgOption, 1)
    directName.value.pop()
    directId.value.pop()
    activeDirectName.value = directName.value[directName.value.length - 1]
  })
})

const switchHandler = (id: number | string) => {
  switchOrg(id).then(res => {
    const token = res.data.token
    userStore.setToken(token)
    userStore.setExp(res.data.exp)
    window.location.reload()
  })
}

const onClickLeft = () => {
  directName.value.pop()
  activeDirectName.value = directName.value[directName.value.length - 1]
  directId.value.pop()
  if (!directName.value.length) {
    showNavBar.value = true
  }
}

const clearOrg = () => {
  if (!directName.value.length) return
  directName.value = []
  activeDirectName.value = ''
  directId.value = []
}

const orgCellClick = (type, val) => {
  if (type !== 'right') {
    switchHandler(val.id)
  } else {
    directName.value.push(val.name)
    activeDirectName.value = val.name
    directId.value.push(val.id)
  }
}

const handleDir = index => {
  if (index === directId.value.length - 1) return
  directId.value = directId.value.slice(0, index + 1)
  directName.value = directName.value.slice(0, index + 1)
  activeDirectName.value = directName.value[directName.value.length - 1]
}

const tableData = ref([])
const directName = ref([])
const directId = ref([])

const dfsTree = (ids, arr) => {
  const id = ids.shift()
  return arr.reduce((pre, ele) => {
    if (id && ele.id === id) {
      if (!ids.length) {
        return ele.children || []
      }
      const children = dfsTree([...ids], ele.children || [])
      pre = children || []
    }
    return pre
  }, [])
}

const activeTableData = computed(() => {
  return directId.value.length ? dfsTree([...directId.value], tableData.value) : tableData.value
})
</script>

<template>
  <div class="de-mobile-user">
    <template v-if="showNavBar">
      <div class="logout flex-center">我的</div>
      <div class="mobile-user-top">
        <van-image round width="48" height="48" :src="userImg" />
        <div class="user-name">
          {{ userStore.name }}
        </div>
      </div>
      <OrgCell
        @click="orgClick"
        label="切换组织"
        prefix-icon="icon_switch_outlined"
        :tips="name"
        nextlevel
      ></OrgCell>
      <div class="logout flex-center danger" @click="logout">注销</div>
    </template>
    <template v-else>
      <van-nav-bar
        safe-area-inset-top
        :title="activeDirectName || navBarTitle"
        left-arrow
        @click-left="onClickLeft"
      />
      <div class="grey">
        <div @click="clearOrg" class="flex-align-center">
          <span class="ellipsis" :class="!!directName.length && 'active'">组织</span>
          <el-icon v-if="!!directName.length">
            <Icon name="icon_right_outlined"></Icon>
          </el-icon>
        </div>
        <div
          @click="handleDir(index)"
          class="flex-align-center"
          v-for="(ele, index) in directName"
          :key="ele"
        >
          <span class="ellipsis" :class="ele !== activeDirectName && 'active'">{{ ele }}</span>
          <el-icon v-if="directName.length > 1 && index !== directName.length - 1">
            <Icon name="icon_right_outlined"></Icon>
          </el-icon>
        </div>
      </div>
      <OrgCell
        @click="type => orgCellClick(type, ele)"
        v-for="ele in activeTableData"
        :key="ele.id"
        :label="ele.name"
        :nextlevel="ele.children"
        :active="name === ele.name"
      ></OrgCell>
    </template>
  </div>
</template>

<style lang="less" scoped>
.de-mobile-user {
  height: 100vh;
  width: 100vw;
  background: #f5f6f7;

  .mobile-user-top {
    padding: 16px;
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    height: 80px;
    background: #fff;

    .user-name {
      font-size: 17px;
      font-weight: 500;
      line-height: 28px;
      margin-left: 12px;
    }
  }

  .logout {
    margin-top: 8px;
    height: 48px;
    font-size: 17px;
    font-weight: 500;
    line-height: 24px;
    background: #fff;
    &.danger {
      color: var(--ed-color-danger);
      font-size: 16px;
      font-weight: 400;
      line-height: 22px;
    }
  }

  .grey {
    height: 44px;
    padding: 16px;
    width: 100%;
    color: #646a73;
    font-size: 14px;
    font-weight: 400;
    line-height: 20px;
    display: flex;
    align-items: center;

    & > div {
      white-space: nowrap;
    }

    .ellipsis {
      max-width: 250px;
    }

    .active {
      color: var(--ed-color-primary);
    }

    .ed-icon {
      font-size: 12px;
      margin: 0 4px;
      color: #8f959e;
    }
  }
}
</style>
