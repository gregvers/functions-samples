/*
** ObjectStoragePutObject version 1.0.
**
** Copyright (c) 2019 Oracle, Inc.  All rights reserved.
**
** The Universal Permissive License (UPL), Version 1.0
**
** Subject to the condition set forth below, permission is hereby granted to any
** person obtaining a copy of this software, associated documentation and/or data
** (collectively the "Software"), free of charge and under any and all copyright
** rights in the Software, and any and all patent rights owned or freely
** licensable by each licensor hereunder covering either (i) the unmodified
** Software as contributed to or provided by such licensor, or (ii) the Larger
** Works (as defined below), to deal in both
**
** (a) the Software, and
** (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
** one is included with the Software (each a "Larger Work" to which the Software
** is contributed by such licensors),
**
** without restriction, including without limitation the rights to copy, create
** derivative works of, display, perform, and distribute the Software and make,
** use, sell, offer for sale, import, export, have made, and have sold the
** Software and the Larger Work(s), and to sublicense the foregoing rights on
** either these or other terms.
**
** This license is subject to the following condition:
** The above copyright notice and either this complete permission notice or at
** a minimum a reference to the UPL must be included in all copies or
** substantial portions of the Software.
**
** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
** IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
** FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
** AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
** LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
** OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
** SOFTWARE.
*/

package com.example.fn;

import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class ObjectStoragePutObject {

    private ObjectStorage objStoreClient = null;
    final ResourcePrincipalAuthenticationDetailsProvider provider
            = ResourcePrincipalAuthenticationDetailsProvider.builder().build();

    public ObjectStoragePutObject() {
        try {
            //print env vars in Functions container
            System.err.println("OCI_RESOURCE_PRINCIPAL_VERSION " + System.getenv("OCI_RESOURCE_PRINCIPAL_VERSION"));
            System.err.println("OCI_RESOURCE_PRINCIPAL_REGION " + System.getenv("OCI_RESOURCE_PRINCIPAL_REGION"));
            System.err.println("OCI_RESOURCE_PRINCIPAL_RPST " + System.getenv("OCI_RESOURCE_PRINCIPAL_RPST"));
            System.err.println("OCI_RESOURCE_PRINCIPAL_PRIVATE_PEM " + System.getenv("OCI_RESOURCE_PRINCIPAL_PRIVATE_PEM"));

            objStoreClient = new ObjectStorageClient(provider);

        } catch (Throwable ex) {
            System.err.println("Failed to instantiate ObjectStorage client - " + ex.getMessage());
        }
    }

    public static class ObjectInfo {

        private String name;
        private String bucketName;
        private String content;

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public ObjectInfo() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

    }

    public String handle(ObjectInfo objectInfo) {
        String result = "FAILED";

        if (objStoreClient == null) {
            System.err.println("There was a problem creating the ObjectStorage Client object. Please check logs");
            return result;
        }
        try {

            String nameSpace = System.getenv().get("NAMESPACE");

            PutObjectRequest por = PutObjectRequest.builder()
                    .namespaceName(nameSpace)
                    .bucketName(objectInfo.bucketName)
                    .objectName(objectInfo.name)
                    .putObjectBody(new ByteArrayInputStream(objectInfo.content.getBytes(StandardCharsets.UTF_8)))
                    .build();

            PutObjectResponse poResp = objStoreClient.putObject(por);
            result = "Successfully submitted Put request for object " + objectInfo.name + " in bucket " + objectInfo.bucketName + ". OPC reuquest ID is " + poResp.getOpcRequestId();
            System.err.println(result);

        } catch (Throwable e) {
            System.err.println("Error storing object in bucket " + e.getMessage());
            result = "Error storing object in bucket " + e.getMessage();
        }

        return result;
    }
}
