package com.divyansh.airbnbapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EnvOnlyTest {

	@Autowired
	private Environment env;

	@Test
	void envVariablesShouldExist() {
		printProp("DB_URL", false);
		printProp("DB_USERNAME", false);
		printProp("DB_PASS", true);
		printProp("JWT_SECRET", true);
	}

	private void printProp(String key, boolean sensitive) {
		String value = env.getProperty(key);

		assertNotNull(value, "❌ Missing property: " + key);
		assertFalse(value.isBlank(), "❌ Blank property: " + key);

		if (sensitive) {
			System.out.println("✔ " + key + " = " + mask(value));
		} else {
			System.out.println("✔ " + key + " = " + value);
		}
	}

	private String mask(String value) {
		if (value.length() <= 4) return "****";
		return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
	}
}
