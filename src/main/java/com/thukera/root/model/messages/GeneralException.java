package com.thukera.root.model.messages;

import com.google.gson.Gson;

import lombok.Data;

@Data
public class GeneralException extends Exception{
	
	private static final long serialVersionUID = 1L;
	
	private String message;
	private Object error;
	
	public GeneralException(String message, Object exceptionError) {
		super();
		this.message = message;
		this.error = exceptionError;
	}
	

	
	@Override
	public String toString() {
		try {
			return new Gson().toJson(this);
		} catch(Exception e) {
			return "{\"GeneralIvrException\":\"message="+ message + ", error=" + error + "\"}";	
		}
	}
	
}
