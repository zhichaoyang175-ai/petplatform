import request from './request'

/** 公开动态列表（分页） */
export function listFeeds(params = {}) {
  return request.get('/feeds', { params })
}

/** 动态详情 */
export function getFeed(id) {
  return request.get(`/feeds/${id}`)
}

/** 发布动态（需登录） */
export function createFeed(data) {
  return request.post('/feeds', data)
}

/** 点赞动态 */
export function likeFeed(id) {
  return request.post(`/feeds/${id}/like`)
}

/** 我的动态（需登录） */
export function getMyFeeds(params = {}) {
  return request.get('/feeds/my', { params })
}
