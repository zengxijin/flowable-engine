package org.flowable.engine.impl.event.logger;

import java.util.ArrayList;
import java.util.List;

import org.flowable.engine.impl.event.logger.handler.EventLoggerEventHandler;
import org.flowable.engine.impl.interceptor.CommandContext;

/**
 * @author Joram Barrez
 */
public abstract class AbstractEventFlusher implements EventFlusher {

  protected List<EventLoggerEventHandler> eventHandlers = new ArrayList<EventLoggerEventHandler>();

  @Override
  public void closed(CommandContext commandContext) {
    // Not interested in closed
  }

  public List<EventLoggerEventHandler> getEventHandlers() {
    return eventHandlers;
  }

  public void setEventHandlers(List<EventLoggerEventHandler> eventHandlers) {
    this.eventHandlers = eventHandlers;
  }

  public void addEventHandler(EventLoggerEventHandler databaseEventLoggerEventHandler) {
    eventHandlers.add(databaseEventLoggerEventHandler);
  }

}
