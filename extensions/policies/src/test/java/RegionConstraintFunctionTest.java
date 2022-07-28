import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.core.base.policy.PolicyContextImpl;
import org.eclipse.dataspaceconnector.mvd.RegionConstraintFunction;
import org.eclipse.dataspaceconnector.policy.model.Operator;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.spi.agent.ParticipantAgent;
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.PolicyContext;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RegionConstraintFunctionTest {

    static final String VERIFIABLE_CREDENTIAL_KEY = "vc";
    final ObjectMapper objectMapper = new ObjectMapper();
    final Monitor monitor = new ConsoleMonitor();
    final RegionConstraintFunction constraintFunction = new RegionConstraintFunction(objectMapper, monitor);
    final static Permission dummyPermission = Permission.Builder.newInstance().build();


    @Test
    public void verifyPolicy_validRegion() {
        var expectedRegion = "eu";
        var claims = toMappedVerifiableCredentials(Map.of("region", expectedRegion));
        var policyContext = getPolicyContext(claims);
        assertThat(constraintFunction.evaluate(Operator.EQ, expectedRegion, dummyPermission, policyContext)).isTrue();
    }

    @Test
    public void verifyPolicy_invalidRegion() {
        var claims = toMappedVerifiableCredentials(Map.of("region", "us"));
        var policyContext = getPolicyContext(claims);
        assertThat(constraintFunction.evaluate(Operator.EQ, "eu", dummyPermission, policyContext)).isFalse();
    }

    @Test
    public void verifyPolicy_invalidVCFormat() {
        var claims = toMappedVerifiableCredentials(Map.of());
        var policyContext = getPolicyContext(claims);
        assertThat(constraintFunction.evaluate(Operator.EQ, "eu", dummyPermission, policyContext)).isFalse();
    }

    private PolicyContext getPolicyContext(Map<String, Object> claims) {
        return new PolicyContextImpl(new ParticipantAgent(claims, Map.of()));
    }

    private Map<String, Object> toMappedVerifiableCredentials(Map<String, Object> regionClaims) {
        var vcId = UUID.randomUUID().toString();
        return Map.of(vcId,
                Map.of(VERIFIABLE_CREDENTIAL_KEY, Map.of("credentialSubject", regionClaims,
                                "id", vcId),
                        // issuer will be ignored when applying policies for now.
                        "iss", String.join("did:web:", UUID.randomUUID().toString())));
    }
}
