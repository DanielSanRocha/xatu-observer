const axios = require('axios')

let token = ''
let host = ''

if (process.browser) {
    token = localStorage.getItem('token')

    const getUrl = window.location
    host = getUrl.protocol + '//' + getUrl.host + '/api'
    if(process.env.NODE_ENV === 'development') {
      host = 'http://localhost:8089/api'
    }
}

const client = axios.create({
    baseURL: host,
    headers: { Authorization: token }
})

if (process.browser) {
    if (!token) {
        const getUrl = window.location
        if (!window.location.href.includes('login')) {
            window.location.href = getUrl.protocol + '//' + getUrl.host + '/login'
        }
    } else {
        client.user = axios.get(
            host + '/user',
            { headers: { Authorization: token } }).then((response) => {
                console.log('Response from /user: ', response)
                const getUrl = window.location
                if (window.location.href.includes('login')) {
                    window.location.href = getUrl.protocol + '//' + getUrl.host
                }

                client.token = token
                return response.data
            }).catch((e) => {
                console.warn(e)
                if (!window.location.href.includes('login')) {
                    const getUrl = window.location
                    window.location.href = getUrl.protocol + '//' + getUrl.host + '/login'
                }
            })
    }
}

export default client
