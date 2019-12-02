package com.paka.processexplorer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jutils.jprocesses.model.ProcessInfo;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProcessesData {
	private List<ProcessInfo> trackedProcesses; // all system processes
	private List<ProcessInfo> uiProcesses; // processes to be shown on graph
	private Double overallCpuUsage;
}
