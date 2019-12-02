package com.paka.processexplorer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SystemInfo implements Serializable {
	private Long currentTime;
	private List<ProcessDTO> trackedProcesses; // top-10 tracked processes
	private List<ProcessDTO> uiProcesses; // processes to be shown on UI
	private Double overallCpuUsage;
}
