const attrsMap = { brColor: 'borderColor', wordColor: 'color', innerBgColor: 'backgroundColor' }
const styleAttrs = ['innerBgColor', 'wordColor', 'brColor']
function timeDateRangeWidget (nodeCache, name, value) {
    const classList = ['.el-range-input', '.el-range-separator']
    classList.forEach(ele => {
        let nodeList = nodeCache.querySelectorAll(ele);
        if (!nodeList.length) return;
        nodeList.forEach(ele => {
            ele.style[attrsMap[name]] = value;
        })
    })
}
function textInputWidget (nodeCache, name, value) {
    let groupAppend = nodeCache.querySelector('.el-input-group__append');
    groupAppend.style[attrsMap[name]] = value;
    if (name === 'brColor') {
        groupAppend.style.borderLeft = 'none'
    }
}

function textSelectGridWidget (nodeCache, name, value) {
    if (name === 'innerBgColor') {
        nodeCache.querySelector('.list').style.backgroundColor = value;
    }
    if (name === 'wordColor') {
        let elRadio = nodeCache.querySelectorAll('.el-radio')
        let elCheckbox = nodeCache.querySelectorAll('.el-checkbox')
        if (elRadio.length) {
            elRadio.forEach(ele => {
                ele.style.color = value;
            });
        }
        if (elCheckbox.length) {
            elCheckbox.forEach(ele => {
                ele.style.color = value;
            });
        }
    }
}
export {
    attrsMap,
    styleAttrs,
    timeDateRangeWidget,
    textInputWidget,
    textSelectGridWidget,
}