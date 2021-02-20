<template>
  <div>
    <api-list-container-with-doc
      :is-api-list-enable="isApiListEnable"
      :active-dom="activeDom"
      @activeDomChange="activeDomChange"
      @isApiListEnableChange="isApiListEnableChange">

      <api-document-item :project-id="projectId" :module-ids="moduleIds"/>

    </api-list-container-with-doc>
  </div>

</template>

<script>

import ApiListContainerWithDoc from "@/business/components/api/definition/components/list/ApiListContainerWithDoc";
import ApiDocumentItem from "@/business/components/api/definition/components/document/ApiDocumentItem";

export default {
  name: "ApiDocumentsPage",
  components: {
    ApiListContainerWithDoc,
    ApiDocumentItem,
  },
  data() {
    return {
    }
  },
  props: {
    projectId:String,
    moduleIds:Array,
    activeDom:String,
    isApiListEnable: {
      type: Boolean,
      default: false,
    },
  },
  created: function () {
  },
  watch: {
  },
  computed: {

  },
  methods: {
    isApiListEnableChange(data){
      this.$emit("isApiListEnableChange",data);
    },
    activeDomChange(data){
      this.$emit("activeDomChange",data);
    }
  },
}
</script>

<style scoped>

</style>
