/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.biomerieux.bmxconnect.client;

import com.biomerieux.bmxconnect.shared.MessageProxy;
import com.biomerieux.bmxconnect.shared.RegistrationInfoProxy;
import com.google.web.bindery.requestfactory.shared.InstanceRequest;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.RequestFactory;
import com.google.web.bindery.requestfactory.shared.ServiceName;

public interface MyRequestFactory extends RequestFactory {

  @ServiceName("com.biomerieux.bmxconnect.server.HelloWorldService")
  public interface HelloWorldRequest extends RequestContext {
    /**
     * Retrieve a "Hello, World" message from the server.
     */
    Request<String> getMessage();
  }

  @ServiceName("com.biomerieux.bmxconnect.server.RegistrationInfo")
  public interface RegistrationInfoRequest extends RequestContext {
    /**
     * Register a device for C2DM messages.
     */
    InstanceRequest<RegistrationInfoProxy, Void> register();

    /**
     * Unregister a device for C2DM messages.
     */
    InstanceRequest<RegistrationInfoProxy, Void> unregister();
  }

  @ServiceName("com.biomerieux.bmxconnect.server.Message")
  public interface MessageRequest extends RequestContext {
    /**
     * Send a message to a device using C2DM.
     */
    InstanceRequest<MessageProxy, String> send();
  }

  HelloWorldRequest helloWorldRequest();

  RegistrationInfoRequest registrationInfoRequest();

  MessageRequest messageRequest();
}
