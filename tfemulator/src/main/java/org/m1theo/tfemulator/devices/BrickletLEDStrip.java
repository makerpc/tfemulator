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
 * Controls up to 320 RGB LEDs
 */
public class BrickletLEDStrip extends AbstractVerticle {
long uidBytes;
int[] apiVersion = new int[3];
private Logger logger;

public final static int DEVICE_IDENTIFIER = 231;
public final static String DEVICE_DISPLAY_NAME = "LED Strip Bricklet";

  public final static byte FUNCTION_SET_RGB_VALUES = (byte)1;
  public final static byte FUNCTION_GET_RGB_VALUES = (byte)2;
  public final static byte FUNCTION_SET_FRAME_DURATION = (byte)3;
  public final static byte FUNCTION_GET_FRAME_DURATION = (byte)4;
  public final static byte FUNCTION_GET_SUPPLY_VOLTAGE = (byte)5;
  public final static byte CALLBACK_FRAME_RENDERED = (byte)6;
  public final static byte FUNCTION_SET_CLOCK_FREQUENCY = (byte)7;
  public final static byte FUNCTION_GET_CLOCK_FREQUENCY = (byte)8;
  public final static byte FUNCTION_SET_CHIP_TYPE = (byte)9;
  public final static byte FUNCTION_GET_CHIP_TYPE = (byte)10;
  public final static byte FUNCTION_GET_IDENTITY = (byte)255;

  public final static int CHIP_TYPE_WS2801 = 2801;
  public final static int CHIP_TYPE_WS2811 = 2811;
  public final static int CHIP_TYPE_WS2812 = 2812;
  String uidString;
  private Buffer chipType = getChipTypeDefault();
  private Buffer rGBValues = getRGBValuesDefault();
  private Buffer frameDuration = getFrameDurationDefault();
  private Buffer clockFrequency = getClockFrequencyDefault();

  /**
   * Starts a verticle for the device with the unique device ID \c uid.
   */
  @Override
  public void start() throws Exception {

    apiVersion[0] = 2;
    apiVersion[1] = 0;
    apiVersion[2] = 2;

    logger = LoggerFactory.getLogger(getClass());

    logger.info("Verticle started: " + BrickletLEDStrip.class);
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
    else if (functionId == FUNCTION_SET_RGB_VALUES) {
      buffer = setRGBValues(packet);
    }
    else if (functionId == FUNCTION_GET_RGB_VALUES) {
      buffer = getRGBValues(packet);
    }
    else if (functionId == FUNCTION_SET_FRAME_DURATION) {
      buffer = setFrameDuration(packet);
    }
    else if (functionId == FUNCTION_GET_FRAME_DURATION) {
      buffer = getFrameDuration(packet);
    }
    else if (functionId == FUNCTION_GET_SUPPLY_VOLTAGE) {
      buffer = getSupplyVoltage(packet);
    }
    else if (functionId == FUNCTION_SET_CLOCK_FREQUENCY) {
      buffer = setClockFrequency(packet);
    }
    else if (functionId == FUNCTION_GET_CLOCK_FREQUENCY) {
      buffer = getClockFrequency(packet);
    }
    else if (functionId == FUNCTION_SET_CHIP_TYPE) {
      buffer = setChipType(packet);
    }
    else if (functionId == FUNCTION_GET_CHIP_TYPE) {
      buffer = getChipType(packet);
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
  private Buffer getSupplyVoltage(Packet packet) {
    logger.debug("function getSupplyVoltage");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 2;
      byte functionId = FUNCTION_GET_SUPPLY_VOLTAGE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBytes(Utils.get2ByteURandomValue(1));        

      return buffer;
    }

    return null;
  }

  /**
   * 
   */
  private Buffer getChipType(Packet packet) {
    logger.debug("function getChipType");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 2;
      byte functionId = FUNCTION_GET_CHIP_TYPE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.chipType);
      return buffer;
    }

    return null;
  }

  private Buffer getChipTypeDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get2ByteURandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer getRGBValues(Packet packet) {
    logger.debug("function getRGBValues");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 48;
      byte functionId = FUNCTION_GET_RGB_VALUES;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.rGBValues);
      return buffer;
    }

    return null;
  }

  private Buffer getRGBValuesDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get1ByteURandomValue(16));        
      buffer.appendBytes(Utils.get1ByteURandomValue(16));        
      buffer.appendBytes(Utils.get1ByteURandomValue(16));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer getFrameDuration(Packet packet) {
    logger.debug("function getFrameDuration");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 2;
      byte functionId = FUNCTION_GET_FRAME_DURATION;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.frameDuration);
      return buffer;
    }

    return null;
  }

  private Buffer getFrameDurationDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get2ByteURandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer getClockFrequency(Packet packet) {
    logger.debug("function getClockFrequency");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 4;
      byte functionId = FUNCTION_GET_CLOCK_FREQUENCY;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.clockFrequency);
      return buffer;
    }

    return null;
  }

  private Buffer getClockFrequencyDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get4ByteURandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer setClockFrequency(Packet packet) {
    logger.debug("function setClockFrequency");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_CLOCK_FREQUENCY;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.clockFrequency = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer setRGBValues(Packet packet) {
    logger.debug("function setRGBValues");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_RGB_VALUES;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.rGBValues = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer setChipType(Packet packet) {
    logger.debug("function setChipType");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_CHIP_TYPE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.chipType = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer setFrameDuration(Packet packet) {
    logger.debug("function setFrameDuration");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_FRAME_DURATION;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.frameDuration = packet.getPayload();
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
