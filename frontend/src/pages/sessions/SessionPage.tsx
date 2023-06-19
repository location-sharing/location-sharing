import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { Fragment, useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Button from "../../components/base/Button";
import Heading from "../../components/base/Heading";
import ErrorAlert from "../../components/base/alerts/ErrorAlert";
import { ClientMessage, GenericMessage, GeolocationPositionData, LocationMessage, TextMessage, modelFromGeolocationPosition } from "../../models/message/ClientMessage";
import GroupEvent from "../../models/message/GroupEvent";
import useAuth from "../../services/auth";
import { getSessionWebSocket } from "../../services/session";
import Map from "./Map";
import { PositionMarker, UserPosition, UserPositions } from "./Positions";
import "./SessionPage.css";
import { distanceInKm, generateRandomPastelColor, generateRandomString } from "../../util/util";
import { renderToStaticMarkup } from "react-dom/server";

// =============================  HELPER FUNCTIONS  =============================
const buildPositionMarker = (
  initialPosition: GeolocationPositionData, 
  username: string,
  color: string,
  onMarkerClick: L.LeafletMouseEventHandlerFn
): PositionMarker => {
  const iconDOMId = generateRandomString(12)
  const iconMarkup = renderToStaticMarkup(
    <div 
      id={iconDOMId} 
      className={`-top-[23px] border-[12px] bg-transparent border-solid rounded-ss-[50%] rounded-se-[50%] rounded-ee-[50%] absolute w-6 h-6 transform -rotate-45 ring-1 ring-slate-500 shadow-md shadow-gray-400`}
    ></div>
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
  )
  .bindTooltip(`${username}`)
  .on('click', onMarkerClick)

  return { iconDOMId, marker, color }
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
  const [includeEveryoneOnMap, setIncludeEveryoneOnMap] = useState<boolean>(true)

  const [showStatsUsername, setShowStatsUsername] = useState<string>()
  const [showStatsPane, setShowStatsPane] = useState<boolean>(false)
  const [showUsersPane, setShowUsersPane] = useState<boolean>(false)

  const [map, setMap] = useState<L.Map>()

  const locationEventLengthLimit = 20

  useEffect(() => checkGeolocationSupport(), [])


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
  }, [userToWatch, userPositions])

  // ==================  INIT  ==================
  const checkGeolocationSupport = () => {
    if (!navigator.geolocation) {
      setError("Your browswer doesn't support the Geolocation API.")
    }
  }

  // ==================  WebSocket   ==================
  // starts to listen to the messages of others
  const startWs = async () => {
    const ws = getSessionWebSocket(groupId!, user!.token)
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

  const stopWs = useCallback(() => {
    if (ws) {
      ws.close()
      setWs(undefined)
    }
  }, [ws])

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

  const stopTracking = useCallback(() => {
    if (positionTracker) {
      navigator.geolocation.clearWatch(positionTracker)
      setPositionTracker(undefined)

      if (userToWatch === user!.username) {
        setUserToWatch(undefined)
      }
    }
  }, [positionTracker, user, userToWatch])

  // ==================  CLEANUP  ==================
  const cleanupResources = useCallback(() => {
    console.log("cleaning up");
    
    stopWs()
    stopTracking()
  }, [stopWs, stopTracking])

  // runs the returned cleanup function on component unmount
  useEffect(() => cleanupResources, [])

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
        
        const positionMarker = buildPositionMarker(
          position, 
          username, 
          generateRandomPastelColor(),
          () => {
            setShowStatsUsername(username)
            setShowStatsPane(true)
          }
        )
        const newUserPosition: UserPosition = {
          username: username,
          positionMarker: positionMarker,
          positionEvents: [position]
        }
        prevUserPositions[username] = newUserPosition

        // add the marker to the map
        addMarkerToMap(positionMarker)
      }

      // if (prevUserPositions[username].positionMarker)

      setChangedUserPosition({...prevUserPositions[username]})
      return {...prevUserPositions}
    })
  }

  // ========================== Map Updates ==========================
  useEffect(() => {
    const updateUserMarker = (userPosition?: UserPosition) => {
      // update and add user markers based on the user positions
      if (!userPosition) return 
          
      const events = userPosition.positionEvents
      if (!events || events.length === 0) return
  
      const { latitude, longitude } = events[events.length-1].coords
      
      if (userPosition) {
        // update marker position
        console.log(`setting marker position for user ${userPosition.username}`);
        userPosition.positionMarker.marker.setLatLng([latitude, longitude])

        // update map to accomodate the changed position
        if (includeEveryoneOnMap) {
          const bounds = map?.getBounds()
          if (bounds && !bounds.contains([latitude, longitude])) {
            const sw = bounds.getSouthWest()
            const ne = bounds.getNorthEast()
            const sw_bounds: L.LatLngExpression = [
              Math.min(sw.lat, latitude),
              Math.min(sw.lng, longitude)
            ]
            const ne_bounds: L.LatLngExpression = [
              Math.max(ne.lat, latitude),
              Math.max(ne.lng, longitude)
            ]
            map?.fitBounds([sw_bounds, ne_bounds], {
              maxZoom: map.getZoom()
            })
          }
        }
      }
      console.log(`updated marker for user ${userPosition.username}`);
    } 
    updateUserMarker(changedUserPosition)     
  }, [changedUserPosition, userPositions, includeEveryoneOnMap])

  const centerMapToPosition = useCallback((position?: L.LatLngExpression) => {
    if (!position) return 
    if (map) {
      map.setView(position)
    } else {
      console.log("center: no map available");
    }
    console.log(`centered map to position ${position}`);
  }, [map])

  useEffect(() => { centerMapToPosition(centeredPosition) }, [centeredPosition, centerMapToPosition])

  const toggleMarkers = () => {
    for (const username in userPositions) {
      if (username === user!.username) continue
      const positionMarker = userPositions[username]?.positionMarker
      if (positionMarker && positionMarker.marker) {
        if (showMarkers && map) {
          addMarkerToMap(positionMarker)
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
      setPositionMarkerColor(positionMarker, positionMarker.color)
    } else {
      setError("Marker could not be added to the map.")
    }
  }

  // ============================ UI ============================
  const userPositionsToList = () => {
    if (!user) return null

    const items = userPositions[user.username] ? [{
      username: user.username,
      markerColor: userPositions[user.username].positionMarker.color,
      lastPosition: userPositions[user.username].positionEvents.slice(-1)[0]
    }] : []

    for (const username in userPositions) {
      if (username === user.username) continue
      items.push({
        username,
        markerColor: userPositions[username].positionMarker.color,
        lastPosition: userPositions[username].positionEvents.slice(-1)[0]
      })
    }

    const loggedInUserPosition = userPositions[user.username]?.positionEvents.slice(-1)[0]

    if (items.length === 0) {
      return (
        <div className="w-full relative top-24 flex flex-col justify-center">
          <p className="text-center">There are no users yet. Find your position or connect to the group first!</p>
        </div>
      )
    }

    return (
        <ul className="h-full">
          { items.map(entry => 
            <li 
              key={entry.username} 
              className="w-full ring-1 rounded-md ring-slate-300 p-3 hover:cursor-pointer hover:shadow-md my-3 first:mt-0"
              onClick={() => {
                setCenteredPosition([
                  entry.lastPosition.coords.latitude,
                  entry.lastPosition.coords.longitude
                ])
                setShowStatsUsername(entry.username)
                setShowStatsPane(true)
              }}
            >
              <div className="flex flex-row gap-x-2 items-center">
                <div className="w-3 h-3 rounded-full" style={{
                  backgroundColor: entry.markerColor
                }}></div>
                <p className="text-md font-medium">{entry.username}</p>
              </div>
              <div>    
                { entry.username !== user.username && loggedInUserPosition ?
                  <p>Distance: { (distanceInKm(
                    entry.lastPosition.coords.latitude,
                    entry.lastPosition.coords.longitude,
                    loggedInUserPosition.coords.latitude,
                    loggedInUserPosition.coords.longitude
                  )).toFixed(4)} km</p>
                  :
                  null
                }
              </div>
            </li>
          )}
        </ul>
    )
  }

  const showStatsForUser = (username: string) => {
    const userPosition = userPositions[username]
    if (!userPosition) return
    const lastPosition = userPosition.positionEvents.slice(-1)[0]
    return (
      <div className="w-full ring-1 rounded-md ring-slate-300 p-6 flex flex-row justify-between items-center flex-wrap">
        <div>
          <div className="flex flex-row gap-x-2 items-center">
            <div className="w-3 h-3 rounded-full" style={{
              backgroundColor: userPosition.positionMarker.color
            }}></div>
            <p className="text-md font-medium">{userPosition.username}</p>
          </div>
          <p className="mt-2">Last update: {new Date(lastPosition.timestamp).toLocaleString()}</p>
        </div>
        <div className="flex flex-col items-center">
          <h6 className="text-lg font-medium">Speed</h6>
          <h2 className="text-4xl font-semibold">{lastPosition.coords.speed ?? "N/A"}</h2>
        </div>
        <div className="flex flex-col items-center">
          <h6 className="text-lg font-medium">Altitude</h6>
          <h2 className="text-4xl font-semibold">{lastPosition.coords.altitude ?? "N/A"}</h2>
        </div>
        <div className="flex flex-col items-center gap-y-3 justify-evenly">    
         <button 
          className="bg-theme-bg-1 ring-1 ring-slate-300 rounded-md px-3 py-2 font-medium"
          onClick={() => centerMapToPosition([
            lastPosition.coords.latitude,
            lastPosition.coords.longitude
          ])}
         >Show on map</button>
        </div>
      </div>
    )
  }

  return (
    <section className="relative w-screen sm:max-w-6xl mb-12 mx-auto flex flex-col items-center gap-y-3">
      { error ? 
          <div className="absolute top-12 w-full">
            <ErrorAlert title="Error" message={error} onClose={() => setError(undefined)}/> 
          </div>
          : 
          null
        }
      <div>
        <Heading>
          Tour sessions
        </Heading>
      </div>
      <div className="w-full h-full flex justify-evenly ">
        <div className="flex flex-col gap-y-3 items-center w-full h-full px-3">
          
          <div className="w-full h-[500px] mx-auto mb-3 z-0">
            <Map setMapInParent={setMap}></Map>
          </div>
          <div className="flex flex-row gap-3 flex-wrap justify-center">
            { ws ?
              <Button btnType="danger" onClick={stopWs}>Disconnect</Button>
              :
              <Button btnType="basic" onClick={startWs}>Connect to group</Button>
            }
            { positionTracker ?
              <Fragment>
                <Button btnType="danger" onClick={stopTracking}>Stop tracking my position</Button>
                { userToWatch === user!.username ?
                  <Button btnType="danger" onClick={() => setUserToWatch(undefined)}>Stop following my position</Button>
                  :
                  <Button btnType="basic" onClick={() => setUserToWatch(user!.username)}>Follow my position</Button>
                }
              </Fragment>
              :
              <Button btnType="basic" onClick={startTracking}>Track my position</Button>
            }
            { ws && positionTracker ?
              ( broadcastMyPosition ?
                <Button btnType="danger" onClick={() => setBroadcastMyPosition(false)}>Stop broadcasting my position</Button>
                :
                <Button btnType="basic" onClick={() => setBroadcastMyPosition(true)}>Start broadcasting my position</Button>
              )
              :
              null
            }
            <Button btnType="basic" onClick={findCurrentPosition}>Find my position</Button>
            <Button btnType="basic" onClick={() => { setIncludeEveryoneOnMap(prev => !prev) }}>Toggle map lock</Button>
            <Button btnType="basic" onClick={toggleMarkers}>Toggle Markers</Button>
            <Button btnType="basic" onClick={() => { setShowUsersPane(!showUsersPane) }}>Toggle Users</Button>
            <Button btnType="basic" onClick={() => {
              // show the user's stats if there is no other user selected and the pane is closed
              if (!showStatsPane) {
                if (!showStatsUsername && user) {
                  setShowStatsUsername(user.username)
                }
                setShowStatsPane(true)
              } else {
                setShowStatsPane(false)
              }
            }}>Toggle stats</Button>
          </div>
        </div>
        { showUsersPane ?
          <div className="w-1/4 overflow-y-auto p-2 h-[550px]">
            { userPositionsToList() }
          </div>
          :
          null
        }
      </div>
      
      <div className="w-full mb-6">
        { showStatsPane && showStatsUsername ?
          showStatsForUser(showStatsUsername)
          :
          (!showStatsUsername && showStatsPane ?
            <p>Select a user first or find your position.</p>
            :
            null
          )
        }
      </div>
    </section>
  )
}