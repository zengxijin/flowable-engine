package org.activiti5.engine.test.regression;

import org.activiti5.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class ThrowBpmnError implements JavaDelegate {
	
	public void execute(DelegateExecution execution) {
		throw new BpmnError("manual", "Manually throwing a BpmnError from this instance of \"Demo Partial Deletion\".");
	}

}