import { WebSocket } from "ws";

export function objectToQueryString(obj: {[key: string]: string}): string {
  const keyValuePairs = Array<String>();
  Object.keys(obj).forEach(key => {
    keyValuePairs.push(`${encodeURIComponent(key)}=${encodeURIComponent(obj[key])}`);
  })  
  return keyValuePairs.join('&');
}

const sessionWsUrl = "ws://localhost:8080"

export const getSessionTestWebSocket = (groupId: string, userId: string, username: string) => new WebSocket(
  `${sessionWsUrl}/test?` + objectToQueryString({
    userId,
    username,
    groupId,
  })
)