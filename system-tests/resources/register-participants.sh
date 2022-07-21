#!/bin/bash

PARTICIPANTS=(provider consumer-eu consumer-us)

# Register dataspace participants
for i in "${PARTICIPANTS[@]}"; do
    echo "Registering $i"
    java -jar $REGISTRATION_SERVICE_CLI_JAR_PATH -d="did:web:$i-did-server:$i" -k=system-tests/resources/vault/$i/private-key.pem -s='http://localhost:8184/api' participants add --ids-url "http://$i:8282"
done
