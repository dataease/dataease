<template>
  <span>
    <span class="dataease-query-control-icon">
      <svg viewBox="0 0 17 16" aria-hidden="true">
        <path
          fill="currentColor"
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M3.32182 2.60967C2.98161 2.60967 2.79671 3.0074 3.01601 3.2675L6.85819 7.8246C6.94943 7.93282 6.99947 8.06981 6.99947 8.21136V12.7338C6.99947 12.898 7.0998 13.0455 7.2525 13.1058L8.73833 13.6928C9.00085 13.7965 9.28531 13.6031 9.28531 13.3208V8.21136C9.28531 8.06981 9.33535 7.93282 9.42659 7.8246L13.2688 3.2675C13.4881 3.0074 13.3032 2.60967 12.963 2.60967H3.32182ZM2.09858 4.04101C1.22139 3.0006 1.96097 1.40967 3.32182 1.40967H12.963C14.3238 1.40967 15.0634 3.0006 14.1862 4.04101L10.4853 8.43054V13.3208C10.4853 14.4498 9.34747 15.2237 8.29742 14.8089L6.81158 14.2219C6.20078 13.9806 5.79947 13.3905 5.79947 12.7338V8.43054L2.09858 4.04101Z"
        />
      </svg>
      <span>查询控件</span>
    </span>
  </span>
</template>

<style scoped lang="less">
.dataease-query-control-icon {
  width: 48px;
  height: 38px;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  color: inherit;
  font-family: var(--de-custom_font, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif);
  letter-spacing: 0;

  svg {
    width: 17px;
    height: 16px;
    flex-shrink: 0;
  }

  span {
    height: 20px;
    margin-top: 2px;
    font-size: 12px;
    line-height: 20px;
    white-space: nowrap;
  }
}
</style>
