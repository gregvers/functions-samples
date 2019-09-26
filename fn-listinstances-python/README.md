# Function that returns the list of instances in the Calling Compartment.

This function uses Resource Principals to securely authorize an instance to make
API calls to OCI services using the [OCI Python SDK](https://oracle-cloud-infrastructure-python-sdk.readthedocs.io/en/latest/index.html).
It returns a list of all instances within the compartment that calls the function.

As you make your way through this tutorial, look out for this icon. ![user input icon](../images/userinput.png) Whenever you see it, it's time for you to perform an action.


Pre-requisites:
---------------
  1. Start by making sure all of your policies are correct from this [guide](https://docs.cloud.oracle.com/iaas/Content/Functions/Tasks/functionscreatingpolicies.htm?tocpath=Services%7CFunctions%7CPreparing%20for%20Oracle%20Functions%7CConfiguring%20Your%20Tenancy%20for%20Function%20Development%7C_____4)

  2. Have [Fn CLI setup with Oracle Functions](https://docs.cloud.oracle.com/iaas/Content/Functions/Tasks/functionsconfiguringclient.htm?tocpath=Services%7CFunctions%7CPreparing%20for%20Oracle%20Functions%7CConfiguring%20Your%20Client%20Environment%20for%20Function%20Development%7C_____0)

### Switch to the correct context
  ![user input icon](../images/userinput.png)
  ```
  fn use context <your context name>
  ```
  Check using
  ```
  fn ls apps
  ```

### Create or Update your Dynamic Group
  In order to use and retrieve information about other OCI Services, your function
  must be part of a dynamic group. For information on how to create a dynamic group,
  click [here](https://docs.cloud.oracle.com/iaas/Content/Identity/Tasks/managingdynamicgroups.htm#To).

  ![user input icon](../images/userinput.png)
  When specifying the *Matching Rules*, consider the following examples:
  * If you want all functions in a compartment to be able to access a resource,
  enter a rule similar to the following that adds all functions in the compartment
  with the specified compartment OCID to the dynamic group:
  ```
  ALL {resource.type = 'fnfunc', resource.compartment.id = 'ocid1.compartment.oc1..aaaaaaaa23______smwa'}
  ```
  * If you want a specific function to be able to access a resource, enter a rule
  similar to the following that adds the function with the specified OCID to the
  dynamic group:
  ```
  resource.id = 'ocid1.fnfunc.oc1.iad.aaaaaaaaacq______dnya'
  ```

### Create or Update Policies
  Now that your dynamic group is created, create a new policy that allows the
  dynamic group to inspect any resources you are interested in receiving
  information about, in this case we will grant access to `instance-family` in the functions related compartment.

  Your policy should look something like this:

  ![user input icon](../images/userinput.png)
  ```
  Allow dynamic-group <your dynamic group name> to inspect instance-family in compartment <your compartment name>
  ```
  e.g.
  ```
  Allow dynamic-group demo-func-dyn-group to inspect instance-family in compartment demo-func-compartment
  ```

  For more information on how to create policies, go [here](https://docs.cloud.oracle.com/iaas/Content/Identity/Concepts/policysyntax.htm).

Create application
------------------
### Create the function boilerplate
  Get the python boilerplate by running:

  ![user input icon](../images/userinput.png)
  ```
  fn init --runtime python <function-name>
  ```
  e.g.
  ```
  fn init --runtime python list-instances
  ```
  Enter the directory:

  ![user input icon](../images/userinput.png)
  ```
  cd list-instances
  ```

### Create an Application that is connected to Oracle Functions
  ![user input icon](../images/userinput.png)
  ```
  fn create app <app-name> --annotation oracle.com/oci/subnetIds='["<subnet-ocid>"]'
  ```
  You can find the subnet-ocid by logging on to [cloud.oracle.com](https://cloud.oracle.com/en_US/sign-in), navigating to Core Infrastructure > Networking > Virtual Cloud Networks. Make sure you are in the correct Region and Compartment, click on your VNC and select the subnet you wish to use.

  e.g.
  ```
  fn create app resource-principal --annotation oracle.com/oci/subnetIds='["ocid1.subnet.oc1.phx.aaaaaaaacnh..."]'
  ```

Writing the Function
------------------
### Requirements
  Update your requirements.txt file to contain the following:

  ![user input icon](../images/userinput.png)
  ```
  fdk
  oci>=2.2.18
  ```

### Open func.py
  Update the imports so that you contain the following.

  ![user input icon](../images/userinput.png)
  ```python
  import io
  import json

  from fdk import response
  import oci
  ```

### The Handler method
  This is what is called when the function is invoked by Oracle Functions, update
  it to call *list_instances* as follow:

  ![user input icon](../images/userinput.png)
  ```python
  def handler(ctx, data: io.BytesIO=None):
      signer = oci.auth.signers.get_resource_principals_signer()
      resp = list_instances(signer)
      return response.Response(
          ctx, response_data=json.dumps(resp),
          headers={"Content-Type": "application/json"}
      )
  ```

### The *list_instances* method
  Create the *list_instances* method. This is where we'll put the bulk of our code
  that will connect to OCI and return the list of instances in our compartment.

  ![user input icon](../images/userinput.png)
  ```python
  def list_instances(signer):
      client = oci.core.ComputeClient(config={}, signer=signer)
      try:
          inst = client.list_instances(signer.compartment_id)
          inst = [[i.id, i.display_name] for i in inst.data]
      except Exception as e:
          inst = str(e)
      resp = { "instances": inst }
      return resp
  ```
  Here we are creating a [ComputeClient](https://oracle-cloud-infrastructure-python-sdk.readthedocs.io/en/latest/api/core/client/oci.core.ComputeClient.html) from the [OCI Python SDK](https://oracle-cloud-infrastructure-python-sdk.readthedocs.io/en/latest/index.html), which allows us to connect to OCI with the resource principal's signer data,
  since resource principal is already pre-configured we do not need to pass in a
  valid config dictionary, we are now able to make a call to identity services
  for information on our compartments.

Test
----
### Deploy the function

  ![user input icon](../images/userinput.png)
  ```
  fn -v deploy --app <your app name>
  ```

  e.g.

  ```
  fn -v deploy --app resource-principal
  ```

### Invoke the function

  ![user input icon](../images/userinput.png)
  ```
  fn invoke <your app name> <your function name>
  ```

  e.g.

  ```
  fn invoke resource-principal list-instances
  ```
  Upon success, you should see all of the instances in your compartment appear in your terminal.
