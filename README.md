# Week7Daily2Navigation
This application aims to explore the Google Maps API for android applications. It features a SupportMapFragment from the Maps library, an EditText to take in address input from the user, a Button to take the inputted address and look for a corresponding address in the API, 5 Radio Buttons in a RadioGroup to choose the map type, and 2 Floating Action Buttons to move the camera to the user's current location and launch the Navigation app with turn-by-turn directions to the inputted address.

## Main Activity
On startup the ACCESS_FINE_LOCATION permission is requested from the user in order to use the MyLocation layer of the GoogleAPI and show a Blue Dot representing the user's last known location. If the permission is denied, then the Floating Action Button for showing the user's current location is hidden. All of the functionality of the application is located within the MainActivity Kotlin file.

<img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot1.png?raw=true" width="200"><img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot2.png?raw=true" width="200">

### Finding an Address
The user inputs the address they wish to find in the EditText, then clicks the "FIND" button to try and find the address. The application uses a Geocoder object to try and find the location. If no matches are found, nothing happens. The Geocoder can find multiple matches, but we only consider the single most-likely match, display it, and use a Marker object to signify its position. This application uses a custom image for the marker instead of the default icon by turning a .png image into a bitmap and assigning it to the marker. 

<img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot3.png?raw=true" width="200">

### Removing Markers
The Google API does not provide an OnLongClickListener for markers, and I wanted to keep the default behavior of the OnClickListener for the marker to display the title and show additional button at the bottom right. To get around this, I made the Markers draggable, but instead of implementing the ability to drag the markers, I removed the markers at the start of a drag event.

### Changing Map Type
Radio buttons give the user the ability to switch between the 5 GoogleMap types. The application starts at the default NORMAL type. Radio buttons for the HYBRID, SATELLITE, TERRAIN, and NONE map types are provided and switch the map once they are selected.

<img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot4.png?raw=true" width="200"> <img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot5.png?raw=true" width="200"> <img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot6.png?raw=true" width="200"> <img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot7.png?raw=true" width="200">

### Showing Current Location
The GoogleMap object allows for the MyLocation layer of the GoogleMap API to be enabled as long as the appropriate permissions are granted by the user. However, I wanted to have this functionality available through a FloatingActionButton instead of the provided button at the top right. In order to do that, I used the FusedLocationProviderClient from LocationServices to query for the last known location of the device, then used the GoogleMap object to move the camera to the appropriate Latitude and Longitude when the Floating Action Button was pressed.

<img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot8.png?raw=true" width="200">

### Launching Navigation App for turn-by-turn directions
In order to start the navigation app for turn-by-turn directions, an Intent with an action is used. A URI defining the destination using Latitude and Longitude is passed to the intent, and the intent's package is set to launch the Navigation App. Finally, an activity using the intent with a defined action, uri and package is started to launch the Navigation application.

<img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot9.png?raw=true" width="200"><img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot10.png?raw=true" width="200"><img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot11.png?raw=true" width="200"><img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot12.png?raw=true" width="200">

### Creating Geofences
Whenever the user searches for an address, a circular geofence with a radius of 30m is created at the found location. When the user's device enters the geofence, a Geofencing Event is sent and picked up by the GeofenceBroadcastReceiver. The broadcast receiver picks up the event and sends out a notification to let the user know that they have arrived at the location. Clicking the notification opens up the application.

<img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot13.jpg?raw=true" width="200"><img src="https://github.com/josecatalasan/Week7Daily2Navigation/blob/master/screenshot14.jpg?raw=true" width="200">
