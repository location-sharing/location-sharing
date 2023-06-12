import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from "react-leaflet";
import Heading from "../../components/base/Heading";
import { useParams } from "react-router-dom";
import { LatLng, LatLngExpression } from "leaflet";
import "leaflet/dist/leaflet.css";
import { useState, useEffect, Fragment } from "react"
import ErrorAlert from "../../components/base/alerts/ErrorAlert";
import { objectToQueryString } from "../../util/util";
import GroupEvent from "../../models/message/GroupEvent";

function LocationMarkers() {

  const [markers, setMarkers] = useState<Array<LatLngExpression>>([[51.505, -0.09]])

  const map = useMapEvents({
    click(e) {
      setMarkers(prevValue => [...prevValue, e.latlng])
    }
  })

  return (
    <Fragment>
      {markers.map((marker, i) => <Marker position={marker} key={i}></Marker>)}
    </Fragment>
  )
}

export default function SessionPage() {

  const { groupId } = useParams()
  const [error, setError] = useState<string>()

  const startWs = async () => {

    // try out normal route
    const res = await fetch(`http://localhost:8080/`)
    console.log(res.body);
    

    if (!groupId) {
      setError("The group does not exist")
      return
    }

    const ws = new WebSocket(`ws://localhost:8080/group/?` + objectToQueryString({ groupId }), )

    ws.onopen = (event) => {
      console.log("websocket handshake done");
      console.log(event);
    }

    ws.onerror = (event) => {
      console.log("an error occurred");
      console.log(event);
      setError("An error occurred.")
    }

    ws.onmessage = (message: MessageEvent<GroupEvent>) => {
      console.log("message");
      console.log(message);
    }
  }

  useEffect(() => { startWs() }, [])

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
    <section className="relative w-full sm:max-w-2xl h-3/4 mx-auto px-2 border border-solid border-red-500">
      { error ? 
        <div className="relative bottom-12 w-full">
          <ErrorAlert title="Error" message={error} onClose={() => setError(undefined)}/> 
        </div>
        : 
        null
      }
    <div>
      <Heading classes="mb-4">
        Tour sessions
      </Heading>
    </div>
    <div className="w-full h-full mx-auto">
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
    </div>
  </section>

  )
}