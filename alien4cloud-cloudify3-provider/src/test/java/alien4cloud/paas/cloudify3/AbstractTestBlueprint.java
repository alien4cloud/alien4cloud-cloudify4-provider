package alien4cloud.paas.cloudify3;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.inject.Inject;

import org.junit.Before;

import alien4cloud.deployment.ArtifactProcessorService;
import alien4cloud.paas.cloudify3.service.BlueprintService;
import alien4cloud.paas.cloudify3.service.CloudifyDeploymentBuilderService;
import alien4cloud.paas.cloudify3.service.PropertyEvaluatorService;
import alien4cloud.paas.cloudify3.util.ApplicationUtil;
import alien4cloud.paas.cloudify3.util.DeploymentLauncher;
import alien4cloud.paas.cloudify3.util.FileTestUtil;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import alien4cloud.utils.FileUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTestBlueprint extends AbstractTest {

    @Inject
    private BlueprintService blueprintService;

    @Inject
    private CloudifyDeploymentBuilderService cloudifyDeploymentBuilderService;

    @Inject
    private DeploymentLauncher deploymentLauncher;

    @Inject
    private ApplicationUtil applicationUtil;

    @Inject
    private PropertyEvaluatorService propertyEvaluatorService;

    @Inject
    private ArtifactProcessorService artifactProcessorService;

    protected boolean record = true;

    /**
     * Set true to this boolean so the blueprint will be uploaded to the manager to verify
     */
    protected boolean verifyBlueprintUpload = false;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
    }

    protected interface DeploymentContextVisitor {
        void visitDeploymentContext(PaaSTopologyDeploymentContext context) throws Exception;
    }

    @SneakyThrows
    protected void testGeneratedBlueprintFile(String topology) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (String location : LOCATIONS) {
            testGeneratedBlueprintFile(topology, location, topology, stackTraceElements[2].getMethodName(), null);
        }
    }

    @SneakyThrows
    protected Path testGeneratedBlueprintFile(String topology, String locationName, String outputFile, String testName,
            DeploymentContextVisitor contextVisitor) {
        if (!applicationUtil.isTopologyExistForLocation(topology, locationName)) {
            log.warn("Topology {} do not exist for location {}", topology, locationName);
            return null;
        }
        PaaSTopologyDeploymentContext context = deploymentLauncher.buildPaaSDeploymentContext(testName, topology, locationName);
        artifactProcessorService.processArtifacts(context);
        if (contextVisitor != null) {
            contextVisitor.visitDeploymentContext(context);
        }
        propertyEvaluatorService.processGetPropertyFunction(context);
        Path generated = blueprintService.generateBlueprint(cloudifyDeploymentBuilderService.buildCloudifyDeployment(context));
        Path generatedDirectory = generated.getParent();
        String recordedDirectory = "src/test/resources/outputs/blueprints/" + locationName + "/" + outputFile;
        if (isRecord()) {
            FileUtil.delete(Paths.get(recordedDirectory));
            FileUtil.copy(generatedDirectory, Paths.get(recordedDirectory), StandardCopyOption.REPLACE_EXISTING);
            if (isVerifyBlueprintUpload()) {
                deploymentLauncher.initializeCloudifyManagerConnection();

                cloudConfigurationHolder.getApiClient().getBlueprintClient().create(topology, generated.toString());
                cloudConfigurationHolder.getApiClient().getBlueprintClient().delete(topology);
            }
        } else {
            FileTestUtil.assertFilesAreSame(Paths.get(recordedDirectory), generatedDirectory, ".+.zip", ".+/cloudify-openstack-plugin/.+", ".+/monitor/.+");
        }
        return generated;
    }

    /**
     * Return true if the blueprint has changed and you want to re-register
     */
    protected abstract boolean isRecord();

    /**
     * return true if you want the blueprint to be uploaded to the manager to verify
     */
    protected abstract boolean isVerifyBlueprintUpload();
}
