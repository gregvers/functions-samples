/*
** ObjectStorageListObjects version 1.0.
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
import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import com.oracle.bmc.objectstorage.responses.ListObjectsResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectStorageListObjects {

    private ObjectStorage objStoreClient = null;

    final ResourcePrincipalAuthenticationDetailsProvider provider
            = ResourcePrincipalAuthenticationDetailsProvider.builder().build();

    public ObjectStorageListObjects() {
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

    public List<String> handleRequest(String bucketName) {

        if (objStoreClient == null) {
            System.err.println("There was a problem creating the ObjectStorage Client object. Please check logs");
            return Collections.emptyList();
        }

        List<String> objNames = null;
        try {
            String nameSpace = System.getenv().get("NAMESPACE");

            ListObjectsRequest lor = ListObjectsRequest.builder()
                    .namespaceName(nameSpace)
                    .bucketName(bucketName)
                    .build();

            System.err.println("Listing objects from bucket " + bucketName);

            ListObjectsResponse response = objStoreClient.listObjects(lor);

            objNames = response.getListObjects().getObjects().stream()
                    .map((objSummary) -> objSummary.getName())
                    .collect(Collectors.toList());

            System.err.println("Got " + objNames.size() + " objects in bucket " + bucketName);

        } catch (Throwable e) {
            System.err.println("Error fetching object list from bucket " + e.getMessage());
        }

        return objNames;
    }
}
