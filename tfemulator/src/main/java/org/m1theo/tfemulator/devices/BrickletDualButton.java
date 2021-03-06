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
 * Two tactile buttons with built-in blue LEDs
 */
public class BrickletDualButton extends AbstractVerticle {
long uidBytes;
int[] apiVersion = new int[3];
private Logger logger;

public final static int DEVICE_IDENTIFIER = 230;
public final static String DEVICE_DISPLAY_NAME = "Dual Button Bricklet";

  public final static byte FUNCTION_SET_LED_STATE = (byte)1;
  public final static byte FUNCTION_GET_LED_STATE = (byte)2;
  public final static byte FUNCTION_GET_BUTTON_STATE = (byte)3;
  public final static byte CALLBACK_STATE_CHANGED = (byte)4;
  public final static byte FUNCTION_SET_SELECTED_LED_STATE = (byte)5;
  public final static byte FUNCTION_GET_IDENTITY = (byte)255;

  public final static short LED_STATE_AUTO_TOGGLE_ON = (short)0;
  public final static short LED_STATE_AUTO_TOGGLE_OFF = (short)1;
  public final static short LED_STATE_ON = (short)2;
  public final static short LED_STATE_OFF = (short)3;
  public final static short BUTTON_STATE_PRESSED = (short)0;
  public final static short BUTTON_STATE_RELEASED = (short)1;
  public final static short LED_LEFT = (short)0;
  public final static short LED_RIGHT = (short)1;
  String uidString;
  private Buffer lEDState = getLEDStateDefault();

  /**
   * Starts a verticle for the device with the unique device ID \c uid.
   */
  @Override
  public void start() throws Exception {

    apiVersion[0] = 2;
    apiVersion[1] = 0;
    apiVersion[2] = 0;

    logger = LoggerFactory.getLogger(getClass());

    logger.info("Verticle started: " + BrickletDualButton.class);
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
    else if (functionId == FUNCTION_SET_LED_STATE) {
      buffer = setLEDState(packet);
    }
    else if (functionId == FUNCTION_GET_LED_STATE) {
      buffer = getLEDState(packet);
    }
    else if (functionId == FUNCTION_GET_BUTTON_STATE) {
      buffer = getButtonState(packet);
    }
    else if (functionId == FUNCTION_SET_SELECTED_LED_STATE) {
      buffer = setSelectedLEDState(packet);
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
  private Buffer getButtonState(Packet packet) {
    logger.debug("function getButtonState");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 2;
      byte functionId = FUNCTION_GET_BUTTON_STATE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBytes(Utils.get1ByteURandomValue(1));        
      buffer.appendBytes(Utils.get1ByteURandomValue(1));        

      return buffer;
    }

    return null;
  }

  /**
   * 
   */
  private Buffer getLEDState(Packet packet) {
    logger.debug("function getLEDState");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 2;
      byte functionId = FUNCTION_GET_LED_STATE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.lEDState);
      return buffer;
    }

    return null;
  }

  private Buffer getLEDStateDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get1ByteURandomValue(1));        
      buffer.appendBytes(Utils.get1ByteURandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer setLEDState(Packet packet) {
    logger.debug("function setLEDState");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_LED_STATE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.lEDState = packet.getPayload();
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

  /**
   * 
   */
  private Buffer setSelectedLEDState(Packet packet) {
    //TODO dummy method
    return null;
  }
}
