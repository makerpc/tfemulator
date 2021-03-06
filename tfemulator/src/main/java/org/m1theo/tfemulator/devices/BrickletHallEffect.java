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
 * Detects presence of magnetic field
 */
public class BrickletHallEffect extends AbstractVerticle {
long uidBytes;
int[] apiVersion = new int[3];
private Logger logger;

public final static int DEVICE_IDENTIFIER = 240;
public final static String DEVICE_DISPLAY_NAME = "Hall Effect Bricklet";

  public final static byte FUNCTION_GET_VALUE = (byte)1;
  public final static byte FUNCTION_GET_EDGE_COUNT = (byte)2;
  public final static byte FUNCTION_SET_EDGE_COUNT_CONFIG = (byte)3;
  public final static byte FUNCTION_GET_EDGE_COUNT_CONFIG = (byte)4;
  public final static byte FUNCTION_SET_EDGE_INTERRUPT = (byte)5;
  public final static byte FUNCTION_GET_EDGE_INTERRUPT = (byte)6;
  public final static byte FUNCTION_SET_EDGE_COUNT_CALLBACK_PERIOD = (byte)7;
  public final static byte FUNCTION_GET_EDGE_COUNT_CALLBACK_PERIOD = (byte)8;
  public final static byte FUNCTION_EDGE_INTERRUPT = (byte)9;
  public final static byte CALLBACK_EDGE_COUNT = (byte)10;
  public final static byte FUNCTION_GET_IDENTITY = (byte)255;

  public final static short EDGE_TYPE_RISING = (short)0;
  public final static short EDGE_TYPE_FALLING = (short)1;
  public final static short EDGE_TYPE_BOTH = (short)2;
  String uidString;
  private Buffer edgeCountConfig = getEdgeCountConfigDefault();
  private Buffer edgeCountCallbackPeriod = getEdgeCountCallbackPeriodDefault();
  private Buffer edgeInterrupt = getEdgeInterruptDefault();

  /**
   * Starts a verticle for the device with the unique device ID \c uid.
   */
  @Override
  public void start() throws Exception {

    apiVersion[0] = 2;
    apiVersion[1] = 0;
    apiVersion[2] = 0;

    logger = LoggerFactory.getLogger(getClass());

    logger.info("Verticle started: " + BrickletHallEffect.class);
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
    else if (functionId == FUNCTION_GET_VALUE) {
      buffer = getValue(packet);
    }
    else if (functionId == FUNCTION_GET_EDGE_COUNT) {
      buffer = getEdgeCount(packet);
    }
    else if (functionId == FUNCTION_SET_EDGE_COUNT_CONFIG) {
      buffer = setEdgeCountConfig(packet);
    }
    else if (functionId == FUNCTION_GET_EDGE_COUNT_CONFIG) {
      buffer = getEdgeCountConfig(packet);
    }
    else if (functionId == FUNCTION_SET_EDGE_INTERRUPT) {
      buffer = setEdgeInterrupt(packet);
    }
    else if (functionId == FUNCTION_GET_EDGE_INTERRUPT) {
      buffer = getEdgeInterrupt(packet);
    }
    else if (functionId == FUNCTION_SET_EDGE_COUNT_CALLBACK_PERIOD) {
      buffer = setEdgeCountCallbackPeriod(packet);
    }
    else if (functionId == FUNCTION_GET_EDGE_COUNT_CALLBACK_PERIOD) {
      buffer = getEdgeCountCallbackPeriod(packet);
    }
    else if (functionId == FUNCTION_EDGE_INTERRUPT) {
      buffer = edgeInterrupt(packet);
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
  private Buffer getEdgeCount(Packet packet) {
    logger.debug("function getEdgeCount");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 4;
      byte functionId = FUNCTION_GET_EDGE_COUNT;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBytes(Utils.get4ByteURandomValue(1));        

      return buffer;
    }

    return null;
  }

  /**
   * 
   */
  private Buffer getValue(Packet packet) {
    logger.debug("function getValue");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 1;
      byte functionId = FUNCTION_GET_VALUE;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBytes(Utils.getBoolRandomValue(1));        

      return buffer;
    }

    return null;
  }

  /**
   * 
   */
  private Buffer getEdgeCountConfig(Packet packet) {
    logger.debug("function getEdgeCountConfig");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 2;
      byte functionId = FUNCTION_GET_EDGE_COUNT_CONFIG;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.edgeCountConfig);
      return buffer;
    }

    return null;
  }

  private Buffer getEdgeCountConfigDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get1ByteURandomValue(1));        
      buffer.appendBytes(Utils.get1ByteURandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer getEdgeCountCallbackPeriod(Packet packet) {
    logger.debug("function getEdgeCountCallbackPeriod");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 4;
      byte functionId = FUNCTION_GET_EDGE_COUNT_CALLBACK_PERIOD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.edgeCountCallbackPeriod);
      return buffer;
    }

    return null;
  }

  private Buffer getEdgeCountCallbackPeriodDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get4ByteURandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer getEdgeInterrupt(Packet packet) {
    logger.debug("function getEdgeInterrupt");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 4;
      byte functionId = FUNCTION_GET_EDGE_INTERRUPT;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      buffer.appendBuffer(this.edgeInterrupt);
      return buffer;
    }

    return null;
  }

  private Buffer getEdgeInterruptDefault() {
      Buffer buffer = Buffer.buffer();
      buffer.appendBytes(Utils.get4ByteURandomValue(1));        
      return buffer;
  }

  /**
   * 
   */
  private Buffer setEdgeInterrupt(Packet packet) {
    logger.debug("function setEdgeInterrupt");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_EDGE_INTERRUPT;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.edgeInterrupt = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer setEdgeCountCallbackPeriod(Packet packet) {
    logger.debug("function setEdgeCountCallbackPeriod");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_EDGE_COUNT_CALLBACK_PERIOD;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.edgeCountCallbackPeriod = packet.getPayload();
    return null;
  }

  /**
   * 
   */
  private Buffer setEdgeCountConfig(Packet packet) {
    logger.debug("function setEdgeCountConfig");
    if (packet.getResponseExpected()) {
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_SET_EDGE_COUNT_CONFIG;
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
//TODO response expected bei settern
      return buffer;
    }
    this.edgeCountConfig = packet.getPayload();
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
  private Buffer edgeInterrupt(Packet packet) {
    //TODO dummy method
    return null;
  }
}
