"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const util_1 = require("./util");
const args = process.argv.slice(2);
const groupId = args[0];
const userCount = Number(args[1]);
const interval = Number(args[2]);
if (!groupId || !userCount || !interval) {
    console.log("define all cli arguments");
    console.log("<groupId> <userCount> <interval>");
    process.exit(1);
}
// const groupId = "b3e69d8c-507f-454d-bd1d-adf80100348a"
// const userCount = 1;
// const interval = 1000;
const users = [];
let ready = 0;
for (let i = 0; i < userCount; ++i) {
    const userId = `${i}`;
    const username = `${i}`;
    const ws = (0, util_1.getSessionTestWebSocket)(groupId, userId, username);
    ws.onopen = () => {
        console.log(`user ${i} connected: \t userId: ${userId} \t  username: ${username}`);
        // we can do this because of single-threadedness
        ready += 1;
    };
    ws.onerror = (err) => {
        console.log(`user ${i} error: ${err}`);
    };
    ws.onmessage = (event) => {
        // console.log(`user ${i} got a message`);
    };
    users.push({
        userId, username, ws
    });
}
// users are connected by this point, start sending messages randomly
function sendMessages() {
    const start_lat = 46.7597345;
    const start_lon = 23.6293937;
    for (let i = 0; i < userCount; ++i) {
        const index = Math.floor(userCount * Math.random());
        const ws = users[index].ws;
        const d_lat = 0.0015;
        const d_lon = 0.0015;
        const random_lat = Math.random() * 2 * d_lat - d_lat + start_lat;
        const random_lon = Math.random() * 2 * d_lon - d_lon + start_lon;
        const randomPosition = {
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
        const locationMessage = {
            type: "LOCATION",
            position: randomPosition
        };
        const clientMessage = {
            payload: JSON.stringify(locationMessage)
        };
        ws.send(JSON.stringify(clientMessage));
        console.log(`user ${index} sent random position`);
    }
}
setInterval(() => {
    if (ready !== userCount) {
        console.log(`Connections not ready yet, waiting... \t ${ready}/${userCount}`);
    }
    else {
        console.log(`Trying to send ${userCount} messages`);
        sendMessages();
    }
}, interval);
