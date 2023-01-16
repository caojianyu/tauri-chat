import request from './Request'

/**
 * 登录
 * 
 * @param params 参数
 * @returns 
 */
export const login = (params: Record<string, any>) => {
    return request.request({
        url: '/login',
        method: 'POST',
        data: params,
    })
}

/** 获取账户信息 */
export const getUserInfo = () => {
    return request.request({
        url: '/getUserInfo',
        method: 'GET',
    })
}

/** 获取好友列表 */
export const getUserList = () => {
    return request.request({
        url: '/getUserList',
        method: 'GET',
    })
}