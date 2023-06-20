import L from "leaflet"
import { memo, useEffect } from "react"

const initMap = (mapId: string) => {
  const iconRetinaUrl = "assets/marker-icon-2x.png";
  const iconUrl = "assets/marker-icon.png";
  const shadowUrl = "assets/marker-shadow.png"

  L.Marker.prototype.options.icon = L.icon({
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

  const startingPoint: L.LatLngExpression = [47.4979, 19.0402]
  const localMap = L.map(mapId)
  .setView(startingPoint)
  .setZoom(maxZoom)
    
  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: maxZoom,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  }).addTo(localMap);

  return localMap
}

const Map = memo(function(props: {
  setMapInParent: (map: L.Map) => void
}) {

  const { setMapInParent } = props

  useEffect(() => {
    console.log("useEffect init map");
    setMapInParent(initMap('map'))
  }, [setMapInParent])

  return (
    <div id='map' className="w-full h-full"></div>
  )
})

export default Map