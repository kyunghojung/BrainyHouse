
// Brainy House Device2
// Kitchen

#include <SPI.h>
#include <Wire.h>
#include <Servo.h> 
#include <QueueList.h>
#include "Adafruit_BLE_UART.h"

// Bluetooth LE
#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2
#define ADAFRUITBLE_RST 9

Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);

// Other Sensors
#define GAS_VALVE_PIN   A1
#define GAS_SENSOR_PIN   A0
#define BUZZER_PIN   44
#define RED_LIGHT_PIN 49

#define SEND_BT_INTERVALS 1000
#define SHORT_MEASUREMENT_INTERVALS 1000
#define LONG_MEASUREMENT_INTERVALS 10000
#define WARNING_BELL_INTERVALS 500

unsigned long sendBTTime = 0;
unsigned long longMeasurementTime = 0;
unsigned long shortMeasurementTime = 0;

unsigned long ringingBellTime = 0;
boolean isRingingBell = false;
boolean ringingBellTiming = false;

Servo gasValve;

String rcvDataStr = "";
String sendDataStr = "";
String rcvSlaveDataStr = "";
boolean rcvingSlaveData = false;

int gasStatus = 0;
int gasValveStatus = 0;
int redLedStatus = 0;
int buzzerStatus = 0;

QueueList <String> BTQueue;

void setup(void)
{ 
  Serial.begin(115200);
  Serial1.begin(115200);

  setupBT();
  
  setupSensors();
  
  longMeasurementTime = millis();
  shortMeasurementTime = millis();
}

void loop()
{
  int rcvSlaveLen = readFromSlave();

  int rcvBTLen = readBT();

  checkWarningAlert();
  
  if(rcvBTLen == 0)
  {
    if(GetETime(shortMeasurementTime) > SHORT_MEASUREMENT_INTERVALS)
    {
      int gas = getGasStatus();
      
      if(gas !=  gasStatus)
      {
        sendDataStr = "";
        sendDataStr.concat("G:"+String(gas));
        writeBT(sendDataStr,"K");
        gasStatus = gas;
      }
      
      if(gasStatus == 1 && isRingingBell == false)
      {
        startWarningAlert();
      }
      else if(gasStatus == 0 && isRingingBell == true)
      {
        stopWarningAlert();
      }
      
      shortMeasurementTime = millis();
    }
  }
  else if(rcvBTLen > 0)
  {
    processCommand(rcvDataStr);
    
    rcvDataStr = "";
  }
  else
  {
    Serial.println("BT isn't Connected!!!!");
    delay(1000);
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
  BTLEserial.setDeviceName("IOTHOM2"); /* 7 characters max! */
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
      Serial.print(rcvDataStr);
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
  gasValve.attach(GAS_VALVE_PIN);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(RED_LIGHT_PIN, OUTPUT);
  
  controlGasValve(true);
  controlBuzzer(false);
  controlRedLed(false);
}

void controlGasValve(boolean openClose)
{
  if(openClose == true)
  {
    gasValveStatus = 1;
    gasValve.write(180);  
  }
  else
  {
    gasValveStatus = 0;
    gasValve.write(0);  
  }
}

void controlBuzzer(boolean onOff)
{
  if(onOff == true)
  {
    buzzerStatus = 1;
    digitalWrite(BUZZER_PIN, LOW);
  }
  else
  {
    buzzerStatus = 0;
    digitalWrite(BUZZER_PIN, HIGH);
  }
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

int getGasStatus()
{
  int gasValue = analogRead(GAS_SENSOR_PIN);
  //Serial.print("gasValue: ");
  //Serial.println(gasValue);
  if(gasValue > 300)
  {
    return 1;
  }
  else
  {
    return 0;
  }
}


void startWarningAlert()
{
    isRingingBell = true;
    ringingBellTiming = true;
    controlRedLed(true);
    controlBuzzer(true);
    ringingBellTime = millis();
}

void checkWarningAlert()
{
  if(ringingBellTime != 0 && GetETime(ringingBellTime) > WARNING_BELL_INTERVALS)
  {
    if(ringingBellTiming == true)
    {
      controlRedLed(false);
      controlBuzzer(false);
      ringingBellTiming = false;
    }
    else
    {
      controlRedLed(true);
      controlBuzzer(true);
      ringingBellTiming = true;
    }
    ringingBellTime = millis();
  }
}

void stopWarningAlert()
{
    isRingingBell = false;
    ringingBellTiming = false;
    controlRedLed(false);
    controlBuzzer(false);
    ringingBellTime = 0;
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
    //Serial.println(command);
    if(command.equals("RN"))
    {
      controlRedLed(true);
    }
    else if(command.equals("RF"))
    {
      controlRedLed(false);
    }
    else if(command.equals("BN"))
    {
      controlBuzzer(true);
    }
    else if(command.equals("BF"))
    {
      controlBuzzer(false);
    }
    else if(command.equals("VO"))
    {
      controlGasValve(true);
    }
    else if(command.equals("VC"))
    {
      controlGasValve(false);
    }

    writeBT("OK:"+command, "K");
}

int readFromSlave()
{
  while (Serial1.available()) 
  {
    char c = Serial1.read();
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
      writeBT(rcvSlaveDataStr, "F");
      rcvingSlaveData = false;
      rcvSlaveDataStr = "";
    }
  }

  if(rcvSlaveDataStr.length() > 0 )
  {
    Serial.print("readFromSlave(): "); 
    Serial.println(rcvSlaveDataStr);
  }

  return rcvSlaveDataStr.length();
}

void writeToSlave(String data)
{
  String SendData = "*"+data+"#";
  Serial.print("writeToSlave(): "); Serial.println(SendData);
  Serial1.print(SendData);
}

