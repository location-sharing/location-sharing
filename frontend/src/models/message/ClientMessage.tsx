import { LatLngExpression } from "leaflet"

// this format is needed by the server
export interface ClientMessage {
  payload: string,
}

// embeddded message in the 'payload' above (arbitrary format agreed upon by the client)
export type GenericMessageType = "LOCATION" | "MESSAGE"

export interface GenericMessage {
  type: GenericMessageType,
} 

export interface LocationMessage extends GenericMessage {
  type: "LOCATION"
  position: GeolocationPositionData
}

export interface GeolocationPositionData {
  coords: {
    accuracy: number,
    altitude: number | null,
    altitudeAccuracy: number | null,
    heading: number | null,
    latitude: number,
    longitude: number,
    speed: number | null,
  }
  timestamp: EpochTimeStamp,
}

export function modelFromGeolocationPosition(data: GeolocationPosition): GeolocationPositionData {
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
  }
}

export interface TextMessage extends GenericMessage {
  type: "MESSAGE"
  text: string
}