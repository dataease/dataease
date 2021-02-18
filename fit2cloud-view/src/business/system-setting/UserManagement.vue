<template>
  <layout-content>
    <dynamic-table>
      <fu-search-bar quick-placeholder="按 姓名/邮箱 搜索" :components="components" @exec="search"/>
    </dynamic-table>
  </layout-content>
</template>

<script>
import DynamicTable from "@/components/dynamic-table";
import LayoutContent from "@/components/layout/LayoutContent";

export default {
  name: "UserManagement",
  components: {LayoutContent, DynamicTable},
  data() {
    return {
      components: [
        {field: "name", label: "姓名", component: "FuInputComponent", defaultOperator: "eq"},
        {field: "email", label: "Email", component: "FuInputComponent"},
        {
          field: "status",
          label: "状态",
          component: "FuSelectComponent",
          options: [
            {label: "运行中", value: "Running"},
            {label: "成功", value: "Success"},
            {label: "失败", value: "Fail"}
          ],
          multiple: true
        },
        {field: "create_time", label: "创建时间", component: "FuDateTimeComponent"},
      ]
    }
  },
  methods: {
    search(condition) {
      console.log(condition)
    }
  }
}
</script>

<style scoped>

</style>
