package com.paka.processexplorer.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paka.processexplorer.mappers.ProcessMapper;
import com.paka.processexplorer.model.ProcessDTO;
import com.paka.processexplorer.service.ProcessService;
import com.paka.processexplorer.service.ScheduledService;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Controller
public class ProcessController {

	private final SimpMessagingTemplate simpMessagingTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private static String NO_PROCESS_WITH_GIVEN_ID_EXISTS = "No process with id {} found";

	@Autowired
	ProcessService processService;
	@Autowired
	ScheduledService scheduledService;
	@Autowired
	ProcessMapper processMapper;

	public ProcessController(SimpMessagingTemplate simpMessagingTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	@MessageMapping("/greetings")
	public void greet(String greeting) {
		System.out.println("Greeting for " + greeting);

		String text = "[" + Instant.now() + "]: " + greeting;
		this.simpMessagingTemplate.convertAndSend("/topic/greetings", text);
	}

	@MessageMapping("/get-processes") // good old rest representation
	public void getProcesses(String timeAt) throws JsonProcessingException {
		System.out.println("Request for getting processes at time: " + timeAt);

		List<ProcessInfo> processesList = processService.getSystemProcesses();
		List<ProcessDTO> processDTOs = processMapper.mapProcessesToProcessDto(processesList);

		String processesListStr = objectMapper.writeValueAsString(processDTOs);
		this.simpMessagingTemplate.convertAndSend("/topic/processes", processesListStr);
	}

	@MessageMapping("/get-processes/{id}")
	public void getProcessById(String pid) throws JsonProcessingException {
		System.out.println("Request for getting process with id: " + pid);

		Optional<ProcessInfo> processInfo = processService.getProcessById(pid);

		if (processInfo.isPresent()) {
			ProcessDTO processDTO = processMapper.fromProcessInfo(processInfo.get());
			String processDTOStr = objectMapper.writeValueAsString(processDTO);
			this.simpMessagingTemplate.convertAndSend("/topic/process-by-id", processDTOStr);
		} else {
			this.simpMessagingTemplate.convertAndSend("/topic/process-by-id", String.format(NO_PROCESS_WITH_GIVEN_ID_EXISTS, pid));
		}
	}

	@MessageMapping("/start-process-polling")
	public void enableScheduler() {
		scheduledService.startProcessesPoll();
	}

	@MessageMapping("/stop-process-polling")
	public void disableScheduler() {
		scheduledService.stopProcessesPoll();
	}
}