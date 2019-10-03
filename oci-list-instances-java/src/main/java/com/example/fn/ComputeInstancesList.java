/*
** ComputeInstancesList version 1.0.
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

package io.fnproject.example;

import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.requests.ListInstancesRequest;
import com.oracle.bmc.core.responses.ListInstancesResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ComputeInstancesList {

    private ComputeClient computeClient = null;
    final ResourcePrincipalAuthenticationDetailsProvider provider
            = ResourcePrincipalAuthenticationDetailsProvider.builder().build();

    public ComputeInstancesList() {

        //print env vars in Functions container
        System.err.println("OCI_RESOURCE_PRINCIPAL_VERSION " + System.getenv("OCI_RESOURCE_PRINCIPAL_VERSION"));
        System.err.println("OCI_RESOURCE_PRINCIPAL_REGION " + System.getenv("OCI_RESOURCE_PRINCIPAL_REGION"));
        System.err.println("OCI_RESOURCE_PRINCIPAL_RPST " + System.getenv("OCI_RESOURCE_PRINCIPAL_RPST"));
        System.err.println("OCI_RESOURCE_PRINCIPAL_PRIVATE_PEM " + System.getenv("OCI_RESOURCE_PRINCIPAL_PRIVATE_PEM"));

        try {

            computeClient = new ComputeClient(provider);

        } catch (Throwable ex) {
            System.err.println("Failed to instantiate ComputeClient - " + ex.getMessage());
        }
    }

    public List<String> handle(String compID) {

        if (computeClient == null) {
            System.err.println("There was a problem creating the ComputeClient object. Please check logs...");
            return Collections.emptyList();
        }

        List<String> names = null;
        try {
            System.err.println("Searching for compute instances in compartment " + compID);

            ListInstancesRequest request = ListInstancesRequest.builder().compartmentId(compID).build();

            ListInstancesResponse instances = computeClient.listInstances(request);

            names = instances.getItems().stream()
                    .map((instance) -> instance.getDisplayName())
                    .collect(Collectors.toList());

            System.err.println("No. of compute instances found in compartment " + names.size());

        } catch (Throwable e) {
            System.err.println("ERROR searching for compute instances in compartment " + compID);
        }

        return names;
    }
}
