package com.paka.processexplorer.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paka.processexplorer.mappers.ProcessMapper;
import com.paka.processexplorer.model.ProcessDTO;
import com.paka.processexplorer.service.ProcessService;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.JProcessesResponse;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ProcessController {

	private boolean pollingActive;
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	ProcessService processService;
	@Autowired
	ProcessMapper processMapper;

	public ProcessController(SimpMessagingTemplate simpMessagingTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	@MessageMapping("/kill-process/{pid}")
	public void killProcess(@DestinationVariable String pid) {
		System.out.println("Request to kill process: " + pid);

		processService.killProcess(pid);

		JProcessesResponse response = JProcesses.killProcess(Integer.valueOf(pid));
		if (response.isSuccess()) {
			processService.updateTopList(pid);
			this.simpMessagingTemplate.convertAndSend("/topic/kill-successful", "Kill successful: " + pid);
		} else {
			this.simpMessagingTemplate.convertAndSend("/topic/kill-failed", "Kill failed: " + pid);
		}
	}

	@MessageMapping("/start-process-polling")
	public void startPolling() {
		this.pollingActive = true;
	}

	@MessageMapping("/stop-process-polling")
	public void stopPolling() {
		this.pollingActive = false;
	}

	@Scheduled(fixedDelay = 1000)
	public void poll() throws JsonProcessingException {
		if (pollingActive) {
			List<ProcessInfo> processesList = processService.getTrackedProcessesInfo(); // limited to 10 processes so far
			List<ProcessDTO> processDTOs = processMapper.mapProcessesToProcessDto(processesList);

			String processesListStr = objectMapper.writeValueAsString(processDTOs);
			this.simpMessagingTemplate.convertAndSend("/topic/processes", processesListStr);
		}
	}
}