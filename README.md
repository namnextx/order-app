# Scala REST API Application with Play Framework

### Technology Stack
- Scala 2.13
- Play Framework 2.8.19
- Slick 5.0.0 (DB Access/Evolutions)
- PostgreSQL
- Guice (DI)
- Silhouette (Authn/Authz)
- HTTPClient (Play WS)
- JSON Conversion (Play JSON)
- Logging (Logback)

### Project Structure
```
── scala-application
   ├── app                                            # The Scala application source code
   │   ├── utils
   │   │   └── auth                                   # Authentication utils
   │   ├── domain
   │   │   ├── tables                                 # Slick tables
   │   │   │   ├── OrderDetailTable.scala             # Represents order_details table
   │   │   │   └── UserTable.scala                    # Represents users table
   │   │   │   └── ProductTable.scala                 # Represents products table
   │   │   │   └── OrderTable.scala                   # Represents orders table
   │   │   ├── models                                 # Contains UserService with its implementation
   │   │   │   ├── OrderDetails.scala                 # OrderDetail model
   │   │   │   └── Order.scala                        # Order model
   │   │   │   └── Product.scala                      # Product model
   │   │   │   └── User.scala                         # User model
   │   │   └── daos
   │   │       ├── DaoRunner.scala                    # Run Slick database actions by transactions
   │   │       ├── DbExecutionContext.scala           # Custom ExecutionContext for running DB connections
   │   │       ├── PasswordInfoDao.scala              # Password dao
   │   │       ├── OrderDao.scala                     # Order dao
   │   │       └── UserDao.scala                      # UserDao dao
   │   │       └── OrderDetailDao.scala               # OrderDetailDao dao
   │   │       └── ProductDao.scala                   # ProductDao dao
   │   ├── system                                     # Play modules
   │   │   └── modules
   │   │       ├── AppModule.scala                    # Bind all application components (Same as Spring @Configuration)
   │   │       └── SilhouetteModule.scala             # Bind silhouette components
   │   └── controllers                                # Application controllers
   │       ├── auth                                   
   │       │   ├── AuthController.scala               # SignUp/SignIn controllers
   │       │   ├── SilhouetteController.scala         # Abstract silhouette controller
   │       │   ├── UnsecuredResourceController.scala  # Example of a un-secured endpoint
   │       └── order                                   
   │       │   ├── OrderController.scala              # Order controllers for CRUD an order
   │       │   ├── OrderControllerComponents.scala    # Order Controller components
   │       │   ├── OrderResource.scala                # Request/Response Order dto
   │       │   └── OrderRouter.scala                  # Order endpoints routing
   │       └── product                                   
   │       │   ├── ProductController.scala            # Product controllers for CRUD a Product
   │       │   ├── ProductControllerComponents.scala  # Product Controller components
   │       │   ├── ProductResource.scala              # Request/Response Product dto
   │       │   └── ProductRouter.scala                # Product endpoints routing
   │       └── user                                   
   │           ├── UserController.scala               # User controllers for CRUD an User
   │           ├── UserControllerComponents.scala     # User Controller components
   │           ├── UserResource.scala                 # Request/Response User dto
   │           └── UserRouter.scala                   # User endpoints routing
   ├── test
   ├── conf
   │   ├── messages                               # Error Messages for messages API
   │   ├── evolutions                             # Play evolutions SQL queries
   │   │   └── default                            # Default database
   │   │       ├── 1.sql                          # Creates schema
   │   │       └── 2.sql                          # Creates db tables
   │   ├── application.conf                       # Play configuration
   │   ├── routes                                 # Play routing
   │   ├── db.conf                                # Database configuration
   │   └── silhouette.conf                        # Silhouette configuration
   ├── project
   ├── build.sbt
   └── target
```

### Getting Started

#### 1. Setup `PostgreSQL` Database
You can install PostgreSQL on your local machine or running the docker compose in the `/docker/database` folder
to get PostgreSQL ready.

#### 2. Run application 
You need to download and install sbt for this application to run.
_Note: I've added the `SBT bin` to this project to build the source code without SBT installation_
Once you have sbt installed, the following at the command prompt will start up Play in development mode:
```bash
./sbt run
```

Play will start up on the HTTP port at <http://localhost:8080/>.   You don't need to deploy or reload anything -- changing any source code while the server is running will automatically recompile and hot-reload the application on the next HTTP request.

#### 3. Run Unit Tests
```bash
./sbt clean test
```

or To generate code coverage report with SCoverage
```bash
./sbt clean coverage test coverageReport
```

#### 4. Run Integration Tests
```bash
./sbt clean integration/test
```

### Usage
_Ref: Postman collection at `postman` folder_

1. Create an User with role Admin by using `POST /SignUp` endpoint
2. You can access the `GET /Unsecured` endpoint without login
3. Using `POST- /SignIn` endpoint to login with newly created user to get JWT token in `X-Auth` response header
4. Get All existing users via `GET /v1/users` endpoint -> empty list returned at first (Admin)
5. Get an existing users via `GET /v1/users/id` endpoint -> empty list returned at first  (Admin)
6. Delete an existing user via `DELETE /v1/users/id` endpoint (Admin)
7. Create new user via `POST /v1/users` endpoint (Admin) (Admin)
8. Create a new Product by using `POST /v1/products` endpoint "Admin", "Operator"
9. Get All Products via `GET /v1/products` endpoint again -> Only one created Post shown ("Admin", "Operator", "User")
10. Get single Product via `GET /v1/products/:id` ("Admin", "Operator", "User")
11. Delete existing Product via `DELETE /v1/products/:id` endpoint ("Admin", "Operator")
12. Update exiting Product Via `PUT /v1/products/id` endpoint ("Admin", "Operator")
13. Get external Products Via `GET /v1/external/products` endpoint ("Admin", "Operator")
14. Create a new Order by using `POST /v1/orders` endpoint "Admin", "User"
15. Get All Order via `GET /v1/orders` endpoint again -> Only one created Post shown "Admin", "User"
16. Get single Order via `GET /v1/orders/:id` ("Admin", "Operator", "User")
17. Delete existing Order via `DELETE /v1/orders/:id` endpoint "Admin", "User"
18. Update exiting Order Via `PUT /v1/orders/id` endpoint "Admin", "User"
