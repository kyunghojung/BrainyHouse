// Brainy House Device1 - RF
// Security
#include <SPI.h>
#include <Wire.h>
#include <Servo.h>
#include <Adafruit_PN532.h>
#include <DistanceGP2Y0A41SK.h>

// RF Reader 
#define PN532_IRQ   2
#define PN532_RESET 3

#define DOOR_PIN 8
#define SERVO_LEFTRIGHT 9
#define SERVO_UPDOWN 10

#define PIR_PIN A0

#define RF_READ_RETRY 1
#define LONG_MEASUREMENT_INTERVALS 5000
#define SHORT_MEASUREMENT_INTERVALS 500
#define RF_MEASUREMENT_INTERVALS 200

Adafruit_PN532 nfc(PN532_IRQ, PN532_RESET);

Servo door;
int doorStatus = 0;

enum ViewDirection
{
    CAMERA_UP,
    CAMERA_DOWN,
    CAMERA_LEFT,
    CAMERA_RIGHT
};

Servo cameraViewLeftRight;    // Move camera Left <-> Right 0 ~ 180(center 90)
Servo cameraViewUpDown;       // Move camera Up <-> Down 80-180(center 120)

int currentViewUpDownAngle = -1;
int currentViewLeftRightAngle = -1;

DistanceGP2Y0A41SK pir;
int pirStatus = 0;

String rcvRFStr = "";
String sendDataStr = "";
String rcvMasterDataStr = "";
boolean rcvingMasterData = false;

unsigned long rfMeasurementTime = 0;
unsigned long shortMeasurementTime = 0;
unsigned long longMeasurementTime = 0;

void setup() 
{
  Serial.begin(115200);
  Serial1.begin(9600);
  
  setupRF();
  setupSensors();
  
  shortMeasurementTime = millis();
  longMeasurementTime = millis();
  
  Serial.println("Started Slave "); 
}

void loop() 
{
  readFromMaster();
  
  if(rcvingMasterData != true) 
  {
    if(GetETime(shortMeasurementTime) > SHORT_MEASUREMENT_INTERVALS)
    {
      monitorPIRstatus();
      shortMeasurementTime = millis();  
    }
    
    if(GetETime(rfMeasurementTime) > RF_MEASUREMENT_INTERVALS)
    {
      if(readRF() == true)
      {
        if(rcvRFStr.length() != 0)
        {
          sendDataStr="RF:"+rcvRFStr;
          writeToMaster(sendDataStr);
          sendDataStr = "";
          rcvRFStr = "";
        }
        delay(1000);
      }

      rfMeasurementTime = millis();   
    }
  }
    
}

void setupRF()
{
  nfc.begin();
  uint32_t versiondata = nfc.getFirmwareVersion();
  if (! versiondata) {
    //Serial.print("Didn't find PN53x board");
    while (1); // halt
  }
  // Got ok data, print it out!
  //Serial.print("Found chip PN5"); //Serial.println((versiondata>>24) & 0xFF, HEX); 
  //Serial.print("Firmware ver. "); //Serial.print((versiondata>>16) & 0xFF, DEC); 
  //Serial.print('.'); //Serial.println((versiondata>>8) & 0xFF, DEC);
  
  nfc.setPassiveActivationRetries(RF_READ_RETRY);
  
  // configure board to read RFID tags
  nfc.SAMConfig();
  
  //Serial.println("Waiting for an ISO14443A Card ...");
}

boolean readRF()
{
  // RF Reader
  uint8_t success;
  uint8_t uid[] = { 0, 0, 0, 0, 0, 0, 0 };  // Buffer to store the returned UID
  uint8_t uidLength;                        // Length of the UID (4 or 7 bytes depending on ISO14443A card type)

  // Wait for an ISO14443A type cards (Mifare, etc.).  When one is found
  // 'uid' will be populated with the UID, and uidLength will indicate
  // if the uid is 4 bytes (Mifare Classic) or 7 bytes (Mifare Ultralight)
  success = nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, uid, &uidLength);
  
  if (success) 
  {
    // Display some basic information about the card
    //Serial.println("Found an ISO14443A card");
    //Serial.print("  UID Length: ");//Serial.print(uidLength, DEC);//Serial.println(" bytes");
    //Serial.print("  UID Value: ");
    //nfc.PrintHex(uid, uidLength);
    //Serial.println("");
    
    rcvRFStr = toHexString(uid, uidLength);
  }
  else
  {
    return false;
  }
  return true;
}

void writeToMaster(String data)
{
  String sendData = "*"+data+"#";
  Serial1.print(sendData);
  Serial.print("writeToMaster: "); Serial.println(sendData);
}

int readFromMaster()
{
  while (Serial1.available()) 
  {
    char c = Serial1.read();
    
    //Serial.println((byte)c); 
    if(c == '*')
    {
      rcvMasterDataStr = "";
      rcvingMasterData = true;
    } 
    
    if(rcvingMasterData == true && c != '*' && c != '#')
    {
      rcvMasterDataStr.concat(c);
    }
    if(rcvingMasterData == true && c == '#')
    {
      processCommand(rcvMasterDataStr);
      rcvingMasterData = false;
      rcvMasterDataStr = "";
    }
  }
  /*
  if(rcvMasterDataStr.length() > 0 )
  {
    Serial.print("rcvingMasterData: "); 
    Serial.print(rcvingMasterData); 
    Serial.print(", rcvMasterDataStr: "); 
    Serial.println(rcvMasterDataStr);
  }
  */
  return rcvMasterDataStr.length();
}

void processCommand(String command)
{
    Serial.print("processCommand: "); Serial.println(command);
    
    if(command.equals("DO"))  //doorOpen
    {
      controlDoor(true);
      writeToMaster("OK:"+command);
    }
    else if(command.equals("DC"))  //doorClose
    {
      controlDoor(false);
      writeToMaster("OK:"+command);
    }
    else if(command.equals("AC"))  //cameraCenter
    {
      cameraCenter();
      writeToMaster("OK:"+command);
    }
    else if(command.equals("AU"))  //cameraUp
    {
      cameraUpDown(CAMERA_UP);
      writeToMaster("OK:"+command);
    }
    else if(command.equals("AD"))  //cameraDown
    {
      cameraUpDown(CAMERA_DOWN);
      writeToMaster("OK:"+command);
    }
    else if(command.equals("AL"))  //cameraLeft
    {
      cameraLeftRight(CAMERA_LEFT);
      writeToMaster("OK:"+command);
    }
    else if(command.equals("AR"))  //cameraRight
    {
      cameraLeftRight(CAMERA_RIGHT);
      writeToMaster("OK:"+command);
    }
}

void setupSensors()
{
  pir.begin(PIR_PIN);
  
  door.attach(DOOR_PIN);
  controlDoor(false);
  
  cameraViewLeftRight.attach(SERVO_LEFTRIGHT);
  cameraViewUpDown.attach(SERVO_UPDOWN);
  cameraCenter();
}

void monitorPIRstatus()
{
  double distance = pir.getDistanceCentimeter();

  //Serial.print("distance: "); Serial.println(distance);
  if(distance < 10 && pirStatus == 0)
  {
    pirStatus = 1;
    sendDataStr="I:"+String(pirStatus);
    writeToMaster(sendDataStr);
  }
  else if(distance >=10 && pirStatus == 1){
    pirStatus = 0;
    sendDataStr="I:"+String(pirStatus);
    writeToMaster(sendDataStr);
  }
}

void controlDoor(boolean openClose)
{
  if(openClose == true)
  {
    doorStatus = 1;
    door.write(180);  
  }
  else
  {
    doorStatus = 0;
    door.write(0);  
  }
}

void cameraCenter()
{
    currentViewLeftRightAngle = 90;
    cameraViewLeftRight.write(currentViewLeftRightAngle);
    currentViewUpDownAngle = 20;
    cameraViewUpDown.write(currentViewUpDownAngle);
}

void cameraUpDown(int UpDown)
{
    if(UpDown == CAMERA_DOWN)
    {
        currentViewUpDownAngle = currentViewUpDownAngle - 5;

        if(currentViewUpDownAngle < 0)
        {
            currentViewUpDownAngle = 0;
        }
    }
    else if(UpDown == CAMERA_UP)
    {
        currentViewUpDownAngle = currentViewUpDownAngle + 5;

        if(currentViewUpDownAngle > 120)
        {
            currentViewUpDownAngle = 120;
        }

    }
    else
    {
        return;
    }

    cameraViewUpDown.write(currentViewUpDownAngle);
}

void cameraLeftRight(int LeftRight)
{
    if(LeftRight == CAMERA_LEFT)
    {
        currentViewLeftRightAngle = currentViewLeftRightAngle - 5;

        if(currentViewLeftRightAngle > 180)
        {
            currentViewLeftRightAngle = 180;
        }

    }
    else if(LeftRight == CAMERA_RIGHT)
    {
        currentViewLeftRightAngle = currentViewLeftRightAngle + 5;

        if(currentViewLeftRightAngle < 0)
        {
            currentViewLeftRightAngle = 0;
        }

    }
    else
    {
        return;
    }
    
    cameraViewLeftRight.write(currentViewLeftRightAngle);
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

String toHexString(const byte * data, const uint32_t numBytes)
{
  String str = "";
  
  for (int i=0; i < numBytes; i++)
  {
    str.concat(String(data[i], HEX));
  }
  return str;
}

