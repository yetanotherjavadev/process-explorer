package com.paka.processexplorer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@Setter
@Getter
public class ProcessDTO implements Serializable {
	private String pid;
	private String cpuPercentage;
	private String state;
	private String creationDate;
	private String executionPath;
	private String name;
}
