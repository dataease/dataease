export const left2RightDrag = {
  inserted(el, binding) {
    el.onmousedown = function (e) {
      const init = e.clientX;
      const parent = el.parentNode;
      const initWidth = parent.offsetWidth;
      document.onmousemove = function (e) {
        const end = e.clientX;
        const newWidth = end - init + initWidth;
        if (newWidth < document.body.clientWidth - 10 && newWidth > 10) {
          parent.style.width = newWidth + "px";
        }
      };
      document.onmouseup = function () {
        document.onmousemove = document.onmouseup = null;
      };
    };
  }
};

export const right2LeftDrag = {
  inserted(el, binding) {
    el.onmousedown = function (e) {
      const init = e.clientX;
      const parent = el.parentNode;
      const initWidth = parent.offsetWidth;
      document.onmousemove = function (e) {
        const end = e.clientX;
        const newWidth = initWidth - (end - init);
        if (newWidth < document.body.clientWidth - 10 && newWidth > 10) {
          parent.style.width = newWidth + "px";
        }
      };
      document.onmouseup = function () {
        document.onmousemove = document.onmouseup = null;
      };
    };
  }
};

export const bottom2TopDrag = {
  inserted(el, binding) {
    el.onmousedown = function (e) {
      const init = e.clientY;
      const parent = el.parentNode;
      const initHeight = parent.offsetHeight;
      document.onmousemove = function (e) {
        const end = e.clientY;
        const newHeight = initHeight - (end - init);
        if (newHeight < document.body.clientHeight - 10 && newHeight > 10) {
          parent.style.height = newHeight + "px";
        }
      };
      document.onmouseup = function () {
        document.onmousemove = document.onmouseup = null;
      };
    };
  }
};
