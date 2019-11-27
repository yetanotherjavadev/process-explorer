package com.paka.processexplorer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paka.processexplorer.mappers.ProcessMapper;
import com.paka.processexplorer.model.ProcessDTO;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduledService {

	private boolean processesPollStarted;
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	ProcessService processService;
	@Autowired
	ProcessMapper processMapper;

	public ScheduledService(SimpMessagingTemplate simpMessagingTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	@Scheduled(fixedRate = 1000)
	public void poll() throws JsonProcessingException {
		if (processesPollStarted) {
			processService.getSystemProcessesShort();

			List<ProcessInfo> processesList = processService.getSystemProcessesShort(); // limited to 5 processes
			List<ProcessDTO> processDTOs = processMapper.mapProcessesToProcessDto(processesList);

			String processesListStr = objectMapper.writeValueAsString(processDTOs);
			this.simpMessagingTemplate.convertAndSend("/topic/processes", processesListStr);
		}
	}

	public void startProcessesPoll() {
		this.processesPollStarted = true;
	}

	public void stopProcessesPoll() {
		this.processesPollStarted = false;
	}
}
