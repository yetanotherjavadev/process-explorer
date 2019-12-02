package com.paka.processexplorer.controllers;

import com.paka.processexplorer.model.KillProcessMessage;
import com.paka.processexplorer.model.SystemInfo;
import com.paka.processexplorer.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import static com.paka.processexplorer.config.WebSocketDestinations.*;

@Controller
public class ProcessController {

	private boolean isPollingActive;
	private final SimpMessagingTemplate simpMessagingTemplate;

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
	@MessageMapping(KILL_PROCESS)
	public void killProcess(@DestinationVariable String pid) {
		System.out.println("Request to kill process: " + pid);

		boolean killSuccessful = processService.killProcess(pid);

		KillProcessMessage kpm = new KillProcessMessage();

		if (killSuccessful) {
			String newProcessId = processService.updateUiProcessList(pid); // move to killProcess(...)?
			kpm.setNewProcessId(newProcessId);
			kpm.setKilledId(pid);
			this.simpMessagingTemplate.convertAndSend(KILL_SUCCESSFUL, kpm);
		} else {
			kpm.setNewProcessId(pid); // in case of killing failure send old PID
			this.simpMessagingTemplate.convertAndSend(KILL_FAILED, kpm);
		}
	}

	@MessageMapping(START_PROCESS_POLLING)
	public void startPolling() {
		this.isPollingActive = true;
	}

	@MessageMapping(STOP_PROCESS_POLLING)
	public void stopPolling() {
		this.isPollingActive = false;
	}

	/**
	 * Every second pushes a message with current "system state" to the client.
	 */
	@Scheduled(fixedDelay = 1000)
	public void poll() {
		if (isPollingActive) {
			SystemInfo info = processService.getCurrentSystemInfo();
			this.simpMessagingTemplate.convertAndSend(GET_ALL_PROCESSES, info);
		}
	}
}