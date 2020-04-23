# Authentication Service
REST service responsible for the management of users credentials and token refreshing.

## Service Dependencies
- MySQL

# Running, Testing, Building
```bash
# make sure MySQL & S3 are running.
sbt run
sbt test

# generate a new artifact & docker image
sbt docker:publishLocal
```
More information on the test and build steps in the [CI config](./.github/scala.yml).

# Configuration
Default configuration values can be found in the [config file](./src/main/resources/application.conf). Configuration values are overridden using environment variables.

For example, `application.conf` features the following property:
```
server.port=8083
server.port=${?SERVER_PORT}
```

The `${?FOO}` syntax will override the default value `8083` if a system or environment variable is present with the key `SERVER_PORT`.