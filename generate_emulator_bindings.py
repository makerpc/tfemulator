#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Java Emulator Generator
Copyright (C) 2015 Theo Weiß <theo@m1theo.org>

generate_emulator_bindings.py: Generator for Emulator bindings

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.
"""

import sys
import os

sys.path.append(os.path.split(os.getcwd())[0])
import common
import emulator_common
import emulator_license

class JavaBindingsDevice(emulator_common.JavaDevice):
    def get_generated_hint_docstring(self):
        hint = """
/* ***********************************************************
 * This file was automatically generated.      *
 *                                                           *
 * If you have a bugfix for this file and want to commit it, *
 * please fix the bug in the emulator generator.             *
 *************************************************************/
"""
        return hint

    def get_java_import(self):
        include = """
package org.m1theo.tfemulator.devices;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import org.m1theo.tfemulator.Brickd;
import org.m1theo.tfemulator.CommonServices;
import org.m1theo.tfemulator.Utils;
import org.m1theo.tfemulator.Utils.Step;
import org.m1theo.tfemulator.protocol.Packet;
"""
        return include

    def get_java_class(self):
        class_str = """
/**
 * {1}
 */
public class {0} extends AbstractVerticle {{
long uidBytes;
int[] apiVersion = new int[3];
private Logger logger;

public final static int DEVICE_IDENTIFIER = {2};
public final static String DEVICE_DISPLAY_NAME = "{3}";

"""

        return class_str.format(self.get_java_class_name(),
                                self.get_description(),
                                self.get_device_identifier(),
                                self.get_long_display_name())

    def get_start_method_end(self):
        cbs_end = '  }\n'
        return cbs_end
    
    def get_java_class_end(self):
        cbs_end = '}\n'
        return cbs_end
        
    def get_java_add_listener(self):
        if self.get_callback_count() == 0:
            return '}'

        listeners = ''
        listener = """
  /**
   * Adds a {0} listener.
   */
  public void add{0}Listener({0}Listener listener) {{
    listener{0}.add(listener);
  }}

  /**
   * Removes a {0} listener.
   */
  public void remove{0}Listener({0}Listener listener) {{
    listener{0}.remove(listener);
  }}
"""

        l = []
        for packet in self.get_packets('callback'):
            name = packet.get_camel_case_name()
            listeners += listener.format(name)
        return listeners + '}'

    def get_java_function_id_definitions(self):
        function_ids = ''
        function_id = '  public final static byte {2}_{0} = (byte){1};\n'
        for packet in self.get_packets():
            function_ids += function_id.format(packet.get_upper_case_name(),
                                               packet.get_function_id(),
                                               packet.get_type().upper())
        return function_ids

    def get_java_constants(self):
        constant = '  public final static {0} {1}_{2} = {3}{4};\n'
        constants = []
        for constant_group in self.get_constant_groups():
            typ = emulator_common.get_java_type(constant_group.get_type())

            for constant_item in constant_group.get_items():
                if constant_group.get_type() == 'char':
                    cast = ''
                    value = "'{0}'".format(constant_item.get_value())
                else:
                    if typ == 'int':
                        cast = '' # no need to cast int, its the default type for number literals
                    else:
                        cast = '({0})'.format(typ)

                    value = str(constant_item.get_value())

                    if typ == 'long':
                        cast = ''
                        value += 'L' # mark longs as such, because int is the default type for number literals

                constants.append(constant.format(typ,
                                                 constant_group.get_upper_case_name(),
                                                 constant_item.get_upper_case_name(),
                                                 cast,
                                                 value))
        constants.append('  String uidString;\n')
        return '\n' + ''.join(constants)

    def get_start_method(self):
        con = """
  /**
   * Starts a verticle for the device with the unique device ID \c uid.
   */
  @Override
  public void start() throws Exception {{

    apiVersion[0] = {1};
    apiVersion[1] = {2};
    apiVersion[2] = {3};

    logger = LoggerFactory.getLogger(getClass());

    logger.info("Verticle started: " + {0}.class);
    uidString = config().getString("uid");
    uidBytes = Utils.uid2long(uidString);

    vertx.eventBus().consumer(uidString, message -> {{
      Buffer msgBuffer = (Buffer) message.body();
      Packet packet = new Packet(msgBuffer);
      logger.trace("got request: {{}}", packet.toString());
      Set<Object> handlerids = vertx.sharedData().getLocalMap(Brickd.HANDLERIDMAP).keySet();
      for (Object handlerid : handlerids) {{
        Buffer buffer = callFunction(packet);
        // TODO add logging
        if (packet.getResponseExpected()) {{
            if (buffer != null) {{
              logger.trace(
                  "sending answer: {{}}", new Packet(buffer).toString());
              vertx.eventBus().publish((String) handlerid, buffer);
            }} else {{
              logger.trace("buffer is null");
            }}
        }}
      }}
      }});

    // broadcast queue for enumeration requests
    vertx.eventBus().consumer(
        CommonServices.BROADCAST_UID,
        message -> {{
          Set<Object> handlerids = vertx.sharedData().getLocalMap(Brickd.HANDLERIDMAP).keySet();
          if (handlerids != null) {{
            logger.debug("sending enumerate answer");
            for (Object handlerid : handlerids) {{
              vertx.eventBus().publish((String) handlerid,
                  Utils.getEnumerateResponse(uidString, uidBytes, DEVICE_IDENTIFIER));
            }}
          }} else {{
            logger.error("no handlerids found");
          }}
        }});

"""

        return con.format(self.get_java_class_name(), *self.get_api_version())

    def get_call_function(self):
        ifelse = ""
        funcstart = """
  private Buffer callFunction(Packet packet) {
    Buffer buffer = null;
    byte functionId = packet.getFunctionId();
    if (functionId == 0 ){
      //TODO raise Exception or log error
    }"""

        func = """
    else if (functionId == FUNCTION_{1}) {{
      buffer = {0}(packet);
    }}"""

        funcend = """
    else {
      // TODO: raise Exception or log error
    }
    return buffer;
  }

"""

        for packet in self.get_packets('function'):
            if packet.get_headless_camel_case_name() == "getProtocol1BrickletName":
                continue
            name_lower = packet.get_headless_camel_case_name()
            name_upper = packet.get_upper_case_name()
            
            ifelse += func.format(name_lower, name_upper)
            
        return funcstart + ifelse + funcend

#     def get_emulator_actor_fields(self):
#         field = "  private Buffer {0} = {1}Default();\n"
#         fields = ""
# #FIXME:  hier sollten wohl besser die  getter verwendet werden und dann sowas wie
# #packet.get_emulater_buffer_values implementiert werden
# # noch einfacher einfach den entsprechenden getter aufrufen
# # private Buffer state = getState(true);\n
#         setter = {}
#         setter.update(self.get_actor_getters())
#         setter.update(self.get_callback_getters())
#         setter.update(self.get_isEnableds())
#         for key in setter.keys():
#             packet = setter[key][0]
#             if packet.function_type != 'callback_period_setter':
#                 fields += field.format(setter[key][1], packet.get_headless_camel_case_name())
#             
#         return fields

    def get_java_methods(self):
        methods = ''
        method = """
  /**
   * 
   */
  private Buffer {0}(Packet packet) {{
    logger.debug("function {0}");
    if (packet.getResponseExpected()) {{
      byte length = (byte) 8 + {3};
      byte functionId = FUNCTION_{1};
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
{2}
      return buffer;
    }}
{4}
    return null;
  }}
"""
        default_method = """
  private Buffer {0}Default() {{
      Buffer buffer = Buffer.buffer();
{1}
      return buffer;
  }}
"""

        value_buffer = '      buffer.appendBuffer(this.{0});'
        set_field = ''
        
        cls = self.get_camel_case_name()
#         sensors = self.get_sensors()
#     # get sensors
#         for key in sensors.keys():
#             packet = sensors[key][0]
#     #for packet in self.get_packets('function'):
#             name_lower = packet.get_headless_camel_case_name()
#             name_upper = packet.get_upper_case_name()
#             buffers, bufferbytes = packet.get_emulator_return_values()
#             
#             methods += method.format(name_lower,
#                                      name_upper,
#                                      buffers, 
#                                      bufferbytes,
#                                      set_field)
        actor_getters = {}
        actor_getters.update(self.get_actor_getters())
        actor_getters.update(self.get_callback_getters())
        actor_getters.update(self.get_isEnableds())
        for key in actor_getters.keys():
            packet = actor_getters[key][0]
            field = actor_getters[key][1]
            value_field = value_buffer.format(field)
            name_lower = packet.get_headless_camel_case_name()
            name_upper = packet.get_upper_case_name()
            buffers, bufferbytes = packet.get_emulator_return_values()

            methods += method.format(name_lower,
                                     name_upper,
                                     value_field, 
                                     bufferbytes,
                                     set_field)
            methods += default_method.format(name_lower, buffers)

        callback_period_getters = self.get_callback_period_getters()
        for key in callback_period_getters:
            packet = callback_period_getters[key][0]
            field = callback_period_getters[key][1]
            name_lower = packet.get_headless_camel_case_name()
            name_upper = packet.get_upper_case_name()
            buffers, bufferbytes = packet.get_emulator_return_values2()

            methods += method.format(name_lower,
                                     name_upper,
                                     buffers, 
                                     bufferbytes,
                                     set_field)
            
        setter = {}
        setter.update(self.get_actors())
        setter.update(self.get_callback_setters())
        setter.update(self.get_enablers())
        setter.update(self.get_disablers())
        for key in setter.keys():
            packet = setter[key][0]
            set_field = '    this.{0} = packet.getPayload();'.format(setter[key][1])
            value_field = '//TODO response expected bei settern'
            name_lower = packet.get_headless_camel_case_name()
            name_upper = packet.get_upper_case_name()
            bufferbytes = packet.get_emulator_return_values()[1]

            methods += method.format(name_lower,
                                     name_upper,
                                     value_field, 
                                     bufferbytes,
                                     set_field)
        getIdentity = self.get_getIdentity()
        for key in getIdentity.keys():
            packet = getIdentity[key]
            set_field = ''
            value_field = '       buffer.appendBuffer(Utils.getIdentityPayload(uidString, uidBytes, DEVICE_IDENTIFIER));'
            name_lower = packet.get_headless_camel_case_name()
            name_upper = packet.get_upper_case_name()
            bufferbytes = packet.get_emulator_return_values()[1]
            methods += method.format(name_lower,
                                     name_upper,
                                     value_field, 
                                     bufferbytes,
                                     set_field)
        return methods

    def get_dummy_methods(self):
        methods = ''
        method = """
  /**
   * 
   */
  private Buffer {0}(Packet packet) {{
    //TODO dummy method
    return null;
  }}
"""
        other_actors = self.get_otherActorMethods()
        for key in other_actors:
            packet = other_actors[key][0]
            name_lower = packet.get_headless_camel_case_name()
            methods += method.format(name_lower)

        other_sensors = self.get_otherSensorMethods()
        for key in other_sensors:
            packet = other_sensors[key][0]
            name_lower = packet.get_headless_camel_case_name()
            methods += method.format(name_lower)

        special_methods = self.get_special_setterMethods()
        for key in special_methods:
            packet = special_methods[key][0]
            name_lower = packet.get_headless_camel_case_name()
            methods += method.format(name_lower)

        others = self.get_otherMethods()
        for key in others:
            packet = others[key][0]
            name_lower = packet.get_headless_camel_case_name()
            methods += method.format(name_lower)

        return methods

    def get_test_import(self):
        imports = """package org.m1theo.tfemulator.tests;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.m1theo.tfemulator.Utils;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.{0};
import com.tinkerforge.IPConnection;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
"""
    
        return imports.format(self.get_java_class_name())

    def get_test_class(self, uid):
        classbody = """
@RunWith(VertxUnitRunner.class)
public class {0}Test {{

  Vertx vertx;
  IPConnection ipcon;
  {0} device;

  @Before
  public void before(TestContext context) {{
    String uid = "{1}";
    String host = "localhost";
    int port = 1234;
    JsonObject emuconfig = new JsonObject().put("devices", new JsonArray().add(
        new JsonObject().put("type", "{0}").put("uid", uid).put("enabled", true)));
    System.out.println(emuconfig.encodePrettily());
    DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(emuconfig);

    vertx = Vertx.vertx();
    Async async = context.async();
    vertx.deployVerticle("org.m1theo.tfemulator.Brickd", deploymentOptions, res -> {{
      async.complete();
    }});
    ipcon = new IPConnection(); // Create IP connection
    try {{
      try {{
        Thread.sleep(100);
      }} catch (InterruptedException e) {{
        // TODO Auto-generated catch block
        e.printStackTrace();
      }}
      ipcon.connect(host, port);
    }} catch (AlreadyConnectedException | IOException e) {{
      context.fail(e);
    }}
    device = new {0}(uid, ipcon);
  }}

  @After
  public void after(TestContext context) {{
    // ipcon.disconnect();
    vertx.close(context.asyncAssertSuccess());
  }}
"""
        return classbody.format(self.get_java_class_name(), uid)


    def get_test_booleans(self):
        methods = ''
        method = """
  @Test
  public void test{0}(TestContext context) {{
    try {{
      device.setResponseExpectedAll(true);
      device.{1}(); //enable
      //assert enabled
      {4} value = device.{3}();
{5}
      device.{2}(); //disable
      // assert disabled
      value = device.{3}();
{6}
    }} catch (Exception e) {{
      context.fail(e);
    }}
  }}
"""        
        assert_enabled='context.assertTrue(value);\n'
        assert_disabled='context.assertFalse(value);\n'
        
        booleans = self.get_booleans()
        for key in booleans.keys():
            packet = booleans[key][0]
            name_enable = booleans[key][1]
            name_disable = booleans[key][2]
            name_isenabled = booleans[key][3]
            type = packet.get_java_return_type()
            if type == "short":
                assert_enabled='      context.assertEquals(value, 1);\n'
                assert_disabled='      context.assertEquals(value, 0);\n'
            methods += method.format(key, name_enable, name_disable, name_isenabled, type, 
                                     assert_enabled, assert_disabled)
            
        return methods

    def get_test_sensors(self):
        methods = ''
        method = """
  @Test
  public void test{4}{0}(TestContext context) {{
    try {{
      {1} value = device.{0}({3});
      {2}
    }} catch (Exception e) {{
      context.fail(e);
    }}
  }}
"""        
        sensors = self.get_sensors()
        for key in sensors.keys():
            packet = sensors[key][0]
            method_args = []
            #0: methodname,  1: type of return value, 2: asserts
            return_type, test_asserts, args = packet.get_test_return_values()
            if return_type == None:
                return_type = self.get_java_class_name() + '.' + packet.get_java_object_name()
            if not args:
                methods += method.format(key, return_type, test_asserts, '', '')
            else:
                for arg in args:
                    if len(args) == 1:
                        if arg == 'boolean':
                            methods += method.format(key, return_type, test_asserts, 'true', '1')
                            methods += method.format(key, return_type, test_asserts, 'false', '2')
                        elif arg == 'short':
                            methods += method.format(key, return_type, test_asserts, '(short) 1', '')
                        else:
                            #TODO:
                            print "omitting method: don't know how to handle arg type " + arg
                    else:
                        #TODO concatenamte args
                        print "omitting method: don't know how to handle multiargs"

        return methods

    def get_actuator_fields(self):
        fields = ''
        field_template = """
  private Buffer {field_name} = {field_default};
        """
    
        for f in self.actor_fields.keys():
            field = self.actor_fields[f]
            if field['skip']:
                continue
            field_name = field['field']
            packet = self.get_packet_for_fieldname(field_name)
            if packet.function_type != 'callback_period_setter':
                fields += field_template.format(field_name=field_name,
                                                field_default="get{0}Default()".format(field_name[0].upper() + field_name[1:])
                                                )
        
        return fields
    
    def get_sensor_fields(self):
        """
  private short illuminance;
  private short illuminance_max = 100;
  private short illuminance_min = 0;
  private short illuminance_step = 1;
  private long illuminance_generator_period = 100;
  private Step illuminance_direction = Step.UP;
  private long illuminance_callback_period;
  private long illuminance_callback_id;
  private short illuminance_last_value_called_back;

        """
        fields = ''
        field_template = """
  private {field_type} {field_name} = {field_default};
  private {field_type} {field_name}_max = {max_value};
  private {field_type} {field_name}_min = {min_value};
  private {field_type} {field_name}_step = {step_value};
  private long {field_name}_generator_period = 100;
  private Step {field_name}_direction = Step.UP;
  private long {field_name}CallbackPeriod;
  private long {field_name}_callback_id;
  private {field_type} {field_name}_last_value_called_back;
"""
        
        for f in self.sensor_fields.keys():
            field = self.sensor_fields[f]
            if field['skip']:
                continue
            fields += field_template.format(
                                field_name=field['field'],
                                field_default=field['default_value'],
                                max_value=field['max_value'],
                                min_value=field['min_value'],
                                step_value=field['step_value'],
                                field_type=emulator_common.get_java_byte_buffer_storage_type(field['field_type'][0])) # ignore cardinality for now
            
        return fields

    def get_sensor_value_generator(self):
        generator_inits = ''
        generators = ''
        generator = """
  private void start{field_name_upper}Generator() {{
    if ({field_name}_step == 0) {{
      return;
    }}
    vertx.setPeriodic({field_name}_generator_period, id -> {{
      if ({field_name}_direction == Step.UP) {{
        if ({field_name} >= {field_name}_max) {{
          {field_name}_direction = Step.DOWN;
          this.{field_name} = ({field_type}) ({field_name} - {field_name}_step);
        }} else {{
          this.{field_name} = ({field_type}) ({field_name} + {field_name}_step);
        }}
      }} else {{
        if ({field_name} <= {field_name}_min) {{
          {field_name}_direction = Step.UP;
          this.{field_name} = ({field_type}) ({field_name} + {field_name}_step);
        }} else {{
          this.{field_name} = ({field_type}) ({field_name} - {field_name}_step);
        }}
      }}
    }});
  }}
        """

        generator_init = '    start{field_name_upper}Generator();\n'

        for f in self.sensor_fields.keys():
            field = self.sensor_fields[f]
            if field['skip']:
                continue
            field_name = field['field']
            field_name_upper = field_name[0].upper() + field_name[1:]
            generators += generator.format(
                                    field_name=field_name,
                                    field_name_upper=field_name_upper,
                                    field_type=emulator_common.get_java_byte_buffer_storage_type(field['field_type'][0])) # ignore cardinality for now
            generator_inits += generator_init.format(field_name_upper=field_name_upper)

        return generators, generator_inits

    def get_sensor_callback_methods(self):
        #FIXME:
        dummy_stop_generator = '//fixme stop_generator callback without sensor {field_name}\n'
        dummy_start_generator = '//fixme start_generator callback without sensor {field_name}\n'
        dummy_getter = '//fixme getter callback without sensor {field_name}\n'
        stop_callback = """
    private void stop{field_name_upper}Callback() {{
        vertx.cancelTimer({field_name}_callback_id);
  }}
        """

        start_callback = """
  private void start{field_name_upper}Callback() {{
    logger.trace("{field_name}CallbackPeriod is {{}}", {field_name}CallbackPeriod);
    {field_name}_callback_id = vertx.setPeriodic({field_name}CallbackPeriod, id -> {{
      if ({field_name} != {field_name}_last_value_called_back) {{
        {field_name}_last_value_called_back = {field_name};
        Set<Object> handlerids = vertx.sharedData().getLocalMap(Brickd.HANDLERIDMAP).keySet();
        if (handlerids != null) {{
          logger.debug("{field_name} sending callback value");
          for (Object handlerid : handlerids) {{
            vertx.eventBus().publish((String) handlerid, get{field_name_upper}4Callback());
          }}
        }} else {{
          logger.info("no handlerids found in {field_name} callback");
        }}
      }} else {{
        logger.debug("{field_name} value already called back");
      }}
    }});
  }}

"""
        
        get_for_callback = """
  private Buffer get{field_name_upper}4Callback() {{
      byte options = (byte) 0;
      return get{field_name_upper}Buffer(CALLBACK_{functionId}, options);
  }}
        """

        stop_generators = ''
        start_generators = ''
        getters = ''
        for f in self.callbacks.keys():
            for key in self.sensor_fields.keys():
                if self.sensor_fields[key]['field'] == f:
                    field = self.sensor_fields[key]
                    field_name = field['field']
                    if field['skip']:
                        continue
                    stop_generators += start_callback.format(
                                                    field_name=field_name,
                                                    field_name_upper=field_name[0].upper() + field_name[1:],
                                                    field_type=emulator_common.get_java_byte_buffer_storage_type(field['field_type'][0]) # ignore cardinality for now
                                                    )
                    start_generators += stop_callback.format(
                                                    field_name=field_name,
                                                    field_name_upper=field_name[0].upper() + field_name[1:]
                                                    )
                    getters += get_for_callback.format(field_name_upper=field_name[0].upper() + field_name[1:],
                                                       functionId=self.get_callback_packet_for_fieldname(field_name).get_upper_case_name())
                else:
                    print "sensorfield not found " + f
            else:
                stop_generators += dummy_stop_generator.format(field_name=f)
                start_generators += dummy_start_generator.format(field_name=f)
                getters += dummy_getter.format(field_name=f)
                
        return start_generators, stop_generators, getters
    
    def get_callback_packet_for_fieldname(self, field_name):
        for packet in self.callback_packets:
            if packet.field == field_name:
                return packet
    
    def get_packet_for_fieldname(self, field_name):
        for packet in self.all_function_packets:
            if packet.field == field_name:
                return packet
    
    def get_sensor_callback_period(self):
        #FIXME: muss beim methoden generieren erledigt werden
        period_setters = ''
        period_template = """
  private Buffer {headless_camel_case_name}(Packet packet) {{
    {field_name} = {payload_function} Utils.unsignedInt(packet.getPayload());
    if ({field_name} == 0) {{
      stop{affected_field}Callback();
    }} else {{
      start{affected_field}Callback();
    }}
    if (packet.getResponseExpected()) {{
      byte length = (byte) 8 + 0;
      byte functionId = FUNCTION_{functionId};
      byte flags = (byte) 0;
      Buffer header = Utils.createHeader(uidBytes, length, functionId, packet.getOptions(), flags);
      Buffer buffer = Buffer.buffer();
      buffer.appendBuffer(header);
      return buffer;
    }}
    return null;
  }}
        """
        for packet in self.get_packets('function'):
            if packet.function_type == 'callback_period_setter':
                headless_camel_case_name=packet.get_headless_camel_case_name()
                name_lower = packet.get_headless_camel_case_name()
                name_upper = packet.get_upper_case_name()
                payload_function = packet.get_emulator_return_values2()[0]
                
                #die stop{field_name}Callback(); braucht nicht {field_name} sondern das feld in mit grossbuchstaben am anfang
                #besser wäre es hier ueberall den fieldname der betroffen ist zur verfuegung zu haben

                period_setters += period_template.format(
                                                         headless_camel_case_name=headless_camel_case_name,
                                                         name_lower=name_lower,
                                                         field_name=packet.field,
                                                         affected_field=packet.field[0].upper() + packet.field[1:-14],
                                                         functionId=name_upper,
                                                         payload_function=payload_function
                                                         )
        return period_setters

    def get_callback_buffer(self):
        #drei funktionen
        # 1/ getIlluminanceBuffer(byte functionId, byte options)
        # 2/ getIlluminance() diese wird von callback aufgerufen und ruft getIlluminanceBuffer(Buffer options) mit  options gleich 0 auf
        # 3/ getIlluminance(Packet packet) wird von den gettern aufgerufen und ruft getIlluminanceBuffer(Buffer options) mit den options aus dem Packet auf
        code = ''
        buffer_function = """
  private Buffer get{field_name}Buffer(byte functionId, byte options) {{
    byte length = (byte) 8 + {bufferbytes};
    byte flags = (byte) 0;
    Buffer header = Utils.createHeader(uidBytes, length, functionId, options, flags);
    Buffer buffer = Buffer.buffer();
    buffer.appendBuffer(header);
{buffers}
    return buffer;
  }}
        """
        
        get_for_packet = """
  private Buffer get{field_name}Buffer(Packet packet) {{
      byte options = packet.getOptions();
      return get{field_name}Buffer(FUNCTION_{functionId}, options);
  }}
"""
        get_for_caller = """
  private Buffer get{field_name}(Packet packet) {{
    logger.debug("function get{field_name}");
    if (packet.getResponseExpected()) {{
      return get{field_name}Buffer(packet);
    }}
    return null;
  }}
"""
        for packet in self.get_packets('function'):
            if packet.subdevice_type == 'sensor':
                buffers, bufferbytes = packet.get_emulator_return_values2()
                affected_field = packet.field[0].upper() + packet.field[1:]
                code += buffer_function.format(field_name=affected_field,
                                               bufferbytes=bufferbytes,
                                               buffers=buffers)
                code += get_for_packet.format(field_name=affected_field,
                                                functionId=packet.get_upper_case_name())
                code += get_for_caller.format(field_name=affected_field)
                
        return code

    def get_readme(self):
        readme = '    * {0} dummy\n'
        readmes = ''
        other_actors = self.get_otherActorMethods()
        for key in other_actors:
            packet = other_actors[key][0]
            name_lower = packet.get_headless_camel_case_name()
            readmes += readme.format(name_lower)

        other_sensors = self.get_otherSensorMethods()
        for key in other_sensors:
            packet = other_sensors[key][0]
            name_lower = packet.get_headless_camel_case_name()
            readmes += readme.format(name_lower)

        special_methods = self.get_special_setterMethods()
        for key in special_methods:
            packet = special_methods[key][0]
            name_lower = packet.get_headless_camel_case_name()
            readmes += readme.format(name_lower)

        others = self.get_otherMethods()
        for key in others:
            packet = others[key][0]
            name_lower = packet.get_headless_camel_case_name()
            readmes += readme.format(name_lower)

        return "  * {0}\n" + readmes

    def get_test_source(self, uid):
        source  = emulator_license.get_license()
        source += self.get_generated_hint_docstring()
        source += self.get_test_import()
        source += self.get_test_class(uid)
        source += self.get_test_sensors()
        source += self.get_test_booleans()
        self.get_method_categories()
        #source += self.get_java_return_objects()
        source += self.get_java_class_end()
        
        return source

    def get_emulator_source(self):
        source  = emulator_license.get_license()
        source += self.get_generated_hint_docstring()
        source += self.get_java_import()
        source += self.get_java_class()
        source += self.get_java_function_id_definitions()
        source += self.get_java_constants()
        #source += self.get_emulator_actor_fields()
        source += self.get_actuator_fields()
        source += self.get_sensor_fields()

        generators, generator_inits = self.get_sensor_value_generator()
        source += self.get_start_method()

        #TODO: need this?
        #source += self.get_java_response_expected()

        # TODO: remove listener definitions for now
        #source += self.get_java_callback_listener_definitions()
        source += generator_inits
        
        source += self.get_start_method_end()

        source += self.get_call_function()

        source += generators

        stop_callback, start_callback, getters = self.get_sensor_callback_methods()
        source += stop_callback
        source += start_callback
        source += getters
        source += self.get_sensor_callback_period()
        source += self.get_callback_buffer()
        #FIXME: add generator start to verts start function
        #e.g artIlluminanceGenerator();
        
        #TODO: I think listeners are not needed
        #source += self.get_java_listener_lists()

        #TODO: need this?
        #source += self.get_java_return_objects()

        #TODO: I think listeners are not needed
        #source += self.get_java_listener_definitions()

        source += self.get_java_methods()

        # TODO: a real method implementation is missing for now
        source += self.get_dummy_methods()
        
        # TODO: remove listener definitions for now
        #source += self.get_java_add_listener()
        source += self.get_java_class_end()

        return source

class JavaBindingsPacket(emulator_common.JavaPacket):

    #TODO: jetzt muss hier element.get_random_value_function_4test() fuer aktoren verwendet werden
    def get_test_return_values(self):
        test_values = ""
        test_assert = """context.assertEquals({0}, value{1});
      """
        test_assert_arr = """      for (int i = 0; i < value{1}.length; i++) {{
        context.assertEquals({0}, value{1}[i]);
      }}
"""
        return_type = None
        args = []
        if len(self.get_elements('out')) == 1:
            value_field = ''
            for element in self.get_elements('out'):
                return_type = element.get_java_type()
                if element.get_cardinality() > 1:
                    return_type += '[]'
                    test_values += test_assert_arr.format(element.get_random_value_function(), value_field)
                else:
                    test_values += test_assert.format(element.get_random_value_function(), value_field)
        elif len(self.get_elements('out')) > 1:
            value_field = '.{0}'
            for element in self.get_elements('out'):
                if element.get_cardinality() > 1 and element.get_type() != 'string':
                    test_values += test_assert_arr.format(element.get_random_value_function(),
                                                          value_field.format(element.get_headless_camel_case_name()))
                else:
                    test_values += test_assert.format(element.get_random_value_function(), 
                                                  value_field.format(element.get_headless_camel_case_name()))
        if len(self.get_elements('in')) != 0:
            for element in self.get_elements('in'):
                args.append(element.get_java_type())

        return return_type, test_values, args

    def get_emulator_return_values2(self):
        bufferbytes = 0
        buffers = ""
        buff = """    buffer.appendBytes({0});
"""
        for element in self.get_elements('out'):
            call = ""
            element_type = element.get_type()
            if element_type == 'int8':
                call = emulator_common.get_extract_value_function(element_type)
                bufferbytes += 1 * element.get_cardinality()
            elif element_type == 'uint8':
                call = emulator_common.get_extract_value_function(element_type)
                bufferbytes += 1 * element.get_cardinality()
            elif element_type == 'int16':
                call = emulator_common.get_extract_value_function(element_type)
                bufferbytes += 2 * element.get_cardinality()
            elif element_type == 'uint16':
                call = emulator_common.get_extract_value_function(element_type)
                bufferbytes += 2 * element.get_cardinality()
            elif element_type == 'int32':
                call = emulator_common.get_extract_value_function(element_type)
                bufferbytes += 4 * element.get_cardinality()
            elif element_type == 'uint32':
                call = emulator_common.get_extract_value_function(element_type)
                bufferbytes += 4 * element.get_cardinality()
            elif element_type == 'uint64':
                call = emulator_common.get_extract_value_function(element_type)
                bufferbytes += 8 * element.get_cardinality()
            elif element_type == 'bool':
                call = emulator_common.get_extract_value_function(element_type)
                bufferbytes += 1 * element.get_cardinality()
            elif element_type == 'char':
                call = emulator_common.get_extract_value_function(element_type)
                bufferbytes += 1 * element.get_cardinality()
            elif element_type == 'string':
                call = emulator_common.get_extract_value_function(element_type)
                bufferbytes += 1 * element.get_cardinality()
            elif element_type == 'float':
                call = emulator_common.get_extract_value_function(element_type)
                bufferbytes += 4 * element.get_cardinality()
            
            buffers += buff.format(call.format(self.field))
        return buffers, bufferbytes

    def get_emulator_return_values(self):
        bufferbytes = 0
        buffers = ""
        buff = """      buffer.appendBytes({0});
"""
        for element in self.get_elements('out'):
            call = ""
            if element.get_type() == 'int8':
                call = 'Utils.get1ByteRandomValue({0})'
                bufferbytes += 1 * element.get_cardinality()
            elif element.get_type() == 'uint8':
                call = 'Utils.get1ByteURandomValue({0})'
                bufferbytes += 1 * element.get_cardinality()
            elif element.get_type() == 'int16':
                call = 'Utils.get2ByteRandomValue({0})'
                bufferbytes += 2 * element.get_cardinality()
            elif element.get_type() == 'uint16':
                call = 'Utils.get2ByteURandomValue({0})'
                bufferbytes += 2 * element.get_cardinality()
            elif element.get_type() == 'int32':
                call = 'Utils.get4ByteRandomValue({0})'
                bufferbytes += 4 * element.get_cardinality()
            elif element.get_type() == 'uint32':
                call = 'Utils.get4ByteURandomValue({0})'
                bufferbytes += 4 * element.get_cardinality()
            elif element.get_type() == 'uint64':
                call = 'Utils.get8ByteURandomValue({0})'
                bufferbytes += 8 * element.get_cardinality()
            elif element.get_type() == 'bool':
                call = 'Utils.getBoolRandomValue({0})'
                bufferbytes += 1 * element.get_cardinality()
            elif element.get_type() == 'char':
                call = 'Utils.getCharRandomValue(1)'
                bufferbytes += 1 * element.get_cardinality()
            elif element.get_type() == 'string':
                call = 'Utils.getCharRandomValue({0})'
                bufferbytes += 1 * element.get_cardinality()
            elif element.get_type() == 'float':
                call = 'Utils.getFloatRandomValue({0})'
                bufferbytes += 4 * element.get_cardinality()
            
            buffers += buff.format(call.format(element.get_cardinality()))
        return buffers, bufferbytes

class JavaBindingsGenerator(common.BindingsGenerator):
    released_files_name_prefix = 'generator'
    testsubdir = os.path.join('tfemulator', 'src', 'test', 'java', 'org', 'm1theo', 'tfemulator', 'tests')
    devicesubdir = os.path.join('tfemulator', 'src', 'main', 'java', 'org', 'm1theo', 'tfemulator', 'devices')
    uidgenerator = emulator_common.UidGenerator()
    readme = ''
    model_config_dir = 'model'

    def prepare(self):
        common.recreate_directory(os.path.join(self.get_bindings_root_directory(), self.devicesubdir))
        common.recreate_directory(os.path.join(self.get_bindings_root_directory(), self.testsubdir))

    def get_bindings_name(self):
        return 'emulator'

    def get_device_class(self):
        return JavaBindingsDevice

    def get_packet_class(self):
        return JavaBindingsPacket

    def get_element_class(self):
        return emulator_common.JavaElement

    def generate(self, device):
        device.load_model_data(os.path.join(self.get_bindings_root_directory(), self.model_config_dir))
        filename = '{0}.java'.format(device.get_java_class_name())
        suffix = ''

        java = open(os.path.join(self.get_bindings_root_directory(), self.devicesubdir + suffix, filename), 'wb')
        java.write(device.get_emulator_source())
        java.close()
        
        ## generate tests
        uid = self.uidgenerator.get_uid_from_name(device.get_headless_camel_case_name())
        testfilename = '{0}Test.java'.format(device.get_java_class_name())
        javatest = open(os.path.join(self.get_bindings_root_directory(), self.testsubdir + suffix, testfilename), 'wb')
        javatest.write(device.get_test_source(uid))
        javatest.close()
        
        self.readme += device.get_readme().format(device.get_java_class_name())
        
        if device.is_released():
            self.released_files.append(filename)

    def finish(self):
        readmename = 'README-DEVICES.md'
        readmefh = open(os.path.join(self.get_bindings_root_directory(), readmename), 'wb')
        readmefh.write(self.readme)
        readmefh.close()
        
        common.BindingsGenerator.finish(self)

def generate(bindings_root_directory):
    common.generate(bindings_root_directory, 'en', JavaBindingsGenerator)

if __name__ == "__main__":
    generate(os.getcwd())
