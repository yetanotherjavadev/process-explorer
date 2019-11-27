package com.paka.processexplorer.service;

import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProcessService {

	/**
	 * Gets all system processes
	 *
	 * @return list of {@link ProcessInfo} reflecting all processes that are currently running
	 */
	public List<ProcessInfo> getSystemProcesses() {
		List<ProcessInfo> processesList = JProcesses.getProcessList();
		return processesList;
	}

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

	// temporary
	public List<ProcessInfo> getSystemProcessesShort() {
		List<ProcessInfo> processesList = JProcesses.getProcessList();
		return processesList.stream().limit(5).collect(Collectors.toList());
	}
}
