package com.paka.processexplorer.mappers;

import com.paka.processexplorer.model.ProcessDTO;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcessMapper {

	public List<ProcessDTO> mapProcessesToProcessDto(List<ProcessInfo> processInfoList) {
		return processInfoList.stream().map(this::fromProcessInfo).collect(Collectors.toList());
	}

	private ProcessDTO fromProcessInfo(ProcessInfo processInfo) {
		ProcessDTO processDTO = new ProcessDTO();
		processDTO.setPid(processInfo.getPid());
		processDTO.setCpuPercentage(processInfo.getCpuUsage());
		processDTO.setExecutionPath(processInfo.getCommand());
		processDTO.setCreationDate(processInfo.getStartTime());
		processDTO.setPhysicalMemory(processInfo.getPhysicalMemory());
		processDTO.setTime(processInfo.getTime());
		processDTO.setVirtualMemory(processInfo.getVirtualMemory());
		processDTO.setName(processInfo.getName());
		return processDTO;
	}
}