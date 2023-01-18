import request from './Request'

/**
 * 登录
 * 
 * @param params 参数
 * @returns 
 */
export const login = (params: Record<string, any>) => {
    return request.request<string>({
        url: '/login',
        method: 'POST',
        data: params,
    })
}

/** 获取账户信息 */
export const getUserInfo = <T>() => {
    return request.request<T>({
        url: '/getUserInfo',
        method: 'GET',
    })
}

/** 获取好友列表 */
export const getUserList = () => {
    return request.request<Record<string, any>[]>({
        url: '/getUserList',
        method: 'GET',
    })
}