/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.engine.common.impl.interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flowable.engine.common.api.FlowableException;
import org.flowable.engine.common.api.FlowableOptimisticLockingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class AbstractCommandContext {

  private static Logger log = LoggerFactory.getLogger(AbstractCommandContext.class);

  protected BaseCommand<?, ? extends AbstractCommandContext> command;
  protected Map<Class<?>, SessionFactory> sessionFactories;
  protected Map<Class<?>, Session> sessions = new HashMap<Class<?>, Session>();
  protected Throwable exception;
  protected List<BaseCommandContextCloseListener<AbstractCommandContext>> closeListeners;
  protected Map<String, Object> attributes; // General-purpose storing of anything during the lifetime of a command context
  protected boolean reused;

  public AbstractCommandContext(BaseCommand<?, ? extends AbstractCommandContext> command) {
    this.command = command;
  }

  public void close() {
    
    // The intention of this method is that all resources are closed properly, even if exceptions occur
    // in close or flush methods of the sessions or the transaction context.

    try {
      try {
        try {
          executeCloseListenersClosing();
          if (exception == null) {
            flushSessions();
          }
        } catch (Throwable exception) {
          exception(exception);
          
        } finally {
          
          try {
            if (exception == null) {
              executeCloseListenersAfterSessionFlushed();
            }
          } catch (Throwable exception) {
            exception(exception);
          }
          
          if (exception != null) {
            logException();
            executeCloseListenersCloseFailure();
          } else {
            executeCloseListenersClosed();
          }
          
        }
      } catch (Throwable exception) {
        // Catch exceptions during rollback
        exception(exception);
      } finally {
        // Sessions need to be closed, regardless of exceptions/commit/rollback
        closeSessions();
      }
    } catch (Throwable exception) {
      // Catch exceptions during session closing
      exception(exception);
    }
    
    if (exception != null) {
      rethrowExceptionIfNeeded();
    }
  }

  protected void logException() {
    if (exception instanceof FlowableOptimisticLockingException) {
      // reduce log level, as normally we're not interested in logging this exception
      log.debug("Optimistic locking exception : " + exception);
    } else {
      log.error("Error while closing command context", exception);
    }
  }

  protected void rethrowExceptionIfNeeded() throws Error {
    if (exception instanceof Error) {
      throw (Error) exception;
    } else if (exception instanceof RuntimeException) {
      throw (RuntimeException) exception;
    } else {
      throw new FlowableException("exception while executing command " + command, exception);
    }
  }

  public void addCloseListener(BaseCommandContextCloseListener<AbstractCommandContext> commandContextCloseListener) {
    if (closeListeners == null) {
      closeListeners = new ArrayList<BaseCommandContextCloseListener<AbstractCommandContext>>(1);
    }
    closeListeners.add(commandContextCloseListener);
  }

  public List<BaseCommandContextCloseListener<AbstractCommandContext>> getCloseListeners() {
    return closeListeners;
  }
  
  protected void executeCloseListenersClosing() {
    if (closeListeners != null) {
      try {
        for (BaseCommandContextCloseListener<AbstractCommandContext> listener : closeListeners) {
          listener.closing(this);
        }
      } catch (Throwable exception) {
        exception(exception);
      }
    }
  }
  
  protected void executeCloseListenersAfterSessionFlushed() {
    if (closeListeners != null) {
      try {
        for (BaseCommandContextCloseListener<AbstractCommandContext> listener : closeListeners) {
          listener.afterSessionsFlush(this);
        }
      } catch (Throwable exception) {
        exception(exception);
      }
    }
  }
  
  protected void executeCloseListenersClosed() {
    if (closeListeners != null) {
      try {
        for (BaseCommandContextCloseListener<AbstractCommandContext> listener : closeListeners) {
          listener.closed(this);
        }
      } catch (Throwable exception) {
        exception(exception);
      }
    }
  }
  
  protected void executeCloseListenersCloseFailure() {
    if (closeListeners != null) {
      try {
        for (BaseCommandContextCloseListener<AbstractCommandContext> listener : closeListeners) {
          listener.closeFailure(this);
        }
      } catch (Throwable exception) {
        exception(exception);
      }
    }
  }

  protected void flushSessions() {
    for (Session session : sessions.values()) {
      session.flush();
    }
  }

  protected void closeSessions() {
    for (Session session : sessions.values()) {
      try {
        session.close();
      } catch (Throwable exception) {
        exception(exception);
      }
    }
  }

  /**
   * Stores the provided exception on this {@link AbstractCommandContext} instance.
   * That exception will be rethrown at the end of closing the {@link AbstractCommandContext} instance. 
   * 
   * If there is already an exception being stored, a 'masked exception' message will be logged.
   */
  public void exception(Throwable exception) {
    if (this.exception == null) {
      this.exception = exception;

    } else {
      log.error("masked exception in command context. for root cause, see below as it will be rethrown later.", exception);
    }
  }

  public void addAttribute(String key, Object value) {
    if (attributes == null) {
      attributes = new HashMap<String, Object>(1);
    }
    attributes.put(key, value);
  }

  public Object getAttribute(String key) {
    if (attributes != null) {
      return attributes.get(key);
    }
    return null;
  }

  @SuppressWarnings({ "unchecked" })
  public <T> T getSession(Class<T> sessionClass) {
    Session session = sessions.get(sessionClass);
    if (session == null) {
      SessionFactory sessionFactory = sessionFactories.get(sessionClass);
      if (sessionFactory == null) {
        throw new FlowableException("no session factory configured for " + sessionClass.getName());
      }
      session = sessionFactory.openSession(this);
      sessions.put(sessionClass, session);
    }

    return (T) session;
  }

  public Map<Class<?>, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public BaseCommand<?, ? extends AbstractCommandContext> getCommand() {
    return command;
  }

  public Map<Class<?>, Session> getSessions() {
    return sessions;
  }

  public Throwable getException() {
    return exception;
  }

  public boolean isReused() {
    return reused;
  }

  public void setReused(boolean reused) {
    this.reused = reused;
  }
  
}
