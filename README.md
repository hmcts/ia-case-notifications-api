# ia-case-notifications-api
 
Immigration &amp; Asylum case notifications API

## Purpose

Immigration &amp; Asylum case notifications API is a Spring Boot based application to send notifications to Immigration & Asylum Appellants and Legal Representatives.

### Prerequisites

To run the project you will need to have the following installed:

* Java 17
* Docker (optional)

For information about the software versions used to build this API and a complete list of it's dependencies see build.gradle

The following environment variables are required when running the api without its dependencies mocked. This includes running the functional tests locally. The examples (the values below are not real):

| Environment Variable | *Example values*  |
|----------------------|----------|
| IA_GOV_NOTIFY_KEY | some-gov-notify-key |
| IA_BAIL_GOV_NOTIFY_KEY | some-gov-notify-key |
| IA_IDAM_CLIENT_ID  |  some-idam-client-id |
| IA_IDAM_SECRET  |  some-idam-secret |
| IA_IDAM_REDIRECT_URI  |  http://localhost:3451/oauth2redirect |
| IA_S2S_SECRET  |  some-s2s-secret |
| IA_S2S_MICROSERVICE  |  some-s2s-gateway |
| IA_ADMIN_NEWPORT_EMAIL | some-email |
| IA_ADMIN_TAYLOR_HOUSE_EMAIL | some-email |
| IA_ADMIN_HATTON_CROSS_EMAIL | some-email |
| IA_ADMIN_MANCHESTER_EMAIL | some-email |
| IA_ADMIN_GLASGOW_EMAIL | some-email |
| IA_ADMIN_BRADFORD_EMAIL | some-email |
| IA_ADMIN_BIRMINGHAM_EMAIL | some-email |
| IA_HEARING_CENTRE_BRADFORD_EMAIL |  some-email |
| IA_HEARING_CENTRE_MANCHESTER_EMAIL |  some-email |
| IA_HEARING_CENTRE_NEWPORT_EMAIL |  some-email |
| IA_HEARING_CENTRE_TAYLOR_HOUSE_EMAIL |  some-email |
| IA_HEARING_CENTRE_NORTH_SHIELDS_EMAIL |  some-email |
| IA_HEARING_CENTRE_BIRMINGHAM_EMAIL |  some-email |
| IA_HEARING_CENTRE_HATTON_CROSS_EMAIL |  some-email |
| IA_HEARING_CENTRE_GLASGOW_EMAIL |  some-email |
| IA_HOME_OFFICE_BRADFORD_EMAIL |  some-email |
| IA_HOME_OFFICE_MANCHESTER_EMAIL |  some-email |
| IA_HOME_OFFICE_NEWPORT_EMAIL |  some-email |
| IA_HOME_OFFICE_TAYLOR_HOUSE_EMAIL |  some-email |
| IA_HOME_OFFICE_NORTH_SHIELDS_EMAIL |  some-email |
| IA_HOME_OFFICE_BIRMINGHAM_EMAIL |  some-email |
| IA_HOME_OFFICE_HATTON_CROSS_EMAIL |  some-email |
| IA_HOME_OFFICE_GLASGOW_EMAIL |  some-email |
| IA_FTPA_HOME_OFFICE_BRADFORD_EMAIL |  some-email |
| IA_FTPA_HOME_OFFICE_TAYLOR_HOUSE_EMAIL |  some-email |
| IA_FTPA_HOME_OFFICE_NEWCASTLE_EMAIL |  some-email |
| IA_FTPA_HOME_OFFICE_HATTON_CROSS_EMAIL |  some-email |
| IA_RESPONDENT_NON_STANDARD_DIRECTION_UNTIL_LISTING_EMAIL |  some-email |
| IA_RESPONDENT_EVIDENCE_DIRECTION_EMAIL |  some-email |
| IA_RESPONDENT_REVIEW_DIRECTION_EMAIL |  some-email |
| IA_HOME_OFFICE_GOV_NOTIFY_ENABLED |  true/false |

### Running the application

To run the API quickly use the docker helper script as follows: (make sure to have set the required environment variables as above)

```
./bin/run-in-docker.sh --clean --install
```

Alternatively, you can start the application from the current source files using Gradle as follows:

```
./gradlew clean bootRun
```

### Using the application

To understand if the application is working, you can call it's health endpoint:

```
curl http://localhost:8093/health
```

If the API is running, you should see this response:

```
{"status":"UP"}
```

### Running verification tests:

You can run the *unit tests* and *integration tests* as follows:

```
./gradlew check
```

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *functional tests* as follows:

```
./gradlew functional
```

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *smoke tests* as follows:

```
./gradlew smoke
```

If you have some time to spare, you can run the *mutation tests* as follows:

```
./gradlew pitest
```

As the project grows, these tests will take longer and longer to execute but are useful indicators of the quality of the test suite.

More information about mutation testing can be found here:
http://pitest.org/ 


## Adding Git Conventions

### Include the git conventions.
* Make sure your git version is at least 2.9 using the `git --version` command
* Run the following command:
```
git config --local core.hooksPath .git-config/hooks
```
Once the above is done, you will be required to follow specific conventions for your commit messages and branch names.

If you violate a convention, the git error message will report clearly the convention you should follow and provide
additional information where necessary.

*Optional:*
* Install this plugin in Chrome: https://github.com/refined-github/refined-github

  It will automatically set the title for new PRs according to the first commit message, so you won't have to change it manually.

  Note that it will also alter other behaviours in GitHub. Hopefully these will also be improvements to you.


*In case of problems*

1. Get in touch with your Technical Lead and inform them, so they can adjust the git hooks accordingly
2. Instruct IntelliJ not to use Git Hooks for that commit or use git's `--no-verify` option if you are using the command-line
3. If the rare eventuality that the above is not possible, you can disable enforcement of conventions using the following command

   `git config --local --unset core.hooksPath`

   Still, you shouldn't be doing it so make sure you get in touch with a Technical Lead soon afterwards.
   
