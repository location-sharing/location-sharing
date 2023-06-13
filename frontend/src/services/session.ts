import { objectToQueryString } from "../util/util"

const sessionHttpUrl = "http://localhost:8080"
const sessionWsUrl = "ws://localhost:8080"

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
