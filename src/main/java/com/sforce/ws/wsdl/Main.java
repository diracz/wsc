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

package com.sforce.ws.wsdl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import com.sforce.ws.util.Verbose;

/**
 * A util class that can be used to test WSDL parser.
 *
 * @author  http://cheenath.com
 * @version 1.0
 * @since   1.0   Nov 5, 2005
 */
public class Main {

  public static void main(String[] args) throws WsdlParseException, MalformedURLException, IOException {
    Definitions definitions = WsdlFactory.create(new URL(args[0]));
    Verbose.log(definitions.toString());

    Types types = definitions.getTypes();

    System.out.println("types");

    for(Schema schema: types.getSchemas() ) { 
      System.out.println("  schema " + schema.getTargetNamespace()); 

      for(ComplexType type: schema.getComplexTypes()) { 
        System.out.println("    " + type.getName() + " extends " + type.getBase());
        Collection sequence = type.getContent();

        if (sequence != null) {
          Iterator<Element> elements = sequence.getElements();

          while(elements.hasNext()) {
            Element element = elements.next();
            System.out.println("      " + element.getName() + " " + element.getType());
          }
        }
      }

    }
  }
}
