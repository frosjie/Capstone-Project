#include <SoftwareSerial.h>
#include <Servo.h>
#include <Wire.h>

SoftwareSerial BTSerial(10, 11);
Servo myservo;

const int trigPin = 5;
const int echoPin = 8;
const int BTStatePin = 7;
const int buzzerPin = 13;
int fsrPin = A0; 

long duration;

boolean smsNotificationState = false;
boolean SMS_NOTIFICATION_FLAG = false;

int BTReg =0;
int distance;
int command;
int mode,inComingByte;
int UnlockPos = 0;    
int lockPos = 90; 
int theftModeActivated = 0;
int buzzerModeActivated = 0;

int enA = 3;
int in1 = 2;
int enB = 6;
int in3 = 4;
int fsrReading;     
int fsrVoltage; 

void setup() {
  BTSerial.begin(9600);
  myservo.attach(9);  
  Wire.begin();
  pinMode(BTStatePin,INPUT);
  pinMode(echoPin,INPUT);
  pinMode(trigPin,OUTPUT);
  pinMode(buzzerPin,OUTPUT);
  pinMode(fsrPin,INPUT);
  motorSetup();
}

void loop() { 
  check_Serial();
  checkBTConnection();
  onHandleHold();
  antiTheftMode();
  buzzerMode();
  smsNotificationTrigger();
  
  switch(mode){
    case 1:
      myservo.write(lockPos);
      BTSerial.write("luggageLocked#"); 
      mode = 0;
      break;

    case 2:
      myservo.write(UnlockPos); 
      BTSerial.write("luggageUnlocked#");
      mode = 0;
      break;

    case 3:
      theftModeActivated = 1;
      BTSerial.write("theftModeActivated#");
      mode = 0;
      break;

    case 4:
      theftModeActivated = 0;
      wireWrite(98);
      BTSerial.write("theftModeDeactivated#");
      mode = 0;
      break;

    case 5:
      buzzerModeActivated = 1;
      BTSerial.write("buzzerModeActivated#");
      mode = 0;
      break;

    case 6:
      buzzerModeActivated = 0;
      BTSerial.write("buzzerModeDeactivated#");   
      mode = 0;
      break;

    case 7:
      setGpsUpdateFrequency(55);
      BTSerial.write("GPS update every 1 minute#");
      mode = 0;
      break;
      
    case 8:
      setGpsUpdateFrequency(56);
      BTSerial.write("GPS update every 15 minute#");
      mode = 0;
      break;   

    case 9:
      setGpsUpdateFrequency(57);
      BTSerial.write("GPS update every 30 minute#");
      mode = 0;
      break; 

    case 10:
      smsNotificationState = true;
      BTSerial.write("SMS Notification Enabled#");
      mode = 0;
      break; 

    case 11:
      smsNotificationState = false;
      SMS_NOTIFICATION_FLAG = false;
      BTSerial.write("SMS Notification Disabled#");
      mode = 0;
      break;
         
    default: 
      break;
    }
}

void check_Serial(){
  if(BTSerial.available()>0){
    command = BTSerial.read();
  }
  
  if(command == 49){//1 Lock luggage
    command = 0;
    mode = 1;
  }else if(command == 50){//2 Unlock luggage
    command = 0;
    mode = 2;
  }else if(command == 51){//3 ATM on
    command = 0;
    mode = 3;
  }else if(command == 52){//4 ATM off
    command = 0;
    mode = 4;
  }else if(command == 53){//5 buzzerMode on
    command = 0;
    mode = 5;
  }else if(command == 54){//6 buzzerMode off
    command = 0;
    mode = 6;
  }else if(command == 55){ //7 GPS data update frequency
    command = 0;
    mode = 7;
  }else if(command == 56){ //8 GPS data update frequency
    command = 0;
    mode = 8;
  }
  else if(command == 57){ //9 GPS data update frequency
    command = 0;
    mode = 9;
  }else if(command == 97){//a smsNotiEnable
    command = 0;
    mode = 10;
  }else if(command == 98){//b smsNotiDisable
    command = 0;
    mode = 11;
  }
}

void antiTheftMode(){
  if(theftModeActivated == 1){
    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);
    duration = pulseIn(echoPin, HIGH);
    distance= duration*0.034/2;
 
    if(distance > 10){
      SMS_NOTIFICATION_FLAG = true;
    }else{
      digitalWrite(buzzerPin,LOW);
      SMS_NOTIFICATION_FLAG = false;
    }
  }else{
    digitalWrite(buzzerPin,LOW);
    SMS_NOTIFICATION_FLAG = false;
  } 
} 

void buzzerMode(){
  if(buzzerModeActivated == 0){
    digitalWrite(buzzerPin,LOW);
  }else{
    digitalWrite(buzzerPin,HIGH);
  }
}

void setGpsUpdateFrequency(int input){
  wireWrite(input);
}

void wireWrite(int input){
  Wire.beginTransmission(8); // transmit to device #8
  Wire.write(input); // sends one byte
  Wire.endTransmission();    // stop transmitting
}

void smsNotificationTrigger(){
  if(smsNotificationState){
    if(SMS_NOTIFICATION_FLAG){
      smsNotificationState = false;
      wireWrite(97);
      SMS_NOTIFICATION_FLAG = false;
    }
  }
}

void checkBTConnection(){
  if(digitalRead(BTStatePin) && BTReg == 0){
    BTReg =1;
  }else if(digitalRead(BTStatePin) == 0 && BTReg == 1){
    BTReg = 0;
  }  
}

void motorSetup(){
  pinMode(enA, OUTPUT);
  pinMode(enB, OUTPUT);
  pinMode(in1, OUTPUT);
  pinMode(in3, OUTPUT);
}

void motorMoveForward(){
  digitalWrite(in1, HIGH);
  analogWrite(enA, 200);
  digitalWrite(in3, HIGH);
  analogWrite(enB, 200);
}

void motorStop(){
  digitalWrite(in1, LOW);
  analogWrite(enA, 0);
  digitalWrite(in3, LOW);
  analogWrite(enB, 0);
}

void onHandleHold(){
  fsrReading = analogRead(fsrPin);  
  fsrVoltage = map(fsrReading, 0, 1023, 0, 5000);

  if (fsrVoltage < 400) {
    motorStop();
  } else {
    motorMoveForward(); 
  }
}
