## GOODS & SHOPS
By [Khoroshiy Ilya](https://vk.com/id33313622) 

This is Restful WEB project, the service of people's monitoring of food prices,
without web interface, only api for works with Postman/Insomnia

1) Registration of users (administrators, users).
2) Editing the profile.
3) Directory of product categories, directory of outlets. Search and filtering.
4) Possibility to add/get/select a product. in different stores (at least two).
5) Ability to graphically display the dynamics of price changes.
6) Ability to batch add information about prices and products by downloading data in xlsx format.

### Prerequisites
You need the following installed and available in your $PATH:

* Java 14
* Apache maven 3.8.1 or greater

### Environment:

* Java 14
* Apache maven 3.8.1
* Apache POI 5.2.0
* Spring Boot 2.6.2 (data JPA, web, security, test)
* PostgreSQL
* Jsonwebtoken 0.9.1
* JUnit Vintage + Jupiter
* Hamcrest
* Liquibase
* Swagger
* IDE: Intellij idea

**NOTE:**
This project not have build with docker or executable archive, and worked in IDE.
Swagger documentation can be accessed by clicking on the link when the program is running - [http://localhost:8080/goods_and_shops/swagger-ui/index.html#/](http://localhost:8080/goods_and_shops/swagger-ui/index.html#/)
