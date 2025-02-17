Amazon Dashboard
This repository contains a simple Amazon-like Dashboard project, showcasing:

Frontend (HTML/CSS/JavaScript) – displays Products, Categories, and Transactions in tables, with filtering by TxID.
Backend (Spark Java) – provides RESTful endpoints for retrieving and filtering data from a MySQL database.

Table of Contents
Overview
Technologies Used
Project Structure
Setup & Installation
Running the Application
Endpoints
Contributing
License
Overview
Amazon Dashboard is a simple demo app that retrieves product, category, and transaction information from a MySQL database via Spark Java. The frontend displays the data in a user-friendly interface, enabling:

Filtering products, categories, or transactions by TxID (or other fields).
Navigation between Products, Categories, and Transactions sections.
Home icons to jump back to the top of the page.
Use it as a starter or reference for integrating a lightweight Java backend (Spark) with a simple HTML/JS frontend.

Technologies Used
Front End:
HTML, CSS, JavaScript
Back End:
Spark Java
MySQL as the database
Gson for JSON serialization
Build/IDE Tools:
IntelliJ IDEA (for Java Spark)
VS Code (for frontend)
Project Structure
A common structure might look like this:

css
Copy
Edit
amazon-dashboard/
  ┣━ frontend/
  │    ┣━ index.html
  │    ┣━ style.css
  │    ┗━ script.js
  ┗━ backend/
       ┣━ src/
       ┃    ┗━ main/
       ┃         ┗━ java/
       ┃              ┗━ ultimateQA/
       ┃                   ┣━ MySQLServer.java
       ┃                   ┗━ ...
       ┗━ pom.xml (or build.gradle)
frontend/: Contains your HTML/CSS/JS for the user interface.
backend/: Contains your Spark Java code (e.g., MySQLServer.java).
In some setups, they may live in one directory without subfolders, or in two separate repos.

Setup & Installation
Clone the Repository

bash
Copy
Edit
git clone https://github.com/<YourUsername>/AmazonDashboard.git
cd AmazonDashboard
MySQL Database

Ensure you have a running MySQL instance.
Create a database (e.g., dbproduct) and add relevant tables (product, category, transaction).
Backend Configuration

Open MySQLServer.java (or your main class).
Update the DB_URL, USER, and PASSWORD to match your local MySQL credentials.
(Optional) Adjust port from 8080 to your preference.
Frontend Configuration

Typically, no special config is needed for simple HTML/JS.
Make sure API_BASE_URL in index.html or script.js matches the Spark server URL (e.g. http://localhost:8080/api).
Running the Application
Start the Spark Server

In IntelliJ (or your IDE), run the main class (e.g., MySQLServer.java).
You should see something like:
csharp
Copy
Edit
Spark server is running on http://localhost:8080 ...
Open the Frontend

You can open index.html (inside frontend/) in your browser directly (e.g., double-click or run a local server).
The tables and dropdowns will fetch data from http://localhost:8080/api/....
Test the Filters

For example, choose a TxID in the dropdown to filter products, categories, or transactions.
Endpoints
Below is an example of the main endpoints. Adjust if your code differs:

Product

GET /api/product – Returns all products.
GET /api/product/filter?txid=xxx – Returns products filtered by TxID (and optionally by title or store if configured).
Category

GET /api/category – Returns all categories.
GET /api/category/filter?txid=xxx – Returns categories filtered by TxID, or partial match on another column (e.g., category).
Transaction

GET /api/transaction – Returns all transactions.
GET /api/transaction/filter?txid=xxx – Returns transactions filtered by TxID (and possibly partial match on status).
Contributing
Fork the repository on GitHub.
Create a feature branch (git checkout -b feature/new-stuff).
Commit your changes (git commit -am 'Add some new feature').
Push to the branch (git push origin feature/new-stuff).
Open a pull request.
License
This project is licensed under the MIT License – feel free to modify and distribute as you see fit. If you choose another license, update the details here.

Additional Tips
Use a .gitignore to exclude IDE files and build artifacts:
gitignore
Copy
Edit
.idea/
target/
*.iml
*.class
If you want to integrate the front end with a local dev server (e.g. Live Server extension in VS Code), just ensure the calls still point to http://localhost:8080/api.