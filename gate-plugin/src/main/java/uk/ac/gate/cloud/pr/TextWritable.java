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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.io.output.CloseShieldOutputStream;

import uk.ac.gate.cloud.client.StreamWritable;

/**
 * Simple <code>StreamWritable</code> implementation to write a string
 * as UTF-8.
 */
public class TextWritable implements StreamWritable {
  
  private String str;
  
  public TextWritable(String str) {
    this.str = str;
  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    // don't close the output stream after writing
    try(CloseShieldOutputStream csos = new CloseShieldOutputStream(out);
        OutputStreamWriter w = new OutputStreamWriter(csos, "UTF-8")) {
      w.write(str);
    }
  }

}
