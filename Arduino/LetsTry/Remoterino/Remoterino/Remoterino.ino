
#include <IRremote.h>
#include <SoftwareSerial.h> //Software Serial Port
#define RxD 5
#define TxD 6

int STATUS_PIN = 2;
const unsigned long powerCode = 0x61D648B7;
const unsigned long sourceCode = 0x61D68877;
const unsigned long volumeUpCode = 0x61D6D827;
const unsigned long volumeDownCode = 0x61D6F807;
const unsigned long channelUpCode = 0x61D658A7;
const unsigned long channelDownCode = 0x61D67887;
const int codeLength = 128;

IRsend irsend;

SoftwareSerial blueToothSerial(RxD,TxD);

void setup()
{
  Serial.begin(9600);
  pinMode(RxD, INPUT);
  pinMode(TxD, OUTPUT);
  pinMode(STATUS_PIN, OUTPUT);
  setupBlueToothConnection(); 
}


void loop() {

  int recvChar;
  if (blueToothSerial.available()){
    recvChar = blueToothSerial.read();
    kake(recvChar);
    
  }

  if(Serial.available()){//check if there's any data sent from the local serial terminal, you can add the other applications here
    recvChar = Serial.read();
    blueToothSerial.print(recvChar);
  }


}

void kake(int x){
 if(x == 49){
    irsend.sendNEC(powerCode, codeLength);
    digitalWrite(STATUS_PIN, HIGH);
    Serial.println("Sent power");
    digitalWrite(STATUS_PIN, LOW);
    delay(50); 
  }
  else if(x == 50){
    irsend.sendNEC(sourceCode, codeLength);
    digitalWrite(STATUS_PIN, HIGH);
    Serial.println("Sent source");
    digitalWrite(STATUS_PIN, LOW);
    delay(50); 
  }
  else if(x == 51){
    irsend.sendNEC(volumeUpCode, codeLength);
    digitalWrite(STATUS_PIN, HIGH);
    Serial.println("Sent volume up");
    digitalWrite(STATUS_PIN, LOW);
    delay(50); 
  }
  else if(x == 52){
    irsend.sendNEC(volumeDownCode, codeLength);
    digitalWrite(STATUS_PIN, HIGH);
    Serial.println("Sent volume down");
    digitalWrite(STATUS_PIN, LOW);
    delay(50); 
  }
  else if(x == 53){
    irsend.sendNEC(channelUpCode, codeLength);
    digitalWrite(STATUS_PIN, HIGH);
    Serial.println("Sent channel up");
    digitalWrite(STATUS_PIN, LOW);
    delay(50); 
  }
  else if(x == 54){
    irsend.sendNEC(channelDownCode, codeLength);
    digitalWrite(STATUS_PIN, HIGH);
    Serial.println("Sent channel down");
    digitalWrite(STATUS_PIN, LOW);
    delay(50); 
  }
  else{
    Serial.println("WTF happend??");
    Serial.print(x);
  }
}  

void setupBlueToothConnection()
{
  blueToothSerial.begin(38400); //Set BluetoothBee BaudRate to default baud rate 38400
  blueToothSerial.print("\r\n+STWMOD=0\r\n"); //set the bluetooth work in slave mode
  blueToothSerial.print("\r\n+STNA=ArduinoRemote\r\n"); //set the bluetooth name as "SeeedBTSlave"
  blueToothSerial.print("\r\n+STPIN=0000\r\n");//Set SLAVE pincode"0000"
  blueToothSerial.print("\r\n+STOAUT=1\r\n"); // Permit Paired device to connect me
  blueToothSerial.print("\r\n+STAUTO=0\r\n"); // Auto-connection should be forbidden here
  delay(2000); // This delay is required.
  blueToothSerial.print("\r\n+INQ=1\r\n"); //make the slave bluetooth inquirable 
  Serial.println("The slave bluetooth is inquirable!");
  delay(2000); // This delay is required.
  blueToothSerial.flush();
}









