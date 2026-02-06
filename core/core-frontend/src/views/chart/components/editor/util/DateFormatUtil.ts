export function transDateFormat(dateStyle: string, datePattern: string): string {
  const split = datePattern?.toLowerCase() === 'date_split' ? '/' : '-'

  if (!dateStyle) {
    return 'YYYY-MM-DD HH:mm:ss'
  }

  switch (dateStyle) {
    case 'y':
      return 'YYYY'
    case 'y_M':
      return `YYYY${split}MM`
    case 'y_M_d':
      return `YYYY${split}MM${split}DD`
    case 'y_M_d_H':
      return `YYYY${split}MM${split}DD HH`
    case 'y_M_d_H_m':
      return `YYYY${split}MM${split}DD HH:mm`
    case 'y_M_d_H_m_s':
      return `YYYY${split}MM${split}DD HH:mm:ss`
    case 'H_m_s':
      return 'HH:mm:ss'
    default:
      return 'YYYY-MM-DD HH:mm:ss'
  }
}
type DatePickerType =
  | 'year'
  | 'years'
  | 'month'
  | 'months'
  | 'date'
  | 'dates'
  | 'datetime'
  | 'week'
  | 'datetimerange'
  | 'daterange'
  | 'monthrange'
  | 'yearrange'

export function transDatePickerType(dateStyle: string | undefined): DatePickerType {
  if (!dateStyle) {
    return 'datetime'
  }
  const map: Record<string, string> = {
    y: 'year',
    y_M: 'month',
    y_M_d: 'date',
    y_M_d_H: 'datetime',
    y_M_d_H_m: 'datetime',
    y_M_d_H_m_s: 'datetime'
  }
  return (
    <
      | 'year'
      | 'years'
      | 'month'
      | 'months'
      | 'date'
      | 'dates'
      | 'datetime'
      | 'week'
      | 'datetimerange'
      | 'daterange'
      | 'monthrange'
      | 'yearrange'
    >map[dateStyle ?? ''] ?? 'datetime'
  )
}
