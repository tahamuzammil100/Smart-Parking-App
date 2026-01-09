# Smart Parking Android App

A real-time parking availability monitoring application that helps users find available parking spots quickly and efficiently.

## Overview

Smart Parking is an Android application that displays parking lot information on an interactive map. It shows real-time availability of parking spaces across multiple parking garages, helping drivers find parking spots without wasting time.

## Main Objective

The main goal of this application is to:
- Save time by helping drivers quickly locate available parking spots
- Display live parking availability that updates every 5 seconds
- Show parking locations on a map with user's current position
- Use color-coded markers to indicate parking status at a glance

## Features

### Interactive Map
- Shows all parking lots on an OpenStreetMap view
- Displays user's current location with a blue dot
- Zoom and pan to explore different parking areas

### Real-Time Availability
- Updates parking data every 5 seconds
- Shows number of free slots vs total slots for each lot
- Displays last update timestamp

### Color-Coded Markers
The app uses three different colored icons to indicate parking status:

- **Green Icon** - Available: Plenty of spots available (30% or more free)
- **Yellow Icon** - Limited: Few spots remaining (less than 30% free)
- **Red Icon** - Full: No parking spots available (0 free)

### Navigation Support
- Tap any parking marker to open Google Maps navigation
- Get turn-by-turn directions to your selected parking lot

### Information Windows
Each parking marker displays:
- Parking lot name
- Available slots (example: "5 free / 10 total")
- Last updated time
- Navigation option

## Screenshots

### Splash Screen
The app starts with a splash screen featuring:
- Smart Parking logo
- Animated circular progress indicator
- 2-second loading time

### Map View
Main screen showing:
- Multiple parking lots with color-coded markers
- User's current location (blue dot)
- Info windows showing real-time availability
- Interactive map controls

### Parking Status Examples
- Green Marker: "CPS2 Smart Garage - 8 free / 10 total"
- Yellow Marker: "ICM Smart Garage - 2 free / 10 total"
- Red Marker: "Main Parking - 0 free / 15 total"

### Error Handling
If the server is disconnected, users see a clear error message with troubleshooting tips.

## Technology Stack

- Language: Kotlin
- UI Framework: Jetpack Compose
- Map Library: OSMDroid (OpenStreetMap)
- Architecture: MVVM (Model-View-ViewModel)
- Networking: Retrofit + Gson
- Location: GPS Location Provider

## Backend Integration

The app connects to a backend server to fetch parking data:
- Endpoint: `/api/parking/lots`
- Update Frequency: Every 5 seconds
- Data Format: JSON with parking lot and slot information

## How It Works

1. Launch App: Splash screen appears for 2 seconds
2. Load Data: App fetches parking information from server
3. Display Map: Shows parking lots with color-coded markers
4. Track Location: User's position appears as a blue dot
5. Real-Time Updates: Parking availability refreshes every 5 seconds
6. Navigate: Tap any marker to get directions

## Requirements

- Android device with API level 21+ (Android 5.0 or higher)
- Internet connection for map tiles and data
- Location permission for showing user position
- Backend server running on configured IP address

## Configuration

The app connects to the backend server at:
```
http://192.168.1.232:8080/
```

To change the server address, update `BASE_URL` in `ApiClient.kt`

## Data Displayed

For each parking lot:
- Name (example: "CPS2 Smart Garage")
- Address and location details
- Geographic coordinates (latitude and longitude)
- Total parking slots capacity
- Currently available free slots
- Real-time occupancy status from sensors

## Benefits

- Save time by eliminating the need to drive around looking for parking
- Reduce fuel consumption from unnecessary driving
- Know parking availability before arriving at the location
- Simple and intuitive user interface
- Live data updates every 5 seconds

## Notes

- The app requires an active backend server to function properly
- Location services must be enabled for the best experience
- Map tiles require an internet connection to load
- Marker colors (green, yellow, red) update automatically based on real-time availability
