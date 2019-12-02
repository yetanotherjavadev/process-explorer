package com.paka.processexplorer.service;

import com.paka.processexplorer.mappers.ProcessMapper;
import com.paka.processexplorer.model.ProcessDTO;
import com.paka.processexplorer.model.ProcessesData;
import com.paka.processexplorer.model.SystemInfo;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessService {

	private final int MAX_UI_PROCESSES = 5; // max no of trackedProcesses that will ever reach UI

	/**
	 * A list of processes that are tracked from the client side (ones that are shown on the chart)
	 */
	private List<String> uiProcessesIds;

	@Autowired
	private ProcessMapper processMapper;

	/**
	 * Kills a process with given Id
	 *
	 * @param pid - process Id
	 * @return true if the kill was successful, false otherwise
	 */
	public boolean killProcess(String pid) {
		return JProcesses.killProcess(Integer.valueOf(pid)).isSuccess();
	}

	/**
	 * Maps the list of trackedProcesses into array of ids and stores it into "UI tracked processes list"
	 *
	 * This method should be called only once
	 */
	private void initTrackingList(List<ProcessInfo> processInfos) {
		uiProcessesIds = processInfos.stream().sorted(Comparator.comparing(o -> -Double.valueOf(o.getCpuUsage())))
				.limit(MAX_UI_PROCESSES).map(ProcessInfo::getPid).collect(Collectors.toList());
	}

	/**
	 * Gets updated information on tracked trackedProcesses and current system state data.
	 */
	private ProcessesData getProcessData() {
		ProcessesData data = new ProcessesData();

		List<ProcessInfo> allSystemProcessesList = JProcesses.getProcessList();

		if (uiProcessesIds == null) { // this is called only once
			initTrackingList(allSystemProcessesList);
		}

		List<ProcessInfo> uiProcesses = allSystemProcessesList.stream().filter(processInfo ->
			uiProcessesIds.stream().anyMatch((id) -> processInfo.getPid().equals(id))
		).collect(Collectors.toList());

		double cpuUsageSum = allSystemProcessesList.stream().mapToDouble(p -> Double.valueOf(p.getCpuUsage())).sum();

		data.setOverallCpuUsage(cpuUsageSum);
		data.setTrackedProcesses(allSystemProcessesList);
		data.setUiProcesses(uiProcesses);

		return data;
	}

	public SystemInfo getCurrentSystemInfo() {
		ProcessesData data = getProcessData();

		List<ProcessDTO> trackedProcesses = processMapper.mapProcessesToProcessDto(data.getTrackedProcesses());
		List<ProcessDTO> uiProcesses = processMapper.mapProcessesToProcessDto(data.getUiProcesses());

		SystemInfo sysInfo = new SystemInfo();
		sysInfo.setTrackedProcesses(trackedProcesses);
		sysInfo.setUiProcesses(uiProcesses);
		sysInfo.setCurrentTime(System.currentTimeMillis());
		sysInfo.setOverallCpuUsage(data.getOverallCpuUsage());

		return sysInfo;
	}

	/**
	 * In case we kill a process we need to:
	 *
	 * 1) in case if the ID was in uiProcessesIds - remove it
	 * 2) grab current running processes and put the top-consuming one which is not yet tracked to the uiProcessesIds
	 *
	 * @return a PID of new process which substituted the killed one
	 */
	public String updateUiProcessList(String killedProcessId) {
		// go through all processes and take the top CPU-consuming one
		List<ProcessInfo> allProcessesList = JProcesses.getProcessList();

		List<ProcessInfo> topCpuConsumingNonTrackedProcess = allProcessesList.stream().filter(processInfo ->
				uiProcessesIds.stream().noneMatch((id) -> processInfo.getPid().equals(id))
		).sorted(Comparator.comparing(o -> -Double.valueOf(o.getCpuUsage())))
				.limit(1).collect(Collectors.toList());

		ProcessInfo newTrackedProcess = topCpuConsumingNonTrackedProcess.get(0); // there will be only one
		System.out.println("New Tracked Process added: " + newTrackedProcess.getPid() + " to substitute killed one: " + killedProcessId);

		// adding new one to track
		uiProcessesIds.add(newTrackedProcess.getPid());

		// in case killed process was tracked in ui list - do the same as above for ui list
		boolean removed = uiProcessesIds.remove(killedProcessId);
		if (removed) {
			// need to update UI list now
			uiProcessesIds.add(newTrackedProcess.getPid());
		}
		return newTrackedProcess.getPid();
	}
}
