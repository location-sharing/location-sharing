import { objectToQueryString } from "../util/util"

const sessionHttpUrl = process.env.REACT_APP_API_SESSION_SERVICE_HTTP
const sessionWsUrl = process.env.REACT_APP_API_SESSION_SERVICE_WS

export const fetchActiveGroupUsers = (groupId: string, token: string) => fetch( 
  `${sessionHttpUrl}/active?` + objectToQueryString({ 
    groupId: groupId, 
    auth: token 
  })
)

export const getSessionWebSocket = (groupId: string, token: string) => new WebSocket(
  `${sessionWsUrl}/group?` + objectToQueryString({
    groupId: groupId,
    auth: token
  })
)

export const getSessionTestWebSocket = (groupId: string, userId: string, username: string) => new WebSocket(
  `${sessionWsUrl}/test?` + objectToQueryString({
    userId,
    username,
    groupId,
  })
)
