"use strict";
exports.__esModule = true;
var util_1 = require("./util");
var args = process.argv.slice(2);
var groupId = args[0];
var userCount = Number(args[1]);
var interval = Number(args[2]);
if (!groupId || !userCount || !interval) {
    console.log("define all cli arguments");
    console.log("<groupId> <userCount> <interval>");
    process.exit(1);
}
// const groupId = "b3e69d8c-507f-454d-bd1d-adf80100348a"
// const userCount = 1;
// const interval = 1000;
var users = [];
var ready = 0;
var _loop_1 = function (i) {
    var userId = "" + i;
    var username = "" + i;
    var ws = util_1.getSessionTestWebSocket(groupId, userId, username);
    ws.onopen = function () {
        console.log("user " + i + " connected: \t userId: " + userId + " \t  username: " + username);
        // we can do this because of single-threadedness
        ready += 1;
    };
    ws.onerror = function (err) {
        console.log("user " + i + " error: " + err);
    };
    ws.onmessage = function (event) {
        // console.log(`user ${i} got a message`);
    };
    users.push({
        userId: userId, username: username, ws: ws
    });
};
for (var i = 0; i < userCount; ++i) {
    _loop_1(i);
}
// users are connected by this point, start sending messages randomly
function sendMessages() {
    var start_lat = 46.7597345;
    var start_lon = 23.6293937;
    for (var i = 0; i < userCount; ++i) {
        var index = Math.floor(userCount * Math.random());
        var ws = users[index].ws;
        var d_lat = 0.0015;
        var d_lon = 0.0015;
        var random_lat = Math.random() * 2 * d_lat - d_lat + start_lat;
        var random_lon = Math.random() * 2 * d_lon - d_lon + start_lon;
        var randomPosition = {
            coords: {
                accuracy: 3.0,
                altitude: null,
                altitudeAccuracy: null,
                heading: null,
                speed: null,
                latitude: random_lat,
                longitude: random_lon
            },
            timestamp: Date.now()
        };
        var locationMessage = {
            type: "LOCATION",
            position: randomPosition
        };
        var clientMessage = {
            payload: JSON.stringify(locationMessage)
        };
        ws.send(JSON.stringify(clientMessage));
        console.log("user " + index + " sent random position");
    }
}
setInterval(function () {
    if (ready !== userCount) {
        console.log("Connections not ready yet, waiting... \t " + ready + "/" + userCount);
    }
    else {
        console.log("Trying to send " + userCount + " messages");
        sendMessages();
    }
}, interval);
