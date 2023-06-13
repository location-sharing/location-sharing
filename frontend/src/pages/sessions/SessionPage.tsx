import { LatLngExpression, LocationEvent } from "leaflet";
import "leaflet/dist/leaflet.css";
import { Fragment, useEffect, useState } from "react";
import { MapContainer, Marker, Popup, TileLayer, useMapEvents } from "react-leaflet";
import { useParams } from "react-router-dom";
import Button from "../../components/base/Button";
import Heading from "../../components/base/Heading";
import ErrorAlert from "../../components/base/alerts/ErrorAlert";
import GroupEvent from "../../models/message/GroupEvent";
import useAuth from "../../services/auth";
import { getSessionWebSocket } from "../../services/session";
import { GenericMessage, LocationMessage, TextMessage } from "../../models/message/ClientMessage";

function LocationMarkers() {

  const [markers, setMarkers] = useState<Array<LatLngExpression>>([[51.505, -0.09]])

  const map = useMapEvents({
    click(e) {
      setMarkers(prevValue => [...prevValue, e.latlng])
    }
  })

  return (
    <Fragment>
      {markers.map((marker, i) => <Marker position={marker} key={i} ></Marker>)}
    </Fragment>
  )
}

export default function SessionPage() {

  const { groupId } = useParams()
  const [error, setError] = useState<string>()

  const { user } = useAuth()
  const [ws, setWs] = useState<WebSocket>()

  const [textMessages, setTextMessages] = useState<Array<TextMessage>>()
  const [locationEvents, setLocationEvents] = useState<Array<LocationMessage>>()
  const [events, setEvents] = useState<Array<GroupEvent>>([])

  const startWs = async () => {
    if (!groupId) {
      setError("The group does not exist")
      return
    }
    const ws = getSessionWebSocket(groupId, user!.token)

    ws.onopen = (event) => {
      console.log("websocket handshake done");
      console.log(event);
    }
    ws.onerror = (event) => {
      console.log("an error occurred");
      console.log(event);
      setError("An error occurred.")
    }
    ws.onmessage = (message: MessageEvent<string>) => {
      console.log(message.data);

      const event: GroupEvent = JSON.parse(message.data)

      // decide what to do depending on the payload 
      
      if (event.type === "MESSAGE" && event.payload) {
        try {
          const genericMessage: GenericMessage = JSON.parse(event.payload)
          switch(genericMessage.type) {
            case "MESSAGE":
              console.log(genericMessage as TextMessage);
              break
            case "LOCATION":
              console.log(genericMessage as LocationMessage);
              
              break
          }
        } catch (err) { /*do nothing*/ }
      }

      setEvents((prevEvents) => {

        console.log(prevEvents.length);
        
        if (prevEvents.length >= 20) {
          prevEvents.pop()
        }

        return [...prevEvents, event]
      })

      console.log(events);
      
    }

    setWs(ws)
  }

  const cleanupResources = () => {
    if (ws) {
      ws.close()
    }
  }

  useEffect(() => cleanupResources, [])

  const [position, setPosition] = useState<LatLngExpression>([51.505, -0.09])

  // const map = useMapEvents({
  //   locationerror: (error) => {
  //     setError(error.message)
  //   },
  //   locationfound: (event) => {
  //     setPosition(event.latlng)
  //     map.flyTo(event.latlng, map.getZoom())
  //   }
  // })

  return (
    <section className="relative w-full sm:max-w-4xl h-5/6 mx-auto px-2 border border-solid border-red-500 flex justify-evenly">
      { error ? 
        <div className="relative bottom-12 w-full">
          <ErrorAlert title="Error" message={error} onClose={() => setError(undefined)}/> 
        </div>
        : 
        null
      }
      <div className="w-2/3 h-full">
        <div>
          <Heading classes="mb-4">
            Tour sessions
          </Heading>
        </div>
        <div className="w-full h-5/6 mx-auto">
          <MapContainer center={position} zoom={13} scrollWheelZoom={false} className="w-full h-full">
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            ></TileLayer>
            <Marker position={position}>
              <Popup>
                A pretty CSS3 popup.
              </Popup>
            </Marker>
            <LocationMarkers></LocationMarkers>
          </MapContainer>

          <Button onClick={startWs}>Start</Button>
        </div>
      </div>
      <div className="border border-solid border-yellow-700 w-1/3">
        <ul className="border border-solid border-red-900 w-full h-full overflow-y-auto">
          {events.map((event, i) => 
            <li key={i}>
              <p>{event.type}</p>
              <p>{event.username}</p>
              {event.payload ? <p>{event.payload}</p> : null}
            </li>
          )}
        </ul>
      </div>
    </section>
  )
}