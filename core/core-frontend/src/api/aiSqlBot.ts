import request from '@/config/axios'

export const findDvSqlBotDataset = dvInfo => request.get({ url: '/sqlbot/dataset/' + dvInfo })
