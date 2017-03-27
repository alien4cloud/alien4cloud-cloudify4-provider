package alien4cloud.paas.cloudify3.blueprint;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import alien4cloud.paas.model.PaaSNodeTemplate;
import org.alien4cloud.tosca.model.types.AbstractInheritableToscaType;

import org.alien4cloud.tosca.normative.ToscaNormativeUtil;

import org.apache.commons.lang3.StringUtils;

import org.alien4cloud.tosca.model.definitions.AbstractPropertyValue;
import org.alien4cloud.tosca.model.definitions.ScalarPropertyValue;
import org.alien4cloud.tosca.model.templates.NodeTemplate;
import alien4cloud.paas.cloudify3.configuration.MappingConfiguration;
import alien4cloud.paas.cloudify3.model.DeploymentPropertiesNames;
import alien4cloud.paas.cloudify3.service.OrchestratorDeploymentPropertiesService;
import alien4cloud.paas.cloudify3.service.PropertyEvaluatorService;
import alien4cloud.paas.cloudify3.service.model.CloudifyDeployment;
import alien4cloud.utils.MapUtil;

import static alien4cloud.utils.AlienUtils.safe;

public class CommonGenerationUtil extends AbstractGenerationUtil {

    private OrchestratorDeploymentPropertiesService deploymentPropertiesService;

    public CommonGenerationUtil(MappingConfiguration mappingConfiguration, CloudifyDeployment alienDeployment, Path recipePath,
            PropertyEvaluatorService propertyEvaluatorService, OrchestratorDeploymentPropertiesService deploymentPropertiesService) {
        super(mappingConfiguration, alienDeployment, recipePath, propertyEvaluatorService);

        this.deploymentPropertiesService = deploymentPropertiesService;
    }

    public String getMonitoringInterval() {
        return deploymentPropertiesService.getValueOrDefault(alienDeployment.getProviderDeploymentProperties(),
                DeploymentPropertiesNames.MONITORING_INTERVAL_INMINUTE);
    }

    public String getScalarPropertyValue(NodeTemplate nodeTemplate, String propertyName) {
        AbstractPropertyValue value = (AbstractPropertyValue) MapUtil.get(nodeTemplate.getProperties(), propertyName);
        if (value != null) {
            return ((ScalarPropertyValue) value).getValue();
        }

        return "";
    }

    public String truncateString(String toBeAbbreviated, int maxWidth) {
        if (StringUtils.isEmpty(toBeAbbreviated)) {
            return toBeAbbreviated;
        } else {
            if (toBeAbbreviated.length() > maxWidth) {
                return toBeAbbreviated.substring(0, maxWidth);
            } else {
                return toBeAbbreviated;
            }
        }
    }

    public boolean isFromType(String type, AbstractInheritableToscaType indexedInheritableToscaElement) {
        return ToscaNormativeUtil.isFromType(type, indexedInheritableToscaElement);
    }

    public boolean doesVelocityFileExists(String velocityFilePath) {
        if (Files.exists(Paths.get(velocityFilePath))) {
            return true;
        }
        return false;
    }

    public Set<Entry> safeEntrySet(Map map) {
        return safe(map).entrySet();
    }

    public boolean isNull(Object o) {
        return o == null;
    }
}
