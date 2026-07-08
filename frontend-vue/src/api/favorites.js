import request from './request'

/** 收藏宠物 */
export function addFavorite(petId) {
  return request.post('/favorites', { petId })
}

/** 取消收藏 */
export function removeFavorite(petId) {
  return request.delete(`/favorites/${petId}`)
}

/** 我的收藏列表 */
export function getFavorites(params = {}) {
  return request.get('/favorites', { params })
}

/** 检查是否已收藏 */
export function checkFavorited(petId) {
  return request.get(`/favorites/check/${petId}`)
}
