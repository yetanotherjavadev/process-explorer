package com.paka.processexplorer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Main data transfer class.
 */
@Getter
@Setter
@NoArgsConstructor
public class SystemInfo implements Serializable {
	private Long currentTime;
	private List<ProcessDTO> trackedProcesses; // top tracked processes
	private List<ProcessDTO> uiProcesses; // processes to be shown on UI
	private ProcessesData data;
	private Double overallCpuUsage;
}
