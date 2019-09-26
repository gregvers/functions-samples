# Copyright (c) 2016, 2018, Oracle and/or its affiliates.  All rights reserved.
import io
import json
from fdk import response

import oci

def handler(ctx, data: io.BytesIO=None):
    signer = oci.auth.signers.get_resource_principals_signer()
    resp = list_instances(signer)
    return response.Response(
        ctx, response_data=json.dumps(resp),
        headers={"Content-Type": "application/json"}
    )

# List instances (in IAD) ------------------------------------------------------
def list_instances(signer):
    client = oci.core.ComputeClient(config={}, signer=signer)
    # OCI API to manage Compute resources such as compute instances, block storage volumes, etc.
    try:
        # Returns a list of all instances in the current compartment
        inst = client.list_instances(signer.compartment_id)
        # Create a list that holds a list of the instances id and name next to each other
        inst = [[i.id, i.display_name] for i in inst.data]
    except Exception as e:
        inst = str(e)
    resp = { "instances": inst }
    return resp
