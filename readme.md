Rest API using akka-http and akka-clustering

## To run:

- Run the Server class
- GET http://localhost:8080/hotel/joey/Bangkok?sort=desc
- Only a single request allowed per 10 minute window
- Exceeding the limit will suspend the client for 5 minutes
- All limits can be configured in _src/main/resources/application.conf_

TODO:
- containerize the app with docker