#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <ESP32Servo.h>

const char* ssid = "YourWifiName";
const char* password = "YourPassword";
const char* serverUrl = "http://YourBackendIpAndPort/api/data/add";

#define TRIG_PIN 32
#define ECHO_PIN 33
#define SOIL_SENSOR_PIN 34
#define WATER_PUMP_PIN 25
#define IRRIGATION_PUMP_PIN 26
#define BATTERY_PIN 34
#define SERVO_X_PIN 14
#define SERVO_Y_PIN 12
#define LDR_A 15
#define LDR_B 19
#define LDR_C 5
#define LDR_D 4

Servo servoX, servoY;
int posX = 90, posY = 90;

void setup() {
    Serial.begin(115200);
    pinMode(TRIG_PIN, OUTPUT);
    pinMode(ECHO_PIN, INPUT);
    pinMode(SOIL_SENSOR_PIN, INPUT);
    pinMode(BATTERY_PIN, INPUT);
    servoX.attach(SERVO_X_PIN);
    servoY.attach(SERVO_Y_PIN);
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) { 
    	delay(1000); 
    	Serial.print("."); 
    }
    Serial.println("\nWiFi connected!");
    servoX.write(posX);
    servoY.write(posY);
}

void loop() {
    trackSun();
    float distance = measureDistance();
    int batteryLevel = analogRead(BATTERY_PIN);
    int soilHumidity = analogRead(SOIL_SENSOR_PIN);
    sendData(distance, batteryLevel, posX, posY, soilHumidity);
    delay(60000);
}

void trackSun() {
    int valA = analogRead(LDR_A);
    int valB = analogRead(LDR_B);
    int valC = analogRead(LDR_C);
    int valD = analogRead(LDR_D);
    int diffX = (valA + valD) - (valB + valC);
    int diffY = (valA + valB) - (valC + valD);
    int threshold = 50;
    
    if (diffX > threshold) 
    	posX = constrain(posX - 2, 10, 170);
    else if (diffX < -threshold) 
    	posX = constrain(posX + 2, 10, 170);
    if (diffY > threshold) 
    	posY = constrain(posY - 2, 10, 170);
    else if (diffY < -threshold) 
    	posY = constrain(posY + 2, 10, 170);
    	
    if (valA < 500 && valB < 500 && valC < 500 && valD < 500) { 
    	posX = 90;
    	posY = 90;
    }
    
    servoX.write(posX);
    servoY.write(posY);
}

float measureDistance() {
    digitalWrite(TRIG_PIN, LOW); 
    delayMicroseconds(2);
    digitalWrite(TRIG_PIN, HIGH); 
    delayMicroseconds(10);
    digitalWrite(TRIG_PIN, LOW);
    long duration = pulseIn(ECHO_PIN, HIGH);
    return duration * 0.034 / 2;
}

void sendData(float distance, int batteryLevel, int angleX, int angleY, int soilHumidity) {
    if (WiFi.status() == WL_CONNECTED) {
        StaticJsonDocument<200> jsonDoc;
        jsonDoc["distance"] = distance;
        jsonDoc["battery_lvl"] = batteryLevel;
        jsonDoc["angle_panneau_x"] = angleX;
        jsonDoc["angle_panneau_y"] = angleY;
        jsonDoc["humidite_sol"] = soilHumidity;
        String jsonString;
        serializeJson(jsonDoc, jsonString);
        sendHttpPost(serverUrl, jsonString);
    } else {
        Serial.println("WiFi not connected.");
    }
}

void sendHttpPost(String url, String payload) {
    HTTPClient http;
    http.begin(url);
    http.addHeader("Content-Type", "application/json");
    int httpResponseCode = http.POST(payload);
    
    if (httpResponseCode > 0) 
    	Serial.println(http.getString());
    else 
    	Serial.print("Error code: "); 
    	
    Serial.println(httpResponseCode);
    http.end();
}

