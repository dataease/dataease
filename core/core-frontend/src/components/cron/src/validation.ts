export const hasInvalidWeekdayW = (value = '') => {
  // WED is a valid weekday name; W as a modifier belongs to day-of-month only.
  return /w/i.test(value.replace(/\bWED\b/gi, ''))
}

export const hasInvalidCronWeekdayW = (value = '') => {
  return hasInvalidWeekdayW(value.trim().split(/\s+/)[5])
}
