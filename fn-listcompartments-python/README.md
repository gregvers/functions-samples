# Function that returns the list of compartments in a User's Tenancy.

This function uses Resource Principals to securely authorize an instance to make
API calls to OCI services using the [OCI Python SDK](https://oracle-cloud-infrastructure-python-sdk.readthedocs.io/en/latest/index.html).
It returns a list of all compartments within the tenancy regardless of region.

As you make your way through this tutorial, look out for this icon. ![user input icon](../images/userinput.png) Whenever you see it, it's time for you to perform an action.


Pre-requisites
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
  fn init --runtime python list-compartments
  ```
  Enter the directory:

  ![user input icon](../images/userinput.png)
  ```
  cd list-compartments
  ```

### Create an Application to run your functions
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
--------------------
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
  import oci.identity
  ```

### The Handler method
  This is what is called when the function is invoked by Oracle Functions, update it to call *list_compartments* as follow:

  ![user input icon](../images/userinput.png)
  ```python
  def handler(ctx, data: io.BytesIO=None):
      signer = oci.auth.signers.get_resource_principals_signer()
      resp = list_compartments(signer)
      return response.Response(
          ctx, response_data=json.dumps(resp),
          headers={"Content-Type": "application/json"}
      )
  ```
  The line `signer = oci.auth.signers.get_resource_principals_signer()` gives us the configuration information of the calling tenancy and compartment which will allow us to gain access to any service in OCI.

### The *list_compartments* method
  Create the *list_compartments* method. This is where we'll put the bulk of our code that will connect to OCI and return the list of compartments in our tenancy.

  ![user input icon](../images/userinput.png)
  ```python
  def list_compartments(signer):
      client = oci.identity.IdentityClient(config={}, signer=signer)
      # OCI API for managing users, groups, compartments, and policies.
      try:
          # Returns a list of all compartments and subcompartments in the tenancy (root compartment)
          compartments = client.list_compartments(
              signer.tenancy_id,
              compartment_id_in_subtree=True,
              access_level='ANY'
          )
          # Create a list that holds a list of the compartments id and name next to each other.
          compartments = [[c.id, c.name] for c in compartments.data]
      except Exception as e:
          compartments = str(e)
      resp = {"compartments": compartments}
      return resp
  ```
  Here we are creating an [IdentityClient](https://oracle-cloud-infrastructure-python-sdk.readthedocs.io/en/latest/api/identity/client/oci.identity.IdentityClient.html) from the [OCI Python SDK](https://oracle-cloud-infrastructure-python-sdk.readthedocs.io/en/latest/index.html), which allows us to connect to OCI with the resource principal's signer data, since resource principal is already pre-configured we do not need to pass in a valid config dictionary, we are now able to make a call to identity services for information on our compartments.

Test
----
### Deploy the function

  ![user input icon](../images/userinput.png)
  ```
  fn -v deploy --app <your app name>
  ```

  e.g.

  ```
  fn -v deploy --app resource-principles
  ```

### Invoke the function

  ![user input icon](../images/userinput.png)
  ```
  fn invoke <your app name> <your function name>
  ```

  e.g.

  ```
  fn invoke resource-principles list-compartments
  ```
  Upon success, you should see all of the compartments in your tenancy appear in your terminal.
