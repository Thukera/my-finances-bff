package com.thukera.user.model.enums;

public enum RoleName {
	ROLE_USER("user"), ROLE_ADMIN("admin");

	private String key;

	RoleName(String key) {
		this.key = key;
	}

	public static RoleName getRole(String key) {
		for (RoleName n : values()) {
			if (n.key.equalsIgnoreCase(key)) {
				return n;
			}
		}
		return null;

	}

}