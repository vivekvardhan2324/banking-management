# Banking Management

A Java 17 console application for customer sign-up/sign-in and bank account CRUD. Deposits and withdrawals update the account and write a transaction record in one JDBC transaction, so a failed operation is rolled back.

## Requirements

- Java 17+
- Maven
- MySQL 8+

Create the database once:

```sql
CREATE DATABASE banking_management;
```

Configure the connection (defaults: URL `jdbc:mysql://localhost:3306/banking_management`, user `root`, empty password):

```sh
export MYSQL_URL='jdbc:mysql://localhost:3306/banking_management'
export MYSQL_USER='root'
export MYSQL_PASSWORD='your-password'
```

The configured MySQL user needs permission to create and modify tables. The application creates its tables on startup.

## Run

```sh
mvn compile exec:java
```

Choose **Sign up** to create login credentials, then sign in. After login, use the account menu to create, list, search, deposit to, withdraw from, or delete your accounts. Account data is scoped to the signed-in user.

Deposits must be greater than zero and at most 50,000. Withdrawals must be at least 100 and no greater than the available balance.
