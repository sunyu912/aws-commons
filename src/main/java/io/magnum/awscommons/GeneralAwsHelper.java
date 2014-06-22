package io.magnum.awscommons;

import io.magnum.awscommons.retry.AbortException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.LaunchConfiguration;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;

/**
 * An interface whose implementations provide general AWS functionality.
 *
 * @author Yu Sun
 */
public interface GeneralAwsHelper {

    /**
     * Returns a list of all of the auto-scaling groups in our account.
     * 
     * @return such a list; possibly empty, but never <code>null</code>
     * @throws AbortException if the request could not be completed
     */
    List<AutoScalingGroup> getAutoScalingGroups() throws AbortException;

    /**
     * Requests a host from EC2 and returns the new {@link Instance}.
     *  
     * @param imageId the image to launch on the host
     * @param instanceType the type of host to launch
     * @param securityGroups the security groups to assign to the host
     * @param keyName the SSH key to install on the host
     * @return an instance
     * @throws AbortException if the request could not be completed
     */
    Instance createInstance(final String imageId, final String instanceType, final List<String> securityGroups,
            final String keyName) throws AbortException;

    /**
     * Looks up an instance by its ID and returns an {@link Instance} object.
     * 
     * @return an instance or null if none could be found
     * @throws AbortException if the request could not be completed
     */
    Instance getInstanceById(final String instanceId) throws AbortException;

    /**
     * Looks up instances by their IDs and returns a map whose keys are
     * instance IDs and whose values are {@link Instance} objects.
     * 
     * @return such a map; possibly empty, but never <code>null</code>
     * @throws AbortException if the request could not be completed
     */
    Map<String, Instance> getInstancesById(final Collection<String> instanceIds) throws AbortException;

    /**
     * Creates a new Amazon Machine Image based upon the specified instance.
     * 
     * @param instanceId the instance from which to copy the image
     * @param name the name to give the new image
     * @return an image Id
     * @throws AbortException if the request could not be completed
     */
    String createImage(final String instanceId, final String name) throws AbortException;

    /**
     * Delete an existing CloudFormation stack.
     * 
     * @param stackName
     */
    void deleteStack(String stackName);
    
    /**
     * Checks if the specified image is available. Images are typically unavailable
     * because they have been recently created.
     * 
     * @param imageId the image
     * @return true if and only if the image exists and has the available status
     * @throws AbortException if the request could not be completed
     */
    boolean isImageAvailable(final String imageId) throws AbortException;

    /**
     * Creates a new CloudFormation stack.
     * 
     * @param stackName the name of the stack
     * @param template the stack's template
     * @param parameters the parameters for the stack template
     * @return the Id of the new stack
     * @throws AbortException if the request could not be completed
     */
    String createStack(final String stackName, final String template, final List<Parameter> parameters)
            throws AbortException;

    /**
     * Updates an existing CloudFormation stack.
     * 
     * @param stackName the name of the stack
     * @param template the stack's template
     * @param parameters the parameters for the stack template
     * @return the Id of the updated stack
     * @throws AbortException if the request could not be completed
     */
    String updateStack(final String stackName, final String template, final List<Parameter> parameters)
            throws AbortException;

    /**
     * Returns a list of all the Cloud Formation stacks in our account.
     * 
     * @return such a list; possibly empty, but never <code>null</code>
     * @throws AbortException if unable to complete the request
     */
    List<Stack> getCloudFormationStacks() throws AbortException;

    /**
     * Get a Cloud Formation stack with {@param stackName}.
     * 
     * @param stackName the name of the stack
     * @return {@link Stack} if the stack is found, null otherwise
     * @throws AbortException if unable to complete the request
     */
    Stack getCloudFormationStack(final String stackName) throws AbortException;

    /**
     * Returns the template for the Cloud Formation stack by the given name.
     * 
     * @param stackName the name of the stack
     * @return the template content
     * @throws AbortException if unable to complete the request
     */
    String getCloudFormationStackTemplate(final String stackName) throws AbortException;
    
    /**
     * Gets the auto scaling group of the specified CloudFormation stack.
     * 
     * @param stackName
     *            the name of the stack
     * @return an auto scaling group, if one exists; otherwise <code>null</code>
     * @throws AbortException
     *             if the more than one auto scaling group was found or the
     *             request could not otherwise be completed
     */
    AutoScalingGroup getCloudFormationStackAutoScalingGroup(final String stackName) throws AbortException;

    /**
     * Gets the elastic load balancer of the specified CloudFormation stack.
     * 
     * @param stackName
     *            the name of the stack
     * @return an elastic load balancer, if one exists; otherwise
     *         <code>null</code>
     * @throws AbortException
     *             if the more than one load balancer was found or the request
     *             could not otherwise be completed
     */
    LoadBalancerDescription getCloudFormationStackLoadBalancer(final String stackName) throws AbortException;

    /**
     * Gets the resources of the specified CloudFormation stack.
     *
     * @param stackName the name of the stack
     * @return list of {@link StackResource}s associated with the stack; possibly empty, but never <code>null</code>
     * @throws AbortException if the request could not be completed
     */
    List<StackResource> getCloudFormationStackResources(final String stackName) throws AbortException;

    /**
     * Retrieves a specific auto scaling group by name.
     * 
     * @param autoScalingGroupName the name of the group to retrieve
     * @return the group, if such a group exists; otherwise, <code>null</code>
     * @throws AbortException if unable to complete the request
     */
    AutoScalingGroup getAutoScalingGroup(final String autoScalingGroupName) throws AbortException;

    /**
     * Retrieves a specific load balancer by name.
     * 
     * @param loadBalancerName the name of the load balancer to retrieve
     * @return the load balancer, if such a load balancer exists;
     * otherwise, <code>null</code>
     * @throws AbortException if unable to complete the request
     */
    LoadBalancerDescription getElasticLoadBalancer(final String loadBalancerName) throws AbortException;

    /**
     * Attempts to terminate one instance via the Auto Scaling service.
     * 
     * @param instanceId the instance to be terminated
     * @throws AbortException if unable to complete the request
     */
    void terminateViaAutoScaling(final String instanceId) throws AbortException;

    /**
     * Attempts to terminate one instance directly using EC2 APIs.
     * 
     * @param instanceId the instance to be terminated
     * @throws AbortException if unable to complete the request
     */
    void terminateViaEc2(final String instanceId) throws AbortException;

    /**
     * Register bundle as AMI
     * 
     * @param manifest the bundle manifest
     * @param desc the bundle description
     * @return ami id
     * @throws AbortException if unable to complete the request
     */
    String registerImage(final String manifest, final String desc) throws AbortException;

    /**
     * Removes the specified instances from the specified load balancer.
     * 
     * @param loadBalancerName the name of the load balancer to remove instances from
     * @param instanceIds the IDs of the instances to be removed
     * @throws AbortException if unable to complete the request
     */
    void removeInstancesFromLoadBalancer(final String loadBalancerName, final Set<String> instanceIds)
            throws AbortException;  

    /**
     * Looks up the health status of the specified instances in the specified
     * load balancer and returns a map whose keys are
     * instance IDs and whose values are
     * {@link ElasticLoadBalancingInstanceState} objects.
     * 
     * @param loadBalancerName the name of the load balancer to examine
     * @param instanceIds optionally, a collection of instance IDs to check;
     * if null, all instances in the LB are examined.
     * @return such a map; possibly empty, but never <code>null</code>
     * @throws AbortException if unable to complete the request
     */
    Map<String, ElasticLoadBalancingInstanceState> getLoadBalancerInstanceHealth(final String loadBalancerName,
            final Collection<String> instanceIds) throws AbortException;

    /**
     * Attempts to add the specified tags to the specified resources.
     * 
     * @param resourceIds the IDs of the resources to tag
     * @param tags the tags to be applied
     * @throws AbortException if unable to complete the request
     */
    void putTags(final Collection<String> resourceIds, final Map<String, String> tags) throws AbortException;

    /**
     * Retrieves the launch configuration for a specific auto scaling group by
     * name.
     * 
     * @param autoScalingGroupName the name of the auto scaling group whose
     * configuration is to be retrieved
     * @return the launch configuration
     * @throws AbortException if unable to complete the request, or if there
     * is no such auto scaling group
     */
    LaunchConfiguration getAutoScalingLaunchConfiguration(final String autoScalingGroupName) throws AbortException;

}