package com.paka.processexplorer.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paka.processexplorer.mappers.ProcessMapper;
import com.paka.processexplorer.model.ProcessDTO;
import com.paka.processexplorer.model.ProcessInfoBatch;
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

	private boolean isPollingActive;
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	ProcessService processService;

	@Autowired
	ProcessMapper processMapper;

	public ProcessController(SimpMessagingTemplate simpMessagingTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	/**
	 * Tries to force kill a process with "-9" param.
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
		this.isPollingActive = true;
	}

	@MessageMapping("/stop-process-polling")
	public void stopPolling() {
		this.isPollingActive = false;
	}

	/**
	 * Every second pushes a message with currently "tracked" processes.
	 * "Tracked" processes are usually those which consume the most of CPU time.
	 *
	 * @throws JsonProcessingException - just to be sure :)
	 */
	@Scheduled(fixedDelay = 1000)
	public void poll() throws JsonProcessingException {
		if (isPollingActive) {
			List<ProcessInfo> processesList = processService.getTrackedProcessesInfo();
			List<ProcessDTO> processDTOs = processMapper.mapProcessesToProcessDto(processesList);
			ProcessInfoBatch batch = new ProcessInfoBatch();
			batch.setProcesses(processDTOs);
			batch.setCurrentTime(System.currentTimeMillis());
			String processesBatchStr = objectMapper.writeValueAsString(batch);
			this.simpMessagingTemplate.convertAndSend("/topic/processes", processesBatchStr);
		}
	}
}