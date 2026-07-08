import request from './request'

/** 提交评价 */
export function createEvaluation(data) {
  return request.post('/evaluations', data)
}

/** 按目标分页查询评价列表 ?targetType=&targetId=&page=&size= */
export function getEvaluations(params = {}) {
  return request.get('/evaluations', { params })
}

/** 查询某目标信用分 ?targetType=&targetId= */
export function getCredit(params = {}) {
  return request.get('/evaluations/credit', { params })
}

/** 查询当前用户作为目标的信用分 */
export function getMyCredit() {
  return request.get('/evaluations/my-credit')
}
