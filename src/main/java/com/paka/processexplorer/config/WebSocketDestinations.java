package com.paka.processexplorer.config;

public class WebSocketDestinations {
	public static final String KILL_SUCCESSFUL = "/topic/kill-successful";
	public static final String KILL_FAILED = "/topic/kill-failed";
	public static final String GET_ALL_PROCESSES = "/topic/processes";
	public static final String KILL_PROCESS = "/kill-process/{pid}";
	public static final String START_PROCESS_POLLING = "/start-process-polling";
	public static final String STOP_PROCESS_POLLING = "/stop-process-polling";
	public static final String PROCESS_EXPLORER_WEBSOCKET = "/pe-websocket";
	public static final String TOPIC = "/topic";
	public static final String APP = "/app";
}
