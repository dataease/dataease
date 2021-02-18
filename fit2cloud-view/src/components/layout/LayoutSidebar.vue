<template>
  <aside :class="['sidebar-container', {'is-collapse': isCollapse}]">
    <slot>
      <sidebar/>
    </slot>
  </aside>
</template>

<script>
import {mapGetters} from "vuex";
import Sidebar from "@/components/layout/sidebar";

export default {
  name: "LayoutSidebar",
  components: {Sidebar},
  computed: {
    ...mapGetters([
      'sidebar'
    ]),
    isCollapse() {
      return !this.sidebar.opened
    },
  }
}
</script>
