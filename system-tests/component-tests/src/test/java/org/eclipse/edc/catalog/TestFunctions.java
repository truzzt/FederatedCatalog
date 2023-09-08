package org.eclipse.edc.catalog;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.FederatedCacheNode;
import org.eclipse.edc.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.message.RemoteMessageDispatcher;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.types.domain.asset.Asset;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.eclipse.edc.junit.testfixtures.TestUtils.getFreePort;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFunctions {
    public static final String BASE_PATH = "/api";
    public static final int PORT = getFreePort();
    private static final String PATH = "/federatedcatalog";

    private static final String CONNECTOR_PATH = "/connectors";

    private static final String INFRASTRUCTURE_PATH = "/infrastructure";

    private static final TypeRef<List<ContractOffer>> CONTRACT_OFFER_LIST_TYPE = new TypeRef<>() {
    };

    private static final TypeRef<List<FederatedCacheNode>> FEDERATED_CACHE_NODE_LIST_TYPE = new TypeRef<>() {
    };

    public static RemoteMessageDispatcher createAndRegisterDispatcher(RemoteMessageDispatcherRegistry registry) {
        var dispatcher = mock(RemoteMessageDispatcher.class);
        when(dispatcher.protocol()).thenReturn("ids-multipart");
        registry.register(dispatcher);

        return dispatcher;
    }

    public static CompletableFuture<Catalog> emptyCatalog() {
        return completedFuture(catalogBuilder()
                .build());
    }

    public static Catalog.Builder catalogBuilder() {
        return Catalog.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .contractOffers(Collections.emptyList());
    }

    public static CompletableFuture<Catalog> catalogOf(ContractOffer... offers) {
        return completedFuture(catalogBuilder().contractOffers(asList(offers)).build());
    }

    public static CompletableFuture<Catalog> randomCatalog(int howMany) {
        return completedFuture(catalogBuilder()
                .contractOffers(IntStream.range(0, howMany).mapToObj(i -> createOffer("Offer_" + UUID.randomUUID())).collect(Collectors.toList()))
                .build());
    }

    public static ContractOffer createOffer(String id) {
        return ContractOffer.Builder.newInstance()
                .id(id)
                .asset(Asset.Builder.newInstance().id(id).build())
                .policy(Policy.Builder.newInstance().build())
                .contractStart(ZonedDateTime.now())
                .contractEnd(ZonedDateTime.now().plus(365, ChronoUnit.DAYS))
                .build();
    }

    public static void insertSingle(FederatedCacheNodeDirectory directory) {
        directory.insert(new FederatedCacheNode("test-node", "http://test-node.com", singletonList("ids-multipart")));
    }

    public static void insertMultiple(FederatedCacheNodeDirectory directory, int count) {
        for (int i = 0; i < count; i++) {
            directory.insert(new FederatedCacheNode("test-node" + i, "http://test-node" + i + ".com", singletonList("ids-multipart")));
        }
    }

    public static List<ContractOffer> queryCatalogApi() {
        return baseRequest()
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post(PATH)
                .body()
                .as(CONTRACT_OFFER_LIST_TYPE);
    }

    public static List<FederatedCacheNode> queryConnectorsApi() {
        return baseRequest()
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .post(CONNECTOR_PATH)
                .body()
                .as(FEDERATED_CACHE_NODE_LIST_TYPE);
    }

    public static Response sendInfrastructureController(String header) {
        return given()
                .baseUri("http://localhost:" + PORT)
                .basePath(BASE_PATH)
                .contentType("multipart/form-data")
                .multiPart("header", header)
                .when()
                .post(INFRASTRUCTURE_PATH);
    }


    private static RequestSpecification baseRequest() {
        return given()
                .header("X-API-Key", "password")
                .baseUri("http://localhost:" + PORT)
                .basePath(BASE_PATH)
                .contentType(ContentType.JSON)
                .when();
    }

    public static String readJsonFile(String jsonPath) {
        try {
            ClassLoader classLoader = TestFunctions.class.getClassLoader();
            Path filePath = Path.of(classLoader.getResource(jsonPath).toURI());

            return Files.readString(filePath);
        } catch (Exception e) {
            throw new EdcException("Error reading JSON file", e);
        }
    }
}
