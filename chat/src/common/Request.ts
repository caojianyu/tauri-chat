/** tauri api */
import { getClient, HttpVerb, Body, ResponseType } from "@tauri-apps/api/http";
import { message } from '@tauri-apps/api/dialog';


/**
 * 请求相关
 * 
 * @author 994Ay
 * @date 2023/1/3 01:14
 * @version 1.0.0
 */
class Request {

    // 服务器基础路径
    baseURL: string;

    constructor(params: { baseURL: string }) {
        this.baseURL = params.baseURL;
    }

    /**
     * 发起请求
     * 
     * @param params 请求相关参数
     * @returns 
     */
    async request(params: { url: string, method: HttpVerb, query?: Record<string, any>, data?: Record<string, any>, headers?: Record<string, any> }) {
        // 基础请求参数
        const url = params.url || '';
        const method = params.method || 'GET';
        const query = params.query || {};
        const data = params.data || {};
        const headers = params.headers || {};

        const token = localStorage.getItem("token");
        if (token) {
            headers.token = token;
        }

        const withBaseURL = url.includes('http://');
        // 请求路径
        const requestURL = withBaseURL ? url : this.baseURL + url;

        const client = await getClient();
        const response = await client.request({
            method,
            url: requestURL,
            query,
            body: Body.json(data),
            headers,
            responseType: ResponseType.JSON
        }) as Record<string, any>;

        const code = response.data.code as number;
        if (code === 0) {
            return response.data;
        } else if (code === 401) {
            // token不能为空，也有可能失效
            // 清除本地token
            localStorage.removeItem('token');
        } else {
            message(response.data.msg || '请求异常', { title: '提示', type: 'error' });
        }
        // TODO 更多错误判断
    }
}

const request = new Request({
    baseURL: import.meta.env.VITE_APP_BASE_URL
})


export default request