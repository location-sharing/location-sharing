import { GeolocationPositionData } from "../../models/message/ClientMessage"

export interface PositionMarker {
  iconDOMId: string,
  marker: L.Marker
}

export interface UserPositionMarkers {
  [key: string]: PositionMarker
}

export interface UserPosition {
  username: string,
  positionMarker: PositionMarker,
  positionEvents: Array<GeolocationPositionData>
}

export interface UserPositions {
  [key: string]: UserPosition
}