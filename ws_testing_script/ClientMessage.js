"use strict";
exports.__esModule = true;
function modelFromGeolocationPosition(data) {
    return {
        coords: {
            accuracy: data.coords.accuracy,
            altitude: data.coords.altitude,
            altitudeAccuracy: data.coords.altitudeAccuracy,
            heading: data.coords.heading,
            latitude: data.coords.latitude,
            longitude: data.coords.longitude,
            speed: data.coords.speed
        },
        timestamp: data.timestamp
    };
}
exports.modelFromGeolocationPosition = modelFromGeolocationPosition;
