<template>
  <el-input
    v-count="value"
    :placeholder="$t('fu.search_bar.please_input')"
    show-word-limit
    :disabled="disabled"
    :value="value"
    @input="handleChange"
    type="textarea"
    maxlength="200"
  />
</template>
<script>
export default {
  name: "DeTextarea",
  props: {
    disabled: Boolean,
    value: String,
  },
  methods: {
    handleChange(val) {
      this.$emit("input", val);
    },
  },
  directives: {
    count: {
      update: function (el, binding) {
        const lg = binding.value?.length || 0;
        const count = el.querySelector(".el-input__count");
        if (!count) return
        if (!lg) {
          if (count?.classList?.contains("no-zore")) {
            count.classList.remove("no-zore");
          }
          count.innerHTML = "0/200";
          return;
        }
        if (el.querySelector(".no-zore")) {
          count.firstChild.innerHTML = lg;
          return;
        }
        const newCount = document.createElement("span");
        const num = document.createElement("span");
        const total = document.createElement("span");
        num.style.color = "#1F2329";
        total.innerHTML = "/200";
        num.innerHTML = lg;
        if (!newCount) return
        newCount.classList.add("el-input__count", "no-zore");
        newCount.appendChild(num);
        newCount.appendChild(total);
        el.replaceChild(newCount, count);
      },
    },
  },
};
</script>