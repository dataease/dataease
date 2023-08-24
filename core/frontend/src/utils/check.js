export function checkRepeat(arrayData, key) {
  for (let i = 0; i < arrayData.length; i++) {
    for (let j = i + 1; j < arrayData.length; j++) {
      if (arrayData[i][key] === arrayData[j][key]) {
        return true
      }
    }
  }
  return false
}

