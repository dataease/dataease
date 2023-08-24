import pyjs from 'js-pinyin'

export function zh2py(str) {
    return pyjs.getFullChars(str).toLowerCase().charCodeAt()
}

export function pySort(arr = []) {
    arr.sort((a, b) => {
        return zh2py(a.name[0]) - zh2py(b.name[0])
    })
    return arr
}