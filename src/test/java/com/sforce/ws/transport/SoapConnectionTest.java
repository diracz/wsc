/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.sforce.ws.transport;

import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.bind.TypeMapper;
import com.sforce.ws.bind.XMLizable;
import com.sforce.ws.parser.PullParserException;
import com.sforce.ws.parser.XmlInputStream;
import com.sforce.ws.parser.XmlOutputStream;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * This class validates parts of the Soap Connection.
 *
 * @author rcornel
 */
public class SoapConnectionTest {

    public static final String INPUT_STRING = "<?xml version=\"1.0\" encoding=\"utf-8\"?>   \n" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "  xmlns:urn=\"urn:enterprise.soap.sforce.com\"\n" +
            "  xmlns:urn1=\"urn:sobject.enterprise.soap.sforce.com\"\n" +
            "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "  <soapenv:Header>\n" +
            "     <urn:SessionHeader>\n" +
            "        <urn:sessionId>QwWsHJyTPW.1pd0_jXlNKOSU</urn:sessionId>\n" +
            "     </urn:SessionHeader>" +
            "  </soapenv:Header>\n" +
            "  <soapenv:Body>\n" +
            "     <urn:create>\n" +
            "        <urn:sObjects> <!--Zero or more repetitions:-->\n" +
            "           <urn1:type>Contact</urn1:type>\n" +
            "           <!--You may enter ANY elements at this point-->\n" +
            "           <AccountId>001D000000HRzKD</AccountId>\n" +
            "           <FirstName>Jane</FirstName>\n" +
            "           <LastName>Doe</LastName>\n" +
            "        </urn:sObjects>\n" +
            "        <urn:sObjects>\n" +
            "           <urn1:type>Account</urn1:type>\n" +
            "           <Name>Acme Rockets, Inc.</Name>\n" +
            "        </urn:sObjects>\n" +
            "     </urn:create>\n" +
            "  </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    // If we cannot identify a header throw an exception.
    @Test(expected = ConnectionException.class)
    public void validateParsingUnrecognizedHeader() throws PullParserException, IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ConnectionException {
        SoapConnection connection = new SoapConnection("http://www.salesforce.com", "sobject", new TypeMapper(), ConnectorConfig.DEFAULT);
        Map<QName, Class> knownHeaders = new HashMap<>();
        connection.setKnownHeaders(knownHeaders);
        ByteArrayInputStream stream = new ByteArrayInputStream(INPUT_STRING.getBytes());
        XmlInputStream xin = new XmlInputStream();
        xin.setInput(stream, "UTF-8");
        xin.nextTag();
        xin.nextTag();

        Method isHeaderMethod = SoapConnection.class.getDeclaredMethod("isHeader", XmlInputStream.class);
        isHeaderMethod.setAccessible(true);

        Method readSoapHeaderMethod = SoapConnection.class.getDeclaredMethod("readSoapHeader", XmlInputStream.class);
        readSoapHeaderMethod.setAccessible(true);
        try {
            assertTrue((Boolean) isHeaderMethod.invoke(connection, xin));
            readSoapHeaderMethod.invoke(connection, xin);
        } catch (InvocationTargetException e) {
            throw (ConnectionException)e.getCause();
        }
    }

    // If we have a correct header mapping we should be able to read with out problems.
    @Test
    public void validateParsingValidHeader() throws PullParserException, IOException, ConnectionException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SoapConnection connection = new SoapConnection("http://www.salesforce.com", "sobject", new TypeMapper(), ConnectorConfig.DEFAULT);
        Map<QName, Class> knownHeaders = new HashMap<>();
        QName name = new QName("urn:enterprise.soap.sforce.com", "SessionHeader"); // //SessionHeader
        knownHeaders.put(name, TestClass.class); // //
        connection.setKnownHeaders(knownHeaders);
        ByteArrayInputStream stream = new ByteArrayInputStream(INPUT_STRING.getBytes());
        XmlInputStream xin = new XmlInputStream();
        xin.setInput(stream, "UTF-8");
        Method envelopMethod = SoapConnection.class.getDeclaredMethod("readSoapEnvelopeStart", XmlInputStream.class);
        envelopMethod.setAccessible(true);
        envelopMethod.invoke(connection, xin);
    }

    public static class TestClass implements XMLizable {

        @Override
        public void write(QName element, XmlOutputStream out, TypeMapper typeMapper) throws IOException {

        }

        @Override
        public void load(XmlInputStream in, TypeMapper typeMapper) throws IOException, ConnectionException {
            QName q = new QName("urn:enterprise.soap.sforce.com", "SessionHeader");
            while(!isEndTagFor(q, in)){
                in.next();
            }
        }

        public static boolean isEndTagFor(QName name, XmlInputStream in) throws ConnectionException {
            return in.getEventType() == XmlInputStream.END_TAG &&
                    name.getNamespaceURI().equals(in.getNamespace()) &&
                    name.getLocalPart().equals(in.getName());
        }
    }
}
