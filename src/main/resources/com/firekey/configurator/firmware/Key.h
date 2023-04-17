/*********************************************************************
* FIREKEY-PROJECT
* 
* KEY.h
* File defining Key class.
*********************************************************************/

/**
 * Key class representing a key on the board and its corresponding LED.
 */
class Key {
private:
  byte rowPin;                      // pin of row key belongs to 
  byte colPin;                      // pin of column key belongs to
  bool state;                       // user handled state for e.g. for toggling
  byte ledIndex;                    // index of corresponding led
  byte downCounter;                 // how long is key pressed - for hold and spam delays
  bool spamMode;                    // is key in spam mode
  byte lastColor[3];                // last rgb value of led

  Adafruit_NeoPixel *ledStrip;      // pointer to global led-strip object
  void (*keyPressedHandler)(Key *); // handler function called if key gets pressed

public:
  Key();
  Key(byte row, byte col, byte ledIndex, Adafruit_NeoPixel *ledStrip, void (*keyPressedHandler)(Key *));

  bool getState();
  byte getIndex();
  void check();
  void setState(bool state);
  void setLedRGB(byte idx, byte red, byte green, byte blue);
  void setLedRGB(byte red, byte green, byte blue);
  void setLedOn();
  void setLedOff();
  void setLedDefault();
};


// ======== CONSTRUCTORS =========

/**
 * Empty constructor (required for array definition).
 */
Key::Key() {}

/**
 * Main constructor
 */
Key::Key(byte row, byte col, byte ledIndex, Adafruit_NeoPixel *ledStrip, void (*keyPressedHandler)(Key *)) {
  // save parameters
  this->rowPin = row;
  this->colPin = col;
  this->ledIndex = ledIndex;
  this->ledStrip = ledStrip;
  this->keyPressedHandler = keyPressedHandler;

  // initialize values
  this->state = false;
  this->spamMode = false;
  this->downCounter = 0;

  // setup matrix
  pinMode(colPin, INPUT_PULLUP);
  pinMode(rowPin, OUTPUT);
  digitalWrite(rowPin, HIGH);
}


// =========== GETTER ============

/**
 * Returns the current state of the key (if it is pressed).
 */
bool Key::getState() {
  return this->state;
}

/**
 * Returns the led index of the key.
 */
byte Key::getIndex() {
  return this->ledIndex;
}


// ============= KEY =============

/**
 * Checks if key is pressed,handles press and updates the key state.
 */
void Key::check() {
  // pull output row to low
  digitalWrite(this->rowPin, LOW);

  // read key pressed
  bool pressed = (digitalRead(this->colPin) == LOW);

  // pull output row to high
  digitalWrite(this->rowPin, HIGH);

  // handle result
  // key pressed
  if (pressed) {
    // key was not pressed before
    if (this->downCounter == 0) {
      (*this->keyPressedHandler)(this);  // call handler function
    }
    // key held
    else if (!this->spamMode && this->downCounter > HOLD_DELAY) {
      (*this->keyPressedHandler)(this);  // call handler function
      this->spamMode = true;             // enter spam mode
      this->downCounter = 0;             // reset counter
    }
    // key held in spam mode
    else if (this->spamMode && this->downCounter > SPAM_DELAY) {
      (*this->keyPressedHandler)(this);  // call handler function
      this->downCounter = 0;             // reset counter
    }
    this->downCounter++;

  // key not pressed, but was pressed before
  } else if (this->downCounter != 0) {
    this->spamMode = false;
    this->downCounter = 0;
  }
}


// =========== SETTER ============

/**
 * Sets the key state for more advanced states.
 */
void Key::setState(bool state) {
  this->state = state;
}

/**
 * Sets the led color for a specific led index.
 */
void Key::setLedRGB(byte idx, byte red, byte green, byte blue) {
  lastColor[0] = red;
  lastColor[1] = green;
  lastColor[2] = blue;
  (*this->ledStrip).setPixelColor(idx, red, green, blue);
  (*this->ledStrip).show();
}

/**
 * Sets the led color for this key.
 */
void Key::setLedRGB(byte red, byte green, byte blue) {
  this->setLedRGB(this->ledIndex, red, green, blue);
}

/**
 * Turns LED on with last RGB value.
 */
void Key::setLedOn() {
  this->setLedRGB(lastColor[0], lastColor[1], lastColor[2]);
}

/**
 * Turns off LED.
 */
void Key::setLedOff() {
  (*this->ledStrip).setPixelColor(this->ledIndex, 0, 0, 0);
  (*this->ledStrip).show();
}

/**
 * Sets the default led color for this key.
 */
void Key::setLedDefault() {
  this->setLedRGB(
    pgm_read_byte_near(&defaultLEDColors[currentLayer][this->ledIndex][0]),
    pgm_read_byte_near(&defaultLEDColors[currentLayer][this->ledIndex][1]),
    pgm_read_byte_near(&defaultLEDColors[currentLayer][this->ledIndex][2])
  );
}