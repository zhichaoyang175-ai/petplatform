import request from './request'

/** 发起看宠预约 */
export function bookVisit(data) {
  return request.post('/visits/book', data)
}

/** 我发起的预约列表 */
export function listMyVisits() {
  return request.get('/visits/my')
}

/** 我收到的预约列表 */
export function listReceivedVisits() {
  return request.get('/visits/received')
}

/** 送养人确认预约 */
export function confirmVisit(id) {
  return request.put(`/visits/${id}/confirm`)
}

/** 完成预约 */
export function completeVisit(id) {
  return request.put(`/visits/${id}/complete`)
}

/** 取消预约 */
export function cancelVisit(id) {
  return request.put(`/visits/${id}/cancel`)
}
