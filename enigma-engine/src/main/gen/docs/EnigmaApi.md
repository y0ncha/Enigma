# EnigmaApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createSession**](EnigmaApi.md#createSession) | **POST** /enigma/session | Create a session for a machine |
| [**deleteSession**](EnigmaApi.md#deleteSession) | **DELETE** /enigma/session | Delete a session |
| [**getCurrentMachineStatus**](EnigmaApi.md#getCurrentMachineStatus) | **GET** /enigma/config | Get current machine status |
| [**getMachineHistory**](EnigmaApi.md#getMachineHistory) | **GET** /enigma/history | Get machine history |
| [**loadMachineFromXml**](EnigmaApi.md#loadMachineFromXml) | **POST** /enigma/load | Load machine configuration from an XML file |
| [**processInput**](EnigmaApi.md#processInput) | **POST** /enigma/process | Process input through the Enigma machine |
| [**resetToOriginalCode**](EnigmaApi.md#resetToOriginalCode) | **PUT** /enigma/config/reset | Reset to original code |
| [**setAutomaticCodeSetup**](EnigmaApi.md#setAutomaticCodeSetup) | **PUT** /enigma/config/automatic | Automatic code setup |
| [**setManualCodeSelection**](EnigmaApi.md#setManualCodeSelection) | **PUT** /enigma/config/manual | Manual code selection |


<a id="createSession"></a>
# **createSession**
> CreateSession200Response createSession(createSessionRequest)

Create a session for a machine

Creates a new session for the given machine name. The machine name must match a machine previously loaded into the system. 

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnigmaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    EnigmaApi apiInstance = new EnigmaApi(defaultClient);
    CreateSessionRequest createSessionRequest = new CreateSessionRequest(); // CreateSessionRequest | 
    try {
      CreateSession200Response result = apiInstance.createSession(createSessionRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnigmaApi#createSession");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **createSessionRequest** | [**CreateSessionRequest**](CreateSessionRequest.md)|  | |

### Return type

[**CreateSession200Response**](CreateSession200Response.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Session was created successfully. |  -  |
| **409** | Conflict - unknown machine name |  -  |

<a id="deleteSession"></a>
# **deleteSession**
> deleteSession(sessionID)

Delete a session

Deletes the session identified by the given sessionID.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnigmaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    EnigmaApi apiInstance = new EnigmaApi(defaultClient);
    String sessionID = "sessionID_example"; // String | The session identifier to delete.
    try {
      apiInstance.deleteSession(sessionID);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnigmaApi#deleteSession");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **sessionID** | **String**| The session identifier to delete. | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Session deleted successfully. |  -  |
| **404** | Session not found. |  -  |

<a id="getCurrentMachineStatus"></a>
# **getCurrentMachineStatus**
> GetCurrentMachineStatus200Response getCurrentMachineStatus(sessionID, verbose)

Get current machine status

Returns the current status and configuration details of the loaded Enigma machine.  The optional &#x60;verbose&#x60; query parameter controls how much detail is returned: - When &#x60;verbose&#x3D;true&#x60;, the response includes &#x60;originalCode&#x60; and &#x60;currentRotorsPosition&#x60;. - When &#x60;verbose&#x3D;false&#x60; or not provided (default), these attributes are omitted. 

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnigmaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    EnigmaApi apiInstance = new EnigmaApi(defaultClient);
    String sessionID = "sessionID_example"; // String | The active session identifier.
    Boolean verbose = false; // Boolean | If true, include detailed code structures (`originalCode` and `currentRotorsPosition`) in the response. Default: false. 
    try {
      GetCurrentMachineStatus200Response result = apiInstance.getCurrentMachineStatus(sessionID, verbose);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnigmaApi#getCurrentMachineStatus");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **sessionID** | **String**| The active session identifier. | |
| **verbose** | **Boolean**| If true, include detailed code structures (&#x60;originalCode&#x60; and &#x60;currentRotorsPosition&#x60;) in the response. Default: false.  | [optional] [default to false] |

### Return type

[**GetCurrentMachineStatus200Response**](GetCurrentMachineStatus200Response.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Current configuration |  -  |

<a id="getMachineHistory"></a>
# **getMachineHistory**
> Map&lt;String, List&lt;HistoryEntry&gt;&gt; getMachineHistory(sessionID, machineName)

Get machine history

Returns processing history based on either a specific session or an entire machine.  Exactly **one** of the following query parameters must be provided: - &#x60;sessionID&#x60; – returns history for a single session - &#x60;machineName&#x60; – returns history across all sessions of the given machine  Providing both parameters or none at all is invalid. 

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnigmaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    EnigmaApi apiInstance = new EnigmaApi(defaultClient);
    String sessionID = "sessionID_example"; // String | Session identifier. When provided, history is returned only for this session. 
    String machineName = "machineName_example"; // String | Machine name. When provided, history is returned across all sessions of this machine. 
    try {
      Map<String, List<HistoryEntry>> result = apiInstance.getMachineHistory(sessionID, machineName);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnigmaApi#getMachineHistory");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **sessionID** | **String**| Session identifier. When provided, history is returned only for this session.  | [optional] |
| **machineName** | **String**| Machine name. When provided, history is returned across all sessions of this machine.  | [optional] |

### Return type

[**Map&lt;String, List&lt;HistoryEntry&gt;&gt;**](List.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | History grouped by code description |  -  |
| **400** | Invalid request – exactly one query parameter must be provided |  -  |

<a id="loadMachineFromXml"></a>
# **loadMachineFromXml**
> LoadMachineFromXml200Response loadMachineFromXml(_file)

Load machine configuration from an XML file

Upload an XML file representing the Enigma machine configuration. The server parses and loads the machine definition. 

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnigmaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    EnigmaApi apiInstance = new EnigmaApi(defaultClient);
    File _file = new File("/path/to/file"); // File | The XML file to upload
    try {
      LoadMachineFromXml200Response result = apiInstance.loadMachineFromXml(_file);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnigmaApi#loadMachineFromXml");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **_file** | **File**| The XML file to upload | |

### Return type

[**LoadMachineFromXml200Response**](LoadMachineFromXml200Response.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Load result |  -  |
| **400** | Invalid or missing file |  -  |

<a id="processInput"></a>
# **processInput**
> ProcessInput200Response processInput(input)

Process input through the Enigma machine

Encrypts/decrypts the provided input using the current machine state. This operation advances rotor positions (stateful). 

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnigmaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    EnigmaApi apiInstance = new EnigmaApi(defaultClient);
    String input = "input_example"; // String | The text to process.
    try {
      ProcessInput200Response result = apiInstance.processInput(input);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnigmaApi#processInput");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **input** | **String**| The text to process. | |

### Return type

[**ProcessInput200Response**](ProcessInput200Response.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Process result |  -  |

<a id="resetToOriginalCode"></a>
# **resetToOriginalCode**
> String resetToOriginalCode(sessionID)

Reset to original code

Resets the current machine configuration back to the original loaded code and returns it.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnigmaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    EnigmaApi apiInstance = new EnigmaApi(defaultClient);
    String sessionID = "sessionID_example"; // String | The active session identifier.
    try {
      String result = apiInstance.resetToOriginalCode(sessionID);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnigmaApi#resetToOriginalCode");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **sessionID** | **String**| The active session identifier. | |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Original code after reset |  -  |

<a id="setAutomaticCodeSetup"></a>
# **setAutomaticCodeSetup**
> String setAutomaticCodeSetup(sessionID)

Automatic code setup

Automatically generates and applies a random valid Enigma code setup.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnigmaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    EnigmaApi apiInstance = new EnigmaApi(defaultClient);
    String sessionID = "sessionID_example"; // String | The active session identifier.
    try {
      String result = apiInstance.setAutomaticCodeSetup(sessionID);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnigmaApi#setAutomaticCodeSetup");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **sessionID** | **String**| The active session identifier. | |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Operation result as a plain string |  -  |

<a id="setManualCodeSelection"></a>
# **setManualCodeSelection**
> String setManualCodeSelection(enigmaManualConfigRequest)

Manual code selection

Manually set the machine&#39;s secret code rotor selection and positions, reflector, and optional plugboard connections.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnigmaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    EnigmaApi apiInstance = new EnigmaApi(defaultClient);
    EnigmaManualConfigRequest enigmaManualConfigRequest = new EnigmaManualConfigRequest(); // EnigmaManualConfigRequest | 
    try {
      String result = apiInstance.setManualCodeSelection(enigmaManualConfigRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnigmaApi#setManualCodeSelection");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **enigmaManualConfigRequest** | [**EnigmaManualConfigRequest**](EnigmaManualConfigRequest.md)|  | |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: text/plain

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Operation result as a plain string |  -  |

