# Copyright (c) 2016, 2018, Oracle and/or its affiliates.  All rights reserved.
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
        content = body["content"]

    except Exception as e:
        error = """
                Input a JSON object in the format: '{"bucketName": "<bucket name>",
                "content": "<content>", "fileName": "<file name>"}'
                """
        raise Exception(error)
    resp = do(signer, bucketName, fileName, content)

    return response.Response(
        ctx, response_data=json.dumps(resp),
        headers={"Content-Type": "application/json"}
    )

def do(signer, bucketName, fileName, content):
    client = oci.object_storage.ObjectStorageClient(config={}, signer=signer)
    try:
        object = client.put_object(os.environ.get("OCI_NAMESPACE"), bucketName, fileName, json.dumps(content))
        output = "Success: Put object '" + fileName + "' in bucket '" + bucketName + "'"
    except Exception as e:
        output = "Failed: " + str(e.message)

    response = {
        "state": output,
    }
    return response
