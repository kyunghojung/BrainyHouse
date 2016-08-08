
// Brainy House Device1
// Living Room

#include <SPI.h>
#include <Wire.h>
#include <QueueList.h>
#include "Adafruit_BLE_UART.h"
#include <SHT1x.h>

#define SHT_DATA_PIN  A4
#define SHT_CLOCK_PIN A5
SHT1x sht1x(SHT_DATA_PIN, SHT_CLOCK_PIN);

// Bluetooth LE
#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2
#define ADAFRUITBLE_RST 9

Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);

#define CURTAIN_PIN_1   37
#define CURTAIN_PIN_2   35
//#define TEMP_HUMI_PIN A1
#define RED_LIGHT_PIN 46
#define PHOTO_PIN   A0
#define LIGHT_PIN   48
#define FAN_MOTOR_PIN_1 47
#define FAN_MOTOR_PIN_2 49

#define SEND_BT_INTERVALS 1000
#define LONG_MEASUREMENT_INTERVALS 10000
#define SHORT_MEASUREMENT_INTERVALS 1000
#define CURTAIN_MOVE_TIME 1700

boolean isCurtainMoving = false;
unsigned long curtainMoveTime = 0;
unsigned long longMeasurementTime = 0;
unsigned long shortMeasurementTime = 0;
unsigned long sendBTTime = 0;

String rcvDataStr = "";
String sendDataStr = "";
String rcvSlaveDataStr = "";
boolean rcvingSlaveData = false;

int ledStatus = 0;
int redLedStatus = 0;
int curtainStatus = 0;
int fanStatus = 0;

float tempValue = 0;
float humiValue = 0;
int luxValue = 0;


QueueList <String> BTQueue;

void setup(void)
{ 
  Serial.begin(115200);
  Serial1.begin(9600);
  
  setupBT();
  
  setupSensors();
  
  longMeasurementTime = millis();
  sendBTTime = millis();

  Serial.println("Master Started");
}

void loop()
{
  int rcvSlaveLen = readFromSlave();
  
  int rcvBTLen = readBT();
  //int rcvBTLen = 0;
  
  if(rcvBTLen == 0)
  {
    if(GetETime(longMeasurementTime) > LONG_MEASUREMENT_INTERVALS)
    {
      float temp = getTempture();
      float humi = getHumidity();
      int lux = getLuxValue();

      int val_int;
      int val_fra;

      if(tempValue != temp)
      {
        char tempBuff[10];
        val_int = (int) temp;
        val_fra = (int) ((temp - (float)val_int) * 10);
        snprintf (tempBuff, sizeof(tempBuff), "%d.%d", val_int, val_fra); 
        
        sendDataStr = "";
        sendDataStr.concat("T:"+String(tempBuff));
        writeBT(sendDataStr, "L");
        
        tempValue = temp;
      }

      if(humiValue != humi)
      {
        char humiBuff[10];
        val_int = (int) humi;
        val_fra = (int) ((humi - (float)val_int) * 10);
        snprintf (humiBuff, sizeof(humiBuff), "%d.%d", val_int, val_fra); 
        
        sendDataStr = "";
        sendDataStr.concat("H:"+String(humiBuff));
        writeBT(sendDataStr, "L");
        
        humiValue = humi;
      }

      if(luxValue != lux)
      {
        sendDataStr = "";
        sendDataStr.concat("L:"+String(lux));
        writeBT(sendDataStr, "L");
        luxValue = lux;
      }
      
      longMeasurementTime = millis();
    }
  }
  else if(rcvBTLen > 0)
  {
    Serial.print("rcvDataStr: ");
    Serial.println(rcvDataStr);

    processCommand(rcvDataStr);
    
    rcvDataStr = "";
  }
  else
  {
    Serial.println("BT isn't Connected!!!!");
    delay(1000);
  }
  
  if(isCurtainMoving == true && GetETime(curtainMoveTime) > CURTAIN_MOVE_TIME)
  {
    curtainStop();
  }
  
  if(GetETime(sendBTTime) > SEND_BT_INTERVALS)
  {
    sendBT();
    sendBTTime = millis();
  }
    
  //delay(100);
}

void setupBT()
{
  // BLE
  BTLEserial.setDeviceName("IOTHOM1"); /* 7 characters max! */
  BTLEserial.begin();
}

int readBT()
{
  BTLEserial.pollACI();
  aci_evt_opcode_t status = BTLEserial.getState();
  
  if (status == ACI_EVT_CONNECTED) 
  {
    if(BTLEserial.available() > 0)
    {
      while (BTLEserial.available()) 
      {
        char c = BTLEserial.read();
        rcvDataStr.concat(c);
      }
      Serial.println(rcvDataStr);
    }
  }
  else
  {
    return -1;
  }
  return rcvDataStr.length();
}

void writeBT(String str, String prefix)
{
  String sendStr = prefix + ";" + str;
  
  BTQueue.push(sendStr);
}

void sendBT()
{
  if(BTQueue.isEmpty() != true)
  {
    uint8_t sendbuffer[20];
    String sendStr = BTQueue.pop ();
    sendStr.getBytes(sendbuffer, 20);
    char sendbuffersize = min(20, sendStr.length());
  
    //Serial.print("BTLEserial.getState(): "); Serial.println(BTLEserial.getState());
    
    // write the data
    if(BTLEserial.getState() == ACI_EVT_CONNECTED)
    {
      Serial.print(F("\n* Sending -> \"")); Serial.print((char *)sendbuffer); Serial.println("\"");
      BTLEserial.write(sendbuffer, sendbuffersize);
    }
    delay(100);
  }
}

void setupSensors()
{
  pinMode(RED_LIGHT_PIN, OUTPUT);
  pinMode(LIGHT_PIN, OUTPUT);
  pinMode(CURTAIN_PIN_1, OUTPUT);
  pinMode(CURTAIN_PIN_2, OUTPUT);
  pinMode(FAN_MOTOR_PIN_1, OUTPUT);
  pinMode(FAN_MOTOR_PIN_2, OUTPUT);
  
  controlLed(false);
  controlRedLed(false);
  curtainStop();
  controlFan(false);
}

void controlCurtain(boolean openClose)
{
  if(openClose == true)
  {
    digitalWrite(CURTAIN_PIN_1, HIGH);
    digitalWrite(CURTAIN_PIN_2, LOW);
  }
  else
  {
    digitalWrite(CURTAIN_PIN_1, LOW);
    digitalWrite(CURTAIN_PIN_2, HIGH);
  }
  
  curtainMoveTime = millis();
  isCurtainMoving = true;
}

void curtainStop()
{
  digitalWrite(CURTAIN_PIN_1, HIGH);
  digitalWrite(CURTAIN_PIN_2, HIGH);
  isCurtainMoving = false;
}

float getTempture()
{
  float temp = sht1x.readTemperatureC();
  
  //Serial.print("Temperature: ");
  //Serial.print(temp);
  //Serial.println(" *C ");
  
  return temp;
}

float getHumidity()
{
  float humi = sht1x.readHumidity();
  
  //Serial.print("Humidity: ");
  //Serial.print(humi);
  //Serial.println(" %");
  
  return humi;
}

void controlRedLed(boolean onOff)
{
  if(onOff == true)
  {
    redLedStatus = 1;
    digitalWrite(RED_LIGHT_PIN, HIGH);
  }
  else
  {
    redLedStatus = 0;
    digitalWrite(RED_LIGHT_PIN, LOW);
  }
}

void controlLed(boolean onOff)
{
  if(onOff == true)
  {
    ledStatus = 1;
    digitalWrite(LIGHT_PIN, HIGH);
  }
  else
  {
    ledStatus = 0;
    digitalWrite(LIGHT_PIN, LOW);
  }
}

int getLuxValue()
{
  int readValue = analogRead(PHOTO_PIN);   // Read the analogue pin
  float Vout=readValue*0.0048828125;      // calculate the voltage
  float res=10.0;
  
  int lux = 500/(res*((5-Vout)/Vout));           // calculate the Lux
  
  /*
  Serial.print("Luminosidad: ");                 // Print the measurement (in Lux units) in the screen
  Serial.print(lux);
  Serial.println(" Lux");

  if (readValue < 10) {
    Serial.println(" - Dark");
  }
  else if (readValue < 200) {
    Serial.println(" - Dim");
  } 
  else if (readValue < 500) {
    Serial.println(" - Light");
  } 
  else if (readValue < 800) {
    Serial.println(" - Bright");
  } 
  else {
    Serial.println(" - Very bright");
  }
  */
  
  /*
  0.0001 lux	Moonless, overcast night sky (starlight)[3]
  0.002 lux	Moonless clear night sky with airglow[3]
  0.27–1.0 lux	Full moon on a clear night[3][4]
  3.4 lux	Dark limit of civil twilight under a clear sky[5]
  50 lux	Family living room lights (Australia, 1998)[6]
  80 lux	Office building hallway/toilet lighting[7][8]
  100 lux	Very dark overcast day[3]
  320–500 lux	Office lighting[6][9][10][11]
  400 lux	Sunrise or sunset on a clear day.
  1000 lux	Overcast day;[3] typical TV studio lighting
  10000–25000 lux	Full daylight (not direct sun)[3]
  32000–100000 lux	Direct sunlight
  */
  return lux;
}

void controlFan(boolean onOff)
{
  if(onOff == true)
  {
    fanStatus = 1;
    digitalWrite(FAN_MOTOR_PIN_1, HIGH);
    digitalWrite(FAN_MOTOR_PIN_2, LOW);
  }
  else
  {
    fanStatus = 0;
    digitalWrite(FAN_MOTOR_PIN_1, HIGH);
    digitalWrite(FAN_MOTOR_PIN_2, HIGH);
  }
}

unsigned long GetETime(unsigned long referenceTime)
{
  unsigned long returnValue;
  unsigned long currentMillis = millis();
  if (referenceTime > currentMillis)
  {
    returnValue = 4294967295 + (currentMillis - referenceTime);
    returnValue++;
  }
  else
  {
    returnValue = currentMillis - referenceTime;
  }
  return returnValue;
}

unsigned long GetEmicroTime(unsigned long referenceTime)
{
  unsigned long returnValue;
  unsigned long currentMicros = micros();
  if (referenceTime > currentMicros)
  {
    returnValue = 4294967295 + (currentMicros - referenceTime);
    returnValue++;
  }
  else
  {
    returnValue = currentMicros - referenceTime;
  }
  return returnValue;
}

void processCommand(String command)
{
    Serial.print("processCommand(): "); Serial.println(command);
    if(command.equals("LN"))  //ledON
    {
      controlLed(true);
    }
    else if(command.equals("LF"))  //ledOff
    {
      controlLed(false);
    }
    else if(command.equals("RN"))  //redLedOn
    {
      controlRedLed(true);
    }
    else if(command.equals("RF"))  //redLedOff
    {
      controlRedLed(false);
    }
    else if(command.equals("CO"))  //curtainOpen
    {
      controlCurtain(true);
    }
    else if(command.equals("CC"))  //curtainclose
    {
      controlCurtain(false);
    }
    else if(command.equals("FN"))  //fanOn
    {
      controlFan(true);
    }
    else if(command.equals("FF"))  //fanOff
    {
      controlFan(false);
    }
    else if(command.equals("DO"))  //doorOpen
    {
      writeToSlave(command);
    }
    else if(command.equals("DC"))  //doorClose
    {
      writeToSlave(command);
    }
    else if(command.equals("AD"))  //cameraDown
    {
      writeToSlave(command);
    }
    else if(command.equals("AU"))  //cameraUp
    {
      writeToSlave(command);
    }
    else if(command.equals("AL"))  //cameraLeft
    {
      writeToSlave(command);
    }
    else if(command.equals("AR"))  //cameraRight
    {
      writeToSlave(command);
    }
    
    if(!command.equals("DO") && !command.equals("DC")
    && !command.equals("AD") && !command.equals("AU") 
    && !command.equals("AL") && !command.equals("AR"))
    {
      writeBT("OK:"+command, "L");
    }
}

int readFromSlave()
{
  while (Serial1.available()) 
  {
    char c = Serial1.read();
    //Serial.println(c);
    if(rcvingSlaveData == false && c == '*')
    {
      rcvSlaveDataStr = "";
      rcvingSlaveData = true;
    }
    if(rcvingSlaveData == true && c != '*' && c != '#')
    {
      rcvSlaveDataStr.concat(c);
    }
    if(rcvingSlaveData == true && c == '#')
    {
      writeBT(rcvSlaveDataStr, "S");
      rcvingSlaveData = false;
      rcvSlaveDataStr = "";
    }
  }

/*
  if(rcvSlaveDataStr.length() > 0 )
  {
    Serial.print("rcvingSlaveData: "); 
    Serial.print(rcvingSlaveData); 
    Serial.print(", readFromSlave(): "); 
    Serial.println(rcvSlaveDataStr);
  }
*/  
  return rcvSlaveDataStr.length();
}

void writeToSlave(String data)
{
  String SendData = "*"+data+"#";
  Serial.print("writeToSlave(): "); Serial.println(SendData);
  Serial1.print(SendData);
}

