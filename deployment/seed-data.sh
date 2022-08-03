#!/bin/bash

set -euxo pipefail

function split_by_comma() {
  local -r val="$1"
  echo "${val}" | tr "," "\n"
}

arr_participant_ids=(`split_by_comma $PARTICIPANT_ID`)
arr_assets_storage_accounts=(`split_by_comma $ASSETS_STORAGE_ACCOUNT`)
arr_edc_hosts=(`split_by_comma $EDC_HOST`)

if [ ${#arr_participant_ids[@]} -ne ${#arr_assets_storage_accounts[@]} ] || [ ${#arr_assets_storage_accounts[@]} -ne ${#arr_edc_hosts[@]} ]; then
    echo "PARTICIPANT_ID,ASSETS_STORAGE_ACCOUNT and EDC_HOST must be of equal length"
    exit 1
fi

for i in "${!arr_edc_hosts[@]}"; do
    echo "Seeding data for Participant ID: ${arr_participant_ids[$i]}, Assets Storage Account: ${arr_assets_storage_accounts[$i]}, EDC Host: ${arr_edc_hosts[$i]}"

    newman run \
      --folder "Publish Master Data" \
      --env-var data_management_url="http://${arr_edc_hosts[$i]}:9191/api/v1/data" \
      --env-var storage_account="${arr_assets_storage_accounts[$i]}" \
      --env-var participant_id="${arr_participant_ids[$i]}" \
      --env-var api_key="$API_KEY" \
      deployment/data/MVD.postman_collection.json
done
