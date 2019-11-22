package com.paka.processexplorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProcessExplorerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessExplorerApplication.class, args);

//		List<ProcessInfo> processesList = JProcesses.getProcessList();
//
//		for (final ProcessInfo processInfo : processesList) {
//			System.out.println("Process PID: " + processInfo.getPid());
//			System.out.println("Process Name: " + processInfo.getName());
//			System.out.println("Process Time: " + processInfo.getTime());
//			System.out.println("User: " + processInfo.getUser());
//			System.out.println("Virtual Memory: " + processInfo.getVirtualMemory());
//			System.out.println("Physical Memory: " + processInfo.getPhysicalMemory());
//			System.out.println("CPU usage: " + processInfo.getCpuUsage());
//			System.out.println("Start Time: " + processInfo.getStartTime());
//			System.out.println("Priority: " + processInfo.getPriority());
//			System.out.println("Full command: " + processInfo.getCommand());
//			System.out.println("------------------");
//		}
	}

}
