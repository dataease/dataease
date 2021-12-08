export const compareItem = {
  type: 'none', // year-yoy/month-yoy等
  resultData: 'percent', // 对比差sub，百分比percent等
  field: '',
  custom: {
    field: '',
    calcType: '0', // 0-增长值，1-增长率
    timeType: '0', // 0-固定日期，1-日期区间
    currentTime: '',
    compareTime: '',
    currentTimeRange: [],
    compareTimeRange: []
  }
}

export const compareYearList = [
  { name: 'year_mom', value: 'year_mom' }
]

export const compareMonthList = [
  { name: 'month_mom', value: 'month_mom' },
  { name: 'year_yoy', value: 'year_yoy' }
]

export const compareDayList = [
  { name: 'day_mom', value: 'day_mom' },
  { name: 'month_yoy', value: 'month_yoy' },
  { name: 'year_yoy', value: 'year_yoy' }
]
