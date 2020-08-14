package com.viettel.vtpgw.model;

import com.viettel.vtpgw.builtin.bpmn.BpmnProcess;

public interface BpmnService extends Service{
	String BUILT_IN_BPMN="BPMN";
	BpmnProcess process();
}
