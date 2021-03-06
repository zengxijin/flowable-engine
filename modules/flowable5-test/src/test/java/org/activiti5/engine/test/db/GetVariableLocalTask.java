package org.activiti5.engine.test.db;

import org.activiti5.engine.RuntimeService;
import org.activiti5.engine.impl.context.Context;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class GetVariableLocalTask implements JavaDelegate {
  @Override
  public void execute(DelegateExecution execution) {
    RuntimeService runtimeService = Context.getProcessEngineConfiguration().getRuntimeService();
    runtimeService.getVariableLocal(execution.getProcessInstanceId(), "Variable-That-Does-Not-Exist");
  }
}