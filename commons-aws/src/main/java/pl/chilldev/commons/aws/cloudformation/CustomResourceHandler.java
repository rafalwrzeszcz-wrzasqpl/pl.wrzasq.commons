/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.aws.cloudformation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.amazonaws.services.lambda.runtime.Context;
import com.sunrun.cfnresponse.CfnRequest;
import com.sunrun.cfnresponse.CfnResponseSender;
import com.sunrun.cfnresponse.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for CloudFormation custom resource requests.
 *
 * @param <InputType> Structure of custom resource configuration.
 * @param <OutputType> Structure of custom resource returner properties.
 */
public class CustomResourceHandler<InputType, OutputType>
{
    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(CustomResourceHandler.class);

    /**
     * CloudFormation response handler.
     */
    private CfnResponseSender sender = new CfnResponseSender();

    /**
     * Action handlers.
     */
    private Map<String, Function<InputType, OutputType>> actions = new HashMap<>();

    /**
     * Initializes handler with all known current CloudFormation actions.
     *
     * @param createAction Callback for resource creation.
     * @param updateAction Callback for resource properties update.
     * @param deleteAction Callback for resource deletion.
     */
    public CustomResourceHandler(
        Function<InputType, OutputType> createAction,
        Function<InputType, OutputType> updateAction,
        Function<InputType, OutputType> deleteAction
    )
    {
        this.actions.put("Create", createAction);
        this.actions.put("Update", updateAction);
        this.actions.put("Delete", deleteAction);
    }

    /**
     * Handles invocation.
     *
     * @param request CloudFormation request.
     * @param context AWS Lambda context.
     */
    public void handle(CfnRequest<InputType> request, Context context)
    {
        this.logger.info(
            "Incoming CloudFormation request {}: {} -> {} {} (response URL: {}).",
            request.getRequestId(),
            request.getStackId(),
            request.getRequestType(),
            request.getLogicalResourceId(),
            request.getResponseURL()
        );

        try {
            this.sender.send(
                request,
                Status.SUCCESS,
                context,
                "OK",
                this.actions.get(request.getRequestType()).apply(request.getResourceProperties()),
                null
            );
            //CHECKSTYLE:OFF: IllegalCatchCheck
        } catch (Exception error) {
            //CHECKSTYLE:ON: IllegalCatchCheck
            this.logger.error("Failed to handle resource action.", error);

            this.sender.send(
                request,
                Status.FAILED,
                context,
                error.getMessage(),
                null,
                null
            );
        }
    }
}
