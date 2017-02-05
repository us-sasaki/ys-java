---
layout: default
title: Using SmartREST
---

## Overview

The Cumulocity REST APIs provide you with a generic IoT protocol that is simple to use from most environments. It can be ad-hoc adapted to any IoT use case and uses standard Internet communication and security mechanisms. While this is a great leap forward over tailored IoT protocols with proprietary technologies, it poses some challenges to very constrained environments such as low-end microcontrollers or low-bandwidth communication channels. 

For these enviroments, Cumulocity offers the so-called "SmartREST" protocol. SmartREST combines the benefits of standard technology and tailored protocols: 

* It continues to work on any network by using standard HTTP technology.
* It supports HTTP authentication and encryption.
* It still gracefully handles protocol versioning.
* Its network traffic usage is close to custom-optimized protocols by transferring pure payload data during normal operation.
* It is based on CSV (comma separated values) and hence is easy to handle from C-based environments.
* It supports server-generated timestamps for devices without clocks.

In the next section, we will discuss the concepts behind SmartREST and the basic protocol that is used. SmartREST is based on separating metadata from payload data by using so-called templates, which are then described. Finally, we show how to send and receive data using SmartREST. For a detailed description of the protocol, see the [SmartREST reference](/guides/reference/smartrest).

## How does SmartREST work?

The image below illustrates how SmartREST works. Devices and other clients connect to a dedicated SmartREST endpoint on Cumulocity and send their data in rows of comma-separated values. These rows are expanded by Cumulocity's SmartREST proxy into standard Cumulocity REST API requests. Similar, responses from Cumulocity are compressed by the proxy from their original JSON format into comma-separated values before sending them back to the device.

![SmartREST architecture](/guides/rest/smartrest.png)

How can Cumulocity interpret comma-separated values into meaningful REST requests? For that purpose, devices register templates with Cumulocity. The templates contain the expanded REST requests together with placeholders into which the Cumulocity SmartREST proxy consecutively inserts the comma-separate values. For responses, the templates describe which values to pick from the structured REST response to construct comma-separated values.

Templates are associated with software or firmware versions of a device. Usually, a particular implementation of a device or application can only issue a particular set of well-defined types of requests. All devices with the same implementation share the same set of request types. Hence, the templates can be defined at implementation time. To make the templates available to Cumulocity, the first device with a particular implementation will send its templates and makes them available for usage by all similar devices.

This process is illustrated below. Assume a device with an implementation version "Device_1.0" starts communicating through SmartREST. After retrieving its credentials, the device will ask the SmartREST proxy if its template is already known. If the template is not found on the server, the device will send its template in a single static text request to Cumulocity. Once this procedure has been carried out, all simmiar devices using that template can start communicating using SmartREST without re-sending the template to the server.

![SmartREST templates](/guides/rest/templates.png)

The example also roughly illustrates the translation process. In "Template 1", "%%" is a placeholder to be filled by the SmartREST proxy. "time" is filled with a server-side timestamp (see below). The remaining placeholders are filled with request data. The line "1,200,20.5" in the example request is interpreted as follows:

* The first column references the template to be used, in this case Template 1.
* "200" refers to the first free placeholder in the template, in this case the ID in the "source" element. (The ID of the device that sends the measurement.)
* "20.5" refers to the second free placeholder in the template, here the value of the temperature measurement.

## The basic SmartREST protocol

The basic structure of all SmartREST requests is as follows:

* All requests are POST requests to the endpoint "/s", regardless of what the requests finally translate to.
* The standard HTTP "Authorization" header is used to authenticate the client.
* An additional "X-Id:" header is used to identify the implementation of the client, either as device type (such as "Device_1.0") or as an identifier returned by the template registration process.
* A request body contains rows of text in comma-separated value format. Each row corresponds to one request to the standard Cumulocity REST API.
* The response is always "200 OK".
* The response body again contains rows of comma-separated values. A row corresponds to a response from the Cumulocity REST API on a particular request. 

Using the above example, a SmartREST request would be as follows:

	POST /s HTTP/1.1
	Authorization: Basic ...
	X-Id: Device_1.0

	1,200,20.5

And the corresponding response would be:

	HTTP/1.1 200 OK
	Content-Length: 6

	20,0

To match the requests and responses, a response line contains, next to the error code, the line of the request that the response answers. In this example, "20" indicates "OK" and "0" refers to the first line of the request.

## How are templates registered?

As described above, a client using SmartREST will first ask if its SmartREST templates are already known to the server. This is done with an empty SmartREST request:

	POST /s HTTP/1.1
	Authorization: Basic ...
	X-Id: Device_1.0

If the device implementation is known, the response will return an ID that can be used as "shorthand" in the "X-Id" header of later requests.

	HTTP/1.1 200 OK

	20,<id>

If the device implementation is unknown, the response will be:

	HTTP/1.1 200 OK

	40,"No template for this X-ID"

In this case, create all templates used in your device implementation. 

	POST /s HTTP/1.1
	Authorization: Basic ...
	X-Id: Device_1.0
	
	10,1,POST,/measurement/measurements,application/vnd.com.nsn.cumulocity.measurement+json,,%%,NOW UNSIGNED NUMBER,{ "time": "%%", "type": ... }
	...

In this example, "10" refers to a request template (whereas "11" would refer to a response template). The template is number "1", so SmartREST requests using this template have a "1" in their first column. The template refers to a "POST" request to the endpoint "/measurement/measurements" with a content type of "application/vnd.com.nsn.cumulocity.measurement+json". The placeholder used in the template is "%%". The placeholders are a time stamp ("NOW"), an unsigned number and a general number. Finally, the last column contains the body of the request to be filled in an sent.

## How are responses handled?

The above example illustrated the handling of requests and request templates. For responses, [JSONPath expressions](http://goessner.net/articles/JsonPath/) translate Cumulocity REST responses into CSV. Assume, for example, a device has a display and can show a message on the display. An operation to update the message would look like this:

	{
		"c8y_Message": {
			 "text": "Hello, world!"
		},
		"creationTime": "2014-02-25T08:32:45.435+01:00",
		"deviceId": "8789602",
		"status": "PENDING",
		...
	}

On the client side, the device mainly needs to know the text to be shown. In JSONPath, the "text" property is extracted using the following syntax:

	$.c8y_Message.text 

In this syntax, "$" refers to the root of the data structure and "." selects an element from a data structure. For more options, please consult the [JSONPath reference](http://goessner.net/articles/JsonPath/).

A device usually queries for all operations that are associated with it and that are in pending state. The standard Cumulocity response to such a query is:

	{
		"operations": [
			{
				"c8y_Message": {
					"text": "Hello, world!"
				},
				"creationTime": "2014-02-25T08:32:45.435+01:00",
				"deviceId": "8789602",
				"status": "PENDING",
				...
			}, {
				"c8y_Relay": {
					...
			}
			...
		]
	]

That is, the response contains a list of operations, and these operations can have different types. To work with such a structure, use the following response template:

	11,2,$.operations,$.c8y_Message,$.c8y_Message.text 

This means, value by value:

* 11: This is a response template.
* 2: It has Number 2.
* $.operations: The response is a list and the list's property is "operations".
* $.c8y_Message: This template applies to responses with the property "c8y_Message".
* $.c8y_Message.text: The text will be extracted from the message and will be returned.

The SmartREST client will thus get the following response:

	HTTP/1.1 200 OK
	
	2,0,"Hello, world!"

That is, the response was created using Template 2, the template to translate display message operations. The response refer to the first request sent. The actual message to set is "Hello, world!".
