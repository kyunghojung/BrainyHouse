// Brainy House Device1 - RF
// Security
#include <SPI.h>
#include <Wire.h>
#include <Adafruit_PN532.h>

// RF Reader 
#define PN532_IRQ   2
#define PN532_RESET 3

#define BUTTON_YELLOW_PIN 6
#define BUTTON_RED_PIN 7

#define RF_READ_RETRY 1
#define RF_MEASUREMENT_INTERVALS 200

Adafruit_PN532 nfc(PN532_IRQ, PN532_RESET);

String rcvRFStr = "";
String sendDataStr = "";
String rcvMasterDataStr = "";
boolean rcvingMasterData = false;

unsigned long rfMeasurementTime = 0;

void setup() 
{
  Serial.begin(115200);
  Serial1.begin(115200);
  
  setupRF();
  setupSensors();
  
  rfMeasurementTime = millis();
  Serial.println("Slave Started");
}

void loop() 
{
  readFromMaster();
  
  if(rcvingMasterData != true) 
  {
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
      
      if(getYellowButtonStatus())
      {
        sendDataStr = "BY";
        writeToMaster(sendDataStr);
      }
    
      if(getRedButtonStatus())
      {
        sendDataStr = "BR";
        writeToMaster(sendDataStr);
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
    // Set the max number of retry attempts to read from a card
  // This prevents us from waiting forever for a card, which is
  // the default behaviour of the PN532.

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

  //Serial.println("readRF()");
  // Wait for an ISO14443A type cards (Mifare, etc.).  When one is found
  // 'uid' will be populated with the UID, and uidLength will indicate
  // if the uid is 4 bytes (Mifare Classic) or 7 bytes (Mifare Ultralight)
  success = nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, uid, &uidLength);
  
  //Serial.print("success: ");  //Serial.println(success);
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
  Serial.print("writeToMaster: ");Serial.println(sendData);
}

int readFromMaster()
{
  while (Serial1.available()) 
  {
    char c = Serial1.read();
    if(rcvingMasterData == false && c == '*')
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
  
  return rcvMasterDataStr.length();
}

void processCommand(String command)
{
  Serial.print("processCommand: ");Serial.println(command);
}

void setupSensors()
{
  pinMode(BUTTON_YELLOW_PIN, INPUT);
  digitalWrite(BUTTON_YELLOW_PIN, HIGH);
  
  pinMode(BUTTON_RED_PIN, INPUT);
  digitalWrite(BUTTON_RED_PIN, HIGH);
}

boolean getYellowButtonStatus()
{
  if(digitalRead(BUTTON_YELLOW_PIN) == LOW)
  {
    return true;
  }
  else
  {
    return false;
  }
}

boolean getRedButtonStatus()
{
  if(digitalRead(BUTTON_RED_PIN) == LOW)
  {
    return true;
  }
  else
  {
    return false;
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

String toHexString(const byte * data, const uint32_t numBytes)
{
  String str = "";
  
  for (int i=0; i < numBytes; i++)
  {
    str.concat(String(data[i], HEX));
  }
  return str;
}
