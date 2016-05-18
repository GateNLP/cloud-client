/*
 * Copyright (c) 2016 The University of Sheffield
 *
 * This file is part of the GATE Cloud REST client library, and is
 * licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.gate.cloud.pr;

import gate.Document;
import gate.corpora.DocumentStaxUtils;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;

import uk.ac.gate.cloud.client.StreamWritable;

/**
 * <code>StreamWritable</code> implementation that writes a document to
 * a stream as GATE XML format FastInfoset.
 */
public class FINFWritable implements StreamWritable {
  
  private Document document;
  
  public FINFWritable(Document document) {
    this.document = document;
  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    XMLStreamWriter xsw = new StAXDocumentSerializer(out);
    try {
      try {
        xsw.writeStartDocument();
        DocumentStaxUtils.writeDocument(document, xsw, "");
      } finally {
        xsw.close();
      }
    } catch(XMLStreamException xse) {
      throw new IOException(xse);
    }
  }

}
