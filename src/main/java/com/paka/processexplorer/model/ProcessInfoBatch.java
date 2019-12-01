package com.paka.processexplorer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProcessInfoBatch {
	private Long currentTime;
	private List<ProcessDTO> processes;
}
