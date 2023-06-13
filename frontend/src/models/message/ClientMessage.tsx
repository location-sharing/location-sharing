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
  location: LatLngExpression
}

export interface TextMessage extends GenericMessage {
  text: string
}