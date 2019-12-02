package com.paka.processexplorer.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paka.processexplorer.model.SystemInfo;
import com.paka.processexplorer.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class ProcessController {

	private boolean isPollingActive;
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	ProcessService processService;

	public ProcessController(SimpMessagingTemplate simpMessagingTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	/**
	 * Tries to force kill a process with good old "kill -9" param.
	 *
	 * In case if the process was successfully killed:
	 *  1) Takes the top cpu consuming process from those which were not yet tracked.
	 *  2) sends "success" message to the client.
	 *
	 * If kill command failed just sends appropriate message to the client.
	 * @param pid - process ID
	 */
	@MessageMapping("/kill-process/{pid}")
	public void killProcess(@DestinationVariable String pid) {
		System.out.println("Request to kill process: " + pid);

		boolean killSuccessful = processService.killProcess(pid);

		if (killSuccessful) {
			processService.updateTopList(pid);
			this.simpMessagingTemplate.convertAndSend("/topic/kill-successful", "Kill successful: " + pid);
		} else {
			this.simpMessagingTemplate.convertAndSend("/topic/kill-failed", "Kill failed: " + pid);
		}
	}

	@MessageMapping("/start-process-polling")
	public void startPolling() {
		this.isPollingActive = true;
	}

	@MessageMapping("/stop-process-polling")
	public void stopPolling() {
		this.isPollingActive = false;
	}

	/**
	 * Every second pushes a message with current "system state".
	 *
	 * @throws JsonProcessingException - just to be sure :)
	 */
	@Scheduled(fixedDelay = 1000)
	public void poll() throws JsonProcessingException {
		if (isPollingActive) {
			SystemInfo info = processService.getCurrentSystemInfo();
			System.out.println(info.getCurrentTime());
			info.getUiProcesses().forEach((processDTO -> {
				System.out.println(processDTO.getName() + ": " + processDTO.getTime());
			}));
			String processesBatchStr = objectMapper.writeValueAsString(info);
			this.simpMessagingTemplate.convertAndSend("/topic/processes", processesBatchStr);
		}
	}
}