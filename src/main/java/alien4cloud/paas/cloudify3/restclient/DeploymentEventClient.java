package alien4cloud.paas.cloudify3.restclient;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import alien4cloud.paas.cloudify3.model.EventType;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DeploymentEventClient extends AbstractEventClient {

    protected Map<String, Object> createEventsQuery() {
        Map<String, Object> eventsQuery = Maps.newHashMap();
        eventsQuery.put("type", "cloudify_event");
        // eventsQuery.put("event_type",
        // new String[] { EventType.TASK_SUCCEEDED, EventType.A4C_PERSISTENT_EVENT, EventType.A4C_WORKFLOW_EVENT, EventType.A4C_WORKFLOW_STARTED });
        return eventsQuery;
    }

    /**
     * @deprecated hack due to event api limitation on filtering on event_type. Delete this when fixed!
     */
    @Override
    protected Set<String> getValidEventTypes() {
        return Sets.newHashSet(EventType.TASK_SUCCEEDED, EventType.A4C_PERSISTENT_EVENT, EventType.A4C_WORKFLOW_EVENT, EventType.A4C_WORKFLOW_STARTED);
    }
}
