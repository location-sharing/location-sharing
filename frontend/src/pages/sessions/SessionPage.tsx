import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { Fragment, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Button from "../../components/base/Button";
import Heading from "../../components/base/Heading";
import ErrorAlert from "../../components/base/alerts/ErrorAlert";
import { ClientMessage, GenericMessage, GeolocationPositionData, LocationMessage, TextMessage, modelFromGeolocationPosition } from "../../models/message/ClientMessage";
import GroupEvent from "../../models/message/GroupEvent";
import useAuth from "../../services/auth";
import { getSessionTestWebSocket } from "../../services/session";
import Map from "./Map";
import { PositionMarker, UserPosition, UserPositions } from "./Positions";
import "./SessionPage.css";
import { generateRandomPastelColor, generateRandomString } from "../../util/util";
import { renderToStaticMarkup } from "react-dom/server";

// =============================  HELPER FUNCTIONS  =============================
const buildPositionMarker = (initialPosition: GeolocationPositionData, username: string): PositionMarker => {
  const iconDOMId = generateRandomString(12)
  const iconMarkup = renderToStaticMarkup(
    <div id={iconDOMId} className={`-top-[23px] border-[12px] bg-transparent border-solid rounded-ss-[50%] rounded-se-[50%] rounded-ee-[50%] absolute w-6 h-6 transform -rotate-45 ring-1 ring-slate-500 shadow-md shadow-gray-500`}></div>
  )
  const icon =  L.divIcon({
    className: 'map-icon',
    html: iconMarkup,
    tooltipAnchor: [15, -12],
    popupAnchor: [0, -25]
  })
  
  const marker = L.marker(
    [
      initialPosition.coords.latitude,
      initialPosition.coords.longitude
    ], 
    { icon }
  ).bindTooltip(`${username}`)

  return { iconDOMId, marker }
}

const setPositionMarkerColor = (positionMarker: PositionMarker, color: string) => {
  const element = document.getElementById(positionMarker.iconDOMId)
  if (element) {      
    element.style.borderColor = color
  }
}

// =============================  COMPONENT  =============================
export default function SessionPage(){

  const { groupId } = useParams()
  const { user } = useAuth()

  const [error, setError] = useState<string>()

  const [ws, setWs] = useState<WebSocket>()
  const [positionTracker, setPositionTracker] = useState<number>()

  const [userPositions, setUserPositions] = useState<UserPositions>({})
  const [changedUserPosition, setChangedUserPosition] = useState<UserPosition>()
  const [centeredPosition, setCenteredPosition] = useState<L.LatLngExpression>()
  const [broadcastMyPosition, setBroadcastMyPosition] = useState<boolean>(false)
  const [userToWatch, setUserToWatch] = useState<string>()
  const [showMarkers, setShowMarkers] = useState<boolean>(false)

  const [locationEventLengthLimit, setLocationEventLengthLimit] = useState<number>(20)

  const [map, setMap] = useState<L.Map>()

  useEffect(() => checkGeolocationSupport(), [])

  // runs the returned cleanup function on component unmount
  useEffect(() => cleanupResources, [])

  // sets centered position to the watched user
  useEffect(() => {
    if (userToWatch && userPositions[userToWatch]) {
      const lastPosition = userPositions[userToWatch].positionEvents.slice(-1)[0]
      if (!lastPosition) return
      setCenteredPosition([
        lastPosition.coords.latitude,
        lastPosition.coords.longitude
      ])
    }
  }, [userToWatch])

  // ==================  INIT  ==================
  const checkGeolocationSupport = () => {
    if (!navigator.geolocation) {
      setError("Your browswer doesn't support the Geolocation API.")
    }
  }

  // ==================  WebSocket   ==================
  // starts to listen to the messages of others
  const startWs = async () => {
    if (!groupId) {
      setError("The group does not exist")
      return
    }
    // const ws = getSessionWebSocket(groupId, user!.token)
    const ws = getSessionTestWebSocket(groupId, user!.userId, user!.username)
    ws.onopen = (event) => {
      console.log("websocket handshake done");
    }
    ws.onerror = (event) => {
      console.log("an error occurred");
      console.log(event);
      setError("A communication error occurred.")
    }
    ws.onmessage = (message: MessageEvent<string>) => {
      const event: GroupEvent = JSON.parse(message.data)
      handleGroupEvent(event)
    }
    setWs(ws)
  }

  const handleGroupEvent = (event: GroupEvent) => {
    // decide what to do depending on the payload  
    if (event.type === "MESSAGE" && event.payload) {
      try {
        const genericMessage: GenericMessage = JSON.parse(event.payload)
        switch(genericMessage.type) {
          
          case "MESSAGE":
            console.log(genericMessage as TextMessage);
            break
          
          case "LOCATION":
            // don't update the logged in user's location here, update it instantly instead (before sending the message)
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

  const stopWs = () => {
    if (ws) {
      ws.close()
      setWs(undefined)
    }
  }

  const findCurrentPosition = () => {
    console.log("finding current position");
    navigator.geolocation.getCurrentPosition(
      position => {
        const positionModel = modelFromGeolocationPosition(position)
        updateUserPositions(user!.username, positionModel)
        setCenteredPosition([
          position.coords.latitude,
          position.coords.longitude,
        ])
      },
      _ => setError("An error occurred while getting your position"),
      {
        enableHighAccuracy: true
      }
    )
  }

  // ==================  TRACKING POSITION  ==================
  const startTracking = () => {
    const watcherId = navigator.geolocation.watchPosition(
      position => {
        if (position) {
          const positionModel = modelFromGeolocationPosition(position)
          updateUserPositions(user!.username, positionModel)
          broadcastPositionToGroup(positionModel)
        }
      },
      _ => setError("An error occurred while watching your position"),
      {
        enableHighAccuracy: true
      }
    )
    setPositionTracker(watcherId)
  }

  const broadcastPositionToGroup = (position: GeolocationPositionData) => {
    console.log("broadcasting position");
    
    if (ws && broadcastMyPosition) {
      console.log("sending position to group");
      
      const locationMessage: LocationMessage = {
        type: "LOCATION",
        position: position
      }

      const clientMessage: ClientMessage = {
        payload: JSON.stringify(locationMessage)
      }
      ws.send(JSON.stringify(clientMessage))
    }
  }

  const stopTracking = () => {
    if (positionTracker) {
      navigator.geolocation.clearWatch(positionTracker)
      setPositionTracker(undefined)

      if (userToWatch === user!.username) {
        setUserToWatch(undefined)
      }
    }
  }

  // ==================  CLEANUP  ==================
  const cleanupResources = () => {
    stopWs()
    stopTracking()
  }

  // ==================  USER POSITIONS  ==================
  const updateUserPositions = (username: string, position: GeolocationPositionData) => {
    setUserPositions(prevUserPositions => {

      console.log("updating user positions");
      
      const userPosition = prevUserPositions[username]
      if (userPosition) {
        if (userPosition.positionEvents.length >= locationEventLengthLimit) {
          userPosition.positionEvents.pop()
        }
        userPosition.positionEvents.push(position)
      } else {
        console.log(`${username} doesn't have an entry, generating one`);
        
        const positionMarker = buildPositionMarker(position, username)
        const newUserPosition: UserPosition = {
          username: username,
          positionMarker: positionMarker,
          positionEvents: [position]
        }
        prevUserPositions[username] = newUserPosition

        // add the marker to the map
        addMarkerToMap(positionMarker)
      }

      if (prevUserPositions[username].positionMarker)

      setChangedUserPosition({...prevUserPositions[username]})
      return {...prevUserPositions}
    })
  }

  // ========================== Map Updates ==========================
  useEffect(() => { updateUserMarker(changedUserPosition) }, [changedUserPosition])
  useEffect(() => { centerMapToPosition(centeredPosition) }, [centeredPosition])

  const updateUserMarker = (userPosition?: UserPosition) => {
    // update and add user markers based on the user positions
    if (!userPosition) return 
        
    const events = userPosition.positionEvents
    if (!events || events.length === 0) return

    const { latitude, longitude } = events[events.length-1].coords
    
    const userPostition = userPositions[userPosition.username]
    if (userPostition) {
      // update marker position
      console.log(`setting marker position for user ${userPosition.username}`);
      userPostition.positionMarker.marker.setLatLng([latitude, longitude])
    }
    console.log(`updated marker for user ${userPosition.username}`);
  }

  const centerMapToPosition = (position?: L.LatLngExpression) => {
    if (!position) return 
    
    if (map) {
      map.setView(position)
    } else {
      console.log("center: no map available");
    }
    console.log(`centered map to position ${position}`);
  }

  const toggleMarkers = () => {
    for (const username in userPositions) {
      if (username === user!.username) continue
      const positionMarker = userPositions[username]?.positionMarker
      if (positionMarker && positionMarker.marker) {
        if (showMarkers && map) {
          positionMarker.marker.addTo(map)
          setPositionMarkerColor(positionMarker, generateRandomPastelColor())
        } else {
          positionMarker.marker.remove()
        }
      }
    }
    setShowMarkers(!showMarkers)
  }

  const addMarkerToMap = (positionMarker: PositionMarker) => {
    if (map) {
      positionMarker.marker.addTo(map)
      setPositionMarkerColor(positionMarker, generateRandomPastelColor())
    } else {
      setError("Marker could not be added to the map.")
    }
  }


  return (
    <section className="relative w-full sm:max-w-6xl h-5/6 mx-auto px-2 flex justify-evenly">
      { error ? 
        <div className="absolute top-12 w-full z-50">
          <ErrorAlert title="Error" message={error} onClose={() => setError(undefined)}/> 
        </div>
        : 
        null
      }
      <div className="flex flex-col gap-y-3 items-center w-full h-full">
        <div>
          <Heading classes="mb-4">
            Tour sessions
          </Heading>
        </div>
        <div className="w-5/6 h-2/3 mx-auto mb-6">
          <Map setMapInParent={setMap}></Map>
        </div>
        <div className="flex flex-row gap-3 flex-wrap justify-center">
          { ws ?
            <Button onClick={stopWs} classes="bg-red-800 hover:bg-red-700">Stop Receiving Group Data</Button>
            :
            <Button onClick={startWs}>Start Receiving Group Data</Button>
          }
          { positionTracker ?
            <Fragment>
              <Button onClick={stopTracking} classes="bg-red-800 hover:bg-red-700">Stop tracking my position</Button>
              { userToWatch === user!.username ?
                <Button onClick={() => setUserToWatch(undefined)} classes="bg-red-800 hover:bg-red-700">Stop following my position</Button>
                :
                <Button onClick={() => setUserToWatch(user!.username)}>Follow my position</Button>
              }
            </Fragment>
            :
            <Button onClick={startTracking}>Track my position</Button>
          }
          { ws && positionTracker ?
            ( broadcastMyPosition ?
              <Button onClick={() => setBroadcastMyPosition(false)} classes="bg-red-800 hover:bg-red-700">Stop broadcasting my position</Button>
              :
              <Button onClick={() => setBroadcastMyPosition(true)}>Start broadcasting my position</Button>
            )
            :
            null
          }
          <Button onClick={findCurrentPosition}>Find my position</Button>
          <Button onClick={toggleMarkers}>Toggle Markers</Button>
          {/* <div className="max-h-12">
            <InputLabel htmlFor="locationEventLimit">Location length limit</InputLabel>
            <Input type="number" onChange={(e) => setLocationEventLengthLimit(Number(e.target.value))}/>
          </div> */}
        </div>
      </div>
      {/* <div className="border border-solid border-yellow-700 w-1/3">
        <ul className="border border-solid border-red-900 w-full h-full overflow-y-auto">
        </ul>
      </div> */}
    </section>
  )
}