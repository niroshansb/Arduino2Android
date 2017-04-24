/*
  Example Bluetooth Serial Passthrough Sketch
  Example test to send humidity and
  temparature data to android device theough bluetooth
  by niroshan
  working properly with bulutooth serial 
*/
#include <SoftwareSerial.h>
#include <dht.h>

#define dht_apin A0 // Analog Pin sensor is connected to

dht DHT;

int bluetoothTx = 2;  // TX-O pin of bluetooth mate, Arduino D2
int bluetoothRx = 3;  // RX-I pin of bluetooth mate, Arduino D3

SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup()
{
  Serial.begin(9600);  // Begin the serial monitor at 9600bps
  delay(1000); //Delay to let system boot

  bluetooth.begin(115200);  // The Bluetooth Mate defaults to 115200bps
  bluetooth.print("$$$");  // Enter command mode
  delay(100);  // Short delay, wait for the Mate to send back CMD
  bluetooth.println("U,9600,N");  // Temporarily Change the baudrate to 9600, no parity
  // 115200 can be too fast at times for NewSoftSerial to relay the data reliably
  bluetooth.begin(9600);  // Start bluetooth serial at 9600
}

void loop()
{
  char c;
  if (bluetooth.available())
  {
    c=bluetooth.read();
    if(c=='t')
    readsensor();
  }
  if (Serial.available()) // If stuff was typed in the serial monitor
  {
    // Send any characters the Serial monitor prints to the bluetooth
    // this has been added to start the module
    bluetooth.print((char)Serial.read());
  }
}
void readsensor()
{
  DHT.read11(dht_apin);
  // Serial.print(DHT.humidity);
  // bluetooth.print(DHT.humidity);

  //bluetooth.print("Humid ");
  bluetooth.print(DHT.humidity);
  bluetooth.print(" :");
  //bluetooth.print(" temperature = ");
  bluetooth.println(DHT.temperature);
  //delay(2000);
}


