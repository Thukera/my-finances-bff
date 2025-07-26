package com.thukera;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MyFinancesBffApplication {
	
	private static final Logger logger = LogManager.getLogger(MyFinancesBffApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MyFinancesBffApplication.class, args);
		
		logger.info("###### ######## ######## ######### ######## ######## ##########");
		logger.info("######## ###  MY FINANCES BFF APPLICATION STARTER  ### ########");
		logger.info("###### ######## ######## ######### ######## ######## ##########");
	}

}
