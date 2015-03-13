android-call-location
=====================

Android application to associate geo-location to call history


Android Call Location is an app that retrieves geo-location each time a phone call or text message is received and stores it internally, allowing users to view where they were at the time of the call.

|| http://lh6.googleusercontent.com/-ucwLCpLtFAo/T66XxRqqk_I/AAAAAAAAAIU/ysUoqMkOnyI/s512/MainActivity.jpg || http://lh3.googleusercontent.com/-TvolrY6Z-0Q/T66XxzLhfII/AAAAAAAAAIY/2dbYrhPG7NE/s512/ServiceNotification.jpg ||

This is achieved by use of a service that runs in the background checking for updated location information and listening out for incomming connections by extending the `PhoneStateListener` interface.

|| http://lh4.googleusercontent.com/-DVjaRwuOT1I/T7ExMHT2mXI/AAAAAAAAAJY/0bpHo62tX3w/s640/CallHistory.jpg || http://lh3.googleusercontent.com/-FDqhQ3dTzOQ/T7ExMQ2gkVI/AAAAAAAAAJY/j8dFOnYAmr8/s640/CallLocation.jpg ||

The Google Maps API is used to show the locations of calls within the applications call history.
