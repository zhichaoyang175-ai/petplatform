import request from './request'

// 审核中心（审核员 / 管理员）
export function listReviews(params = {}) {
  return request.get('/reviews', { params })
}

export function getReviewDetail(id) {
  return request.get(`/reviews/${id}`)
}

export function approveReview(id, comment) {
  return request.put(`/reviews/${id}/approve`, comment ? { comment } : {})
}

export function rejectReview(id, comment) {
  return request.put(`/reviews/${id}/reject`, { comment })
}
