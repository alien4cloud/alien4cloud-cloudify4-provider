package alien4cloud.paas.cloudify3;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;

import alien4cloud.paas.cloudify3.service.*;
import alien4cloud.paas.exception.PaaSNotYetDeployedException;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import alien4cloud.orchestrators.plugin.ILocationConfiguratorPlugin;
import alien4cloud.orchestrators.plugin.IOrchestratorPlugin;
import alien4cloud.orchestrators.plugin.model.PluginArchive;
import alien4cloud.paas.IPaaSCallback;
import alien4cloud.paas.cloudify3.configuration.CloudConfiguration;
import alien4cloud.paas.cloudify3.configuration.CloudConfigurationHolder;
import alien4cloud.paas.cloudify3.event.AboutToDeployTopologyEvent;
import alien4cloud.paas.cloudify3.location.ITypeAwareLocationConfigurator;
import alien4cloud.paas.cloudify3.service.model.CloudifyDeployment;
import alien4cloud.paas.cloudify3.util.FutureUtil;
import alien4cloud.paas.exception.OperationExecutionException;
import alien4cloud.paas.exception.PaaSAlreadyDeployedException;
import alien4cloud.paas.exception.PluginConfigurationException;
import alien4cloud.paas.model.AbstractMonitorEvent;
import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.InstanceInformation;
import alien4cloud.paas.model.NodeOperationExecRequest;
import alien4cloud.paas.model.PaaSDeploymentContext;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import lombok.extern.slf4j.Slf4j;

/**
 * The cloudify 3 PaaS Provider implementation
 */
@Slf4j
@Component("cloudify-paas-provider-bean")
public class CloudifyOrchestrator implements IOrchestratorPlugin<CloudConfiguration> {

    @Resource(name = "cloudify-deployment-service")
    private DeploymentService deploymentService;

    @Resource(name = "cloudify-custom-workflow-service")
    private CustomWorkflowService customWorkflowService;

    @Resource(name = "cloudify-configuration-holder")
    private CloudConfigurationHolder cloudConfigurationHolder;

    @Resource(name = "cloudify-event-service")
    private EventService eventService;

    @Resource(name = "cloudify-deployment-builder-service")
    private CloudifyDeploymentBuilderService cloudifyDeploymentBuilderService;

//    @Resource(name = "cloudify-snapshot-service")
//    private SnapshotService snapshotService;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private StatusService statusService;

    @Resource(name= "cloudify-async-http-request-factory")
    private SimpleClientHttpRequestFactory simpleClientHttpRequestFactory;

    @Inject
    private OpenStackAvailabilityZonePlacementPolicyService osAzPPolicyService;

    @Inject
    private PluginArchiveService archiveService;

    private List<PluginArchive> archives;

    public synchronized void parseOrchestratorArchive() {
        if (archives == null) {
            archives = Lists.newArrayList();
            archives.add(archiveService.parsePluginArchives("provider/common/configuration"));
        }
    }

    @Override
    public synchronized List<PluginArchive> pluginArchives() {
        if (this.archives == null) {
            parseOrchestratorArchive();
        }
        return this.archives;
    }

    @Resource
    private PropertyEvaluatorService propertyEvaluatorService;

    /**
     * ********************************************************************************************************************
     * *****************************************************Deployment*****************************************************
     * ********************************************************************************************************************
     */

    @Override
    public void deploy(PaaSTopologyDeploymentContext deploymentContext, final IPaaSCallback callback) {
        // first of all, let's check this deployment's status
        DeploymentStatus currentStatus = statusService.getFreshStatus(deploymentContext.getDeploymentPaaSId());
        if (!DeploymentStatus.UNDEPLOYED.equals(currentStatus)) {
            log.warn("Not possible to deploy {} for alien deployment {}: deployment is active on Cloudify.", deploymentContext.getDeploymentPaaSId(),
                    deploymentContext.getDeploymentId());
            callback.onFailure(new PaaSAlreadyDeployedException("Deployment " + deploymentContext.getDeploymentPaaSId()
                    + " is active (must undeploy first) or is in unknown state (must wait for status available)"));
            return;
        }
        // Cloudify 3 will use recipe id to identify a blueprint and a deployment instead of deployment id
        log.info("Deploying {} for alien deployment {}", deploymentContext.getDeploymentPaaSId(), deploymentContext.getDeploymentId());
        eventService.registerDeployment(deploymentContext.getDeploymentPaaSId(), deploymentContext.getDeploymentId());
        statusService.registerDeployment(deploymentContext.getDeploymentPaaSId());

        applicationContext.publishEvent(new AboutToDeployTopologyEvent(this, deploymentContext));
        try {
            // TODO Better do it in Alien4Cloud or in plugin ?
            propertyEvaluatorService.processGetPropertyFunction(deploymentContext);

            // pre-process the topology to add availability zones.
            osAzPPolicyService.process(deploymentContext);

            CloudifyDeployment deployment = cloudifyDeploymentBuilderService.buildCloudifyDeployment(deploymentContext);
            FutureUtil.associateFutureToPaaSCallback(deploymentService.deploy(deployment), callback);
        } catch (Throwable e) {
            statusService.registerDeploymentStatus(deploymentContext.getDeploymentPaaSId(), DeploymentStatus.FAILURE);
            callback.onFailure(e);
        }
    }

    @Override
    public void update(PaaSTopologyDeploymentContext deploymentContext, IPaaSCallback callback) {
        // first of all, let's check this deployment's status
        DeploymentStatus currentStatus = statusService.getFreshStatus(deploymentContext.getDeploymentPaaSId());
        if (!(DeploymentStatus.DEPLOYED.equals(currentStatus) || DeploymentStatus.UPDATED.equals(currentStatus)
                || DeploymentStatus.UPDATE_FAILURE.equals(currentStatus))) {
            log.warn("Not possible to update {} for alien deployment {}: deployment is not active on Cloudify.", deploymentContext.getDeploymentPaaSId(),
                    deploymentContext.getDeploymentId());
            callback.onFailure(new PaaSNotYetDeployedException("Deployment " + deploymentContext.getDeploymentPaaSId()
                    + " is not active (must be deployed to be updated) or is in unknown state (must wait for status available)"));
            return;
        }

        log.info("Deploying {} for alien deployment {}", deploymentContext.getDeploymentPaaSId(), deploymentContext.getDeploymentId());
        eventService.registerDeployment(deploymentContext.getDeploymentPaaSId(), deploymentContext.getDeploymentId());
        statusService.registerDeploymentStatus(deploymentContext.getDeploymentPaaSId(), DeploymentStatus.UPDATE_IN_PROGRESS);

        applicationContext.publishEvent(new AboutToDeployTopologyEvent(this, deploymentContext));
        try {
            // TODO Better do it in Alien4Cloud or in plugin ?
            propertyEvaluatorService.processGetPropertyFunction(deploymentContext);

            // pre-process the topology to add availability zones.
            osAzPPolicyService.process(deploymentContext);

            CloudifyDeployment deployment = cloudifyDeploymentBuilderService.buildCloudifyDeployment(deploymentContext);
            ListenableFuture<Void> deploymentUpdate = deploymentService.update(deployment);
            FutureUtil.associateFutureToPaaSCallback(deploymentUpdate, callback);

        } catch (Throwable e) {
            statusService.registerDeploymentStatus(deploymentContext.getDeploymentPaaSId(), DeploymentStatus.UPDATE_FAILURE);
            callback.onFailure(e);
        }
    }

    @Override
    public void undeploy(PaaSDeploymentContext deploymentContext, IPaaSCallback callback) {
        FutureUtil.associateFutureToPaaSCallback(deploymentService.undeploy(deploymentContext), callback);
    }

    /**
     * ********************************************************************************************************************
     * *****************************************************Configurations*************************************************
     * ********************************************************************************************************************
     */

    @Override
    public void init(Map<String, PaaSTopologyDeploymentContext> activeDeployments) {
        log.info("Initializing orchestrator");
//        snapshotService.init();
        if (activeDeployments == null) {
            return;
        } else {
            eventService.init(activeDeployments);
            statusService.init(activeDeployments);
        }
    }

    @Override
    public void setConfiguration(String orchestratorId, CloudConfiguration newConfiguration) throws PluginConfigurationException {
        if (newConfiguration == null) {
            throw new PluginConfigurationException("Configuration must not be null");
        }
        if (newConfiguration.getUrl() == null) {
            throw new PluginConfigurationException("Url must be defined.");
        }

        // -1 == system timeout
        Integer timeout = newConfiguration.getConnectionTimeout() == null ? -1 : newConfiguration.getConnectionTimeout();
        simpleClientHttpRequestFactory.setConnectTimeout(timeout);

        cloudConfigurationHolder.setConfigurationAndNotifyListeners(newConfiguration);
    }

    /**
     * ********************************************************************************************************************
     * *****************************************************Events*********************************************************
     * ********************************************************************************************************************
     */

    @Override
    public void getStatus(PaaSDeploymentContext deploymentContext, IPaaSCallback<DeploymentStatus> callback) {
        statusService.getStatus(deploymentContext.getDeploymentPaaSId(), callback);
    }

    @Override
    public void getInstancesInformation(PaaSTopologyDeploymentContext deploymentContext,
            IPaaSCallback<Map<String, Map<String, InstanceInformation>>> callback) {
        statusService.getInstancesInformation(deploymentContext, callback);
    }

    @Override
    public void getEventsSince(Date lastTimestamp, int batchSize, final IPaaSCallback<AbstractMonitorEvent[]> eventsCallback) {
        ListenableFuture<AbstractMonitorEvent[]> events = eventService.getEventsSince(lastTimestamp, batchSize);
        Futures.addCallback(events, new FutureCallback<AbstractMonitorEvent[]>() {
            @Override
            public void onSuccess(AbstractMonitorEvent[] result) {
                eventsCallback.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable t) {
                eventsCallback.onFailure(t);
            }
        });
    }

    /**
     * ********************************************************************************************************************
     * *****************************************************Not implemented operation**************************************
     * ********************************************************************************************************************
     */

    @Override
    public void scale(PaaSDeploymentContext deploymentContext, String nodeTemplateId, int instances, IPaaSCallback callback) {
        FutureUtil.associateFutureToPaaSCallback(customWorkflowService.scale(deploymentContext.getDeploymentPaaSId(), nodeTemplateId, instances), callback);
    }

    @Override
    public void launchWorkflow(PaaSDeploymentContext deploymentContext, String workflowName, Map<String, Object> workflowParameters,
            IPaaSCallback<?> callback) {
        FutureUtil.associateFutureToPaaSCallback(
                customWorkflowService.launchWorkflow(deploymentContext.getDeploymentPaaSId(), workflowName, workflowParameters), callback);
    }

    @Override
    public void executeOperation(PaaSTopologyDeploymentContext deploymentContext, NodeOperationExecRequest nodeOperationExecRequest,
            IPaaSCallback<Map<String, String>> callback) throws OperationExecutionException {
        CloudifyDeployment deployment = cloudifyDeploymentBuilderService.buildCloudifyDeployment(deploymentContext);
        ListenableFuture<Map<String, String>> executionFutureResult = customWorkflowService.executeOperation(deployment, nodeOperationExecRequest);
        FutureUtil.associateFutureToPaaSCallback(executionFutureResult, callback);
    }

    @Override
    public void switchInstanceMaintenanceMode(PaaSDeploymentContext arg0, String arg1, String arg2, boolean arg3) {
        throw new NotImplementedException();
    }

    @Override
    public void switchMaintenanceMode(PaaSDeploymentContext arg0, boolean arg1) {
        throw new NotImplementedException();
    }

    @Override
    public ILocationConfiguratorPlugin getConfigurator(String locationType) {
        Collection<ITypeAwareLocationConfigurator> typeAwareLocationConfigurators = applicationContext.getBeansOfType(ITypeAwareLocationConfigurator.class)
                .values();
        for (ITypeAwareLocationConfigurator typeAwareLocationConfigurator : typeAwareLocationConfigurators) {
            if (typeAwareLocationConfigurator.getManagedLocationTypes().contains(locationType)) {
                return typeAwareLocationConfigurator;
            }
        }
        return null;
    }
}
