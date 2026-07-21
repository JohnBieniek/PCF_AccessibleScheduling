Accessible Scheduling
============

Accessible scheduling is a dynamic and robust nurse scheduling tool used to help provide in home care to patients in need. This tool improves the experience of administrators and nurses alike by providing a suite of options to improve how shifts are generated, assigned, and communicated. This tool has the ability to take in common and uncommon information to allow for dynamic schedule generation and assignment. A client has the ability to ask for shifts on a daily, weekly, bi-weekly, every x week of the month, monthly basis and more. They have the ability to denote special needs like no smokers, no one with cat allergies, and more. This can include things like requiring ASL for the deaf, special certifications for the disabled, and other configurable requirements as needed. AS is built to accomdate the shifting regulatory landscape of the industry. Administrators can add in any custom requirements they need to clients and any custom certifcations to staff. This can allow you to request only staff from a certain area for a client, only allow staff with a certain training to help individuals with special needs, and require any new form of requirement that comes up as it's needed. 
Staff have the ability to make very dynamic and robust requests from their employers as well. This can include minimum hours, maximum hours, what days and hours they are avaible, and more. They can request overtime, call off, note their credentials, set prefered clients, and more. 
This robust set of data allows for shifts to be generated quickly and accurately. Instead of trying to set shifts each day that's needed for several every other week requirements, every other day requirements, and more that can vary week by week an administrator can generate shifts for all of the above with a click of a button. 

Shift assignment is easy as well. When assigning an individual shift administrators can see who is in most need of shifts, who has the credentials required, who whould go into overtime by taking the shift, who has the tightest schedule and would have a hard time finding other shifts to meet their needed hours and more. However, scheduling is even more robust than showing an admin needs on a per shift basis. All needs can be taken into account with a single click. An entire months schedule can be assigned dynamicly in a few minutes. This process will go through each client, match them with valid staff, ensure those staff meet their minimum hours, work to keep them below their maximum hours, ensure they are only scheduled when available and more. This tool makes sure that every need of the client is met from certifications, to finding people local to them, to ensuring those staff can reach them and aren't over worked. This goes so far as to ensure that if a staff member gets off work they won't be assigned to another shift across town a minute later. 

THe admin has great control over how shifts are scheduled. They can ensure that there is a maximum number of shifts assigned to any staff member per day and per week. They can choose to ensure overtime is limited to the least amount needed. They can also override staff inactivity and unavailable staff to try to find staffing when there are not enough individuals present to meet the needs of clients. 

This tool not only works to generate and staff shifts, but it also works to facilitate communication between administrators and nurses. This tool allows admins to choose when a months schedule is realeased. Once it is all staffing needs are accurate to the minute for all employees. If a nurse gets a new shift, or if a nurse has to call off the information is communicated to all involved within 60 seconds. This makes it easy for administrators to quickly respond to call offs and for nurses to quickly see if they've been assigned additional work. 
Each staff member has the ability to view thier own schedule to see when they work, where, and for how long. Team leads can see the schedules of all staff they manage to facilitate understanding and communication between workers. 

This tool makes shift generation and assignment only a click away. It does this through the inteligent use of modern algorithims to find the best fit for any given combination of client and staff needs. Under the hood this tool uses a dynamic form of simulated annealing to schedule off each shift one by one until all needs are met. It does this by first working to meet the most difficult needs, then moving toward more simple asks. In this context this means that shfits that require rare skillsets during hours where few staff are available are often scheduled first. It can also work to meet the needs of staff who have very few open timeslots but have difficult to reach minimum hour requirements given their skillsets and the available shifts. From there it will move toward more common skillsets, more accessible hours, and less constrained staff. It works to ensure that all minimum hours are met for all staff while keeping under the max desired hours for all nurses. It acts to not overwork staff, it ensures they don't work everyday, endlessly back to back, and beyond what is reasonable and healthy. However, it does allow for overtime, and for staff to be requested when there are simply not enough available individuals to meet the needs of a current set of clients.

With all these features Accessible Scheduling has made staffing easy in areas where it used to be difficult, slow, tedious, and error prone. It's improved the lives of staff by getting them accurate schedules and updates earlier than they could have ever expected under manual assignment plans. With minute accurate schedules adn the tools needed to deal with changes as they come Accessible scheduling has been a game changer for those working hard in the field of in home nursing. 

This is an application using database services on [Cloud Foundry](http://cloudfoundry.org) with the [Spring Framework](http://spring.io).

This application has been built to store domain objects in a key-value stores. 

The application use Spring Java configuration and [bean profiles](https://spring.io/blog/2011/02/14/spring-3-1-m1-introducing-profile/) to configure the application and the connection objects needed to use the persistence stores. It also uses the [Spring Cloud Connectors](http://cloud.spring.io/spring-cloud-connectors/) library to inspect the environment when running on Cloud Foundry. See the [Cloud Foundry documentation](http://docs.cloudfoundry.org/buildpacks/java/spring-service-bindings.html) for details on configuring a Spring application for Cloud Foundry.

## Running the application locally

One Spring bean profile should be activated to choose the database provider that the application should use. The profile is selected by setting the system property `spring.profiles.active` when starting the app.

The application can be started locally using the following command:

~~~
$ ./gradlew tomcatRun -Dspring.profiles.active=<profile>
~~~

where `<profile>` is one of the following values:

* `in-memory` (no external database required)
* `mongodb`

If no profile is provided, `in-memory` will be used. If any other profile is provided, the appropriate database server
must be started separately. The application will use the host name `localhost` and the default port to connect to the database.

If more than one of these profiles is provided, the application will throw an exception and fail to start.

## Running the application on Cloud Foundry

When running on Cloud Foundry, the application will detect the type of database service bound to the application
(if any). If a service of one of the supported types (MySQL, Postgres, Oracle, MongoDB, or Redis) is bound to the app, the
appropriate Spring profile will be configured to use the database service. The connection strings and credentials
needed to use the service will be extracted from the Cloud Foundry environment.

If no bound services are found containing any of these values in the name, then the `in-memory` profile will be used.

If more than one service containing any of these values is bound to the application, the application will throw an
exception and fail to start.

After installing the 'cf' [command-line interface for Cloud Foundry](http://docs.cloudfoundry.org/cf-cli/), targeting a Cloud Foundry instance, and logging in, the application can be built and pushed using these commands:

~~~
$ ./gradlew clean assemble test --rereun-tasks

$cf login -a api.run.pivotal.io

email
pass

$ cf push
or
$ update
~~~

The application will be pushed using settings in the provided `manifest.yml` file. The output from the command will show the URL that has been assigned to the application.

### Creating and binding services

Using the provided manifest, the application will be created without an external database (in the `in-memory` profile).
You can create and bind database services to the application using the information below.

#### System-managed services

Depending on the Cloud Foundry service provider, persistence services might be offered and managed by the platform. These
steps can be used to create and bind a service that is managed by the platform:

~~~
# view the services available
$ cf marketplace
# create a service instance
$ cf create-service <service> <service plan> <service name>
# bind the service instance to the application
$ cf bind-service <app name> <service name>
# restart the application so the new service is detected
$ cf restart
~~~

#### User-provided services

Cloud Foundry also allows service connection information and credentials to be provided by a user. In order for the
application to detect and connect to a user-provided service, a single `uri` field should be given in the credentials
using the form `<dbtype>://<username>:<password>@<hostname>:<port>/<databasename>`.

These steps use examples for username, password, host name, and database name that should be replaced with real values.

~~~
# create a user-provided Oracle database service instance
$ cf create-user-provided-service oracle-db -p '{"uri":"oracle://root:secret@dbserver.example.com:1521/mydatabase"}'
# create a user-provided MySQL database service instance
$ cf create-user-provided-service mysql-db -p '{"uri":"mysql://root:secret@dbserver.example.com:3306/mydatabase"}'
# bind a service instance to the application
$ cf bind-service <app name> <service name>
# restart the application so the new service is detected
$ cf restart
~~~

#### Changing bound services

To test the application with different services, you can simply stop the app, unbind a service, bind a different
database service, and start the app:

~~~
$ cf unbind-service <app name> <service name>
$ cf bind-service <app name> <service name>
$ cf restart
~~~

#### Database drivers

Database drivers for MySQL, Postgres, MongoDB, and Redis are included in the project. To connect to an Oracle database,
you will need to download the appropriate driver (e.g. from http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html?ssSourceSiteId=otnjp),
add the driver .jar file to the `src/main/webapp/WEB-INF/lib` directory in the project, and re-build the
application .war file using `./gradlew assemble`.


