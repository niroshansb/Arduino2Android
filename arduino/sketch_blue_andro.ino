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
  bluetooth.begin(9600);
}

void loop()
{
  DHT.read11(dht_apin);
  // Serial.print(DHT.humidity);
  // bluetooth.print(DHT.humidity);

  bluetooth.print("Humid ");
  bluetooth.print(DHT.humidity);
  bluetooth.print("  temperature = ");
  bluetooth.println(DHT.temperature);
  delay(2000);
}

