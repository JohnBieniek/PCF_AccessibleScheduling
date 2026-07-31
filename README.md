Accessible Scheduling
============

Accessible Scheduling is a dynamic and robust nurse scheduling tool used to help provide in-home care to patients in need. This tool improves the experience of administrators and nurses alike by providing a suite of options that improve how shifts are generated, assigned, and communicated.

The tool can take in both common and uncommon information to allow for dynamic schedule generation and assignment. A client can request shifts on a daily, weekly, biweekly, every-X-weeks, monthly, or other recurring basis. They can also identify special needs, such as no smokers, no one with cat allergies, and more. This can include requirements such as ASL proficiency for deaf clients, special certifications for clients with disabilities, and other configurable needs.

Accessible Scheduling is built to accommodate the shifting regulatory landscape of the industry. Administrators can add any custom requirements they need for clients and any custom certifications for staff. This can allow them to request only staff from a certain area for a client, only allow staff with specific training to assist individuals with special needs, and add new requirements as they arise.

Staff can make dynamic and detailed requests of their employers as well. These can include minimum hours, maximum hours, the days and hours they are available, and more. They can request overtime, call off, list their credentials, set preferred clients, and manage other scheduling preferences.

This detailed set of data allows shifts to be generated quickly and accurately. Instead of manually creating shifts each day for requirements that occur every other week, every other day, or on schedules that vary from week to week, an administrator can generate all required shifts with the click of a button.

Shift assignment is easy as well. When assigning an individual shift, administrators can see who is most in need of hours, who has the required credentials, who would go into overtime by taking the shift, who has the tightest schedule and may have difficulty finding other shifts to meet their required hours, and more.

Scheduling goes beyond showing an administrator these needs on a shift-by-shift basis. All requirements can be considered with a single click. An entire month’s schedule can be assigned dynamically in a few minutes. This process goes through each client, matches them with valid staff, ensures those staff meet their minimum hours, works to keep them below their maximum hours, ensures they are scheduled only when available, and more.

The tool makes sure that every client need is met, from required certifications and local staffing to ensuring that assigned staff can reach the client and are not overworked. It even ensures that, when a staff member finishes one shift, they are not assigned to another shift across town one minute later.

Administrators have significant control over how shifts are scheduled. They can set a maximum number of shifts for any staff member per day and per week. They can choose to limit overtime to the minimum amount needed. They can also override staff inactivity and availability settings when there are not enough available individuals to meet client needs.

The tool not only generates and staffs shifts, but also supports communication between administrators and nurses. Administrators can choose when a month’s schedule is released. Once released, all staffing information is accurate to the minute for all employees. When a nurse receives a new shift or has to call off, the information is communicated to everyone involved within 60 seconds.

This makes it easy for administrators to respond quickly to call-offs and for nurses to see when they have been assigned additional work. Each staff member can view their own schedule to see when they work, where they work, and for how long. Team leads can see the schedules of all staff they manage, supporting communication and coordination between workers.

Accessible Scheduling makes shift generation and assignment only a click away. It does this through the intelligent use of modern algorithms to find the best fit for any combination of client and staff needs.

Under the hood, the tool uses a dynamic form of simulated annealing to schedule each shift one by one until all needs are met. It does this by first addressing the most difficult requirements, then moving toward simpler requests. In this context, shifts that require rare skill sets during hours when few staff are available are often scheduled first.

The system can also prioritize staff members who have very few open time slots but difficult-to-reach minimum-hour requirements based on their skill sets and the available shifts. From there, it moves toward more common skill sets, more accessible hours, and less-constrained staff.

It works to ensure that minimum-hour requirements are met for all staff while keeping nurses below their desired maximum hours. It helps prevent staff from being overworked by ensuring they do not work every day, receive endless back-to-back assignments, or work beyond what is reasonable and healthy. However, it still allows for overtime and for unavailable staff to be considered when there are not enough available individuals to meet the needs of the current client base.

With all these features, Accessible Scheduling has made staffing easier in areas where it was previously difficult, slow, tedious, and error-prone. It has improved the lives of staff by providing accurate schedules and updates earlier than they could have expected under manual assignment processes.

With minute-accurate schedules and the tools needed to respond to changes as they occur, Accessible Scheduling has been a game changer for those working in the field of in-home nursing.

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


