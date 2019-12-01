package com.paka.processexplorer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Simplified version of a Process description object.
 */
@Setter
@Getter
@NoArgsConstructor
public class ProcessDTO implements Serializable {
	private String pid;
	private String name;
	private String cpuPercentage;
	private String creationDate;
	private String executionPath;
	private String physicalMemory;
	private String virtualMemory;
	private String time;
}
