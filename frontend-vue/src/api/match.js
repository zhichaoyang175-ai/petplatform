import request from './request'

/**
 * 为你推荐 — 智能匹配
 * @param {Object} params { page, size }
 */
export function getRecommendations(params = {}) {
  return request.get('/match/recommendations', { params })
}
