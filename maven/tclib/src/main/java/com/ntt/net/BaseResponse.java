package com.ntt.net;

public interface BaseResponse {
	int getStatus();
	String getMessage();
	byte[] getBody();
}
