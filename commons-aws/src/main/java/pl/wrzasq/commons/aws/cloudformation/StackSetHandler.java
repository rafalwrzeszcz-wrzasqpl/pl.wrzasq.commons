/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.cloudformation;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStackSetOperationRequest;
import com.amazonaws.services.cloudformation.model.StackSetOperation;
import com.amazonaws.services.cloudformation.model.StackSetOperationStatus;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CloudFormation stack set routines handler.
 */
public class StackSetHandler
{
    /**
     * Interface for a sleep function.
     */
    @FunctionalInterface
    public interface SleepProvider
    {
        /**
         * Function should wait given period of time.
         *
         * @param milliseconds Amount of milliseconds to wait.
         * @throws InterruptedException If the wait operation is interrupted.
         */
        void sleep(long milliseconds) throws InterruptedException;
    }

    /**
     * Default sleep interval (1 minute).
     */
    private static final long DEFAULT_SLEEP_INTERVAL = 60000;

    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(StackSetHandler.class);

    /**
     * AWS CloudFormation API client.
     */
    private AmazonCloudFormation cloudFormation;

    /**
     * Sleep interval for status change checks.
     */
    @Setter
    private long sleepInterval = StackSetHandler.DEFAULT_SLEEP_INTERVAL;

    /**
     * Sleep handler.
     */
    @Setter
    private StackSetHandler.SleepProvider sleepHandler = Thread::sleep;

    /**
     * Initializes object with given CloudFormation client.
     *
     * @param cloudFormation AWS CloudFormation client.
     */
    public StackSetHandler(AmazonCloudFormation cloudFormation)
    {
        this.cloudFormation = cloudFormation;
    }

    /**
     * Waits for stack set operation to succeed.
     *
     * @param stackSetName Stack set name.
     * @param operationId Operation ID.
     */
    public void waitForStackSetOperation(String stackSetName, String operationId)
    {
        // wait until operation is finished
        StackSetOperation operation;
        do {
            operation = this.cloudFormation.describeStackSetOperation(
                new DescribeStackSetOperationRequest()
                    .withStackSetName(stackSetName)
                    .withOperationId(operationId)
            )
                .getStackSetOperation();

            switch (StackSetOperationStatus.fromValue(operation.getStatus())) {
                case FAILED:
                case STOPPED:
                    this.logger.error("Stack operation {} failed with status.", operationId, operation.getStatus());
                    throw new IllegalStateException(
                        String.format(
                            "Stack operation %s (%s) for stack %s failed with status %s.",
                            operation.getAction(),
                            operationId,
                            operation.getStackSetId(),
                            operation.getStatus()
                        )
                    );

                case RUNNING:
                case STOPPING:
                    this.logger.info("Stack operation {} in progress.", operationId);
                    this.sleep();
                    break;

                case SUCCEEDED:
                    this.logger.info("Stack operation {} succeeded.", operationId);
                    break;
            }
        } while (StackSetOperationStatus.fromValue(operation.getStatus()) != StackSetOperationStatus.SUCCEEDED);
    }

    /**
     * Performs a wait.
     */
    private void sleep()
    {
        try {
            this.sleepHandler.sleep(this.sleepInterval);
        } catch (InterruptedException error) {
            this.logger.error("Wait interval interrupted.", error);
        }
    }
}
