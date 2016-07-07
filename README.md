Code for the Activiti & Spring Boot webinar  (http://www.jorambarrez.be/blog/2014/09/29/webinar-on-youtube/)

First, clone the latest and greatest Activiti from https://github.com/Activiti/Activiti

This project was cloned from the url above.   

I have just played with it and pushed in case needed later.   

Started to dockerize the application.   

Build the docker image: 
`mvn clean package docker:build`

Run the docker container: 
`docker run --net="host" -p 8888:8888 -t demo`


