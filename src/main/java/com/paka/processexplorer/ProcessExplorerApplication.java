package com.paka.processexplorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProcessExplorerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessExplorerApplication.class, args);
	}
}
