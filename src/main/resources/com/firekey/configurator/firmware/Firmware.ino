/*************************************************************************
* FIREKEY-PROJECT
* Firmware Version 1.1.2
* 
* Required Library:
* - Keyboard          (https://github.com/arduino-libraries/Keyboard) 
*   - USBHost         (https://github.com/arduino-libraries/USBHost)
* - Adafruit NeoPixel (https://github.com/adafruit/Adafruit_NeoPixel)
*   - Adafruit BusIO  (https://github.com/adafruit/Adafruit_BusIO)
*   - Adafruit GFX    (https://github.com/adafruit/Adafruit-GFX-Library)
* - U8g2              (https://github.com/olikraus/u8g2)
* - avr/pgmspace      (part of Pro Micro Board - preinstalled)
* >> can all be installed via Arduino IDE library manager
*
* Required Board:
* - Arduino AVR Boards
* 
* This Firmware is designed to run on the FireKey hardware.
*************************************************************************/

// TODO header split (.h & .cpp)? [nice to have]
// TODO change USB Device information? [Nice to have]


// ===== LIBRARIES & CONFIG ======
#include "Config.h"


// ========= VARIABLES ===========
unsigned long lastRefresh;   // last time checked matrix (for debouncing)
unsigned long lastKeyPress;  // last time a key was pressed (for timeout)
bool sleeping;               // is display & led strip sleeping?

// Keys
Key keys[ROW_COUNT][COL_COUNT];

// LED strip
Adafruit_NeoPixel ledStrip(LED_COUNT, LED_PIN, NEO_GRB + NEO_KHZ800);

// OLEDs
U8G2_SH1106_128X64_NONAME_1_HW_I2C oled1(U8G2_R0, U8X8_PIN_NONE, OLED_SCL_PIN, OLED_SDA_PIN);
U8G2_SH1106_128X64_NONAME_1_HW_I2C oled2(U8G2_R0, U8X8_PIN_NONE, OLED_SCL_PIN, OLED_SDA_PIN);

// ============ SETUP ============
void setup() {
  // initialize serial print if debug
  DEBUG_BEGIN();
  DEBUG_PRINTLN("Startup with DEBUG enabled");

  // disable onboard RX/TX LEDs
  pinMode(LED_BUILTIN_TX, INPUT);
  pinMode(LED_BUILTIN_RX, INPUT);
  DEBUG_PRINTLN("RX/TX LEDs disabled");

  // initialize displays
  oled1.setFont(u8g2_font_7x14_mf);
  oled1.setI2CAddress(OLED1_ADDR * 2);    // no clue why *2
  oled1.enableUTF8Print();
  oled2.setFont(u8g2_font_7x14_mf);
  oled2.setI2CAddress(OLED2_ADDR * 2);
  oled2.enableUTF8Print();
  oled1.begin();  
  oled2.begin();
  DEBUG_PRINTLN("Displays initialized");

  // initialize LED strip
  ledStrip.begin();
  ledStrip.setBrightness(LED_BRIGHT);
  ledStrip.show();
  DEBUG_PRINTLN("LED-Strip initialized");

  // initialize key matrix
  for (byte r = 0; r < ROW_COUNT; r++)
    for (byte c = 0; c < COL_COUNT; c++)
      keys[r][c] = Key(rows[r], cols[c], getLedIndex(r, c), &ledStrip, &handleKeyPress);
  DEBUG_PRINTLN("Matrix initialized");

  // initialize keyboard
  Keyboard.begin(KeyboardLayout_de_DE);
  DEBUG_PRINTLN("Keyboard initialized");

  // initialize start values
  lastKeyPress = millis();
  sleeping = false;
  setLedDefaultValues();
  refreshDisplays();
  DEBUG_PRINTLN("Values initialized");
}


// ============ LOOP =============
void loop() {
  //Serial.println(freeMemory()); // TODO remove?

  // check if debounce time is over
  if ((millis() - lastRefresh) > DEBOUNCE_DELAY) {
    readMatrix();
    lastRefresh = millis();
  }

  // check if timout time is over
  if ((millis() - lastKeyPress) > ((long)SLEEP_DELAY * 1000)) {
    if(!sleeping)
      sleep();
  }
}


// ========== DISPLAY ============

/**
 * Refresh displays using @link setTextDisplay1 & @link setTextDisplay2, if @link sleeping is false.
 */
void refreshDisplays() {
  if (!sleeping) {
    DEBUG_PRINTLN("Refreshing display 1...");
    oled1.firstPage();
    do {
      setTextDisplay1();
    } while (oled1.nextPage());
    DEBUG_PRINTLN("Refreshing display 2...");
    oled2.firstPage();
    do {
      setTextDisplay2();
    } while (oled2.nextPage());
  }
}

/**
 * Writes texts of corresponding layer to display 1.
 * @see drawText
 * @see refreshDisplays
 * @see currentLayer
 * @see layerNames
 * @see layerButtonFunc
 */
void setTextDisplay1() {  
  // draw layer text
  char layerBuf[MAX_LAYER_LENGTH + 1];  // buffer to read layer name to
  getProgMemStr(layerNames[currentLayer], layerBuf);
  drawText(layerBuf, CENTER, 0, &oled1);

  // draw lines
  oled1.drawLine(LEFT, HLINE1, RIGHT, HLINE1);  // -----
  oled1.drawLine(VLINE1, TOP, VLINE1, BOTTOM);  //   |

  // draw key texts
  char actionBuf[MAX_KEY_LENGTH + 1];  // buffer to read key name to
  for (byte i = 0; i < OLED_KEY_COUNT; i++) {
    byte row = i / OLED_COL_COUNT;
    byte col = i % OLED_COL_COUNT;
    getProgMemStr(layerButtonFunc[currentLayer][i], actionBuf);
    drawText(actionBuf, col * COL_WIDTH + (COL_WIDTH / 2), row * ROW_HEIGHT + TOP, &oled1);
  }
}

/**
 * Writes texts of corresponding layer to display 2.
 * @see drawText
 * @see refreshDisplays
 * @see currentLayer
 * @see layerNames
 * @see layerButtonFunc
 */
void setTextDisplay2() {
  // draw lines
  oled2.drawLine(LEFT, HLINE1, RIGHT, HLINE1);  // -----
  oled2.drawLine(VLINE1, TOP, VLINE1, BOTTOM);  //   |

  // draw key texts
  char actionBuf[MAX_KEY_LENGTH + 1];  // buffer to read key name to
  for (byte i = 0; i < OLED_KEY_COUNT; i++) {
    byte row = i / OLED_COL_COUNT;
    byte col = i % OLED_COL_COUNT;
    getProgMemStr(layerButtonFunc[currentLayer][i + OLED_KEY_COUNT], actionBuf);
    drawText(actionBuf, col * COL_WIDTH + (COL_WIDTH / 2), row * ROW_HEIGHT + TOP, &oled2);
  }
}

/**
 * Draw a text at a specific position on the display taking text height and width into account.
 */
void drawText(const char *buf, byte xPosition, byte yPosition, U8G2 *oled) {
  // get text dimensions
  byte h = oled->getFontAscent() - oled->getFontDescent();
  byte w = oled->getStrWidth(buf);

  // draw text
  oled->setCursor(xPosition - w / 2, yPosition + h);
  oled->print(buf);
}


// ============ LEDs =============

/**
 * Turns all LEDs on and sets their default color according to the current layer.
 */
void setLedDefaultValues() {
  DEBUG_PRINTLN("Setting default led colors...");

  for (byte rowIndex = 0; rowIndex < ROW_COUNT; rowIndex++)
    for (byte colIndex = 0; colIndex < COL_COUNT; colIndex++)
      keys[rowIndex][colIndex].setLedDefault();
}

/**
 * Get the led index of the specified key row and column.
 * @return the led index as byte.
 */
byte getLedIndex(byte rowIdx, byte colIdx) {
  byte index = rowIdx * COL_COUNT;
  
  // account for S-shape of led strip
  index += rowIdx % 2 == 0 ? colIdx : COL_COUNT - 1 - colIdx;
  return index;
}


// ========== MATRIX =============

/**
 * Scans the key matrix to see if a key is pressed.
 * @see keys
 * @see keyPressed
 * @see resetKey
 */
void readMatrix() {
  // iterate through matrix
  for (byte rowIndex = 0; rowIndex < ROW_COUNT; rowIndex++) {
    for (byte colIndex = 0; colIndex < COL_COUNT; colIndex++) {
      // check if key is pressed and handle press if necessary
      keys[rowIndex][colIndex].check();
    }
  }
}

/**
 * Key-pressed event handler passed to key objects.
 * Calls the corresponding handler function of the pressed key.
 * @see handleLayerKeyPress
 * @see keyOnePressed
 * @see keyTwoPressed
 * @see keyThreePressed
 * @see keyFourPressed
 * @see keyFivePressed
 * @see keySixPressed
 * @see keySevenPressed
 * @see keyEightPressed
 * @see keyNinePressed
 * @see keyTenPressed
 * @see keyElevenPressed
 * @see keyTwelvePressed
 */
void handleKeyPress(Key *key) {
  lastKeyPress = millis();

  if(sleeping){   // if sleeping just wake up
    wake();
    return;
  }
  
  switch (key->getIndex()) {
    case KEY_LAYER_UP:
      currentLayer = (currentLayer + 1) % MAX_LAYER;
      setLedDefaultValues();
      refreshDisplays();
      DEBUG_PRINTLN("Layer up pressed");
      break;
    case KEY_LAYER_HOME:
      currentLayer = HOME_LAYER;
      setLedDefaultValues();
      refreshDisplays();
      DEBUG_PRINTLN("Layer home pressed");
      break;
    case KEY_LAYER_DOWN:
      currentLayer = (currentLayer - 1 + MAX_LAYER) % MAX_LAYER;
      setLedDefaultValues();
      refreshDisplays();
      DEBUG_PRINTLN("Layer down pressed");
      break;
    case KEY_1:
      keyOnePressed(key);
      DEBUG_PRINTLN("Key one pressed");
      break;
    case KEY_2:
      keyTwoPressed(key);
      DEBUG_PRINTLN("Key two pressed");
      break;
    case KEY_3:
      keyThreePressed(key);
      DEBUG_PRINTLN("Key three pressed");
      break;
    case KEY_4:
      keyFourPressed(key);
      DEBUG_PRINTLN("Key four pressed");
      break;
    case KEY_5:
      keyFivePressed(key);
      DEBUG_PRINTLN("Key five pressed");
      break;
    case KEY_6:
      keySixPressed(key);
      DEBUG_PRINTLN("Key six pressed");
      break;
    case KEY_7:
      keySevenPressed(key);
      DEBUG_PRINTLN("Key seven pressed");
      break;
    case KEY_8:
      keyEightPressed(key);
      DEBUG_PRINTLN("Key eight pressed");
      break;
    case KEY_9:
      keyNinePressed(key);
      DEBUG_PRINTLN("Key nine pressed");
      break;
    case KEY_10:
      keyTenPressed(key);
      DEBUG_PRINTLN("Key ten pressed");
      break;
    case KEY_11:
      keyElevenPressed(key);
      DEBUG_PRINTLN("Key eleven pressed");
      break;
    case KEY_12:
      keyTwelvePressed(key);
      DEBUG_PRINTLN("Key twelve pressed");
      break;
    default:
      DEBUG_PRINTLN("ERROR: Unknown key pressed");
      break;
  }

  Keyboard.releaseAll();  // protection against unwanted spam
}


// =========== SLEEP =============

/**
 * Sets components to sleep.
 */
void sleep() {
  sleeping = true;
  sleepDisplays();
  sleepLedStrip();
}

/**
 * Wakes components from sleep.
 */
void wake() {
  sleeping = false;
  wakeDisplay();
  wakeLEDStrip();
}

/**
 * Forces @link displays into sleep.
 */
void sleepDisplays() {
  DEBUG_PRINTLN("Display going to sleep...");
  oled1.sleepOn();
  oled2.sleepOn();
}

/**
 * Wakes @link displays from sleep.
 */
void wakeDisplay() {
  DEBUG_PRINTLN("Display waking up...");
  oled1.sleepOff();
  oled2.sleepOff();
  refreshDisplays();
}

/**
 * Forces @link ledStrip into sleep.
 */
void sleepLedStrip() {
  DEBUG_PRINTLN("LEDs going to sleep...");
  ledStrip.clear();
  ledStrip.show();
}

/**
 * Wakes @link ledStrip from sleep and sets each led with its last color.
 */
void wakeLEDStrip() {
  DEBUG_PRINTLN("LEDs waking up...");
  for (byte rowIndex = 0; rowIndex < ROW_COUNT; rowIndex++)
    for (byte colIndex = 0; colIndex < COL_COUNT; colIndex++)
      keys[rowIndex][colIndex].setLedOn();
}


// ======== PROGMEM ==============

/**
 * Copies a string from the program memory into the destination.
 * Reduced RAM usage.
 */
void getProgMemStr(const char str[], char dest[]) {
  char c;
  byte i = 0;
  while ((c = pgm_read_byte(str++))) {
    dest[i] = c;
    i++;
  }
  dest[i] = '\0';
}