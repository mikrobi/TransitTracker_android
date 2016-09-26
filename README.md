# TransitTracker_android
TransitTracker mobile app for Android http://transit-tracker.net/

## Setup
The keys for the Google Maps API are not stored in this repository. You have to manually set them in

    app/src/debug/res/values/google_maps_api.xml
    app/src/release/res/values/google_maps_api.xml

Before setting the keys you should make sure that your changes won't be committed back into this repository:

    git update-index --assume-unchanged app/src/debug/res/values/google_maps_api.xml
    git update-index --assume-unchanged app/src/release/res/values/google_maps_api.xml
