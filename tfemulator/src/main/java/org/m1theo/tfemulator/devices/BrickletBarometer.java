/*
 *  Copyright (c) 2015 Thomas Weiss <theo@m1theo.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
/* ***********************************************************
 * This file was automatically generated.      *
 *                                                           *
 * If you have a bugfix for this file and want to commit it, *
 * please fix the bug in the emulator generator.             *
 *************************************************************/

package org.m1theo.tfemulator.devices;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import org.m1theo.tfemulator.Brickd;
import org.m1theo.tfemulator.CommonServices;
import org.m1theo.tfemulator.Utils;
import org.m1theo.tfemulator.protocol.Packet;

/**
 * Measures air pressure and altitude changes
 */
public class BrickletBarometer extends AbstractVerticle {
long uidBytes;
int[] apiVersion = new int[3];
private Logger logger;

public final static int DEVICE_IDENTIFIER = 221;
public final static String DEVICE_DISPLAY_NAME = "Barometer Bricklet";

  public final static byte FUNCTION_GET_AIR_PRESSURE = (byte)1;
  public final static byte FUNCTION_GET_ALTITUDE = (byte)2;
  public final static byte FUNCTION_SET_AIR_PRESSURE_CALLBACK_PERIOD = (byte)3;
  public final static byte FUNCTION_GET_AIR_PRESSURE_CALLBACK_PERIOD = (byte)4;
  public final static byte FUNCTION_SET_ALTITUDE_CALLBACK_PERIOD = (byte)5;
  public final static byte FUNCTION_GET_ALTITUDE_CALLBACK_PERIOD = (byte)6;
  public final static byte FUNCTION_SET_AIR_PRESSURE_CALLBACK_THRESHOLD = (byte)7;
  public final static byte FUNCTION_GET_AIR_PRESSURE_CALLBACK_THRESHOLD = (byte)8;
  public final static byte FUNCTION_SET_ALTITUDE_CALLBACK_THRESHOLD = (byte)9;
  public final static byte FUNCTION_GET_ALTITUDE_CALLBACK_THRESHOLD = (byte)10;
  public final static byte FUNCTION_SET_DEBOUNCE_PERIOD = (byte)11;
  public final static byte FUNCTION_GET_DEBOUNCE_PERIOD = (byte)12;
  public final static byte FUNCTION_SET_REFERENCE_AIR_PRESSURE = (byte)13;
  public final static byte FUNCTION_GET_CHIP_TEMPERATURE = (byte)14;
  public final static byte CALLBACK_AIR_PRESSURE = (byte)15;
  public final static byte CALLBACK_ALTITUDE = (byte)16;
  public final static byte CALLBACK_AIR_PRESSURE_REACHED = (byte)17;
  public final static byte CALLBACK_ALTITUDE_REACHED = (byte)18;
  public final static byte FUNCTION_GET_REFERENCE_AIR_PRESSURE = (byte)19;
  public final static byte FUNCTION_SET_AVERAGING = (byte)20;
  public final static byte FUNCTION_GET_AVERAGING = (byte)21;
  public final static byte FUNCTION_GET_IDENTITY = (byte)255;

  public final static char THRESHOLD_OPTION_OFF = 'x';
  public final static char THRESHOLD_OPTION_OUTSIDE = 'o';
  public final static char THRESHOLD_OPTION_INSIDE = 'i';
  public final static char THRESHOLD_OPTION_SMALLER = '<';
  public final static char THRESHOLD_OPTION_GREATER = '>';
  String uidString;
  private Buffer airPressureCallbackPeriod = getAirPressureCallbackPeriodDefault();
  private Buffer altitudeCallbackThreshold = getAltitudeCallbackThresholdDefault();
  private Buffer altitudeCallbackPeriod = getAltitudeCallbackPeriodDefault();
  private Buffer referenceAirPressure = getReferenceAirPressureDefault();
  private Buffer averaging = getAveragingDefault();
  private Buffer airPressureCallbackThreshold = getAirPressureCallbackThresholdDefault();
  private Buffer debouncePeriod = getDebouncePeriodDefault();

  /**
   * Starts a verticle for the device with the unique device ID \c uid.
   */
  @Override
  public void start() throws Exception {

    apiVersion[0] = 2;
    apiVersion[1] = 0;
    apiVersion[2] = 1;

    logger = LoggerFactory.getLogger(getClass());

    logger.info("Verticle started: " + BrickletBarometer.class);
    uidString = config().getString("uid");
    uidBytes = Utils.uid2long(uidString);

    vertx.eventBus().consumer(uidString, message -> {
      Buffer msgBuffer = (Buffer) message.body();
      Packet packet = new Packet(msgBuffer);
      logger.trace("got request: {}", packet.toString());
      Set<Object> handlerids = vertx.sharedData().getLocalMap(Brickd.HANDLERIDMAP).keySet();
      for (Object handlerid : handlerids) {
        Buffer buffer = callFunction(packet);
        // TODO add logging
        if (packet.getResponseExpected()) {
            if (buffer != null) {
              logger.trace(
                  "sending answer: {}", new Packet(buffer).toString());
              vertx.eventBus().publish((String) handlerid, buffer);
            } else {
              logger.trace("buffer is null");
            }
        }
      }
      });

    // broadcast queue for enumeration requests
    vertx.eventBus().consumer(
        CommonServices.BROADCAST_UID,
        message -> {
          Set<Object> handlerids = vertx.sharedData().getLocalMap(Brickd.HANDLERIDMAP).keySet();
          if (handlerids != null) {
            logger.debug("sending enumerate answer");
            for (Object handlerid : handlerids) {
              vertx.eventBus().publish((String) handlerid,
                  Utils.getEnumerateResponse(uidString, uidBytes, DEVICE_IDENTIFIER));
            }
          } else {
            logger.error("no handlerids found");
          }
        });

  }

  private Buffer callFunction(Packet packet) {
    Buffer buffer = null;
    byte functionId = packet.getFunctionId();
    if (functionId == 0 ){
      //TODO raise Exception or log error
    }
    else if (functionId == FUNCTION_GET_AIR_PRESSURE) {
      buffer = getAirPressure(packet);
    }
    else if (functionId == FUNCTION_GET_ALTITUDE) {
      buffer = getAltitude(packet);
    }
    else if (functionId == FUNCTION_SET_AIR_PRESSURE_CALLBACK_PERIOD) {
      buffer = setAirPressureCallbackPeriod(packet);
    }
    else if (functionId == FUNCTION_GET_AIR_PRESSURE_CALLBACK_PERIOD) {
      buffer = getAirPressureCallbackPeriod(packet);
    }
    else if (functionId == FUNCTION_SET_ALTITUDE_CALLBACK_PERIOD) {
      buffer = setAltitudeCallbackPeriod(packet);
    }
    else if (functionId == FUNCTION_GET_ALTITUDE_CALLBACK_PERIOD) {
      buffer = getAltitudeCallbackPeriod(packet);
    }
    else if (functionId == FUNCTION_SET_AIR_PRESSURE_CALLBACK_THRESHOLD) {
      buffer = setAirPressureCallbackThreshold(packet);
    }
    else if (functionId == FUNCTION_GET_AIR_PRESSURE_CALLBACK_THRESHOLD) {
      buffer = getAirPressureCallbackThreshold(packet);
    }
    else if (functionId == FUNCTION_SET_ALTITUDE_CALLBACK_THRESHOLD) {
      buffer = setAltitudeCallbackThreshold(packet);
    }
    else if (functionId == FUNCTION_GET_ALTITUDE_CALLBACK_THRESHOLD) {
      buffer = getAltitudeCallbackThreshold(packet);
    }
    else if (functionId == FUNCTION_SET_DEBOUNCE_PERIOD) {
      buffer = setDebouncePeriod(packet);
    }
    else if (functionId == FUNCTION_GET_DEBOUNCE_PERIOD) {
      buffer = getDebouncePeriod(packet);
    }
    else if (functionId == FUNCTION_SET_REFERENCE_AIR_PRESSURE) {
      buffer = setReferenceAirPressure(packet);
    }
    else if (functionId == FUNCTION_GET_CHIP_TEMPERATURE) {
      buffer = getChipTemperature(packet);
    }
    else if (functionId == FUNCTION_GET_REFERENCE_AIR_PRESSURE) {
      buffer = getReferenceAirPressure(packet);
    }
    else if (functionId == FUNCTION_SET_AVERAGING) {
      buffer = setAveraging(packet);
    }
    else if (functionId == FUNCTION_GET_AVERAGING) {
      buffer = getAveraging(packet);
    }
    else if (functionId == FUNCTION_GET_IDENTITY) {
      buffer = getIdentity(packet);
    }
    else {
      // TODO: raise Exception or log error
    }
    return buffer;
  }


  /**
   * 
   */
  private Buffer getAltitude(Packet packet) {
    logger.debug("function getAltitude");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 4;
      byte functionId = FUNCTION_GET_ALTITUDE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBytes(Utils.get4ByteRandomValue(1));        

      return buffer;
    }

    return null;
  }

  /**
   * 
   */
  private Buffer getChipTemperature(Packet packet) {
    logger.debug("function getChipTemperature");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 2;
      byte functionId = FUNCTION_GET_CHIP_TEMPERATURE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBytes(Utils.get2ByteRandomValue(1));        

      return buffer;
    }

    return null;
  }

  /**
   * 
   */
  private Buffer getAirPressure(Packet packet) {
    logger.debug("function getAirPressure");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 4;
      byte functionId = FUNCTION_GET_AIR_PRESSURE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBytes(Utils.get4ByteRandomValue(1));        

      return buffer;
    }

    return null;
  }

  /**
   * 
   */
  private Buffer getAirPressureCallbackPeriod(Packet packet) {
    logger.debug("function getAirPressureCallbackPeriod");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 4;
      byte functionId = FUNCTION_GET_AIR_PRESSURE_CALLBACK_PERIOD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.airPressureCallbackPeriod);
      return buffer;
    }

    return null;
  }

  private Buffer getAirPressureCallbackPeriodDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get4ByteURandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer getAltitudeCallbackThreshold(Packet packet) {
    logger.debug("function getAltitudeCallbackThreshold");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 9;
      byte functionId = FUNCTION_GET_ALTITUDE_CALLBACK_THRESHOLD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.altitudeCallbackThreshold);
      return buffer;
    }

    return null;
  }

  private Buffer getAltitudeCallbackThresholdDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.getCharRandomValue(1));        
      buffer.appendBytes(Utils.get4ByteRandomValue(1));        
      buffer.appendBytes(Utils.get4ByteRandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer getAltitudeCallbackPeriod(Packet packet) {
    logger.debug("function getAltitudeCallbackPeriod");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 4;
      byte functionId = FUNCTION_GET_ALTITUDE_CALLBACK_PERIOD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.altitudeCallbackPeriod);
      return buffer;
    }

    return null;
  }

  private Buffer getAltitudeCallbackPeriodDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get4ByteURandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer getReferenceAirPressure(Packet packet) {
    logger.debug("function getReferenceAirPressure");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 4;
      byte functionId = FUNCTION_GET_REFERENCE_AIR_PRESSURE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.referenceAirPressure);
      return buffer;
    }

    return null;
  }

  private Buffer getReferenceAirPressureDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get4ByteRandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer getAveraging(Packet packet) {
    logger.debug("function getAveraging");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 3;
      byte functionId = FUNCTION_GET_AVERAGING;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.averaging);
      return buffer;
    }

    return null;
  }

  private Buffer getAveragingDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get1ByteURandomValue(1));        
      buffer.appendBytes(Utils.get1ByteURandomValue(1));        
      buffer.appendBytes(Utils.get1ByteURandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer getAirPressureCallbackThreshold(Packet packet) {
    logger.debug("function getAirPressureCallbackThreshold");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 9;
      byte functionId = FUNCTION_GET_AIR_PRESSURE_CALLBACK_THRESHOLD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.airPressureCallbackThreshold);
      return buffer;
    }

    return null;
  }

  private Buffer getAirPressureCallbackThresholdDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.getCharRandomValue(1));        
      buffer.appendBytes(Utils.get4ByteRandomValue(1));        
      buffer.appendBytes(Utils.get4ByteRandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer getDebouncePeriod(Packet packet) {
    logger.debug("function getDebouncePeriod");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 4;
      byte functionId = FUNCTION_GET_DEBOUNCE_PERIOD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.debouncePeriod);
      return buffer;
    }

    return null;
  }

  private Buffer getDebouncePeriodDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get4ByteURandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer setReferenceAirPressure(Packet packet) {
    logger.debug("function setReferenceAirPressure");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_REFERENCE_AIR_PRESSURE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.referenceAirPressure = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer setAirPressureCallbackPeriod(Packet packet) {
    logger.debug("function setAirPressureCallbackPeriod");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_AIR_PRESSURE_CALLBACK_PERIOD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.airPressureCallbackPeriod = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer setAltitudeCallbackThreshold(Packet packet) {
    logger.debug("function setAltitudeCallbackThreshold");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_ALTITUDE_CALLBACK_THRESHOLD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.altitudeCallbackThreshold = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer setAltitudeCallbackPeriod(Packet packet) {
    logger.debug("function setAltitudeCallbackPeriod");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_ALTITUDE_CALLBACK_PERIOD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.altitudeCallbackPeriod = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer setAveraging(Packet packet) {
    logger.debug("function setAveraging");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_AVERAGING;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.averaging = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer setAirPressureCallbackThreshold(Packet packet) {
    logger.debug("function setAirPressureCallbackThreshold");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_AIR_PRESSURE_CALLBACK_THRESHOLD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.airPressureCallbackThreshold = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer setDebouncePeriod(Packet packet) {
    logger.debug("function setDebouncePeriod");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_DEBOUNCE_PERIOD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.debouncePeriod = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer getIdentity(Packet packet) {
    logger.debug("function getIdentity");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 25;
      byte functionId = FUNCTION_GET_IDENTITY;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
       buffer.appendBuffer(Utils.getIdentityPayload(uidString, uidBytes, DEVICE_IDENTIFIER));
      return buffer;
    }

    return null;
  }
}
