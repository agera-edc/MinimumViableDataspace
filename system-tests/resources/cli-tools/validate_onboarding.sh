#!/bin/bash

# stop on error
set -euo pipefail

participantName="$1"
participantDid="$2"

echo "Fetching $participantName onboarding status..."

retryCount=0
maxRetryCount=30
onboardingCompleted=false

while [ $retryCount -lt $maxRetryCount ]; do
    cmd="java -jar registration-service-cli.jar \
                    -d=did:web:did-server:registration-service \
                    --http-scheme \
                    -k=/resources/vault/$participantName/private-key.pem \
                    -c=$participantDid \
                    participants get"
    status=$($cmd|jq ".status")
    echo "Status is $status"
    if [ "$status" == "\"ONBOARDED\"" ]; then
        echo "$participantName is onboarded successfully"
        onboardingCompleted=true
        break
    else
        echo "Onboarding is not completed yet for $participantName. Waiting for 1 seconds..."
        sleep 1
    fi
    retryCount=$((retryCount+1))
done

if [ "$onboardingCompleted" == false ]; then
    echo "$participantDid onboarding is not completed yet. Exiting..."
    exit 1
fi
