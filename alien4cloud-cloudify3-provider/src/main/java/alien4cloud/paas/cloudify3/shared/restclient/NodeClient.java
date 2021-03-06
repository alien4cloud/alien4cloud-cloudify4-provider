package alien4cloud.paas.cloudify3.shared.restclient;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import alien4cloud.paas.cloudify3.model.ListNodeResponse;
import alien4cloud.paas.cloudify3.model.ListResponse;
import alien4cloud.paas.cloudify3.model.Node;
import alien4cloud.paas.cloudify3.util.FutureUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeClient {
    private static final String NODES_PATH = "/api/v3/nodes";
    private final ApiHttpClient client;

    public NodeClient(ApiHttpClient apiHttpClient) {
        this.client = apiHttpClient;
    }

    private ListenableFuture<Node[]> unwrapListResponse(ListenableFuture<ListNodeResponse> listExecutionResponse) {
        return Futures.transform(listExecutionResponse, (Function<ListNodeResponse, Node[]>) ListResponse::getItems);
    }

    public ListenableFuture<Node[]> asyncList(String deploymentId, String nodeId) {
        if (log.isDebugEnabled()) {
            log.debug("List nodes for deployment {}", deploymentId);
        }
        if (deploymentId == null || deploymentId.isEmpty()) {
            throw new IllegalArgumentException("Deployment id must not be null or empty");
        }
        if (nodeId != null) {
            return unwrapListResponse(FutureUtil.unwrapRestResponse(
                    client.getForEntity(client.buildRequestUrl(NODES_PATH, "deployment_id", "id"), ListNodeResponse.class, deploymentId, nodeId)));
        } else {
            return unwrapListResponse(FutureUtil
                    .unwrapRestResponse(client.getForEntity(client.buildRequestUrl(NODES_PATH, "deployment_id"), ListNodeResponse.class, deploymentId)));
        }
    }

    @SneakyThrows
    public Node[] list(String deploymentId, String nodeId) {
        return asyncList(deploymentId, nodeId).get();
    }

}
