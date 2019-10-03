#
# oci-objectstorage-get-object version 1.0.
#
# Copyright (c) 2019 Oracle, Inc.  All rights reserved.
#
# The Universal Permissive License (UPL), Version 1.0
#
# Subject to the condition set forth below, permission is hereby granted to any
# person obtaining a copy of this software, associated documentation and/or data
# (collectively the "Software"), free of charge and under any and all copyright
# rights in the Software, and any and all patent rights owned or freely
# licensable by each licensor hereunder covering either (i) the unmodified
# Software as contributed to or provided by such licensor, or (ii) the Larger
# Works (as defined below), to deal in both
#
# (a) the Software, and
# (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
# one is included with the Software (each a "Larger Work" to which the Software
# is contributed by such licensors),
#
# without restriction, including without limitation the rights to copy, create
# derivative works of, display, perform, and distribute the Software and make,
# use, sell, offer for sale, import, export, have made, and have sold the
# Software and the Larger Work(s), and to sublicense the foregoing rights on
# either these or other terms.
#
# This license is subject to the following condition:
# The above copyright notice and either this complete permission notice or at
# a minimum a reference to the UPL must be included in all copies or
# substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#

import io
import os
import json
import sys
from fdk import response

import oci.object_storage

def handler(ctx, data: io.BytesIO=None):
    signer = oci.auth.signers.get_resource_principals_signer()
    try:
        body = json.loads(data.getvalue())
        bucketName = body["bucketName"]
        fileName = body["fileName"]
    except Exception as e:
        error = 'Input a JSON object in the format: \'{"bucketName": "<bucket name>"}, "fileName": "<filename>"}\' '
        raise Exception(error)
    resp = get_object(signer, bucketName, fileName)
    return response.Response(
        ctx,
        response_data=json.dumps(resp),
        headers={"Content-Type": "application/json"}
    )

def get_object(signer, bucketName, fileName):
    client = oci.object_storage.ObjectStorageClient(config={}, signer=signer)
    message = "Failed: The object " + str(fileName) + " could not be retrieved."
    try:
        print("Searching for bucket and object", file=sys.stderr)
        object = client.get_object(os.environ.get("OCI_NAMESPACE"), bucketName, fileName)
        print("found object", file=sys.stderr)
        if object.status == 200:
            message = "Success: The object " + str(fileName) + " was retrieved, content: " + str(object.data.text)
    except Exception as e:
        message = "Failed: " + str(e.message)
    response = { "content": message }
    return response
