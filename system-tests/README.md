# System tests

The test copy a file from provider to consumer blob storage account.

## Publish/Build Tasks

MVD dependencies are Eclipse DataSpaceConnector(EDC) and Registration Service. Both of these dependencies are not published to any central artifactory yet so in local development we have to use locally published dependencies.

<br />

### EDC

<br />

Checkout [Eclipse DataSpaceConnector repository](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector). Next, publish `EDC` libraries to local Maven artifactory by executing the following command  from `EDC` root folder


```bash
./gradlew publishToMavenLocal -Pskip.signing
```

_Note for Windows PowerShell, the following command should be used:_

```powershell
./gradlew publishToMavenLocal -P"skip.signing"
```

<br />

### Registration Service

<br />

Next checkout the [Registration Service repository](https://github.com/eclipse-dataspaceconnector/RegistrationService).  As before, publish `Registration Service` libraries to local Maven artifactory by executing the following command from `Registration Service` root folder:

```bash
./gradlew publishToMavenLocal
```

<br />

### MVD

<br />

Now that the publishing to the local repositories has been completed, `MVD` can be built by running the following command from the root of the `MVD` project folder:

```bash
./gradlew build -x test
```

## Local Test Execution

- `MVD` system tests can be executed locally against a local `MVD` instance. 
- `MVD` runs three `EDC Connectors` and one `Registration Service`.

_Note: Ensure that you are able to build `MVD` locally as described in the previous [section](#mvd)._

First, we need to build `EDC Connector launcher` and `Registration Service launcher`.

From the `MVD` root folder, execute the following command:

```bash
./gradlew -DuseFsVault="true" :launcher:shadowJar
```

From the `Registration Service` root folder, execute the following command:

```bash
./gradlew :launcher:shadowJar
```

From the `Registration Service` root folder execute the following commands to set the `Registration Launcher` path environment variable and start `MVD` using the `docker-compose.yml` file.

```bash
export REGISTRATION_SERVICE_LAUNCHER_PATH=/home/user/RegistrationService/launcher
docker-compose -f system-tests/docker-compose.yml up --build
```

_Note for Windows PowerShell, the following commands should be used:_

```powershell
$Env:REGISTRATION_SERVICE_LAUNCHER_PATH="/home/user/RegistrationService/launcher"
docker-compose -f system-tests/docker-compose.yml up --build
```

Once completed, these commands will start:
- Three `EDC Connectors`
- A `Registration Service`
- A `HTTP Nginx Server` (to serve DIDs) 
- An `Azurite blob storage service` which will also be seeded with initial required data using a [postman collection](../deployment/data/MVD.postman_collection.json).

_Note, the `Newman` docker container will automatically stop after seeding initial data from postman scripts._

`EDC Connectors` need to be registered using `Registration Service` CLI client jar. After publishing `Registration Service` locally the client jar should be available under the `RegistrationService-Root/client-cli/build/libs` folder.

```bash
export REGISTRATION_SERVICE_CLI_JAR_PATH=<registration service client jar path>
./system-tests/resources/register-participants.sh
```

_Note for Windows PowerShell, the following should be used to set the environment variable:_

```powershell
$Env:REGISTRATION_SERVICE_CLI_JAR_PATH = "<registration service client jar path>"
```

_Note for Windows, the shell script located in `./system-tests/resources/register-participants.sh` can be run from within a `bash` shell or with `git-bash`._

Set the environment variable `TEST_ENVIRONMENT` to `local` to enable local blob transfer test and then run `MVD` system test using the following command:

```bash
export TEST_ENVIRONMENT=local
./gradlew :system-tests:test
```

_Note for Windows PowerShell, the following commands should be used:_

```powershell
$Env:TEST_ENVIRONMENT = "local"
./gradlew :system-tests:test
```

> [Storage Explorer](https://azure.microsoft.com/features/storage-explorer/) can be used to connect to the `Azurite` storage container on `127.0.0.1:10000` port and under `consumereuassets`, account transferred blob can be viewed.

### Local Test Resources

The following test resources are provided in order to run `MVD` locally. `system-tests/docker-compose.yml` usages it to start `MVD`.

Each `EDC Connector` has its own set of Private and Public keys with java keystore e.g. `system-tests/resources/provider`. These were generated using the following commands:

```bash
# generate a private key
openssl ecparam -name prime256v1 -genkey -noout -out private-key.pem
# generate corresponding public key
openssl ec -in private-key.pem -pubout -out public-key.pem
# create a self-signed certificate
openssl req -new -x509 -key private-key.pem -out cert.pem -days 360
```

Generated keys are imported to keystores e.g. `system-tests/resources/provider/provider-keystore.jks`. Each keystore has password `test123`.

> [KeyStore Explorer](https://keystore-explorer.org/) can be used to manage keystores from UI.

`MVD` local instance usage `EDC File System Vault` and its keys are managed using a java properties file e.g.`system-tests/resources/provider/provider-vault.properties`.

> ! IMPORTANT !
> 
> *File System Vault is NOT a secure vault and thus should only be used for testing purposes*

Web DIDs are available under `system-tests/resources/webdid` folder. The `publicKeyJwk` section of each `did.json` was generated by converting the corresponding public key to JWK format, for example provider connector public key was converted to JWK using following command:

```bash
docker run -i danedmunds/pem-to-jwk:1.2.1 --public --pretty < system-tests/resources/provider/public-key.pem > key.public.jwk
```

### Debugging MVD locally

Follow the instructions in the previous sections to run an MVD with a consumer and provider locally using docker-compose.

Once running, you can use a Java debugger to connect to the consumer (port 5006) and provider (port 5005) instances. If you are using IntelliJ you can use the provided "EDC consumer" or "EDC provider" [runtime configurations](../.run) to remote debug the connector instances.

### Issuing requests manually with Postman

A [postman collection](../deployment/data/MVD.postman_collection.json) can be used to issue requests to an MVD instance of your choice. You will need to adapt the environment variables accordingly to match your target MVD instance.
