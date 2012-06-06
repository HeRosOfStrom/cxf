/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.systest.jms.continuations;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import org.apache.cxf.BusFactory;
import org.apache.cxf.hello_world_jms.HelloWorldPortType;
import org.apache.cxf.hello_world_jms.HelloWorldService;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.apache.cxf.testutil.common.AbstractBusTestServerBase;
import org.apache.cxf.testutil.common.EmbeddedJMSBrokerLauncher;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProviderJMSContinuationTest extends AbstractBusClientServerTestBase {
    static final String PORT = Server.PORT;

    static EmbeddedJMSBrokerLauncher broker;
    
    public static class Server extends AbstractBusTestServerBase {
        public static final String PORT = allocatePort(Server.class);

        protected void run()  {
            setBus(BusFactory.getDefaultBus());
            broker.updateWsdl(getBus(),
                "/org/apache/cxf/systest/jms/continuations/jms_test.wsdl");

            Object implementor = new HWSoapMessageDocProvider();        
            String address = "http://localhost:" + PORT + "/SoapContext/SoapPort";
            Endpoint endpoint = Endpoint.publish(address, implementor);
            ((EndpointImpl)endpoint).getInInterceptors().add(new IncomingMessageCounterInterceptor());
        }
    }
    
    @BeforeClass
    public static void startServers() throws Exception {
        broker = new EmbeddedJMSBrokerLauncher("vm://ProviderJMSContinuationTest");
        System.setProperty("EmbeddedBrokerURL", broker.getBrokerURL());
        launchServer(broker);
        launchServer(new Server());
    }
    @AfterClass
    public static void clearProperty() {
        System.clearProperty("EmbeddedBrokerURL");
    }

    public URL getWSDLURL(String s) throws Exception {
        return getClass().getResource(s);
    }
    public QName getServiceName(QName q) {
        return q;
    }
    public QName getPortName(QName q) {
        return q;
    }
    
        
    @Test
    public void testProviderContinuation() throws Exception {
        QName serviceName = getServiceName(new QName("http://cxf.apache.org/hello_world_jms", 
                             "HelloWorldService"));
        QName portName = getPortName(
                new QName("http://cxf.apache.org/hello_world_jms", "HelloWorldPort"));
        URL wsdl = getWSDLURL("/org/apache/cxf/systest/jms/continuations/jms_test.wsdl");
        assertNotNull(wsdl);
        String wsdlString = wsdl.toString();
        broker.updateWsdl(getStaticBus(), wsdlString);

        HelloWorldService service = new HelloWorldService(wsdl, serviceName);
        assertNotNull(service);
        HelloWorldPortType greeter = service.getPort(portName, HelloWorldPortType.class);
        greeter.greetMe("ffang");
        ((java.io.Closeable)greeter).close();
    }
        
}

