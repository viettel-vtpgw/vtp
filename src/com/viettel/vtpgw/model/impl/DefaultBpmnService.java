package com.viettel.vtpgw.model.impl;

import com.viettel.vtpgw.builtin.bpmn.BpmnProcess;
import com.viettel.vtpgw.model.BpmnService;

public class DefaultBpmnService extends DefaultService implements BpmnService {
	BpmnProcess process;
	@Override
	public BpmnProcess process() {
		return process;
	}
	public void setProcess(BpmnProcess process){
		this.process = process; 
	}
}
