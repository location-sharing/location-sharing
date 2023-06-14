"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getSessionTestWebSocket = exports.objectToQueryString = void 0;
const ws_1 = require("ws");
function objectToQueryString(obj) {
    const keyValuePairs = Array();
    Object.keys(obj).forEach(key => {
        keyValuePairs.push(`${encodeURIComponent(key)}=${encodeURIComponent(obj[key])}`);
    });
    return keyValuePairs.join('&');
}
exports.objectToQueryString = objectToQueryString;
const sessionWsUrl = "ws://localhost:8080";
const getSessionTestWebSocket = (groupId, userId, username) => new ws_1.WebSocket(`${sessionWsUrl}/test?` + objectToQueryString({
    userId,
    username,
    groupId,
}));
exports.getSessionTestWebSocket = getSessionTestWebSocket;
