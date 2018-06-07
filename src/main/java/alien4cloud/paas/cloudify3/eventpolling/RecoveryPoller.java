package alien4cloud.paas.cloudify3.eventpolling;

import alien4cloud.dao.IGenericSearchDAO;
import alien4cloud.paas.cloudify3.util.DateUtil;
import alien4cloud.paas.cloudify3.util.SyspropConfig;
import alien4cloud.paas.model.PaaSDeploymentLog;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This poller is responsible of polling events that have been missed when the system was down.
 */
@Slf4j
@Setter
public class RecoveryPoller extends AbstractPoller {

    /**
     * This is the max recovery period we will use if no event is found in the system.
     */
    private static final Period MAX_HISTORY_PERIOD = Period.ofDays(SyspropConfig.getInt(SyspropConfig.RECOVERYPOLLER_MAX_HISTORY_PERIOD_IN_DAYS, 10));

    private static final boolean ACTIVATED = SyspropConfig.getBoolean(SyspropConfig.RECOVERYPOLLER_ACTIVATED, true);

    @Override
    public String getPollerNature() {
        return "Recovery stream";
    }

    @Resource(name = "alien-monitor-es-dao")
    private IGenericSearchDAO alienMonitorDao;

    /**
     * Used to submit the recovery poll task.
     */
    private ScheduledExecutorService scheduler;

    /**
     * Poll historical events since the last known event date (stored in the system A4C) to now.
     * By this way all events that have been missed while the poller was offline will be polled.
     */
    @Override
    public void start() {
        if (!ACTIVATED) {
            log.info("Recovery is desactivated");
            return;
        }
        scheduler.submit(() -> {
            try {
                recovery();
            } catch (Exception e) {
                log.error("Fatal error occurred: ", e);
            }
        });
    }

    private void recovery() {
        logInfo("Starting recovery polling");
        // poll events until :
        // now
        // + LivePoller.POLL_PERIOD (the live poller will take in charge the live event stream after sleeping the POLL_PERIOD).
        Instant toDate = Instant.now().plus(LivePoller.POLL_PERIOD);

        PaaSDeploymentLog lastEvent = null;
        try {
            logDebug("Searching for last event stored in the system");
            lastEvent = alienMonitorDao.buildQuery(PaaSDeploymentLog.class).prepareSearch().setFieldSort("timestamp", true).find();
            if (log.isDebugEnabled()) {
                logDebug("The last event found in the system date from {}", (lastEvent == null) ? "(no event found)" : DateUtil
                        .logDate(lastEvent.getTimestamp()));
            }
//            if (lastEvent != null) {
            // we don't want this event to be re-polled
            // FIXME: useless since the id stored in ES is autogerated and will not match the eventId from cfy
//                    getEventCache().blackList(lastEvent.getId());
//            }
        } catch (Exception e) {
            log.warn("Not able to find last known event timestamp ({})", e.getMessage());
        }
        // add 1 ms to the last event received to avoid repolling it
        final Instant fromDate = (lastEvent == null) ? Instant.now().minus(MAX_HISTORY_PERIOD) : lastEvent.getTimestamp().toInstant().plus(1, ChronoUnit.MILLIS);
        logInfo("Will poll historical epoch {} -> {}", DateUtil.logDate(fromDate), DateUtil.logDate(toDate));
        try {
            PollResult pollResult = pollEpoch(fromDate, toDate);
            String duration = DurationFormatUtils.formatDurationHMS(toDate.until(Instant.now(), ChronoUnit.MILLIS));
            logInfo("Recovery polling terminated: {} events polled in {} batches, {} dispatched (took {}ms)", pollResult.eventPolledCount, pollResult.bactchCount, pollResult.eventDispatchedCount, duration);
        } catch (PollingException e) {
            // TODO: manage disaster recovery
            logError("Giving up polling after several retries", e);
            return;
        }
    }

}