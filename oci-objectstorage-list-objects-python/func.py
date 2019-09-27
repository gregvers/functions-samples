# Copyright (c) 2019, Oracle and/or its affiliates.  All rights reserved.
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
    except Exception as e:
        error = 'Input a JSON object in the format: \'{"bucketName": "<bucket name>"}\' '
        raise Exception(error)
        )
    resp = list_objects(signer, bucketName)
    return response.Response(
        ctx,
        response_data=json.dumps(resp),
        headers={"Content-Type": "application/json"}
    )

def list_objects(signer, bucketName):
    client = oci.object_storage.ObjectStorageClient(config={}, signer=signer)
    try:
        print("Searching for objects in bucket " + bucketName, file=sys.stderr)
        object = client.list_objects(os.environ.get("OCI_NAMESPACE"), bucketName)
        print("found objects", file=sys.stderr)
        objects = [b.name for b in object.data.objects]
    except Exception as e:
        objects = str(e)
    response = { "The objects found in bucket '" + bucketName + "'": objects }
    return response
