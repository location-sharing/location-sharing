import React from 'react'
import L, { icon } from "leaflet";
import "leaflet/dist/leaflet.css"
import "./map.css"
import { Marker } from 'leaflet'

export default class Map extends React.Component {

  defaultZoom = 15
  maxZoom = 19

  componentDidMount(): void {

    // fix marker not showing up
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

    const map = L.map('map')
    .locate({
      setView: true, maxZoom: 
      this.maxZoom
    });
      
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: this.maxZoom,
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);


    map.on('locationfound', event => {
      const radius = event.accuracy
      L.circle(event.latlng, radius).addTo(map)

      const iconColor = "#a07"
      const iconStyles = `
      border-color: ${iconColor}
      `

      const icon = L.divIcon({
        className: 'map-icon',
        html:'<div class="icon-test" style="'+iconStyles+'"></div>',
        tooltipAnchor: [15, -12],
        popupAnchor: [0, -25]
      })

      const popup = L.popup({
        content: `Popup: You are within ${radius} meters`,
      })

      L.marker(event.latlng, {
        icon
      })
      .bindTooltip(`Tooltip: You are within ${radius} meters`)
      .bindPopup(popup)
      .addTo(map)
    })

    map.on('locationerror', event => {
      alert(event.message)
    })
  }

  render() {
    return (
      <div>
        <div id='map'></div>

        <div className='icon-test'></div>
      </div>
    );
  }
}