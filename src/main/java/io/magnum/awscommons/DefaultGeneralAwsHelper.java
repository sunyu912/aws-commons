package io.magnum.awscommons;

import io.magnum.awscommons.retry.AbortException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsRequest;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsResult;
import com.amazonaws.services.autoscaling.model.LaunchConfiguration;
import com.amazonaws.services.autoscaling.model.TerminateInstanceInAutoScalingGroupRequest;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.GetTemplateRequest;
import com.amazonaws.services.cloudformation.model.GetTemplateResult;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;
import com.amazonaws.services.cloudformation.model.UpdateStackResult;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RegisterImageRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthResult;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.InstanceState;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;

/**
 * A helper class that provides AWS functionality.
 * 
 * @author Yu Sun
 */
public class DefaultGeneralAwsHelper implements GeneralAwsHelper {   

    private final static Logger logger = LoggerFactory.getLogger(DefaultGeneralAwsHelper.class);
    
    private final AwsContext context;

    public DefaultGeneralAwsHelper(final AwsContext context) {
        this.context = context;
    }
    
    @Override
    public List<AutoScalingGroup> getAutoScalingGroups() throws AbortException {

        String nextToken = null;
        List<AutoScalingGroup> groups = new ArrayList<AutoScalingGroup>();
        do {
            DescribeAutoScalingGroupsRequest request = new DescribeAutoScalingGroupsRequest();
            request.setNextToken(nextToken);
            DescribeAutoScalingGroupsResult result = context.getAutoScalingClient().describeAutoScalingGroups(request);
            groups.addAll(result.getAutoScalingGroups());
            nextToken = result.getNextToken();
        } while (nextToken != null);
        
        return groups;
    }
    
    @Override
    public Instance createInstance(final String imageId, final String instanceType, final List<String> securityGroups,
            final String keyName) {

        RunInstancesRequest request = new RunInstancesRequest();
        request.setImageId(imageId);
        request.setInstanceType(instanceType);
        request.setSecurityGroups(securityGroups);
        request.setKeyName(keyName);
        request.setMinCount(1);
        request.setMaxCount(1);

        RunInstancesResult result = context.getEc2Client().runInstances(request);
        return result.getReservation().getInstances().get(0);
    }

    @Override
    public Instance getInstanceById(final String instanceId) {
        Collection<String> ids = Collections.singleton(instanceId);
        Map<String, Instance> instances = getInstancesById(ids);

        return instances.get(instanceId);
    }

    @Override
    public Map<String, Instance> getInstancesById(final Collection<String> instanceIds) {
        Map<String, Instance> map = new HashMap<String, Instance>();
        DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceIds);
        DescribeInstancesResult result = context.getEc2Client().describeInstances(request);
        for (Reservation reservation : result.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                map.put(instance.getInstanceId(), instance);
            }
        }
        return map;
    }

    @Override
    public String createImage(final String instanceId, final String name) {
        CreateImageRequest imageRequest = new CreateImageRequest(instanceId, name);
        CreateImageResult imageResult = context.getEc2Client().createImage(imageRequest);
        return imageResult.getImageId();
    }

    @Override
    public boolean isImageAvailable(final String imageId) {
        DescribeImagesRequest describeImageRequest = new DescribeImagesRequest();
        describeImageRequest.setImageIds(Collections.singletonList(imageId));

        DescribeImagesResult describeImagesResult = context.getEc2Client().describeImages(describeImageRequest);

        List<Image> images = describeImagesResult.getImages();
        if (images.isEmpty()) {
            return false;
        }

        return "available".equalsIgnoreCase(images.get(0).getState());
    }

    @Override
    public String createStack(final String stackName, final String template, final List<Parameter> parameters) {
        CreateStackRequest request = new CreateStackRequest();
        request.setStackName(stackName);
        request.setTemplateBody(template);
        request.setParameters(parameters);
        request.setCapabilities(Arrays.asList(new String[]{"CAPABILITY_IAM"}));
        CreateStackResult result = context.getCloudFormationClient().createStack(request);
        return result.getStackId();
    }

    @Override
    public String updateStack(final String stackName, final String template, final List<Parameter> parameters) {
        UpdateStackRequest request = new UpdateStackRequest();
        request.setStackName(stackName);
        request.setTemplateBody(template);
        request.setParameters(parameters);
        request.setCapabilities(Arrays.asList(new String[]{"CAPABILITY_IAM"}));
        UpdateStackResult updateResult = context.getCloudFormationClient().updateStack(request);
        return updateResult.getStackId();
    }

    @Override
    public List<Stack> getCloudFormationStacks() {
        return context.getCloudFormationClient().describeStacks().getStacks();
    }

    
    @Override
    public Stack getCloudFormationStack(final String stackName) throws AbortException {
        if (stackName == null || stackName.isEmpty()) throw new AbortException("Stack name provided is empty!");
        
        AmazonCloudFormation cf = context.getCloudFormationClient();
        DescribeStacksRequest request = new DescribeStacksRequest();
        request.setStackName(stackName);

        DescribeStacksResult response = cf.describeStacks(request);

        List<Stack> stacks = response.getStacks();
        if (stacks != null && stacks.size() == 1) {
            return stacks.get(0);
        }
        return null;
    }

    @Override
    public String getCloudFormationStackTemplate(String stackName) throws AbortException {
        GetTemplateRequest request = new GetTemplateRequest();
        request.setStackName(stackName);
        
        GetTemplateResult templateRequest = context.getCloudFormationClient().getTemplate(request);
        return templateRequest.getTemplateBody();
    }
    
    @Override
    public AutoScalingGroup getCloudFormationStackAutoScalingGroup(final String stackName) throws AbortException {
        List<StackResource> resources = getCloudFormationStackResourcesOfType(stackName,
                AwsResourceType.AUTOSCALING_GROUP);
        
        if (resources.isEmpty()) {
            return null;
        } else if (resources.size() == 1) {
            String resourceName = resources.get(0).getPhysicalResourceId();
            return getAutoScalingGroup(resourceName);
        } else {
            throw new AbortException("Unexpected number of auto scaling groups");
        }
    }

    @Override
    public LoadBalancerDescription getCloudFormationStackLoadBalancer(final String stackName) throws AbortException {
        List<StackResource> resources = getCloudFormationStackResourcesOfType(stackName,
                AwsResourceType.ELASTIC_LOAD_BALANCER);
        
        if (resources.isEmpty()) {
            return null;
        } else if (resources.size() == 1) {
            String resourceName = resources.get(0).getPhysicalResourceId();
            return getElasticLoadBalancer(resourceName);
        } else {
            throw new AbortException("Unexpected number of auto scaling groups");
        }
    }

    private List<StackResource> getCloudFormationStackResourcesOfType(final String stackName, AwsResourceType desiredType) {
        List<StackResource> allStackResources = getCloudFormationStackResources(stackName);

        List<StackResource> matchingResources = new LinkedList<StackResource>();
        for (StackResource resource : allStackResources) {
            AwsResourceType actualType = AwsResourceType.parse(resource.getResourceType());
            if (desiredType.equals(actualType)) {
                matchingResources.add(resource);
            }
        }
        return matchingResources;
    }

    @Override
    public List<StackResource> getCloudFormationStackResources(final String stackName) {
        DescribeStackResourcesRequest request = new DescribeStackResourcesRequest();
        request.setStackName(stackName);
        DescribeStackResourcesResult response = context.getCloudFormationClient().describeStackResources(request);

        return response.getStackResources() != null ? response.getStackResources() : Collections.<StackResource>emptyList(); 
    }
    
    @Override
    public AutoScalingGroup getAutoScalingGroup(final String autoScalingGroupName) {
        DescribeAutoScalingGroupsRequest request = new DescribeAutoScalingGroupsRequest();
        request.setAutoScalingGroupNames(Collections.singleton(autoScalingGroupName));
        DescribeAutoScalingGroupsResult result = context.getAutoScalingClient().describeAutoScalingGroups(request);
        List<AutoScalingGroup> groups = result.getAutoScalingGroups();
        if(groups == null || groups.size() != 1) {
            return null;
        }
        return groups.get(0);
    }
    
    @Override
    public LoadBalancerDescription getElasticLoadBalancer(final String loadBalancerName) {
        DescribeLoadBalancersRequest request = new DescribeLoadBalancersRequest();
        request.setLoadBalancerNames(Collections.singleton(loadBalancerName));
        DescribeLoadBalancersResult result = context.getElasticLoadBalancingClient().describeLoadBalancers(request);
        List<LoadBalancerDescription> descriptions = result.getLoadBalancerDescriptions();
        if (descriptions != null && descriptions.size() == 1) {
            return descriptions.get(0);
        }
        return null;
    }

    @Override
    public void terminateViaAutoScaling(final String instanceId) {
        logger.info("terminating instance " + instanceId);
        TerminateInstanceInAutoScalingGroupRequest request = new TerminateInstanceInAutoScalingGroupRequest();
        request.setInstanceId(instanceId);
        request.setShouldDecrementDesiredCapacity(false);
        // This request does not produce a result.
        context.getAutoScalingClient().terminateInstanceInAutoScalingGroup(request);
    }
    
    @Override
    public void terminateViaEc2(final String instanceId) {
        logger.info("terminating instance " + instanceId);
        TerminateInstancesRequest request = new TerminateInstancesRequest(Collections.singletonList(instanceId));
        // This request does not produce a result.
        context.getEc2Client().terminateInstances(request);
    }
    
    @Override
    public String registerImage(final String manifest, final String desc) {
        RegisterImageRequest r = new RegisterImageRequest(manifest);
        r.setDescription(desc);
        return context.getEc2Client().registerImage(r).getImageId();
    }
    
    @Override
    public void removeInstancesFromLoadBalancer(final String loadBalancerName, final Set<String> instanceIds) {
        List<com.amazonaws.services.elasticloadbalancing.model.Instance> instances = new LinkedList<com.amazonaws.services.elasticloadbalancing.model.Instance>();
        for (final String instanceId : instanceIds) {
            instances.add(new com.amazonaws.services.elasticloadbalancing.model.Instance(instanceId));
        }

        // Deregister the instances from the load balancer.
        DeregisterInstancesFromLoadBalancerRequest request = new DeregisterInstancesFromLoadBalancerRequest();
        request.setLoadBalancerName(loadBalancerName);
        request.setInstances(instances);
        context.getElasticLoadBalancingClient().deregisterInstancesFromLoadBalancer(request);
    }
        
    @Override
    public Map<String, ElasticLoadBalancingInstanceState> getLoadBalancerInstanceHealth(
            final String loadBalancerName, final Collection<String> instanceIds) {
        
        DescribeInstanceHealthRequest request = new DescribeInstanceHealthRequest(loadBalancerName);
        if (instanceIds != null) {
            List<com.amazonaws.services.elasticloadbalancing.model.Instance> instances = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>(
                    instanceIds.size());
            for (String instanceId : instanceIds) {
                instances.add(new com.amazonaws.services.elasticloadbalancing.model.Instance(instanceId));
            }
        }

        Map<String, ElasticLoadBalancingInstanceState> stateByInstanceId = new HashMap<String, ElasticLoadBalancingInstanceState>();
        DescribeInstanceHealthResult result = context.getElasticLoadBalancingClient().describeInstanceHealth(request);
        for (InstanceState state : result.getInstanceStates()) {
            stateByInstanceId.put(state.getInstanceId(), ElasticLoadBalancingInstanceState.parse(state.getState()));
        }
        return stateByInstanceId;
    }

    @Override
    public void putTags(final Collection<String> resourceIds, final Map<String, String> tags) {
        List<String> resourceIdList; // API requires a list for some reason
        if (resourceIds instanceof List) {
            resourceIdList = (List<String>) resourceIds;
        } else {
            resourceIdList = new ArrayList<String>(resourceIds);
        }
        
        List<Tag> tagList = new ArrayList<Tag>(tags.size());
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            tagList.add(new Tag(tag.getKey(), tag.getValue()));
        }

        CreateTagsRequest request = new CreateTagsRequest(resourceIdList, tagList);
        context.getEc2Client().createTags(request);
    }
    
    @Override
    public LaunchConfiguration getAutoScalingLaunchConfiguration(final String autoScalingGroupName) throws AbortException {
        AutoScalingGroup asg = getAutoScalingGroup(autoScalingGroupName);
        if (asg == null) {
            throw new AbortException("no such auto scaling group: " + autoScalingGroupName);
        }

        DescribeLaunchConfigurationsRequest request = new DescribeLaunchConfigurationsRequest();
        request.setLaunchConfigurationNames(Collections.singleton(asg.getLaunchConfigurationName()));
        DescribeLaunchConfigurationsResult result = context.getAutoScalingClient()
                .describeLaunchConfigurations(request);
        List<LaunchConfiguration> configs = result.getLaunchConfigurations();
        if (configs.size() < 0) {
            throw new AbortException("group exists but has no configuration: " + autoScalingGroupName);
        }
        return configs.get(0);
    }

	@Override
	public void deleteStack(String stackName) {
		DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
		deleteStackRequest.setStackName(stackName);
		context.getCloudFormationClient().deleteStack(deleteStackRequest);		
	}        
    
}
