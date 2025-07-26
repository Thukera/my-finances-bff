# HEY! LISTEN! 

## It's dangerous to go alone! Take this!

## About the Project
This is a core project that can be used with Huge Solutions. 
It´s already with a JWT authentication build, that allows create a Auth Method with your Front-End Application with controlled sessions, or build a  Middleware that need to rotate Token frequently. 
Create and Manage Users and it´s roles.
User will choose a password, that will be encrypted with your own Private Key, and Hashed512 to the database. 
Sign user and generate a temporary tokens to access controlled services, with roles permissions. 
Rotate temporary token with designed parameter. 
Token is encrypted with your Private Key , and self-contained with Digital Signature for Backend Validation.  
Self-Contained Token includes Role, User and Expiration Time. 
Validation Occurs  each request, Spring doesn’t store anything — the token contains everything needed to trust the request (username, expiration, signature).
It scales well, is secure, and avoids session bloat.

## Running Environment
You must allow expose of endpoints on: com.thukera.config.WebSecurity.
Applications use Log4J and rotate logs while running on Java, or WebServices according with your YML configuration. 
For WebServices, manage your dependencies and develop yourself, adding property spring build configuration.
For build on containers, you can configure your preffers on current Dockerfile; 
Current Dockerfile is configured to run on render. 

### You can access exposed endpoints at:
http://{aplication-url}/swagger-ui/index.html

### Configuration Files
-src/main/resources/application.properties
-src/main/resources/config/log4j2.yml
-dockerfile


### Generate your own Private Key with 
on bash : openssl rand -base64 64

### Create Roles on Database
INSERT INTO public.tb_roles (id, "name") 
VALUES 
	(1, 'ROLE_USER'),
	(1, 'ROLE_ADMIN');


Hope you enjoy!


----------------------------------------------------------------------------------------------------------------------------

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.3/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.3/maven-plugin/build-image.html)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.