#include <TinyGPS++.h>
#include <SoftwareSerial.h>
#include <Wire.h>

const int buzzerPin = 7;
static const int RXPin = 4, TXPin = 3;
static const uint32_t GPSBaud = 9600;
boolean PUSH_DATA = false;
boolean GPS_VALID = true;
boolean SMS_FLAG = false;
double flat, flng;
int seconds = 0;
int receiveData = 0;
int update_frequent = 1;

TinyGPSPlus gps;
SoftwareSerial GPS_MODULE(RXPin, TXPin);

void setup()
{
  Serial.begin(9600);
  GPS_MODULE.begin(GPSBaud);
  Wire.begin(8);
  Wire.onReceive(receiveEvent);
  timerOneInit();
  sei();
  pinMode(buzzerPin,OUTPUT);
}

void loop(){
  readGpsString();
  sendSMSFunction();
  pushDataToServer();
}

void displayInfo()
{
  if (gps.location.isValid()){
    flat = gps.location.lat();
    flng = gps.location.lng();
    }
  else{
    GPS_VALID = false;
  }
}

void timerOneInit(){
  TCCR1A = 0;
  TCCR1B = 0;
  TCNT1  = 0;
  OCR1A = 15624;
  TCCR1B |= (1 << WGM12);
  TCCR1B |= (1 << CS12) | (1 << CS10);  
  TIMSK1 |= (1 << OCIE1A);
}

void readGpsString(){
  while (GPS_MODULE.available() > 0)
    if (gps.encode(GPS_MODULE.read()))

  if (millis() > 5000 && gps.charsProcessed() < 10){
    while(true);
  }  
}

void pushDataToServer(){
  if(PUSH_DATA){
    displayInfo();
    if(GPS_VALID){
      sendData(flat,flng);
    }else{
      GPS_VALID = true;
    }  
  }
  PUSH_DATA = false;
}

void receiveEvent(int howMany) {
  while (Wire.available() > 0) {
    receiveData = Wire.read();
  }
  buzzerStateChecker();
  GpsUpdateFreqDecider();
  sendSMS();
}

void buzzerStateChecker(){
  if(receiveData == 98){
    digitalWrite(buzzerPin,LOW);  
  }
}

void GpsUpdateFreqDecider(){
  if(receiveData == 55){
    update_frequent = 1; 
  }else if(receiveData == 56){
    update_frequent = 2;
  }else if(receiveData == 57){
    update_frequent = 3;
  }
}

void sendSMS(){
  if(receiveData == 97){
    SMS_FLAG = true;
    digitalWrite(buzzerPin,HIGH);
  }
}

void sendSMSFunction() {
  if(SMS_FLAG){
    Serial.println("AT+CMGF=1"); 
    delay(1000);
    Serial.println("AT+CMGS=\"+60109080146\"");
    delay(1000);
    Serial.println("Alert! Your luggage is being moved!"); 
    delay(1000);
    Serial.println((char)26); 
    delay(100);
    SMS_FLAG = false;
  } 
}

void sendData(double Latitude,double Longitude){
  Serial.println("AT+CGATT=0");
  delay(1500);
  Serial.println("AT+CGATT=1");
  delay(3000);
  Serial.println("AT+SAPBR=3,1,\"APN\",\"tunetalk\"");
  delay(2000);
  Serial.println("AT+SAPBR=1,1");
  delay(1500);
  Serial.println("AT+HTTPINIT");
  delay(1500);
  Serial.println("AT+HTTPPARA=\"CID\",1");
  delay(1500);
  Serial.print("AT+HTTPPARA=\"URL\",\"http://iotcourier.com/WeiJie/GPS.php?");
  Serial.print("Latitude=");
  Serial.print(Latitude);
  Serial.print("&Longitude=");
  Serial.print(Longitude);
  Serial.println("\"");
  delay(1500);
  Serial.println("AT+HTTPACTION=1");
  delay(8000);
  Serial.println("AT+HTTPTERM");
  delay(1500);
}

ISR(TIMER1_COMPA_vect){
  seconds ++;
  if(update_frequent == 1 && seconds == 60){
    seconds = 0;    
    PUSH_DATA = true;  
  }else if(update_frequent == 2 && seconds == 900){
    seconds = 0;
    PUSH_DATA = true;
  }else if(update_frequent == 3 && seconds == 1800){
    seconds = 0;
    PUSH_DATA = true;
  }
}
