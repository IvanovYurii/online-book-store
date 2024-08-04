<h1 align="center" style="display: block; font-size: 2.5em; font-weight: bold; margin-block-start: 1em; margin-block-end: 1em;">
<picture>
          <img src="images/online-book-store-picture.png" alt="BookWorld Logo">
</picture>
  <br /><br /><strong>BookWorld: Online Book Store</strong>
</h1>
<p align="center" style="text-decoration: none">
  Help & Feedback
  <br />
  <br />
  <a href="https://github.com/IvanovYurii/online-book-store/issues/new?assignees=&labels=bug&template=01_BUG_REPORT.md&title=bug%3A+">Report a Bug</a>
  <>
  <a href="https://github.com/IvanovYurii/online-book-store/issues/new?assignees=&labels=enhancement&template=FEATURE_REQUEST.md&title=feat%3A+">Request a Feature</a>
  <>
  <a href="https://github.com/IvanovYurii/online-book-store/discussions">Ask a Question</a>
</p>

<div id="table-of-contents" style="font-size: 16px;">

---

## <picture><img alt="Table of contents icon" src="images/table-of-contents-icon.png" title="Table of contents" style="vertical-align: middle;"></picture> Table of contents
- [Inspiration for the Project](#inspiration-for-the-project)
- [The problem that the Project solves](#the-problem-that-the-project-solves)
- [Advantages of the Project](#advantages-of-the-project)
- [Technologies used](#technologies-used)
- [Clone sources](#clone-sources)
- [Build and launch the Project](#build-and-launch-the-project)
  - [Build with `cmake`](#build-with-cmake)
  - [Build with `msbuild`](#build-with-msbuild)
  - [Build with WSL](#build-with-wsl)
  - [Build with IDEs](#build-with-ides)
    - [Build with Microsoft Visual Studio](#build-with-microsoft-visual-studio)
    - [Build with Visual Studio Code](#build-with-visual-studio-code)
- [Entities](#entities)
  - [User](#user-entity)
  - [Role](#role-entity)
  - [Book](#book-entity)
  - [Category](#category-entity)
  - [ShoppingCart](#shopping-cart-entity)
  - [CartItem](#cart-item-entity)
  - [Order](#order-entity)
  - [OrderItem](#order-item-entity)
- [Database structure](#database-structure)
- [Examples](#examples)
- [License](#license)
- [Call to action](#call-to-action)
- [Thank you all!](#thank-you-all)
</div>
<div id="inspiration-for-the-project" style="font-size: 16px;">

---

## <picture><img alt="Inspiration for the Project icon" src="images/inspiration-for-the-project-icon.png" style="vertical-align: middle;"></picture> Inspiration for the Project
Creating a convenient and efficient way to purchase books in the online store was inspired by the desire to facilitate the process of choosing 
and purchasing books for users. The vision was to develop a platform that would allow users to easily search for books by various criteria, 
conveniently make purchases, and track their statuses. Against the background of the growing popularity of online shopping, especially in the 
context of the pandemic, it was important to provide an alternative to traditional bookstores and make the process of book purchases more 
convenient and accessible.
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>
</div>
<div id="the-problem-that-the-project-solves" style="font-size: 16px;">

---

## <picture><img alt="The problem that the Project solves icon" src="images/the-problem-that-the-project-solves-icon.png" style="vertical-align: middle;"></picture> The problem that the Project solves
The main problem that the project aims to solve is to create a convenient and simple way to purchase books in an online format. 
Traditional bookstores can be limited in selection and availability, while this project allows users to easily find and purchase books directly 
from their home computer or mobile device. In addition, the project solves the problem of the lack of a convenient tool for managing orders and 
shopping carts, which makes the shopping process more transparent and efficient for users.
<div style="text-align: right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>
<div id="advantages-of-the-project" style="font-size: 16px;"></div>
</div>

---

## <picture><img alt="Advantages of the Project icon" src="images/advantages-of-the-project-icon.png" style="vertical-align: middle;"></picture> Advantages of the Project
* Convenience: Users can easily search for books and make purchases online without having to visit a physical store.
* Wide Selection: The platform offers a wide range of books by different authors and genres, meeting the needs of various readers.
* Convenient Order Management: Built-in services allow users to easily add books to the cart, place orders, and track their status.
* Efficiency and Accessibility: The project makes the process of purchasing books more efficient and accessible, reducing barriers to access to literary works.
* Integration with Other Applications: The application can be easily integrated with other websites or applications, expanding its functionality and user reach.
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>
<div id="technologies-used" style="font-size: 16px;">

---

## <picture><img alt="Technologies used icon" src="images/technologies-used-icon.png" style="vertical-align: middle;"></picture> Technologies used
* <picture><img src="images/java-icon.png"></picture> Java 17+
* <picture><img src="images/maven-icon.png"></picture> Maven
* <picture><img src="images/springboot-icon.png"></picture> Spring Boot
* <picture><img src="images/springdata-icon.png"></picture> Spring Data JPA
* <picture><img src="images/springsescurity-icon.png"></picture> Spring Boot Security
* <picture><img src="images/jwt-icon.png"></picture> JSON Web Token
* <picture><img src="images/lombok-icon.png"></picture> Lombok
* <picture><img src="images/mapstruct-icon.png"></picture> MapStruct
* <picture><img src="images/liquibase-icon.png"></picture> Liquibase
* <picture><img src="images/mysql-icon.png"></picture> MySql 8
* <picture><img src="images/hibernate-icon.png"></picture> Hibernate
* <picture><img src="images/junit-icon.png"></picture> JUnit5
* <picture><img src="images/testcontainers-icon.png"></picture> Testcontainers
* <picture><img src="images/docker-icon.png"></picture> Docker
* <picture><img src="images/swagger-icon.png"></picture> Swagger
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>
</div>
<div id="clone-sources" style="font-size: 16px;">

---

## <picture><img alt="Clone sources icon" src="images/clone-icon.png" style="vertical-align: middle;"></picture> Clone sources
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>
</div>
<div id="build-and-launch-the-project" style="font-size: 16px;">

---

## <picture><img alt="Build and launch the Project icon" src="images/build-and-launch-the-project-icon.png" style="vertical-align: middle;"></picture> Build and launch the Project
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>
</div>
<div id="entities" style="font-size: 16px;">

---

## <picture><img alt="Entities icon" src="images/entity-icon.png" style="vertical-align: middle;"></picture> Entities
<div id="user-entity"></div>

### <picture><img alt="User icon" src="images/user-icon.png" style="vertical-align: middle;"></picture> User
<details><summary title="Click to show/hide details">Click to show/hide <strong>User entity</strong>.</summary><br/>
<blockquote>
The user entity represents information about registered users who have their own shopping cart with which they can manipulate,
add and delete selected books and then proceed to the order processing stage.
</blockquote>
</details>
<div id="role-entity"></div>

### <picture><img alt="Role icon" src="images/role-icon.png" style="vertical-align: middle;"></picture> Role
<details><summary title="Click to show/hide details">Click to show/hide <strong>Role entity</strong>.</summary><br/>
<blockquote>
The role determines the user's level of access to the system. Each user is assigned a specific role. There are currently
two types of roles: USER and ADMINISTRATOR. The USER can view and edit their own orders and selected books, create orders,
and the ADMINISTRATOR has the ability to add, delete and update data.
</blockquote>
</details>
<div id="book-entity"></div>

### <picture><img alt="Book icon" src="images/book-icon.png" style="vertical-align: middle;"></picture> Book
<details><summary title="Click to show/hide details">Click to show/hide <strong>Book entity</strong>.</summary><br/>
<blockquote>
The purpose of the book is to provide a comprehensive description of the book available on the service.
</blockquote>
</details>
<div id="category-entity"></div>

### <picture><img alt="Category icon" src="images/category-icon.png" style="vertical-align: middle;"></picture> Category
<details><summary title="Click to show/hide details">Click to show/hide <strong>Category entity</strong>.</summary><br/>
<blockquote>
The core of the category enables you to classify books into different groups and enhances their searchability.
</blockquote>
</details>
<div id="shopping-cart-entity"></div>

### <picture><img alt="ShoppingCart icon" src="images/shopping-cart-icon.png" style="vertical-align: middle;"></picture> ShoppingCart
<details><summary title="Click to show/hide details">Click to show/hide <strong>ShoppingCart entity</strong>.</summary><br/>
<blockquote>
The ShoppingCart for a user is a space where the books the user has selected for purchase are kept.
</blockquote>
</details>
<div id="cart-item-entity"></div>

### <picture><img alt="CartItem icon" src="images/item-icon.png" style="vertical-align: middle;"></picture> CartItem
<details><summary title="Click to show/hide details">Click to show/hide <strong>CartItem entity</strong>.</summary><br/>
<blockquote>
A CartItem entity represents an individual item in the cart, containing specific information about a book.
</blockquote>
</details>
<div id="order-entity"></div>

### <picture><img alt="Order icon" src="images/order-icon.png" style="vertical-align: middle;"></picture> Order
<details><summary title="Click to show/hide details">Click to show/hide <strong>Order entity</strong>.</summary><br/>
<blockquote>
The Order entity represents details about a user's order, including the time and delivery address, and contains the order items.
</blockquote>
</details>
<div id="order-item-entity"></div>

### <picture><img alt="OrderItem icon" src="images/item-icon.png" style="vertical-align: middle;"></picture> OrderItem
<details><summary title="Click to show/hide details">Click to show/hide <strong>OrderItem entity</strong>.</summary><br/>
<blockquote>
An OrderItem entity represents information about a book and is included within the order.
</blockquote>
</details>
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>
</div>

---

<div id="database-structure"></div>

## <picture><img alt="Database structure icon" src="images/database-structure-icon.png" style="vertical-align: middle;"></picture> Database structure
  
<picture><img src="images/scheme-picture.png"></picture>
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>

---

<details>
  <summary>Click to show/hide video: clone Project in IntelliJ IDEA Ultimate</summary>

  [Clone Project in IntelliJ IDEA Ultimate](https://github.com/user-attachments/assets/75445a6d-c10e-4fb6-9254-3be1fcaf3e5f)

</details>
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>
