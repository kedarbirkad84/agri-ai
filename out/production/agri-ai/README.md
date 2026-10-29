# AgriAI – AI & ML in Agriculture
Frontend: HTML/CSS/JS (web/) · Backend: Core Java + JDBC · Database: MySQL

## Setup
1. Install JDK 11+ and MySQL. Download MySQL Connector/J jar into this folder (e.g. mysql-connector-j-8.x.jar).
2. Create DB:  mysql -u root -p < sql/schema.sql
3. Edit DB_USER / DB_PASS in Server.java.
4. Compile:  javac Server.java
5. Run (Linux/Mac):  java -cp .:mysql-connector-j-8.x.jar Server
   Run (Windows):    java -cp .;mysql-connector-j-8.x.jar Server
6. Open http://localhost:8080

## Notes
- Disease detection matches crop + symptom against the `diseases` table. Photo upload is a preview only;
  true image-based detection needs a trained CNN model (e.g. PlantVillage dataset) – can be added later.
- Every search is saved in the `search_log` table.
