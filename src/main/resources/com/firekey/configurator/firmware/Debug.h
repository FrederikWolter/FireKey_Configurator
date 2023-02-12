/*************************************************************************
* FIREKEY-PROJECT
* 
* DEBUG.h
* File defining preprocessor macros used for an efficient implementation
* of a debug mode.
*************************************************************************/

#ifndef DEBUG_H
  #define DEBUG_H

  #ifdef DEBUG
    #define DEBUG_BEGIN()       { Serial.begin(9600); while(!Serial); Serial.print(F("\n")); }
    #define DEBUG_PRINT(str)    { Serial.print(F(str)); }
    #define DEBUG_PRINTLN(str)  { Serial.println(F(str)); }
  #else
    #define DEBUG_BEGIN()
    #define DEBUG_PRINT(str)
    #define DEBUG_PRINTLN(str)
  #endif

#endif