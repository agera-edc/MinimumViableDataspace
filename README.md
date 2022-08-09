# Minimum Viable Dataspace

The Minimum Viable Dataspace (MVD) is a sample implementation of a dataspace that leverages the [Eclipse Dataspace Connector (EDC)](https://github.com/eclipse-dataspaceconnector/dataspaceconnector). The main purpose is to demonstrate the capabilities of the EDC, make dataspace concepts tangible based on a specific implementation, and to serve as a starting point to implement a custom dataspace.

The MVD allows developers and decision makers to gauge the current progress of the EDC and its capabilities to satisfy the functionality of a fully operational dataspace.

As a fully decentralized dataspace is hard to imagine, the MVD also serves the purpose of demonstrating how decentralization can be practically implemented.

## MVD Documentation

Developer documentation can be found under [docs/developer](docs/developer/), where the main concepts and decisions are captured as [decision records](docs/developer/decision-records/).

## Create Dataspace Deployment

To be able to deploy your own dataspace instances, you first need to [fork the MVD repository and set up your environment](docs/developer/continuous-deployment/continuous_deployment.md).

Once your environment is set up, follow these steps to create a new dataspace instance:

- Go to your MVD fork in GitHub.
- Select the tab called `Actions`.
- Select the workflow called `Deploy`.
- Provide your own resources name prefix. Please, use at most 3 characters, composed of lower case letters and numbers.
  This name prefix guarantees the resources name's uniqueness and avoids resource name conflicts.
  Note down the used prefix.
- Click on `Run workflow` to trigger the deployment.

## Destroy Dataspace Deployment

Follow these steps to delete a dataspace instance and free up the corresponding resources:

- Go to your MVD fork in GitHub.
- Select the tab called `Actions`
- Select the workflow called `Destroy`
- Click on `Run workflow`
- Provide the resources prefix that you used when you deployed your DataSpace.
- Click on `Run workflow` to trigger to destroy your MinimumViableDataspace DataSpace.

## Local Development Setup

Additionally, to the services described in [system-tests/README.md](system-tests/README.md) the local development setup
contains three MVD UIs (Data Dashboards) for each EDC participant. Systems Tests are not dependent on the Data
Dashboards. Please follow the instructions in [system-tests/README.md](system-tests/README.md) to set up a local MVD environment for
development purposes with the following exception to use the profile `ui` as described below.

You need to check out the
repository [eclipse-dataspaceconnector/DataDashboard](https://github.com/eclipse-dataspaceconnector/DataDashboard) or
your corresponding fork. Set the environment variable `MVD_UI_PATH` to the path of the DataDashboard repository.

Bash:

```bash
export MVD_UI_PATH="/path/to/mvd/datadashboard"
docker-compose --profile ui -f system-tests/docker-compose.yml up --build
```

PowerShell:

> Docker Compose expects the path to use forward slashes instead of backslashes.

```powershell
$Env:MVD_UI_PATH="/path/to/mvd/datadashboard"
docker-compose --profile ui -f system-tests/docker-compose.yml up --build
```

The profile `ui` creates three Data Dashboards each connected to an EDC participant. The respective `app.config.json`
files can be found in the respective directories:

- `resources/appconfig/provider/app.config.json`
- `resources/appconfig/consumer-eu/app.config.json`
- `resources/appconfig/consumer-us/app.config.json`

That's it to run the local development environment. The following section `Run A Standard Scenario Locally` describes a
standard scenario which can be optionally used with the local development environment.

> Tip: The console output from the services spin up by Docker compose can be noisy. To decrease the output from the
> services on the console set `EDC_CATALOG_CACHE_EXECUTION_PERIOD_SECONDS` to a higher value, e.g. 60, for each EDC
> participant in `system-tests/docker-compose.yml`.

### Run A Standard Scenario Locally

Prerequisite: create a test document manually:

- Connect to the **local** blob storage account (provided by Azurite) of the provider.
  - Storage account name: `providerassets`, storage account key: `key1`.
  - [Microsoft Azure Storage Explorer](https://azure.microsoft.com/features/storage-explorer/) can be used to connect to the local
    storage account on `localhost:10000`.
- Create a container named `src-container`. (Container name is defined for Postman request `Publish Master Data`
  in `deployment/data/MVD.postman_collection.json`)
- Copy `deployment/terraform/participant/sample-data/text-document.txt` into the newly created container.
  - N.B.: it does not have to be this exact file as long you create a file which has the name `text-document.txt`.

All this can also be done using Azure CLI with the following lines from the root of the MVD repository:

Bash:

```bash
conn_str="DefaultEndpointsProtocol=http;AccountName=providerassets;AccountKey=key1;BlobEndpoint=http://127.0.0.1:10000/providerassets;"
az storage container create --name src-container --connection-string $conn_str
az storage blob upload -f .\deployment\terraform\participant\sample-data\text-document.txt --container-name src-container --name text-document.txt --connection-string $conn_str
```

PowerShell:

```powershell
$conn_str="DefaultEndpointsProtocol=http;AccountName=providerassets;AccountKey=key1;BlobEndpoint=http://127.0.0.1:10000/providerassets;"
az storage container create --name src-container --connection-string $conn_str
az storage blob upload -f .\deployment\terraform\participant\sample-data\text-document.txt --container-name src-container --name text-document.txt --connection-string $conn_str
```

This should result in a similar output as follows. Via the Microsoft Azure Storage Explorer it would be possible to
review the new container and the uploaded blob.

```bash
{
  "created": true
}

Finished[#############################################################]  100.0000%
{
  "etag": "\"0x1CC7CAB96842160\"",
  "lastModified": "2022-08-08T15:14:01+00:00"
}
```

The following steps initiate and complete a file transfer with the provided test document.

- Connect to provider (e.g. <http://localhost:7080>) and verify the existence of two assets in the section `Assets`.
- Connect to consumer-eu (e.g. <http://localhost:7081>) and verify two existing assets from the provider in
  the `Catalog Browser`.
  - In the `Catalog Browser` click `Negotiate` for the asset `test-document_company1`.
    - There should be a message `Contract Negotiation complete! Show me!` in less than a minute.
- From the previous message click `Show me!`. If you missed it, switch manually to the section `Contracts`.
  - There should be a new contract. Click `Transfer` to initiate the transfer process.
  - Select as destination `AzureStorage` and click `Start transfer`.
  - There should be a message `Transfer [id] complete! Show me!` in less than a minute. (Where `id` is a GUID.)
- To verify the successful transfer the Storage Explorer can be used to look into the storage account of `consumer-eu`.
  - Storage account name and key is set in `system-tests/docker-compose.yml` for the service `azurite`. Default name
    is `consumereuassets`, key is `key2`.

## Contributing

See [how to contribute](CONTRIBUTING.md).