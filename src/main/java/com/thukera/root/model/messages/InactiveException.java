package com.thukera.root.model.messages;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Usuário Inativo, contacte seu administrador!")
public class InactiveException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InactiveException(String message) {

        super(message);
    }
}