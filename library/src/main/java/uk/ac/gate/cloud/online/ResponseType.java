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
package uk.ac.gate.cloud.online;

/**
 * Enumeration of valid online API response types.
 *
 * @author Ian Roberts
 */
public enum ResponseType {
  JSON("application/json"), GATE_XML("application/gate+xml"), FINF("application/fastinfoset");

  private ResponseType(String contentType) {
    this.contentType = contentType;
  }

  private final String contentType;

  public String getContentType() {
    return contentType;
  }

  public String getContentType(boolean includeText) {
    if(!includeText) {
      return contentType + "; includeText=no";
    } else {
      return contentType;
    }
  }
}
