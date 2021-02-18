<template>
  <layout>
    <template v-slot:header>
      <horizontal-header/>
    </template>
  </layout>
</template>

<script>

import Layout from "@/components/layout";
import HorizontalHeader from "@/business/app-layout/horizontal-layout/HorizontalHeader";

export default {
  name: "HorizontalLayout",
  components: {HorizontalHeader, Layout},
}
</script>
