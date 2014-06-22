package io.magnum.awscommons;

import io.magnum.awscommons.retry.AbortException;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;

/**
 * A helper for fetching information about and creating {@link HD4ARStack}s.
 * 
 * @author Yu Sun
 */
public class StackHelper {
    
    private final GeneralAwsHelper awsHelper;
        
    public StackHelper(GeneralAwsHelper awsHelper) {
        this.awsHelper = awsHelper;
    }
       
    public List<HD4ARStack> getStacks() throws AbortException {
        List<Stack> stacks = awsHelper.getCloudFormationStacks();

        final List<HD4ARStack> result = new ArrayList<HD4ARStack>(stacks.size());
        for (final Stack stack : stacks) {
            result.add(convert(stack));
        }
        return result;
    }
      
    public HD4ARStack getStack(String stackName) throws AbortException {
        Stack stack = awsHelper.getCloudFormationStack(stackName);
        return stack != null ? convert(stack) : null;
    }
       
    private HD4ARStack convert(Stack stack) throws AbortException {
        String template = awsHelper.getCloudFormationStackTemplate(stack.getStackName());
        
        // Resources
        List<StackResource> stackResources = awsHelper.getCloudFormationStackResources(stack.getStackName());
        
        // AutoScalingGroup and LoadBalancer
        AutoScalingGroup group = null;
        LoadBalancerDescription loadBalancer = null;
        for (final StackResource resource : stackResources) {
            final AwsResourceType resourceType = AwsResourceType.parse(resource.getResourceType());
            if (resourceType == null) continue; // ignore unknown resource types
            final String physicalId = resource.getPhysicalResourceId();
            switch (resourceType) {
                case AUTOSCALING_GROUP:
                    group = awsHelper.getAutoScalingGroup(physicalId);
                    if(group == null) {
                        throw new AbortException("AutoScaling could not find an auto-scaling group by the name of " + physicalId);
                    }
                    break;
                case ELASTIC_LOAD_BALANCER:
                    loadBalancer = awsHelper.getElasticLoadBalancer(physicalId);
                    if(loadBalancer == null) {
                        throw new AbortException("ElasticLoadBalancing could not find a load balancer by the name of " + physicalId);
                    }
                    break;
                default: // uninteresting resource, ignore
                    break;
            }
        }
        
        return new HD4ARStack(stack, template, stackResources, group, loadBalancer);
    }
}
