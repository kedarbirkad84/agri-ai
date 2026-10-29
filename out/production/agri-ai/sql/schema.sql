CREATE DATABASE IF NOT EXISTS agri_ai;
USE agri_ai;

CREATE TABLE IF NOT EXISTS crops (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50), soil_type VARCHAR(20),
  min_ph DOUBLE, max_ph DOUBLE, min_rain INT, max_rain INT,
  min_temp INT, max_temp INT, season VARCHAR(40), tips VARCHAR(200)
);
CREATE TABLE IF NOT EXISTS diseases (
  id INT AUTO_INCREMENT PRIMARY KEY,
  crop VARCHAR(50), symptom VARCHAR(30),
  disease VARCHAR(80), cause VARCHAR(150), treatment VARCHAR(250)
);
CREATE TABLE IF NOT EXISTS search_log (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tool VARCHAR(20), query_text VARCHAR(250),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO crops (name,soil_type,min_ph,max_ph,min_rain,max_rain,min_temp,max_temp,season,tips) VALUES
('Rice','Clay',5.5,7.0,1000,2500,20,35,'Kharif (Jun-Oct)','Needs standing water; transplant seedlings at 20-25 days.'),
('Wheat','Loamy',6.0,7.5,400,900,10,25,'Rabi (Nov-Mar)','Sow in cool weather; irrigate at crown-root stage.'),
('Maize','Loamy',5.8,7.0,500,1000,18,32,'Kharif (Jun-Sep)','Well-drained soil; apply nitrogen in split doses.'),
('Cotton','Black',6.0,8.0,600,1200,21,35,'Kharif (May-Dec)','Black soil holds moisture well; watch for bollworm.'),
('Tomato','Loamy',6.0,7.0,400,800,18,30,'Year-round','Use stakes and drip irrigation to reduce disease.'),
('Potato','Sandy',5.0,6.5,400,700,15,22,'Rabi (Oct-Feb)','Earth-up plants; avoid waterlogging.'),
('Groundnut','Sandy',6.0,7.0,500,1000,22,32,'Kharif (Jun-Oct)','Add gypsum at flowering for better pods.'),
('Sugarcane','Clay',6.5,7.5,1100,1500,21,35,'Feb-Mar planting','Heavy water and nutrient requirement.'),
('Soybean','Black',6.0,7.5,600,1000,20,30,'Kharif (Jun-Oct)','Treat seeds with Rhizobium culture.'),
('Millet','Sandy',5.5,7.5,300,600,25,35,'Kharif (Jun-Sep)','Drought tolerant; ideal for low-rainfall areas.');

INSERT INTO diseases (crop,symptom,disease,cause,treatment) VALUES
('Rice','brown_spots','Brown Spot','Fungus Bipolaris oryzae; nutrient-poor soil','Spray Mancozeb 2.5 g/L; balance potassium and nitrogen.'),
('Rice','wilting','Bacterial Leaf Blight','Xanthomonas bacteria','Avoid excess nitrogen; spray copper oxychloride; drain field.'),
('Wheat','white_powder','Powdery Mildew','Fungus Blumeria graminis','Spray wettable sulphur or Propiconazole; improve airflow.'),
('Wheat','yellow_leaves','Yellow Rust','Fungus Puccinia striiformis','Spray Tebuconazole; use resistant varieties.'),
('Tomato','brown_spots','Early Blight','Fungus Alternaria solani','Remove infected leaves; spray Chlorothalonil; rotate crops.'),
('Tomato','curled_leaves','Leaf Curl Virus','Whitefly-transmitted virus','Control whiteflies with neem oil; remove infected plants.'),
('Tomato','wilting','Fusarium Wilt','Soil-borne fungus','Use resistant varieties; solarize soil; avoid waterlogging.'),
('Potato','brown_spots','Late Blight','Phytophthora infestans; cool humid weather','Spray Metalaxyl + Mancozeb; destroy infected tubers.'),
('Cotton','curled_leaves','Cotton Leaf Curl Virus','Whitefly vector','Use tolerant hybrids; spray Imidacloprid on whiteflies.'),
('Maize','yellow_leaves','Nitrogen Deficiency / Downy Mildew','Low nitrogen or Peronosclerospora fungus','Apply urea top-dressing; treat seed with Metalaxyl.'),
('Soybean','white_powder','Powdery Mildew','Fungus Microsphaera diffusa','Spray Hexaconazole; avoid dense planting.'),
('Groundnut','brown_spots','Tikka Leaf Spot','Fungus Cercospora','Spray Carbendazim + Mancozeb at 30 and 45 days.');
