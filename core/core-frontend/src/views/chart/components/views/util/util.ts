export const reverseColor = colorValue => {
  colorValue = '0x' + colorValue.replace(/#/g, '')
  const str = '000000' + (0xffffff - colorValue).toString(16)
  return '#' + str.substring(str.length - 6, str.length)
}
