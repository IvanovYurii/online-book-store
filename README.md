<h1 align="center" style="display: block; font-size: 2.5em; font-weight: bold; margin-block-start: 1em; margin-block-end: 1em;">
<picture>
          <img src="images/online-book-store-picture.png" alt="BookWorld Logo">
</picture>
  <br /><br /><strong>BookWorld: Online Book Store</strong>
</h1>

<div align="center">
  Help & Feedback
  <br />
  <br />
  <a href="https://github.com/IvanovYurii/online-book-store/issues/new?assignees=&labels=bug&template=01_BUG_REPORT.md&title=bug%3A+">Report a Bug</a>
  ·
  <a href="https://github.com/IvanovYurii/online-book-store/issues/new?assignees=&labels=enhancement&template=FEATURE_REQUEST.md&title=feat%3A+">Request a Feature</a>
  .
  <a href="https://github.com/IvanovYurii/online-book-store/discussions">Ask a Question</a>
</div>

---

<img id="table-of-contents" alt="Table of contents icon" src="images/table-of-contents-icon.png" title="Table of contents"/>
<h2 style="display: inline; vertical-align: middle;">Table of contents</h2>

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
- [Integration](#integration)
  - [Start a Project](#start-a-project)
  - [Create Services](#create-services)
  - [Configure Multicast Router](#configure-multicast-router)
  - [Configure Logging](#configure-logging)
- [Database structure](#database-structure)
- [Use cases and benefits](#use-cases-and-benefits)
- [Examples](#examples)
- [License](#license)
- [Call to action](#call-to-action)
- [Thank you all!](#thank-you-all)

---

<picture id="inspiration-for-the-project">
    <img src="images/inspiration-for-the-project-icon.png" alt="Inspiration for the Project icon" />
</picture>
<h2 style="display: inline; vertical-align: middle;">Inspiration for the Project</h2>

Creating a convenient and efficient way to purchase books in the online store was inspired by the desire to facilitate the process of choosing 
and purchasing books for users. The vision was to develop a platform that would allow users to easily search for books by various criteria, 
conveniently make purchases, and track their statuses. Against the background of the growing popularity of online shopping, especially in the 
context of the pandemic, it was important to provide an alternative to traditional bookstores and make the process of book purchases more 
convenient and accessible.
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>

---

<picture>
  <img src="images/the-problem-that-the-project-solves-icon.png" alt="The problem that the Project solves icon" style="vertical-align: middle;">
</picture>
<h2 id="the-problem-that-the-project-solves" style="display: inline; vertical-align: middle; margin: 0;">The problem that the Project solves</h2>

The main problem that the project aims to solve is to create a convenient and simple way to purchase books in an online format. 
Traditional bookstores can be limited in selection and availability, while this project allows users to easily find and purchase books directly 
from their home computer or mobile device. In addition, the project solves the problem of the lack of a convenient tool for managing orders and 
shopping carts, which makes the shopping process more transparent and efficient for users.
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>

---

<picture style="display: inline-block; vertical-align: middle;">
  <img src="images/advantages-of-the-project-icon.png" alt="Advantages of the Project icon" style="vertical-align: middle;">
</picture>
<h2 id="advantages-of-the-project" style="display: inline; vertical-align: middle; margin: 0;">Advantages of the Project</h2>

* Convenience: Users can easily search for books and make purchases online without having to visit a physical store.
* Wide Selection: The platform offers a wide range of books by different authors and genres, meeting the needs of various readers.
* Convenient Order Management: Built-in services allow users to easily add books to the cart, place orders, and track their status.
* Efficiency and Accessibility: The project makes the process of purchasing books more efficient and accessible, reducing barriers to access to literary works.
* Integration with Other Applications: The application can be easily integrated with other websites or applications, expanding its functionality and user reach.
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>

---

<picture style="display: inline-block; vertical-align: middle;">
  <img src="images/technologies-used-icon.png" alt="Technologies used icon" style="vertical-align: middle;">
</picture>
<h1 style="margin: 0; padding: 0; height: 60px; visibility: hidden;">&nbsp;</h1> <!-- Невидимий заголовок для простору -->
<h2 id="technologies-used" style="display: inline; vertical-align: middle; margin: 0;">Technologies used</h2>

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

---

<picture style="display: inline-block; vertical-align: middle;">
  <img src="images/clone-icon.png" alt="Clone sources icon" style="vertical-align: middle;">
</picture>
<h2 id="clone-sources" style="display: inline; vertical-align: middle; margin: 0;">Clone sources</h2>
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>

---

<picture style="display: inline-block; vertical-align: middle;">
  <img src="images/build-and-launch-the-project-icon.png" alt="Build and launch the Project icon" style="vertical-align: middle;">
</picture>
<h2 id="build-and-launch-the-project" style="display: inline; vertical-align: middle; margin: 0;">Build and launch the Project</h2>
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>

---

<picture style="display: inline-block; vertical-align: middle;">
  <img src="images/database-structure-icon.png" alt="Database structure icon" style="vertical-align: middle;">
</picture>
<h2 id="database-structure" style="display: inline; vertical-align: middle; margin: 0;">Database structure</h2>
  
<picture><img src="images/scheme-picture.png"></picture>
<div align="right">[ <a href="#table-of-contents">↑ Back to top ↑</a> ]</div>

---

<details>
  <summary>Click to show/hide video: clone Project in IntelliJ IDEA Ultimate</summary>

  [Clone Project in IntelliJ IDEA Ultimate](https://github.com/user-attachments/assets/75445a6d-c10e-4fb6-9254-3be1fcaf3e5f)

</details>
