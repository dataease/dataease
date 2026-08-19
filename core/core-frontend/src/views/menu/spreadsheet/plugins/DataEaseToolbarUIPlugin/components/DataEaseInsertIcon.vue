<template>
  <span class="dataease-insert-icon">
    <span class="dataease-insert-icon__top">
      <svg class="dataease-insert-icon__add" viewBox="0 0 16 16" aria-hidden="true">
        <path
          d="M7.99996 15.3334C12.05 15.3334 15.3333 12.0502 15.3333 8.00008C15.3333 3.94999 12.05 0.666748 7.99996 0.666748C3.94987 0.666748 0.666626 3.94999 0.666626 8.00008C0.666626 12.0502 3.94987 15.3334 7.99996 15.3334ZM7.33329 5.00008C7.33329 4.81599 7.48253 4.66675 7.66663 4.66675H8.33329C8.51739 4.66675 8.66663 4.81599 8.66663 5.00008V7.33341H11C11.1841 7.33341 11.3333 7.48265 11.3333 7.66675V8.33341C11.3333 8.51751 11.1841 8.66675 11 8.66675H8.66663V11.0001C8.66663 11.1842 8.51739 11.3334 8.33329 11.3334H7.66663C7.48253 11.3334 7.33329 11.1842 7.33329 11.0001V8.66675H4.99996C4.81586 8.66675 4.66663 8.51751 4.66663 8.33341V7.66675C4.66663 7.48265 4.81586 7.33341 4.99996 7.33341H7.33329V5.00008Z"
          fill="currentColor"
        />
      </svg>
      <svg class="dataease-insert-icon__arrow" viewBox="0 0 8 8" aria-hidden="true">
        <path
          d="M3.99996 5.28796L6.71054 2.57738C6.77562 2.51229 6.88115 2.51229 6.94624 2.57738L7.18194 2.81308C7.24703 2.87817 7.24703 2.9837 7.18194 3.04878L4.23566 5.99506C4.10549 6.12524 3.89443 6.12524 3.76426 5.99506L0.817981 3.04878C0.752893 2.9837 0.752893 2.87817 0.817981 2.81308L1.05368 2.57738C1.11877 2.51229 1.2243 2.51229 1.28939 2.57738L3.99996 5.28796Z"
          fill="currentColor"
        />
      </svg>
    </span>
    <span class="dataease-insert-icon__label">插入</span>
  </span>
</template>

<style scoped lang="less">
.dataease-insert-icon {
  width: 28px;
  height: 38px;
  color: #1f2329;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  font-family: var(--de-custom_font, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif);

  &__top {
    width: 28px;
    height: 16px;
    display: flex;
    align-items: flex-start;
  }

  &__add {
    width: 16px;
    height: 16px;
    color: #3370ff;
    display: block;
    flex-shrink: 0;
  }

  &__arrow {
    width: 8px;
    height: 8px;
    margin: 4px 0 0 4px;
    color: #1f2329;
    display: block;
    flex-shrink: 0;
  }

  &__label {
    width: 28px;
    height: 20px;
    color: #1f2329;
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
    text-align: center;
    white-space: nowrap;
    display: block;
    margin-top: 4px;
  }
}
</style>
