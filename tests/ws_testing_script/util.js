"use strict";
exports.__esModule = true;
var ws_1 = require("ws");
function objectToQueryString(obj) {
    var keyValuePairs = Array();
    Object.keys(obj).forEach(function (key) {
        keyValuePairs.push(encodeURIComponent(key) + "=" + encodeURIComponent(obj[key]));
    });
    return keyValuePairs.join('&');
}
exports.objectToQueryString = objectToQueryString;
// ws://localhost/connections
var sessionWsUrl = process.env.API_SESSION_SERVICE_WS;
exports.getSessionTestWebSocket = function (groupId, userId, username) { return new ws_1.WebSocket(sessionWsUrl + "/test?" + objectToQueryString({
    userId: userId,
    username: username,
    groupId: groupId
})); };
