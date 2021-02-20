<template>
  <el-card v-loading="result.loading">
    <template slot="header">
      <span style="font-size: 15px; color: #1E90FF">{{ $t('test_track.review.comment') }}</span>
      <i class="el-icon-refresh" @click="getComments()"
         style="margin-left:10px;font-size: 14px; cursor: pointer"/>
    </template>
    <div style="height: 100%;overflow:auto;">
      <review-comment-item v-for="(comment,index) in comments"
                           :key="index"
                           :comment="comment"
                           :read-only="readOnly"
                           @refresh="getComments()"/>
      <div v-if="comments.length === 0" style="text-align: center">
        <i class="el-icon-chat-line-square" style="font-size: 15px;color: #8a8b8d;">
                <span style="font-size: 15px; color: #8a8b8d;">
                  {{ $t('test_track.comment.no_comment') }}
                </span>
        </i>
      </div>
    </div>
  </el-card>
</template>

<script>
import ReviewCommentItem from "@/business/components/track/review/commom/ReviewCommentItem";

export default {
  name: "CaseComment",
  components: {ReviewCommentItem},
  props: {
    caseId: {
      type: String,
      default: ''
    },
    readOnly: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    caseId() {
      this.comments = [];
      this.getComments();
    }
  },
  created() {
    this.comments = [];
    this.getComments();
  },
  data() {
    return {
      comments: [],
      result: {}
    }
  },
  methods: {
    getComments() {
      if (this.caseId) {
        this.result = this.$get('/test/case/comment/list/' + this.caseId, res => {
          this.comments = res.data;
        })
      }
    }
  }
}
</script>

<style scoped>

</style>
