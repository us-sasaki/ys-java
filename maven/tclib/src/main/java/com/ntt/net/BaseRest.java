package com.ntt.net;

import java.io.IOException;

public interface BaseRest<T, R extends BaseResponse> {
	String getLocation();
	void putHeader(String key, String value);
	void removeHeader(String key);
	R get(String location) throws IOException;
	R delete(String location) throws IOException;
	R put(String location, T target) throws IOException;
	R put(String location, String body) throws IOException;
	R put(String location, byte[] body) throws IOException;
	R post(String location, T targetjson) throws IOException;
	R post(String location, String body) throws IOException;
	R post(String location, byte[] body) throws IOException;
	void disconnect();
}
