# aws-photosharing-example
This is a classic three-tier application written in Java 8 to easily upload and share photos, the application is just for demo purposes to show how scalable Java web applications can be build using Amazon Elastic Beanstalk.

## Building & Running the application

Using Maven, it is possible to build a WAR-package:

```sh
$ cd aws-photosharing-example
$ mvn -Dmaven.test.skip=true package
```

## Test the sample application

To run the unit-tests, run the following Maven-command:

```sh
$ cd aws-photosharing-example
$ mvn test
```

## Launching the app on AWS

To create an AWS Elastic Beanstalk app on AWS using this code:
- Create an Amazon VPC
- Create an Amazon RDS instance
- Create an Amazon ElastiCache instance
- Modify the application configuration in META-INF/Context.xml and in com.amazon.photosharing.enums.Configuration
- Build the application using `mvn package`
- Deploy the application in Amazon Elastic Beanstalk


## Release Notes
### Release 1.0.0 (March 27, 2017)
* Initial commit