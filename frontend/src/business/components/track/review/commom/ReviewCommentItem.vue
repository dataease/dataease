<template>
  <div class="main">
    <div class="comment-left">
      <div class="icon-title">
        {{ comment.authorName.substring(0, 1) }}
      </div>
    </div>
    <div class="comment-right">
      <span style="font-size: 14px;color: #909399;font-weight: bold">{{ comment.authorName }}</span>
      <span style="color: #8a8b8d; margin-left: 8px; font-size: 12px">
        {{ comment.createTime | timestampFormatDate }}
      </span>
      <span class="comment-delete">
        <el-link icon="el-icon-edit" style="font-size: 9px;margin-right: 6px;" @click="openEdit" :disabled="readOnly"/>
        <el-link icon="el-icon-close" @click="deleteComment" :disabled="readOnly"/>
      </span>
      <br/>
      <div class="comment-desc" style="font-size: 10px;color: #303133">
        <pre>{{ comment.description }}</pre>
      </div>
    </div>

    <el-dialog
      :title="$t('commons.edit')"
      :visible.sync="visible"
      width="30%"
      :destroy-on-close="true"
      :append-to-body="true"
      :close-on-click-modal="false"
      show-close>
      <el-input
        type="textarea"
        :rows="5"
        v-model="description">
      </el-input>
      <span slot="footer" class="dialog-footer">
        <ms-dialog-footer
          @cancel="visible = false"
          @confirm="editComment"/>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {getCurrentUser} from "@/common/js/utils";

export default {
  name: "ReviewCommentItem",
  components: {MsDialogFooter},
  props: {
    comment: Object,
    readOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      visible: false,
      description: ""
    }
  },
  methods: {
    deleteComment() {
      if (getCurrentUser().id !== this.comment.author) {
        this.$warning(this.$t('test_track.comment.cannot_delete'));
        return;
      }
      this.$parent.result = this.$get("/test/case/comment/delete/" + this.comment.id, () => {
        this.$success(this.$t('commons.delete_success'));
        this.$emit("refresh");
      });
    },
    openEdit() {
      if (getCurrentUser().id !== this.comment.author) {
        this.$warning(this.$t('test_track.comment.cannot_edit'));
        return;
      }
      this.description = this.comment.description;
      this.visible = true;
    },
    editComment() {
      this.$post("/test/case/comment/edit", {id: this.comment.id, description: this.description}, () => {
        this.visible = false;
        this.$success(this.$t('commons.modify_success'));
        this.$emit("refresh");
      });
    }
  }
}
</script>

<style scoped>
.main {
  overflow-y: scroll;
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.comment-left {
  float: left;
  width: 50px;
  height: 100%;
}

.comment-right {
  float: left;
  width: 90%;
  padding: 0;
  line-height: 25px;
}

.icon-title {
  color: #fff;
  width: 30px;
  background-color: #72dc91;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-radius: 30px;
  font-size: 14px;
}


.comment-desc {
  overflow-wrap: break-word;
  word-break: break-all;
  border-bottom: 1px solid #ced3de;
}

pre {
  margin: 0 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  width: 100%;
  line-height: 20px;
}

.comment-delete {
  float: right;
  margin-right: 5px;
  cursor: pointer;
}
</style>
