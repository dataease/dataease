<script setup>
import { ref } from "vue";
import AssistantConversationPanel from "@/components/assistant/AssistantConversationPanel.vue";

const turns = ref([
  { questionId: "one", question: "第一条提问", answer: "第一条回答" },
]);
const input = ref("");
const send = () => {
  turns.value.push({
    questionId: String(turns.value.length + 1),
    question: input.value,
    answer: "第二条回答",
  });
  input.value = "";
};
</script>

<template>
  <AssistantConversationPanel
    title="问答"
    :turns="turns"
    v-model="input"
    @send="send"
  />
</template>
