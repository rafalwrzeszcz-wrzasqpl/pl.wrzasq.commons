/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.aws.cloudformation;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStackSetOperationRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackSetOperationResult;
import com.amazonaws.services.cloudformation.model.StackSetOperation;
import com.amazonaws.services.cloudformation.model.StackSetOperationStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrzasq.commons.aws.cloudformation.StackSetHandler;

@ExtendWith(MockitoExtension.class)
public class StackSetHandlerTest {
    private static final String STACK_SET_NAME = "test-stack-set";

    private static final String OPERATION_ID = "test-operation";

    @Mock
    private StackSetHandler.SleepProvider sleepProvider;

    @Mock
    private AmazonCloudFormation cloudFormation;

    @Captor
    ArgumentCaptor<DescribeStackSetOperationRequest> describeOperationRequest;

    @Test
    public void waitForStackSetOperationSucceeds() {
        this.runDescribeOperationRequestsSequence(
            new DescribeStackSetOperationResult()
                .withStackSetOperation(
                    new StackSetOperation()
                        .withStatus(StackSetOperationStatus.RUNNING)
                ),
            new DescribeStackSetOperationResult()
                .withStackSetOperation(
                    new StackSetOperation()
                        .withStatus(StackSetOperationStatus.SUCCEEDED)
                )
        );

        this.verifyDescribeOperationRequestsArguments(2);
    }

    @Test
    public void waitForStackSetOperationFails() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> this.runDescribeOperationRequestsSequence(
                new DescribeStackSetOperationResult()
                    .withStackSetOperation(
                        new StackSetOperation()
                            .withStatus(StackSetOperationStatus.RUNNING)
                    ),
                new DescribeStackSetOperationResult()
                    .withStackSetOperation(
                        new StackSetOperation()
                            .withStatus(StackSetOperationStatus.FAILED)
                    )
            ),
            "StackSetHandler.waitFormStackSetOperation() should throw an exception if operation fails."
        );

        this.verifyDescribeOperationRequestsArguments(2);
    }

    @Test
    public void waitForStackSetOperationStops() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> this.runDescribeOperationRequestsSequence(
                new DescribeStackSetOperationResult()
                    .withStackSetOperation(
                        new StackSetOperation()
                            .withStatus(StackSetOperationStatus.RUNNING)
                    ),
                new DescribeStackSetOperationResult()
                    .withStackSetOperation(
                        new StackSetOperation()
                            .withStatus(StackSetOperationStatus.STOPPING)
                    ),
                new DescribeStackSetOperationResult()
                    .withStackSetOperation(
                        new StackSetOperation()
                            .withStatus(StackSetOperationStatus.STOPPED)
                    )
            ),
            "StackSetHandler.waitFormStackSetOperation() should throw an exception if operation was stopped."
        );

        this.verifyDescribeOperationRequestsArguments(3);
    }

    @Test
    public void waitForStackSetOperationInterrupted() throws InterruptedException {
        Mockito
            .doThrow(InterruptedException.class)
            .when(this.sleepProvider)
            .sleep(Mockito.anyLong());
        Mockito
            .when(this.cloudFormation.describeStackSetOperation(Mockito.any(DescribeStackSetOperationRequest.class)))
            .thenReturn(
                new DescribeStackSetOperationResult()
                    .withStackSetOperation(
                        new StackSetOperation()
                            .withStatus(StackSetOperationStatus.RUNNING)
                    ),
                new DescribeStackSetOperationResult()
                    .withStackSetOperation(
                        new StackSetOperation()
                            .withStatus(StackSetOperationStatus.SUCCEEDED)
                    )
            );

        var handler = new StackSetHandler(this.cloudFormation);
        handler.setSleepHandler(this.sleepProvider);
        handler.waitForStackSetOperation(null, null);

        Mockito
            .verify(this.sleepProvider)
            .sleep(Mockito.anyLong());

        Mockito
            .verify(this.cloudFormation, Mockito.times(2))
            .describeStackSetOperation(Mockito.any(DescribeStackSetOperationRequest.class));
    }

    private void runDescribeOperationRequestsSequence(DescribeStackSetOperationResult... results) {
        var handler = new StackSetHandler(this.cloudFormation);
        handler.setSleepInterval(1);

        var stubbing = Mockito
            .when(this.cloudFormation.describeStackSetOperation(this.describeOperationRequest.capture()));

        for (var result : results) {
            stubbing = stubbing.thenReturn(result);
        }

        handler.waitForStackSetOperation(
            StackSetHandlerTest.STACK_SET_NAME,
            StackSetHandlerTest.OPERATION_ID
        );
    }

    private void verifyDescribeOperationRequestsArguments(int count) {
        var requests = this.describeOperationRequest.getAllValues();

        Assertions.assertEquals(
            count,
            requests.size(),
            "StackSetHandler.waitFormStackSetOperation() should keep checking stack set operation status."
        );

        requests.forEach(
            request -> {
                Assertions.assertEquals(
                    StackSetHandlerTest.STACK_SET_NAME,
                    request.getStackSetName(),
                    "StackSetHandler.waitFormStackSetOperation() keep requesting operation status of given stack set."
                );
                Assertions.assertEquals(
                    StackSetHandlerTest.OPERATION_ID,
                    request.getOperationId(),
                    "StackSetHandler.waitFormStackSetOperation() keep requesting operation status of same operation."
                );
            }
        );
    }
}
