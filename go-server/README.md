# go-server
Ian's project


## api

1. POST /userID/day/hourtimeInterval/stepCount
2. GET /single/userID/day
3. GET /current/userID
4. GET /range/userID/startDay/numDays

### example
local:

1. curl -X POST http://localhost:8080/1/1/11/300
2. curl -X GET http://localhost:8080/single/1/1
3. curl -X GET http://localhost:8080/current/1
4. curl -X GET http://localhost:8080/range/1/1/1


## local run
dev_appserver.py app.yaml

## deploy
gcloud app deploy

## view
gcloud app browse
