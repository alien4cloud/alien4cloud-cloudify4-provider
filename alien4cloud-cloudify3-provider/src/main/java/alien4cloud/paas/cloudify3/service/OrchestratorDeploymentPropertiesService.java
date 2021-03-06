package alien4cloud.paas.cloudify3.service;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alien4cloud.tosca.model.definitions.PropertyConstraint;
import org.alien4cloud.tosca.model.definitions.PropertyDefinition;
import org.alien4cloud.tosca.model.definitions.ScalarPropertyValue;
import org.alien4cloud.tosca.model.definitions.constraints.GreaterThanConstraint;
import org.alien4cloud.tosca.normative.types.ToscaTypes;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import alien4cloud.paas.cloudify3.model.DeploymentPropertiesNames;
import alien4cloud.utils.MapUtil;

public class OrchestratorDeploymentPropertiesService {

    Map<String, PropertyDefinition> deploymentProperties;

    @PostConstruct
    public void buildDeploymentProperties() {
        deploymentProperties = Maps.newHashMap();

        // Field 1 : monitoring_interval_inSec
        PropertyDefinition monitoringInterval = new PropertyDefinition();
        monitoringInterval.setType(ToscaTypes.INTEGER.toString());
        monitoringInterval.setRequired(false);
        monitoringInterval.setDescription("Interval time in seconds we should check the liveliness of a compute for this deployment. Default is 1min");
        monitoringInterval.setDefault(new ScalarPropertyValue("1"));
        GreaterThanConstraint intervalConstraint = new GreaterThanConstraint();
        intervalConstraint.setGreaterThan("0");
        monitoringInterval.setConstraints(Arrays.asList((PropertyConstraint) intervalConstraint));
        deploymentProperties.put(DeploymentPropertiesNames.MONITORING_INTERVAL_INMINUTE, monitoringInterval);

        // Field 2 : auto_heal
        PropertyDefinition autoHeal = new PropertyDefinition();
        autoHeal.setType(ToscaTypes.BOOLEAN.toString());
        autoHeal.setRequired(false);
        autoHeal.setDescription("Whether to enable or not the auto-heal process on this deployment. Default is disabled.");
        autoHeal.setDefault(new ScalarPropertyValue("false"));
        deploymentProperties.put(DeploymentPropertiesNames.AUTO_HEAL, autoHeal);
    }

    public Map<String, PropertyDefinition> getDeploymentProperties() {
        return deploymentProperties;
    }

    public String getValue(Map<String, String> propertiesValues, String propertyName) {
        return (String) MapUtil.get(propertiesValues, propertyName);
    }

    public String getValueOrDefault(Map<String, String> propertiesValues, String propertyName) {
        String value = getValue(propertiesValues, propertyName);
        if (StringUtils.isBlank(value)) {
            PropertyDefinition definition = deploymentProperties.get(propertyName);
            if (definition != null) {
                value = definition.getDefault().getValue().toString();
            }
        }
        return value;
    }

}
