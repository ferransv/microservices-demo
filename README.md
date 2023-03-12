# Running the application
- Please enter the correct credentials in twitter4j.properties file.
- Then run mvn install -DskipTests command
- Then go to docker-compose folder and run docker-compose up command to run kafka cluster and twitter-to-kafka-service together
- We need to isntall sdkman, and then isntall springboot cli to encrypt our secrets
- Check twitter-to-kafka-service and config server, where we changed bootstrap.yml file and set encrypted password 
- Remember to add ENCRYPT_KEY as environment variable before starting config server and twitter to kafka service