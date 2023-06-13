import L, { LatLngExpression, Marker, icon } from "leaflet";
import "leaflet/dist/leaflet.css";
import { useEffect, useState } from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { useParams } from "react-router-dom";
import Button from "../../components/base/Button";
import Heading from "../../components/base/Heading";
import ErrorAlert from "../../components/base/alerts/ErrorAlert";
import { ClientMessage, GenericMessage, GeolocationPositionData, LocationMessage, TextMessage, modelFromGeolocationPosition } from "../../models/message/ClientMessage";
import GroupEvent from "../../models/message/GroupEvent";
import useAuth from "../../services/auth";
import { getSessionWebSocket } from "../../services/session";
import { generateRandomString } from "../../util/util";
import "./SessionPage.css";

export default function SessionPage() {

  const { groupId } = useParams()
  const [error, setError] = useState<string>()

  const { user } = useAuth()
  const [ws, setWs] = useState<WebSocket>()

  const [map, setMap] = useState<L.Map>()

  interface PositionMarker {
    iconDOMId: string,
    marker: L.Marker
  }
  const buildPositionMarker = (initialPosition: LatLngExpression, username: string): PositionMarker => {
    const iconDOMId = generateRandomString(12)
    const iconMarkup = renderToStaticMarkup(
      <div id={iconDOMId} className={`-top-[23px] border-[12px] bg-transparent border-solid rounded-ss-[50%] rounded-se-[50%] rounded-ee-[50%] absolute w-6 h-6 transform -rotate-45`}></div>
    )
    const icon =  L.divIcon({
      className: 'map-icon',
      html: iconMarkup,
      tooltipAnchor: [15, -12],
      popupAnchor: [0, -25]
    })
    
    const marker = L.marker(initialPosition, { icon })
    .bindTooltip(`${username}`)

    return { iconDOMId, marker }
  }

  const setPositionMarkerColor = (positionMarker: PositionMarker, color: string) => {
    const element = document.getElementById(positionMarker.iconDOMId)
    if (element) {      
      element.style.borderColor = color
    }
  }

  interface UserPosition {
    positionMarker: PositionMarker,
    positionEvents: Array<GeolocationPositionData>
  }
  interface UserPositions {
    [key: string]: UserPosition
  }
  const [userPositions, setUserPositions] = useState<UserPositions>({})
  const [positionWatcher, setPositionWatcher] = useState<number>()

  const locationEventLengthLimit = 20
  const updateUserPositions = (username: string, position: GeolocationPositionData) => {
    setUserPositions(prevUserPositions => {

      const latlng: LatLngExpression = [
        position.coords.latitude,
        position.coords.longitude
      ]
  
      console.log("updating user positions");
      // console.log(prevUserPositions);
      // console.log(prevUserPositions[username]);
      
      const userPosition = prevUserPositions[username]
      if (userPosition) {
        if (userPosition.positionEvents.length >= locationEventLengthLimit) {
          userPosition.positionEvents.pop()
          // maybe remove a point from the map if we keep the trails
        }
  
        userPosition.positionEvents.push(position)
  
        if (map) {
          userPosition.positionMarker.marker.setLatLng(latlng)
        } else {
          console.log("map undefined when trying to modify marker");
        }
  
      } else {
        console.log(`${username} doesn't have an entry, generating one`);
        
        // create a new entry for the user
        const randomColor = `#${Math.floor(Math.random()*16777215).toString(16)}`
  
        const newUserPosition: UserPosition = {
          positionMarker: buildPositionMarker(latlng, username),
          positionEvents: [position]
        }
        prevUserPositions[username] = newUserPosition
  
        if (map) {
          newUserPosition.positionMarker.marker.addTo(map)
          setPositionMarkerColor(newUserPosition.positionMarker, randomColor)
        } else {
          console.log("map undefined when trying to add marker");
          
        }
      }
      return prevUserPositions
    })
  }

  // starts to listen to the messages of others
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
      // console.log(message.data);

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
              // don't update the location via the received message, update it instantly instead
              if (event.userId === user!.userId) {
                break
              }
              console.log(genericMessage as LocationMessage);
              const locationEvent = genericMessage as LocationMessage
              updateUserPositions(event.username, locationEvent.position)
              break
          }
        } catch (err) { /*do nothing*/ }
      }
    }
    setWs(ws)
  }

  const stopWs = () => {
    if (ws) {
      ws.close()
      setWs(undefined)
    }
  }

  const cleanupResources = () => {
    stopWs()
    stopTracking()
    setMap(undefined)
  }

  const findCurrentPosition = () => {
    console.log("finding current position");
    
    navigator.geolocation.getCurrentPosition(
      position => {
        const positionModel = modelFromGeolocationPosition(position)
        updateUserPositions(user!.username, positionModel)
        centerMapToPosition(positionModel)
      },
      _ => setError("An error occurred while getting your position"),
      {
        enableHighAccuracy: true
      }
    )
  }

  const initMap = () => {
    if (map) return

    const iconRetinaUrl = "assets/marker-icon-2x.png";
    const iconUrl = "assets/marker-icon.png";
    const shadowUrl = "assets/marker-shadow.png"

    Marker.prototype.options.icon = icon({
      iconRetinaUrl,
      iconUrl,
      shadowUrl,
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      tooltipAnchor: [16, -28],
      shadowSize: [41, 41]
    })

    const maxZoom = 19

    const startingPoint: LatLngExpression = [47.4979, 19.0402]
    const localMap = L.map('map')
    .setView(startingPoint)
    .setZoom(maxZoom)
      
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: maxZoom,
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(localMap);

    setMap(localMap)
  }

  const checkGeolocationSupport = () => {
    if (!navigator.geolocation) {
      setError("Your browswer doesn't support the Geolocation API.")
    }
  }


  useEffect(checkGeolocationSupport, [])
  useEffect(initMap, [])
  useEffect(() => { 
    // idk why this only adds the markers if I check the map here...
    if (map) {
      findCurrentPosition() 
    }
  }, [map])

  // runs the returned cleanup function on component unmount
  useEffect(() => cleanupResources, [])

  const sendPositionToGroup = (position: GeolocationPositionData) => {
    
    if (ws) {
      console.log("sending position to group");
      // console.log(position);
      

      const locationMessage: LocationMessage = {
        type: "LOCATION",
        position: position
      }

      // console.log(locationMessage);
      // console.log(JSON.stringify(position));
      
      const clientMessage: ClientMessage = {
        payload: JSON.stringify(locationMessage)
      }
      ws.send(JSON.stringify(clientMessage))
    }
  }

  const startTracking = () => {
    // console.log("start clicked");
    const watcherId = navigator.geolocation.watchPosition(
      position => {
        if (position) {
          const positionModel = modelFromGeolocationPosition(position)
          updateUserPositions(user!.username, positionModel)
          sendPositionToGroup(positionModel)
        }
      },
      _ => setError("An error occurred while watching your position"),
      {
        enableHighAccuracy: true
      }
    )
    setPositionWatcher(watcherId)
  }

  const stopTracking = () => {
    // console.log("stop clicked");
    if (positionWatcher) {
      navigator.geolocation.clearWatch(positionWatcher)
      setPositionWatcher(undefined)
    }
  }

  const centerMapToPosition = (position: GeolocationPositionData) => {
    if (map) {
      console.log("centering map position");      
      map.setView(
        [position.coords.latitude, position.coords.longitude],
        map.getZoom()
      )
    }
  }

  const centerMapToUser = () => {
    console.log("center map to user");
    // console.log(userPositions);
    // console.log(user);
    // console.log(userPositions[user!.username]);
    
    const userPosition = userPositions[user!.username]
    if (!userPosition) {
      findCurrentPosition()
    } else {
      console.log("centering to last position");      
      centerMapToPosition(userPosition.positionEvents[userPosition.positionEvents.length-1])
    }
  }

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
          <div id='map' className="w-full h-full"></div>
          {ws ?
            <Button onClick={stopWs}>Disconnect</Button>
            :
            <Button onClick={startWs}>Connect</Button>
          }
          { positionWatcher ?
            <Button onClick={stopTracking}>Stop tracking</Button>
            :
            <Button onClick={startTracking}>Track my position</Button>
          }
          <Button onClick={centerMapToUser}>Center to my position</Button>
        </div>
      </div>
      <div className="border border-solid border-yellow-700 w-1/3">
        <ul className="border border-solid border-red-900 w-full h-full overflow-y-auto">
          {/* {events.map((event, i) => 
            <li key={i}>
              <p>{event.type}</p>
              <p>{event.username}</p>
              {event.payload ? <p>{event.payload}</p> : null}
            </li>
          )} */}
        </ul>
      </div>
    </section>
  )
}