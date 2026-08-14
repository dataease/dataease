type DatePickerType = 'year' | 'month' | 'date' | 'datetime'

// 与图表时间字段的展示粒度保持一致，确保条件值可直接参与比较
export function transDateFormat(dateStyle?: string, datePattern?: string): string {
  const split = datePattern?.toLowerCase() === 'date_split' ? '/' : '-'

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
    default:
      return 'YYYY-MM-DD HH:mm:ss'
  }
}

export function transDatePickerType(dateStyle?: string): DatePickerType {
  const typeMap: Record<string, DatePickerType> = {
    y: 'year',
    y_M: 'month',
    y_M_d: 'date',
    y_M_d_H: 'datetime',
    y_M_d_H_m: 'datetime',
    y_M_d_H_m_s: 'datetime'
  }
  return typeMap[dateStyle ?? ''] ?? 'datetime'
}
