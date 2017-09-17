# Memorable_Places_App

An android app which allows you to open up a google map and save your location.
The location is added to a list which you can click on to take the map to that location.

To add a location just longpress on the map:
- This will create a marker at the location
- It will make a call to googles GeoLocation service @ https://maps.googleapis.com/maps/api/geocode/json?latlng=
- Using the latitude and longituded of your click location it will return an address
- You are free to adjust the position by another longpress which will override the previous marker
- Only when you navigate back to the list does it store that location for later retireval

Note that this is just a trial app to test out functionality. It doesn't at present use SQLite to store the data on app create (or any other online server)
