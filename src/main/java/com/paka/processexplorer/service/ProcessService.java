package com.paka.processexplorer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paka.processexplorer.mappers.ProcessMapper;
import com.paka.processexplorer.model.ProcessDTO;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProcessService {


	private final int NO_OF_TRACKED_PROCESSES = 10;
	private List<String> topConsumingProcessesIds;

	@Autowired
	ProcessMapper processMapper;

	/**
	 * Gets a process with given ID
	 *
	 * @param pid - process ID
	 * @return Optional with process with give ID if it exists and Optional(null) otherwise
	 */
	public Optional<ProcessInfo> getProcessById(String pid) {
		List<ProcessInfo> processesList = JProcesses.getProcessList();
		List<ProcessInfo> filtered = processesList.stream().filter((processInfo -> processInfo.getPid().equals(pid))).collect(Collectors.toList());
		return Optional.of(filtered.get(0));
	}

	/**
	 * Kills a process with given Id
	 *
	 * @param pid - process Id
	 * @return true if the kill was successful, false otherwise
	 */
	public boolean killProcess(String pid) {
		boolean success = JProcesses.killProcess(Integer.valueOf(pid)).isSuccess();
		return success;
	}

	// bakes the list of processes into array of ids
	private void updateTopConsumingProcessesList(List<ProcessInfo> processInfos) {
		topConsumingProcessesIds = processInfos.stream().map(ProcessInfo::getPid).collect(Collectors.toList());
	}

	/**
	 * Gets top-20 processes by cpuUsage (should be done once)
	 */
	public List<ProcessInfo> getTopConsumingProcesses() {
		List<ProcessInfo> allProcessesList = JProcesses.getProcessList();
		List<ProcessInfo> topProcesses = allProcessesList.stream().sorted(Comparator.comparing(o -> -Double.valueOf(o.getCpuUsage())))
				.limit(NO_OF_TRACKED_PROCESSES).collect(Collectors.toList());

		updateTopConsumingProcessesList(topProcesses);

		return topProcesses;
	}

	/**
	 * Gets updated information on tracked processes
	 */
	public List<ProcessInfo> getTrackedProcessesInfo() {
		List<ProcessInfo> trackedProcessesInfo;
		if (topConsumingProcessesIds == null) {
			trackedProcessesInfo = getTopConsumingProcesses();
		} else {
			List<ProcessInfo> allProcessesList = JProcesses.getProcessList();
			trackedProcessesInfo = allProcessesList.stream().filter(processInfo ->
					topConsumingProcessesIds.stream().anyMatch((id) -> processInfo.getPid().equals(id))
			).collect(Collectors.toList());
		}

		return trackedProcessesInfo;
	}

	// in case we kill a process we need to add one more to the list of top CPU-consuming processes
	public void updateTopList(String killedProcessId) {
		// remove killed process id from the tracked ones
		topConsumingProcessesIds.remove(killedProcessId);
		// go through all processes excluding the stored ones and take the top consuming
		List<ProcessInfo> allProcessesList = JProcesses.getProcessList();

		List<ProcessInfo> trackedProcessesInfo = allProcessesList.stream().filter(processInfo ->
				topConsumingProcessesIds.stream().anyMatch((id) -> processInfo.getPid().equals(id))
		).collect(Collectors.toList()); // this will have (NO_OF_TRACKED_PROCESSES - 1) items now

		List<ProcessInfo> theLastOne = allProcessesList.stream().filter(processInfo ->
				topConsumingProcessesIds.stream().noneMatch((id) -> processInfo.getPid().equals(id))
		).sorted(Comparator.comparing(o -> -Double.valueOf(o.getCpuUsage())))
				.limit(1).collect(Collectors.toList());

		ProcessInfo newTrackedProcess = theLastOne.get(0);
		System.out.println("New Tracked Process added: " + newTrackedProcess.getPid() + " to subst: " + killedProcessId);
		topConsumingProcessesIds.add(newTrackedProcess.getPid());
		trackedProcessesInfo.add(newTrackedProcess); // new one comes in here
	}

}
